# coding: utf-8

import bisect
import contextlib
import csv
from datetime import datetime
import functools
import json
import logging
import random
import threading
import time
import unittest
import uuid

import redis

QUIT = False
SAMPLE_COUNT = 100

config_connection = None


# 代码清单 5-1
# <start id="recent_log"/>
# 设置一个字典，它可以帮助我们将大部分日志的安全级别转换成某种一致的东西。
SEVERITY = {                                                   
    logging.DEBUG: 'debug',                                    
    logging.INFO: 'info',                                      
    logging.WARNING: 'warning',                                
    logging.ERROR: 'error',                                    
    logging.CRITICAL: 'critical',                              
}                                                              
SEVERITY.update((name, name) for name in SEVERITY.values())    

def log_recent(conn, name, message, severity=logging.INFO, pipe=None):
    # 尝试将日志的级别转换成简单的字符串。
    severity = str(SEVERITY.get(severity, severity)).lower()   
    # 创建负责存储消息的键。
    destination = 'recent:%s:%s'%(name, severity)              
    # 将当前时间添加到消息里面，用于记录消息的发送时间。
    message = time.asctime() + ' ' + message                   
    # 使用流水线来将通信往返次数降低为一次。
    pipe = pipe or conn.pipeline()                             
    # 将消息添加到日志列表的最前面。
    pipe.lpush(destination, message)                           
    # 对日志列表进行修剪，让它只包含最新的100条消息。
    pipe.ltrim(destination, 0, 99)                             
    # 执行两个命令。
    pipe.execute()                                           
# <end id="recent_log"/>


# 代码清单 5-2
# <start id="common_log"/>
def log_common(conn, name, message, severity=logging.INFO, timeout=5):
    # 设置日志的级别。
    severity = str(SEVERITY.get(severity, severity)).lower() 
    # 负责存储最新日志的键。
    destination = 'common:%s:%s'%(name, severity)
    # 因为程序每小时需要轮换一次日志，所以它使用一个键来记录当前所处的小时数。
    start_key = destination + ':start'
    pipe = conn.pipeline()
    end = time.time() + timeout
    while time.time() < end:
        try:
            # 对记录当前小时数的键进行监视，确保轮换操作可以正确地执行。
            pipe.watch(start_key)
            # 取得当前时间。
            now = datetime.utcnow().timetuple()
            # 取得当前所处的小时数。
            hour_start = datetime(*now[:4]).isoformat() 

            existing = pipe.get(start_key)
            # 创建一个事务。
            pipe.multi()
            # 如果目前的常见日志列表是上一个小时的……
            if existing and existing < hour_start: 
                # ……那么将旧的常见日志信息进行归档。
                pipe.rename(destination, destination + ':last')
                pipe.rename(start_key, destination + ':pstart') 
                # 更新当前所处的小时数。
                pipe.set(start_key, hour_start)

            # 对记录日志出现次数的计数器执行自增操作。
            pipe.zincrby(destination, message) 
            # log_recent()函数负责记录日志并调用execute()函数。
            log_recent(pipe, name, message, severity, pipe) 
            return
        except redis.exceptions.WatchError:
            # 如果程序因为其他客户端在执行归档操作而出现监视错误，那么重试。
            continue
# <end id="common_log"/>


# 代码清单 5-3
# <start id="update_counter"/>
# 以秒为单位的计数器精度，分别为1秒钟、5秒钟、1分钟、5分钟、1小时、5小时、1天——用户可以按需调整这些精度。
PRECISION = [1, 5, 60, 300, 3600, 18000, 86400]         #A

def update_counter(conn, name, count=1, now=None):
    # 通过取得当前时间来判断应该对哪个时间片执行自增操作。
    now = now or time.time() 
    # 为了保证之后的清理工作可以正确地执行，这里需要创建一个事务型流水线。
    pipe = conn.pipeline() 
    # 为我们记录的每种精度都创建一个计数器。
    for prec in PRECISION:
        # 取得当前时间片的开始时间。
        pnow = int(now / prec) * prec
        # 创建负责存储计数信息的散列。
        hash = '%s:%s'%(prec, name)
        # 将计数器的引用信息添加到有序集合里面，
        # 并将其分值设置为0，以便在之后执行清理操作。
        pipe.zadd('known:', hash, 0)
        # 对给定名字和精度的计数器进行更新。
        pipe.hincrby('count:' + hash, pnow, count) 
    pipe.execute()
# <end id="update_counter"/>


# 代码清单 5-4
# <start id="get_counter"/>
def get_counter(conn, name, precision):
    # 取得存储着计数器数据的键的名字。
    hash = '%s:%s'%(precision, name)
    # 从Redis里面取出计数器数据。
    data = conn.hgetall('count:' + hash) 
    # 将计数器数据转换成指定的格式。
    to_return = []
    for key, value in data.iteritems():
        to_return.append((int(key), int(value))) 
    # 对数据进行排序，把旧的数据样本排在前面。
    to_return.sort() 
    return to_return
# <end id="get_counter"/>

# <start id="clean_counters"/>
def clean_counters(conn):
    pipe = conn.pipeline(True)
    # 为了平等地处理更新频率各不相同的多个计数器，程序需要记录清理操作执行的次数。
    passes = 0
    # 持续地对计数器进行清理，直到退出为止。
    while not QUIT:
        # 记录清理操作开始执行的时间，用于计算清理操作执行的时长。
        start = time.time()
        # 渐进地遍历所有已知的计数器。
        index = 0
        while index < conn.zcard('known:'):
            # 取得被检查计数器的数据。
            hash = conn.zrange('known:', index, index)
            index += 1
            if not hash:
                break
            hash = hash[0]
            # 取得计数器的精度。
            prec = int(hash.partition(':')[0])
            # 因为清理程序每60秒钟就会循环一次，
            # 所以这里需要根据计数器的更新频率来判断是否真的有必要对计数器进行清理。
            bprec = int(prec // 60) or 1
            # 如果这个计数器在这次循环里不需要进行清理，
            # 那么检查下一个计数器。
            # （举个例子，如果清理程序只循环了三次，而计数器的更新频率为每5分钟一次，
            # 那么程序暂时还不需要对这个计数器进行清理。）
            if passes % bprec:
                continue

            hkey = 'count:' + hash
            # 根据给定的精度以及需要保留的样本数量，
            # 计算出我们需要保留什么时间之前的样本。
            cutoff = time.time() - SAMPLE_COUNT * prec 
            # 获取样本的开始时间，并将其从字符串转换为整数。
            samples = map(int, conn.hkeys(hkey))
            # 计算出需要移除的样本数量。
            samples.sort()
            remove = bisect.bisect_right(samples, cutoff) 

            # 按需移除计数样本。
            if remove:
                conn.hdel(hkey, *samples[:remove]) 
                # 这个散列可能已经被清空。
                if remove == len(samples):
                    try:
                        # 在尝试修改计数器散列之前，对其进行监视。
                        pipe.watch(hkey)
                        # 验证计数器散列是否为空，如果是的话，
                        # 那么从记录已知计数器的有序集合里面移除它。
                        if not pipe.hlen(hkey):
                            pipe.multi()
                            pipe.zrem('known:', hash)  
                            pipe.execute()
                            # 在删除了一个计数器的情况下，
                            # 下次循环可以使用与本次循环相同的索引。
                            index -= 1
                        else:
                            # 计数器散列并不为空，
                            # 继续让它留在记录已有计数器的有序集合里面。
                            pipe.unwatch()
                    # 有其他程序向这个计算器散列添加了新的数据，
                    # 它已经不再是空的了，继续让它留在记录已知计数器的有序集合里面。
                    except redis.exceptions.WatchError:
                        pass

        # 为了让清理操作的执行频率与计数器更新的频率保持一致，
        # 对记录循环次数的变量以及记录执行时长的变量进行更新。
        passes += 1 
        duration = min(int(time.time() - start) + 1, 60)  
        # 如果这次循环未耗尽60秒钟，那么在余下的时间内进行休眠；
        # 如果60秒钟已经耗尽，那么休眠一秒钟以便稍作休息。
        time.sleep(max(60 - duration, 1))   
# <end id="clean_counters"/>


# 代码清单 5-6
# <start id="update_stats"/>
def update_stats(conn, context, type, value, timeout=5):
    # 设置用于存储统计数据的键。
    destination = 'stats:%s:%s'%(context, type) 
    # 像common_log()函数一样，
    # 处理当前这一个小时的数据和上一个小时的数据。
    start_key = destination + ':start'
    pipe = conn.pipeline(True)
    end = time.time() + timeout
    while time.time() < end:
        try:
            pipe.watch(start_key) 
            now = datetime.utcnow().timetuple() 
            hour_start = datetime(*now[:4]).isoformat() 

            existing = pipe.get(start_key)
            pipe.multi()
            if existing and existing < hour_start:
                pipe.rename(destination, destination + ':last') 
                pipe.rename(start_key, destination + ':pstart') 
                pipe.set(start_key, hour_start)

            tkey1 = str(uuid.uuid4())
            tkey2 = str(uuid.uuid4())
            # 将值添加到临时键里面。
            pipe.zadd(tkey1, 'min', value)
            pipe.zadd(tkey2, 'max', value)                     
            # 使用合适聚合函数MIN和MAX，
            # 对存储统计数据的键和两个临时键进行并集计算。
            pipe.zunionstore(destination,                     
                [destination, tkey1], aggregate='min')          
            pipe.zunionstore(destination,                      
                [destination, tkey2], aggregate='max')        

            # 删除临时键。
            pipe.delete(tkey1, tkey2)                           
            # 对有序集合中的样本数量、值的和、值的平方之和三个成员进行更新。
            pipe.zincrby(destination, 'count')                  
            pipe.zincrby(destination, 'sum', value)            
            pipe.zincrby(destination, 'sumsq', value*value)    

            # 返回基本的计数信息，以便函数调用者在有需要时做进一步的处理。
            return pipe.execute()[-3:]                        
        except redis.exceptions.WatchError:
            # 如果新的一个小时已经开始，并且旧的数据已经被归档，那么进行重试。
            continue                                           
# <end id="update_stats"/>


# 代码清单 5-7
# <start id="get_stats"/>
def get_stats(conn, context, type):
    # 程序将从这个键里面取出统计数据。
    key = 'stats:%s:%s'%(context, type)                            
    # 获取基本的统计数据，并将它们都放到一个字典里面。
    data = dict(conn.zrange(key, 0, -1, withscores=True))            
    # 计算平均值。
    data['average'] = data['sum'] / data['count']                     
    # 计算标准差的第一个步骤。
    numerator = data['sumsq'] - data['sum'] ** 2 / data['count']       
    # 完成标准差的计算工作。
    data['stddev'] = (numerator / (data['count'] - 1 or 1)) ** .5      
    return data
# <end id="get_stats"/>


# 代码清单 5-8
# <start id="access_time_context_manager"/>
# 将这个Python生成器用作上下文管理器。
@contextlib.contextmanager                                            
def access_time(conn, context):
    # 记录代码块执行前的时间。
    start = time.time()                                               
    # 运行被包裹的代码块。
    yield                                                              

    # 计算代码块的执行时长。
    delta = time.time() - start                                        
    # 更新这一上下文的统计数据。
    stats = update_stats(conn, context, 'AccessTime', delta)           
    # 计算页面的平均访问时长。
    average = stats[1] / stats[0]                                      

    pipe = conn.pipeline(True)
    # 将页面的平均访问时长添加到记录最慢访问时间的有序集合里面。
    pipe.zadd('slowest:AccessTime', context, average)                 
    # AccessTime有序集合只会保留最慢的100条记录。
    pipe.zremrangebyrank('slowest:AccessTime', 0, -101)                
    pipe.execute()
# <end id="access_time_context_manager"/>


# <start id="access_time_use"/>
# 这个视图（view）接受一个Redis连接以及一个生成内容的回调函数为参数。
def process_view(conn, callback):             
    # 计算并记录访问时长的上下文管理器就是这样包围代码块的。
    with access_time(conn, request.path):     
        # 当上下文管理器中的yield语句被执行时，这个语句就会被执行。
        return callback()                      
# <end id="access_time_use"/>


# 代码清单 5-9
# <start id="_1314_14473_9188"/>
def ip_to_score(ip_address):
    score = 0
    for v in ip_address.split('.'):
        score = score * 256 + int(v, 10)
    return score
# <end id="_1314_14473_9188"/>


# 代码清单 5-10
# <start id="_1314_14473_9191"/>
# 这个函数在执行时需要给定GeoLiteCity-Blocks.csv文件所在的位置。
def import_ips_to_redis(conn, filename):             
    csv_file = csv.reader(open(filename, 'rb'))
    for count, row in enumerate(csv_file):
        # 按需将IP地址转换为分值。
        start_ip = row[0] if row else ''             
        if 'i' in start_ip.lower():
            continue
        if '.' in start_ip:                            
            start_ip = ip_to_score(start_ip)           
        elif start_ip.isdigit():                       
            start_ip = int(start_ip, 10)               
        else:
            # 略过文件的第一行以及格式不正确的条目。
            continue                                  

        # 构建唯一城市ID。
        city_id = row[2] + '_' + str(count)            
        # 将城市ID及其对应的IP地址分值添加到有序集合里面。
        conn.zadd('ip2cityid:', city_id, start_ip) 
# <end id="_1314_14473_9191"/>


# 代码清单 5-11
# <start id="_1314_14473_9194"/>
# 这个函数在执行时需要给定GeoLiteCity-Location.csv文件所在的位置。
def import_cities_to_redis(conn, filename):  
    for row in csv.reader(open(filename, 'rb')):
        if len(row) < 4 or not row[0].isdigit():
            continue
        row = [i.decode('latin-1') for i in row]
        # 准备好需要添加到散列里面的信息。
        city_id = row[0]                          
        country = row[1]                           
        region = row[2]                            
        city = row[3]                             
        # 将城市信息添加到Redis里面。
        conn.hset('cityid2city:', city_id, 
            json.dumps([city, region, country])) 
# <end id="_1314_14473_9194"/>


# 代码清单 5-12
# <start id="_1314_14473_9197"/>
def find_city_by_ip(conn, ip_address):
    # 将IP地址转换为分值以便执行ZREVRANGEBYSCORE命令。
    if isinstance(ip_address, str):                        #A
        ip_address = ip_to_score(ip_address)               #A

    # 查找唯一城市ID。
    city_id = conn.zrevrangebyscore(                       #B
        'ip2cityid:', ip_address, 0, start=0, num=1)       #B

    if not city_id:
        return None

    # 将唯一城市ID转换为普通城市ID。
    city_id = city_id[0].partition('_')[0]                 #C
    # 从散列里面取出城市信息。
    return json.loads(conn.hget('cityid2city:', city_id))  #D
# <end id="_1314_14473_9197"/>


# 代码清单 5-13
# <start id="is_under_maintenance"/>
LAST_CHECKED = None
IS_UNDER_MAINTENANCE = False

def is_under_maintenance(conn):
    # 将两个变量设置为全局变量以便在之后对它们进行写入。
    global LAST_CHECKED, IS_UNDER_MAINTENANCE   #A

    # 距离上次检查是否已经超过1秒钟？
    if LAST_CHECKED < time.time() - 1:          #B
        # 更新最后检查时间。
        LAST_CHECKED = time.time()              #C
        # 检查系统是否正在进行维护。
        IS_UNDER_MAINTENANCE = bool(            #D
            conn.get('is-under-maintenance'))   #D

    # 返回一个布尔值，用于表示系统是否正在进行维护。
    return IS_UNDER_MAINTENANCE                 #E
# <end id="is_under_maintenance"/>


# 代码清单 5-14
# <start id="set_config"/>
def set_config(conn, type, component, config):
    conn.set(
        'config:%s:%s'%(type, component),
        json.dumps(config))
# <end id="set_config"/>
#END


# 代码清单 5-15
# <start id="get_config"/>
CONFIGS = {}
CHECKED = {}

def get_config(conn, type, component, wait=1):
    key = 'config:%s:%s'%(type, component)

    # 检查是否需要对这个组件的配置信息进行更新。
    if CHECKED.get(key) < time.time() - wait:     
        # 有需要对配置进行更新，记录最后一次检查这个连接的时间。
        CHECKED[key] = time.time() 
        # 取得Redis存储的组件配置。
        config = json.loads(conn.get(key) or '{}')    
        # 将潜在的Unicode关键字参数转换为字符串关键字参数。
        config = dict((str(k), config[k]) for k in config)
        # 取得组件正在使用的配置。
        old_config = CONFIGS.get(key)                  

        # 如果两个配置并不相同……
        if config != old_config:                    
            # ……那么对组件的配置进行更新。
            CONFIGS[key] = config                     

    return CONFIGS.get(key)
# <end id="get_config"/>


# 代码清单 5-16
# <start id="redis_connection"/>
REDIS_CONNECTIONS = {}

# 将应用组件的名字传递给装饰器。
def redis_connection(component, wait=1):                        #A
    # 因为函数每次被调用都需要获取这个配置键，所以我们干脆把它缓存起来。
    key = 'config:redis:' + component                           #B
    # 包装器接受一个函数作为参数，并使用另一个函数来包裹这个函数。
    def wrapper(function):                                      #C
        # 将被包裹函数里的一些有用的元数据复制到配置处理器。
        @functools.wraps(function)                              #D
        # 创建负责管理连接信息的函数。
        def call(*args, **kwargs):                              #E
            # 如果有旧配置存在，那么获取它。
            old_config = CONFIGS.get(key, object())             #F
            # 如果有新配置存在，那么获取它。
            _config = get_config(                               #G
                config_connection, 'redis', component, wait)    #G

            config = {}
            # 对配置进行处理并将其用于创建Redis连接。
            for k, v in _config.iteritems():                    #L
                config[k.encode('utf-8')] = v                   #L

            # 如果新旧配置并不相同，那么创建新的连接。
            if config != old_config:                            #H
                REDIS_CONNECTIONS[key] = redis.Redis(**config)  #H

            # 将Redis连接以及其他匹配的参数传递给被包裹函数，然后调用函数并返回执行结果。
            return function(                                    #I
                REDIS_CONNECTIONS.get(key), *args, **kwargs)    #I
        # 返回被包裹的函数。
        return call                                             #J
    # 返回用于包裹Redis函数的包装器。
    return wrapper                                              #K
# <end id="redis_connection"/>


# 代码清单 5-17
'''
# <start id="recent_log_decorator"/>
@redis_connection('logs')                   # redis_connection()装饰器非常容易使用。
def log_recent(conn, app, message):         # 这个函数的定义和之前展示的一样，没有发生任何变化。
    'the old log_recent() code'

log_recent('main', 'User 235 logged in')    # 我们再也不必在调用log_recent()函数时手动地向它传递日志服务器的连接了。
# <end id="recent_log_decorator"/>
'''

#--------------- 以下是用于测试代码的辅助函数 --------------------------------

class request:
    pass

# a faster version with pipelines for actual testing
def import_ips_to_redis(conn, filename):
    csv_file = csv.reader(open(filename, 'rb'))
    pipe = conn.pipeline(False)
    for count, row in enumerate(csv_file):
        start_ip = row[0] if row else ''
        if 'i' in start_ip.lower():
            continue
        if '.' in start_ip:
            start_ip = ip_to_score(start_ip)
        elif start_ip.isdigit():
            start_ip = int(start_ip, 10)
        else:
            continue

        city_id = row[2] + '_' + str(count)
        pipe.zadd('ip2cityid:', city_id, start_ip)
        if not (count+1) % 1000:
            pipe.execute()
    pipe.execute()

def import_cities_to_redis(conn, filename):
    pipe = conn.pipeline(False)
    for count, row in enumerate(csv.reader(open(filename, 'rb'))):
        if len(row) < 4 or not row[0].isdigit():
            continue
        row = [i.decode('latin-1') for i in row]
        city_id = row[0]
        country = row[1]
        region = row[2]
        city = row[3]
        pipe.hset('cityid2city:', city_id,
            json.dumps([city, region, country]))
        if not (count+1) % 1000:
            pipe.execute()
    pipe.execute()

class TestCh05(unittest.TestCase):
    def setUp(self):
        global config_connection
        import redis
        self.conn = config_connection = redis.Redis(db=15)
        self.conn.flushdb()

    def tearDown(self):
        self.conn.flushdb()
        del self.conn
        global config_connection, QUIT, SAMPLE_COUNT
        config_connection = None
        QUIT = False
        SAMPLE_COUNT = 100
        print
        print

    def test_log_recent(self):
        import pprint
        conn = self.conn

        print "Let's write a few logs to the recent log"
        for msg in xrange(5):
            log_recent(conn, 'test', 'this is message %s'%msg)
        recent = conn.lrange('recent:test:info', 0, -1)
        print "The current recent message log has this many messages:", len(recent)
        print "Those messages include:"
        pprint.pprint(recent[:10])
        self.assertTrue(len(recent) >= 5)

    def test_log_common(self):
        import pprint
        conn = self.conn

        print "Let's write some items to the common log"
        for count in xrange(1, 6):
            for i in xrange(count):
                log_common(conn, 'test', "message-%s"%count)
        common = conn.zrevrange('common:test:info', 0, -1, withscores=True)
        print "The current number of common messages is:", len(common)
        print "Those common messages are:"
        pprint.pprint(common)
        self.assertTrue(len(common) >= 5)

    def test_counters(self):
        import pprint
        global QUIT, SAMPLE_COUNT
        conn = self.conn

        print "Let's update some counters for now and a little in the future"
        now = time.time()
        for delta in xrange(10):
            update_counter(conn, 'test', count=random.randrange(1,5), now=now+delta)
        counter = get_counter(conn, 'test', 1)
        print "We have some per-second counters:", len(counter)
        self.assertTrue(len(counter) >= 10)
        counter = get_counter(conn, 'test', 5)
        print "We have some per-5-second counters:", len(counter)
        print "These counters include:"
        pprint.pprint(counter[:10])
        self.assertTrue(len(counter) >= 2)
        print

        tt = time.time
        def new_tt():
            return tt() + 2*86400
        time.time = new_tt

        print "Let's clean out some counters by setting our sample count to 0"
        SAMPLE_COUNT = 0
        t = threading.Thread(target=clean_counters, args=(conn,))
        t.setDaemon(1) # to make sure it dies if we ctrl+C quit
        t.start()
        time.sleep(1)
        QUIT = True
        time.time = tt
        counter = get_counter(conn, 'test', 86400)
        print "Did we clean out all of the counters?", not counter
        self.assertFalse(counter)

    def test_stats(self):
        import pprint
        conn = self.conn

        print "Let's add some data for our statistics!"
        for i in xrange(5):
            r = update_stats(conn, 'temp', 'example', random.randrange(5, 15))
        print "We have some aggregate statistics:", r
        rr = get_stats(conn, 'temp', 'example')
        print "Which we can also fetch manually:"
        pprint.pprint(rr)
        self.assertTrue(rr['count'] >= 5)

    def test_access_time(self):
        import pprint
        conn = self.conn

        print "Let's calculate some access times..."
        for i in xrange(10):
            with access_time(conn, "req-%s"%i):
                time.sleep(.5 + random.random())
        print "The slowest access times are:"
        atimes = conn.zrevrange('slowest:AccessTime', 0, -1, withscores=True)
        pprint.pprint(atimes[:10])
        self.assertTrue(len(atimes) >= 10)
        print

        def cb():
            time.sleep(1 + random.random())

        print "Let's use the callback version..."
        for i in xrange(5):
            request.path = 'cbreq-%s'%i
            process_view(conn, cb)
        print "The slowest access times are:"
        atimes = conn.zrevrange('slowest:AccessTime', 0, -1, withscores=True)
        pprint.pprint(atimes[:10])
        self.assertTrue(len(atimes) >= 10)

    def test_ip_lookup(self):
        conn = self.conn

        try:
            open('GeoLiteCity-Blocks.csv', 'rb')
            open('GeoLiteCity-Location.csv', 'rb')
        except:
            print "********"
            print "You do not have the GeoLiteCity database available, aborting test"
            print "Please have the following two files in the current path:"
            print "GeoLiteCity-Blocks.csv"
            print "GeoLiteCity-Location.csv"
            print "********"
            return

        print "Importing IP addresses to Redis... (this may take a while)"
        import_ips_to_redis(conn, 'GeoLiteCity-Blocks.csv')
        ranges = conn.zcard('ip2cityid:')
        print "Loaded ranges into Redis:", ranges
        self.assertTrue(ranges > 1000)
        print

        print "Importing Location lookups to Redis... (this may take a while)"
        import_cities_to_redis(conn, 'GeoLiteCity-Location.csv')
        cities = conn.hlen('cityid2city:')
        print "Loaded city lookups into Redis:", cities
        self.assertTrue(cities > 1000)
        print

        print "Let's lookup some locations!"
        rr = random.randrange
        for i in xrange(5):
            print find_city_by_ip(conn, '%s.%s.%s.%s'%(rr(1,255), rr(256), rr(256), rr(256)))

    def test_is_under_maintenance(self):
        print "Are we under maintenance (we shouldn't be)?", is_under_maintenance(self.conn)
        self.conn.set('is-under-maintenance', 'yes')
        print "We cached this, so it should be the same:", is_under_maintenance(self.conn)
        time.sleep(1)
        print "But after a sleep, it should change:", is_under_maintenance(self.conn)
        print "Cleaning up..."
        self.conn.delete('is-under-maintenance')
        time.sleep(1)
        print "Should be False again:", is_under_maintenance(self.conn)

    def test_config(self):
        print "Let's set a config and then get a connection from that config..."
        set_config(self.conn, 'redis', 'test', {'db':15})
        @redis_connection('test')
        def test(conn2):
            return bool(conn2.info())
        print "We can run commands from the configured connection:", test()

if __name__ == '__main__':
    unittest.main()
