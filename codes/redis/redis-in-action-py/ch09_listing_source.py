# coding: utf-8

import binascii
import bisect
from datetime import date, timedelta
from collections import defaultdict
import math
import time
import unittest
import uuid

import redis

def readblocks(conn, key, blocksize=2**17):
    lb = blocksize
    pos = 0
    while lb == blocksize:                                  #A
        block = conn.substr(key, pos, pos + blocksize - 1)  #B
        yield block                                         #C
        lb = len(block)                                     #C
        pos += lb                                           #C
    yield ''

# 代码清单 9-1
'''
# <start id="ziplist-configuration-options"/>
list-max-ziplist-entries 512    # 列表结构使用压缩列表表示的限制条件。
list-max-ziplist-value 64       #

hash-max-ziplist-entries 512    # 散列结构使用压缩列表表示的限制条件
hash-max-ziplist-value 64       #（Redis 2.6 以前的版本会为散列结构使用不同的编码表示，并且选项的名字也与此不同）。

zset-max-ziplist-entries 128    # 有序集合使用压缩列表表示的限制条件。
zset-max-ziplist-value 64       #
# <end id="ziplist-configuration-options"/>
'''

'''
# <start id="ziplist-test"/>
>>> conn.rpush('test', 'a', 'b', 'c', 'd')  # 首先将四个元素推入到列表。
4                                           #
>>> conn.debug_object('test')                                       # debug object 命令可以查看特定对象的相关信息。
{'encoding': 'ziplist', 'refcount': 1, 'lru_seconds_idle': 20,      # “encoding”信息表示这个对象的编码为压缩列表，
'lru': 274841, 'at': '0xb6c9f120', 'serializedlength': 24,          # 这个压缩列表占用了 24 字节内存。
'type': 'Value'}                                                    #
>>> conn.rpush('test', 'e', 'f', 'g', 'h')  # 再将四个元素推入到列表。
8                                           #
>>> conn.debug_object('test')
{'encoding': 'ziplist', 'refcount': 1, 'lru_seconds_idle': 0,   # 对象的编码依然是压缩列表，只是体积增长到了 36 字节
'lru': 274846, 'at': '0xb6c9f120', 'serializedlength': 36,      # （前面推入的四个元素，每个元素都需要花费 1 字节进行储存，并带来 2 字节的额外消耗）。
'type': 'Value'}
>>> conn.rpush('test', 65*'a')          # 当一个超出编码允许大小的元素被推入到列表里面的时候，
9                                       # 列表将从压缩列表编码转换为标准的链表。
>>> conn.debug_object('test')
{'encoding': 'linkedlist', 'refcount': 1, 'lru_seconds_idle': 10,   # 尽管序列化长度下降了，
'lru': 274851, 'at': '0xb6c9f120', 'serializedlength': 30,          # 但是对于压缩列表编码以及集合的特殊编码之外的其他编码来说，这个数值并不代表结构的实际内存占用量。
'type': 'Value'}
>>> conn.rpop('test')                                               # 当压缩列表被转换为普通的结构之后，
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' # 即使结构将来重新满足配置设置的限制条件，
>>> conn.debug_object('test')                                       # 结构也不会转换回压缩列表。
{'encoding': 'linkedlist', 'refcount': 1, 'lru_seconds_idle': 0,    #
'lru': 274853, 'at': '0xb6c9f120', 'serializedlength': 17,
'type': 'Value'}
# <end id="ziplist-test"/>
'''

'''
# <start id="intset-configuration-option"/>
set-max-intset-entries 512      # 集合使用整数集合表示的限制条件。
# <end id="intset-configuration-option"/>
'''

'''
# <start id="intset-test"/>
>>> conn.sadd('set-object', *range(500))                        # 即使向集合添加 500 个元素，
500                                                             # 它的编码仍然为整数集合。
>>> conn.debug_object('set-object')                             #
{'encoding': 'intset', 'refcount': 1, 'lru_seconds_idle': 0,    #
'lru': 283116, 'at': '0xb6d1a1c0', 'serializedlength': 1010,
'type': 'Value'}
>>> conn.sadd('set-object', *range(500, 1000))                  # 当集合的元素数量超过限定的 512 个时，
500                                                             # 整数集合将被转换为散列表表示。
>>> conn.debug_object('set-object')                             #
{'encoding': 'hashtable', 'refcount': 1, 'lru_seconds_idle': 0, #
'lru': 283118, 'at': '0xb6d1a1c0', 'serializedlength': 2874,
'type': 'Value'}
# <end id="intset-test"/>
'''

# <start id="rpoplpush-benchmark"/>
# 为了以不同的方式进行性能测试，函数需要对所有测试指标进行参数化处理。
def long_ziplist_performance(conn, key, length, passes, psize): 
    # 删除指定的键，确保被测试数据的准确性。
    conn.delete(key)                    
    # 通过从右端推入指定数量的元素来对列表进行初始化。
    conn.rpush(key, *range(length))     
    # 通过流水线来降低网络通信给测试带来的影响。
    pipeline = conn.pipeline(False)     

    # 启动计时器。
    t = time.time()                     
    # 根据 passes 参数来决定流水线操作的执行次数。
    for p in xrange(passes):           
        # 每个流水线操作都包含了 psize 次 RPOPLPUSH 命令调用。
        for pi in xrange(psize):        
            # 每个 rpoplpush() 函数调用都会将列表最右端的元素弹出，
            # 并将它推入到同一个列表的左端。
            pipeline.rpoplpush(key, key)
        # 执行 psize 次 RPOPLPUSH 命令。
        pipeline.execute()              

    # 计算每秒钟执行的 RPOPLPUSH 调用数量。
    return (passes * psize) / (time.time() - t or .001) 
# <end id="rpoplpush-benchmark"/>

'''
# <start id="rpoplpush-performance"/>
>>> long_ziplist_performance(conn, 'list', 1, 1000, 100)        # 当压缩列表编码的列表包含的节点数量不超过 1000 个时，
52093.558416505381                                              # Redis 每秒钟可以执行大约五万次操作。
>>> long_ziplist_performance(conn, 'list', 100, 1000, 100)      #
51501.154762768667                                              #
>>> long_ziplist_performance(conn, 'list', 1000, 1000, 100)     #
49732.490843316067                                              #
>>> long_ziplist_performance(conn, 'list', 5000, 1000, 100)     # 当压缩列表编码的列表包含的节点数量超过 5000 个时，
43424.056529592635                                              # 内存复制带来的消耗就会越来越大，
>>> long_ziplist_performance(conn, 'list', 10000, 1000, 100)    # 导致性能下降。
36727.062573334966                                              #
>>> long_ziplist_performance(conn, 'list', 50000, 1000, 100)    # 当压缩列表的节点数量达到 5000 个时，
16695.140684975777                                              # 性能出现明显下降。
>>> long_ziplist_performance(conn, 'list', 100000, 500, 100)    # 当节点数量达到十万个时，
553.10821080054586                                              # 压缩列表的性能低得根本没法用了。
# <end id="rpoplpush-performance"/>
'''

def long_ziplist_index(conn, key, length, passes, psize): #A
    conn.delete(key)                    #B
    conn.rpush(key, *range(length))     #C
    length >>= 1
    pipeline = conn.pipeline(False)     #D
    t = time.time()                     #E
    for p in xrange(passes):            #F
        for pi in xrange(psize):        #G
            pipeline.lindex(key, length)#H
        pipeline.execute()              #I
    return (passes * psize) / (time.time() - t or .001) #J

def long_intset_performance(conn, key, length, passes, psize): #A
    conn.delete(key)                    #B
    conn.sadd(key, *range(1000000, 1000000+length))     #C
    cur = 1000000-1
    pipeline = conn.pipeline(False)     #D
    t = time.time()                     #E
    for p in xrange(passes):            #F
        for pi in xrange(psize):        #G
            pipeline.spop(key)#H
            pipeline.sadd(key, cur)
            cur -= 1
        pipeline.execute()              #I
    return (passes * psize) / (time.time() - t or .001) #J


# 代码清单 9-7
# <start id="calculate-shard-key"/>
# 在调用 shard_key() 函数时，
# 用户需要给定基础散列的名字、将要被储存到分片散列里面的键、预计的元素总数量以及请求的分片数量。
def shard_key(base, key, total_elements, shard_size):  
    # 如果值是一个整数或者一个看上去像是整数的字符串，
    # 那么它将被直接用于计算分片 ID 。
    if isinstance(key, (int, long)) or key.isdigit():  
        # 整数键将被程序假定为连续指派的 ID ，
        # 并基于这个整数 ID 的二进制位的高位来选择分片 ID 。
        # 此外，程序在进行整数转换的时候还使用了显式的基数（以及 str()`` 函数），
        # 使得键 010 可以被转换为 10 ，而不是 8 。
        shard_id = int(str(key), 10) // shard_size     
    else:
        # 对于不是整数的键，
        # 程序将基于预计的元素总数量以及请求的分片数量，
        # 计算出实际所需的分片总数量。
        shards = 2 * total_elements // shard_size      
        # 在得知了分片的数量之后，
        # 程序就可以通过计算键的散列值与分片数量之间的模数来得到分片 ID 。
        shard_id = binascii.crc32(key) % shards         
    # 最后，程序会把基础键和分片 ID 组合在一起，得出分片键。
    return "%s:%s"%(base, shard_id)                    
# <end id="calculate-shard-key"/>


# 代码清单 9-8
# <start id="sharded-hset-hget"/>
def shard_hset(conn, base, key, value, total_elements, shard_size):
    # 计算出应该由哪个分片来储存值。
    shard = shard_key(base, key, total_elements, shard_size)   
    # 将值储存到分片里面。
    return conn.hset(shard, key, value)                        

def shard_hget(conn, base, key, total_elements, shard_size):
    # 计算出值可能被储存到了哪个分片里面。
    shard = shard_key(base, key, total_elements, shard_size)    
    # 取得储存在分片里面的值。
    return conn.hget(shard, key)                               
# <end id="sharded-hset-hget"/>


# 代码清单 9-9
'''
# <start id="sharded-ip-lookup"/>
TOTAL_SIZE = 320000                                             # 把传递给分片函数的参数设置为全局常量，
SHARD_SIZE = 1024                                               # 确保每次传递的值总是相同的。

def import_cities_to_redis(conn, filename):
    for row in csv.reader(open(filename)):
        ...
        shard_hset(conn, 'cityid2city:', city_id,               # 为了对数据进行设置，用户需要传递 TOTAL_SIZE 参数和 SHARD_SIZE 参数。
            json.dumps([city, region, country]),                # 不过因为这个程序处理的 ID 都是数字，
            TOTAL_SIZE, SHARD_SIZE)                             # 所以 TOTAL_SIZE 实际上并没有被使用。

def find_city_by_ip(conn, ip_address):
    ...
    data = shard_hget(conn, 'cityid2city:', city_id,            # 程序在获取数据时，
        TOTAL_SIZE, SHARD_SIZE)                                 # 需要根据相同的 TOTAL_SIZE 参数和 SHARD_SIZE 参数查找被分片的键。
    return json.loads(data)
# <end id="sharded-ip-lookup"/>
'''


# 代码清单 9-10
# <start id="sharded-sadd"/>
def shard_sadd(conn, base, member, total_elements, shard_size):
    shard = shard_key(base,
        # 计算成员应该被储存到哪个分片集合里面；
        # 因为成员并非连续 ID ，所以程序在计算成员所属的分片之前，会先将成员转换为字符串。
        'x'+str(member), total_elements, shard_size)           
    # 将成员储存到分片里面。
    return conn.sadd(shard, member)                            
# <end id="sharded-sadd"/>


# 代码清单 9-11
# <start id="unique-visitor-count"/>
# 为整数集合编码的集合预设一个典型的分片大小。
SHARD_SIZE = 512                       

def count_visit(conn, session_id):
    # 取得当天的日期，并生成唯一访客计数器的键。
    today = date.today()                               
    key = 'unique:%s'%today.isoformat()                
    # 计算或者获取当天的预计唯一访客人数。
    expected = get_expected(conn, key, today)          
 
    # 根据 128 位的 UUID ，计算出一个 56 位的 ID 。
    id = int(session_id.replace('-', '')[:15], 16)     
    # 将 ID 添加到分片集合里面。
    if shard_sadd(conn, key, id, expected, SHARD_SIZE): 
        # 如果 ID 在分片集合里面并不存在，那么对唯一访客计数器执行加一操作。
        conn.incr(key)                                  
# <end id="unique-visitor-count"/>


# 代码清单 9-12
# <start id="expected-viewer-count"/>
# 这个初始的预计每日访客人数会设置得稍微比较高一些。
DAILY_EXPECTED = 1000000                               
# 在本地储存一份计算得出的预计访客人数副本。
EXPECTED = {}                                          

def get_expected(conn, key, today):
    # 如果程序已经计算出或者获取到了当日的预计访客人数，
    # 那么直接使用已计算出的数字。
    if key in EXPECTED:                                
        return EXPECTED[key]                            
 
    exkey = key + ':expected'
    # 如果其他客户端已经计算出了当日的预计访客人数，
    # 那么直接使用已计算出的数字。
    expected = conn.get(exkey)                         
 
    if not expected:
        # 获取昨天的唯一访客人数，如果该数值不存在就使用默认值一百万。
        yesterday = (today - timedelta(days=1)).isoformat() 
        expected = conn.get('unique:%s'%yesterday)          
        expected = int(expected or DAILY_EXPECTED)          
 
        # 基于“明天的访客人数至少会比今天的访客人数多 50%”这一假设，
        # 给昨天的访客人数加上 50% ，然后向上舍入至下一个底数为 2 的幂。
        expected = 2**int(math.ceil(math.log(expected*1.5, 2))) 
        # 将计算出的预计访客人数写入到 Redis 里面，以便其他程序在有需要时使用。
        if not conn.setnx(exkey, expected):                 
            # 如果在我们之前，
            # 已经有其他客户端储存了当日的预计访客人数，
            # 那么直接使用已储存的数字。
            expected = conn.get(exkey)                      
 
    # 将当日的预计访客人数记录到本地副本里面，并将它返回给调用者。
    EXPECTED[key] = int(expected)                       
    return EXPECTED[key]                                
# <end id="expected-viewer-count"/>


# 代码清单 9-13
# <start id="location-tables"/>
# 一个由 ISO3 国家编码组成的字符串表格，
# 调用 split() 函数会根据空白对这个字符串进行分割，
# 并将它转换为一个由国家编码组成的列表。
COUNTRIES = '''
ABW AFG AGO AIA ALA ALB AND ARE ARG ARM ASM ATA ATF ATG AUS AUT AZE BDI
BEL BEN BES BFA BGD BGR BHR BHS BIH BLM BLR BLZ BMU BOL BRA BRB BRN BTN
BVT BWA CAF CAN CCK CHE CHL CHN CIV CMR COD COG COK COL COM CPV CRI CUB
CUW CXR CYM CYP CZE DEU DJI DMA DNK DOM DZA ECU EGY ERI ESH ESP EST ETH
FIN FJI FLK FRA FRO FSM GAB GBR GEO GGY GHA GIB GIN GLP GMB GNB GNQ GRC
GRD GRL GTM GUF GUM GUY HKG HMD HND HRV HTI HUN IDN IMN IND IOT IRL IRN
IRQ ISL ISR ITA JAM JEY JOR JPN KAZ KEN KGZ KHM KIR KNA KOR KWT LAO LBN
LBR LBY LCA LIE LKA LSO LTU LUX LVA MAC MAF MAR MCO MDA MDG MDV MEX MHL
MKD MLI MLT MMR MNE MNG MNP MOZ MRT MSR MTQ MUS MWI MYS MYT NAM NCL NER
NFK NGA NIC NIU NLD NOR NPL NRU NZL OMN PAK PAN PCN PER PHL PLW PNG POL
PRI PRK PRT PRY PSE PYF QAT REU ROU RUS RWA SAU SDN SEN SGP SGS SHN SJM
SLB SLE SLV SMR SOM SPM SRB SSD STP SUR SVK SVN SWE SWZ SXM SYC SYR TCA
TCD TGO THA TJK TKL TKM TLS TON TTO TUN TUR TUV TWN TZA UGA UKR UMI URY
USA UZB VAT VCT VEN VGB VIR VNM VUT WLF WSM YEM ZAF ZMB ZWE'''.split()

STATES = {
    # 加拿大的省信息和属地信息。
    'CAN':'''AB BC MB NB NL NS NT NU ON PE QC SK YT'''.split(),      
    # 美国各个州的信息。
    'USA':'''AA AE AK AL AP AR AS AZ CA CO CT DC DE FL FM GA GU HI IA ID
IL IN KS KY LA MA MD ME MH MI MN MO MP MS MT NC ND NE NH NJ NM NV NY OH
OK OR PA PR PW RI SC SD TN TX UT VA VI VT WA WI WV WY'''.split(),     
}
# <end id="location-tables"/>


# 代码清单 9-14
# <start id="location-to-code"/>
def get_code(country, state):
    # 寻找国家对应的偏移量。
    cindex = bisect.bisect_left(COUNTRIES, country)             
    # 没有找到指定的国家时，将索引设置为 -1 。
    if cindex > len(COUNTRIES) or COUNTRIES[cindex] != country: 
        cindex = -1                                             
    # 因为 Redis 里面的未初始化数据在返回时会被转换为空值，
    # 所以我们要将“未找到指定国家”时的返回值改为 0 ，
    # 并将第一个国家的索引变为 1 ，以此类推。
    cindex += 1                                                 

    sindex = -1
    if state and country in STATES:
        # 尝试取出国家对应的州信息。
        states = STATES[country]                               
        # 寻找州对应的偏移量。
        sindex = bisect.bisect_left(states, state)             
        # 像处理“未找到指定国家”时的情况一样，处理“未找到指定州”的情况。
        if sindex > len(states) or states[sindex] != state:     
            sindex = -1                                        
    # 如果没有找到指定的州，那么索引为 0 ；
    # 如果找到了指定的州，那么索引大于 0 。
    sindex += 1                                                

    # chr() 函数会将介于 0 至 255 之间的整数值转换为对应的 ASCII 字符。
    return chr(cindex) + chr(sindex)                            
# <end id="location-to-code"/>


# 代码清单 9-15
# <start id="set-location-information"/>
# 设置每个分片的大小。
USERS_PER_SHARD = 2**20                                   

def set_location(conn, user_id, country, state):
    # 取得用户所在位置的编码。
    code = get_code(country, state)                        
    
    # 查找分片 ID 以及用户在指定分片中的位置（position）。
    shard_id, position = divmod(user_id, USERS_PER_SHARD)   
    # 计算用户数据的偏移量。
    offset = position * 2                                  

    pipe = conn.pipeline(False)
    # 将用户的位置信息储存到分片后的位置表格里面。
    pipe.setrange('location:%s'%shard_id, offset, code)     

    # 对记录目前已知最大用户 ID 的有序集合进行更新。
    tkey = str(uuid.uuid4())                                
    pipe.zadd(tkey, 'max', user_id)                         
    pipe.zunionstore('location:max',                        
        [tkey, 'location:max'], aggregate='max')            
    pipe.delete(tkey)                                       

    pipe.execute()
# <end id="set-location-information"/>


# 代码清单 9-16
# <start id="aggregate-population"/>
def aggregate_location(conn):
    # 初始化两个特殊结构，
    # 以便快速地对已存在的计数器以及缺失的计数器进行更新。
    countries = defaultdict(int)                                
    states = defaultdict(lambda:defaultdict(int))               

    # 获取目前已知的最大用户 ID ，
    # 并使用它来计算出程序需要访问的最大分片 ID 。
    max_id = int(conn.zscore('location:max', 'max'))            
    max_block = max_id // USERS_PER_SHARD                       

    # 按顺序地处理每个分片……
    for shard_id in xrange(max_block + 1):                      
        # 读取每个块……
        for block in readblocks(conn, 'location:%s'%shard_id):  
            # 从块里面提取出每个编码，
            # 并根据编码查找原始的位置信息，
            # 然后对这些位置信息进行聚合计算。
            for offset in xrange(0, len(block)-1, 2):           
                code = block[offset:offset+2]
                # 对聚合数据进行更新。
                update_aggregates(countries, states, [code])    

    return countries, states
# <end id="aggregate-population"/>


# 代码清单 9-17
# <start id="code-to-location"/>
def update_aggregates(countries, states, codes):
    for code in codes:
        # 只对合法的编码进行查找。
        if len(code) != 2:                             
            continue                                   

        # 计算出国家和州在查找表格中的实际偏移量。
        country = ord(code[0]) - 1                     
        state = ord(code[1]) - 1                      
        
        # 如果国家所处的偏移量不在合法范围之内，那么跳过这个编码。
        if country < 0 or country >= len(COUNTRIES):    
            continue                                   

        # 获取 ISO3 国家编码。
        country = COUNTRIES[country]                   
        # 在对国家信息进行解码之后，
        # 把用户计入到这个国家对应的计数器里面。
        countries[country] += 1                         

        # 如果程序没有找到指定的州信息，
        # 或者查找州信息时的偏移量不在合法的范围之内，
        # 那么跳过这个编码。
        if country not in STATES:                       
            continue                                    
        if state < 0 or state >= STATES[country]:       
            continue                                    

        # 根据编码获取州名。
        state = STATES[country][state]                  
        # 对州计数器执行加一操作。
        states[country][state] += 1                     
# <end id="code-to-location"/>


# 代码清单 9-18
# <start id="aggregate-limited"/>
def aggregate_location_list(conn, user_ids):
    # 设置流水线，减少操作执行过程中与 Redis 的通信往返次数。
    pipe = conn.pipeline(False)                                
    #  和之前一样，设置好基本的聚合数据。
    countries = defaultdict(int)                               
    states = defaultdict(lambda: defaultdict(int))             

    for i, user_id in enumerate(user_ids):
        # 查找用户位置信息所在分片的 ID ，以及信息在分片中的偏移量。
        shard_id, position = divmod(user_id, USERS_PER_SHARD)   
        offset = position * 2                                   

        # 发送另一个被流水线包裹的命令，获取用户的位置信息。
        pipe.substr('location:%s'%shard_id, offset, offset+1)   

        # 每处理 1000 个请求，
        # 程序就会调用之前定义的辅助函数对聚合数据进行一次更新。
        if (i+1) % 1000 == 0:                                   
            update_aggregates(countries, states, pipe.execute())

    # 对遍历余下的最后一批用户进行处理。
    update_aggregates(countries, states, pipe.execute())        

    # 返回聚合数据。
    return countries, states                                   
# <end id="aggregate-limited"/>

class TestCh09(unittest.TestCase):
    def setUp(self):
        self.conn = redis.Redis(db=15)
        self.conn.flushdb()
    def tearDown(self):
        self.conn.flushdb()

    def test_long_ziplist_performance(self):
        long_ziplist_performance(self.conn, 'test', 5, 10, 10)
        self.assertEquals(self.conn.llen('test'), 5)

    def test_shard_key(self):
        base = 'test'
        self.assertEquals(shard_key(base, 1, 2, 2), 'test:0')
        self.assertEquals(shard_key(base, '1', 2, 2), 'test:0')
        self.assertEquals(shard_key(base, 125, 1000, 100), 'test:1')
        self.assertEquals(shard_key(base, '125', 1000, 100), 'test:1')

        for i in xrange(50):
            self.assertTrue(0 <= int(shard_key(base, 'hello:%s'%i, 1000, 100).partition(':')[-1]) < 20)
            self.assertTrue(0 <= int(shard_key(base, i, 1000, 100).partition(':')[-1]) < 10)

    def test_sharded_hash(self):
        for i in xrange(50):
            shard_hset(self.conn, 'test', 'keyname:%s'%i, i, 1000, 100)
            self.assertEquals(shard_hget(self.conn, 'test', 'keyname:%s'%i, 1000, 100), str(i))
            shard_hset(self.conn, 'test2', i, i, 1000, 100)
            self.assertEquals(shard_hget(self.conn, 'test2', i, 1000, 100), str(i))

    def test_sharded_sadd(self):
        for i in xrange(50):
            shard_sadd(self.conn, 'testx', i, 50, 50)
        self.assertEquals(self.conn.scard('testx:0') + self.conn.scard('testx:1'), 50)

    def test_unique_visitors(self):
        global DAILY_EXPECTED
        DAILY_EXPECTED = 10000
        
        for i in xrange(179):
            count_visit(self.conn, str(uuid.uuid4()))
        self.assertEquals(self.conn.get('unique:%s'%(date.today().isoformat())), '179')

        self.conn.flushdb()
        self.conn.set('unique:%s'%((date.today() - timedelta(days=1)).isoformat()), 1000)
        for i in xrange(183):
            count_visit(self.conn, str(uuid.uuid4()))
        self.assertEquals(self.conn.get('unique:%s'%(date.today().isoformat())), '183')

    def test_user_location(self):
        i = 0
        for country in COUNTRIES:
            if country in STATES:
                for state in STATES[country]:
                    set_location(self.conn, i, country, state)
                    i += 1
            else:
                set_location(self.conn, i, country, '')
                i += 1
        
        _countries, _states = aggregate_location(self.conn)
        countries, states = aggregate_location_list(self.conn, range(i+1))
        
        self.assertEquals(_countries, countries)
        self.assertEquals(_states, states)

        for c in countries:
            if c in STATES:
                self.assertEquals(len(STATES[c]), countries[c])
                for s in STATES[c]:
                    self.assertEquals(states[c][s], 1)
            else:
                self.assertEquals(countries[c], 1)

if __name__ == '__main__':
    unittest.main()
