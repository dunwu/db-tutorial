# coding: utf-8

import binascii
from collections import defaultdict
from datetime import date
from decimal import Decimal
import functools
import json
from Queue import Empty, Queue
import threading
import time
import unittest
import uuid

import redis

CONFIGS = {}
CHECKED = {}

def get_config(conn, type, component, wait=1):
    key = 'config:%s:%s'%(type, component)

    if CHECKED.get(key) < time.time() - wait:           #A
        CHECKED[key] = time.time()                      #B
        config = json.loads(conn.get(key) or '{}')      #C
        config = dict((str(k), config[k]) for k in config)
        old_config = CONFIGS.get(key)                   #D

        if config != old_config:                        #E
            CONFIGS[key] = config                       #F

    return CONFIGS.get(key)

REDIS_CONNECTIONS = {}
config_connection = None

def redis_connection(component, wait=1):                        #A
    key = 'config:redis:' + component                           #B
    def wrapper(function):                                      #C
        @functools.wraps(function)                              #D
        def call(*args, **kwargs):                              #E
            old_config = CONFIGS.get(key, object())             #F
            _config = get_config(                               #G
                config_connection, 'redis', component, wait)    #G

            config = {}
            for k, v in _config.iteritems():                    #L
                config[k.encode('utf-8')] = v                   #L

            if config != old_config:                            #H
                REDIS_CONNECTIONS[key] = redis.Redis(**config)  #H

            return function(                                    #I
                REDIS_CONNECTIONS.get(key), *args, **kwargs)    #I
        return call                                             #J
    return wrapper                                              #K

def index_document(conn, docid, words, scores):
    pipeline = conn.pipeline(True)
    for word in words:                                                  #I
        pipeline.sadd('idx:' + word, docid)                             #I
    pipeline.hmset('kb:doc:%s'%docid, scores)
    return len(pipeline.execute())                                      #J

def parse_and_search(conn, query, ttl):
    id = str(uuid.uuid4())
    conn.sinterstore('idx:' + id,
        ['idx:'+key for key in query])
    conn.expire('idx:' + id, ttl)
    return id

def search_and_sort(conn, query, id=None, ttl=300, sort="-updated", #A
                    start=0, num=20):                               #A
    desc = sort.startswith('-')                                     #B
    sort = sort.lstrip('-')                                         #B
    by = "kb:doc:*->" + sort                                        #B
    alpha = sort not in ('updated', 'id', 'created')                #I

    if id and not conn.expire(id, ttl):     #C
        id = None                           #C

    if not id:                                      #D
        id = parse_and_search(conn, query, ttl=ttl) #D

    pipeline = conn.pipeline(True)
    pipeline.scard('idx:' + id)                                     #E
    pipeline.sort('idx:' + id, by=by, alpha=alpha,                  #F
        desc=desc, start=start, num=num)                            #F
    results = pipeline.execute()

    return results[0], results[1], id                               #G

def zintersect(conn, keys, ttl):
    id = str(uuid.uuid4())
    conn.zinterstore('idx:' + id,
        dict(('idx:'+k, v) for k,v in keys.iteritems()))
    conn.expire('idx:' + id, ttl)
    return id

def search_and_zsort(conn, query, id=None, ttl=300, update=1, vote=0,   #A
                    start=0, num=20, desc=True):                        #A

    if id and not conn.expire(id, ttl):     #B
        id = None                           #B

    if not id:                                      #C
        id = parse_and_search(conn, query, ttl=ttl) #C

        scored_search = {                           #D
            id: 0,                                  #D
            'sort:update': update,                  #D
            'sort:votes': vote                      #D
        }
        id = zintersect(conn, scored_search, ttl)   #E

    pipeline = conn.pipeline(True)
    pipeline.zcard('idx:' + id)                                     #F
    if desc:                                                        #G
        pipeline.zrevrange('idx:' + id, start, start + num - 1)     #G
    else:                                                           #G
        pipeline.zrange('idx:' + id, start, start + num - 1)        #G
    results = pipeline.execute()

    return results[0], results[1], id                               #H

def execute_later(conn, queue, name, args):
    t = threading.Thread(target=globals()[name], args=tuple(args))
    t.setDaemon(1)
    t.start()

HOME_TIMELINE_SIZE = 1000
POSTS_PER_PASS = 1000

def shard_key(base, key, total_elements, shard_size):   #A
    if isinstance(key, (int, long)) or key.isdigit():   #B
        shard_id = int(str(key), 10) // shard_size      #C
    else:
        shards = 2 * total_elements // shard_size       #D
        shard_id = binascii.crc32(key) % shards         #E
    return "%s:%s"%(base, shard_id)                     #F

def shard_sadd(conn, base, member, total_elements, shard_size):
    shard = shard_key(base,
        'x'+str(member), total_elements, shard_size)            #A
    return conn.sadd(shard, member)                             #B

SHARD_SIZE = 512
EXPECTED = defaultdict(lambda: 1000000)


# 代码清单 10-1
# <start id="get-connection"/>
def get_redis_connection(component, wait=1):
    key = 'config:redis:' + component
    # 尝试获取旧的配置。
    old_config = CONFIGS.get(key, object())           
    # 尝试获取新的配置。
    config = get_config(                                
        config_connection, 'redis', component, wait)    

    # 如果新旧配置不相同，那么创建一个新的连接。
    if config != old_config:                            
        REDIS_CONNECTIONS[key] = redis.Redis(**config)  

    # 返回用户指定的连接对象。
    return REDIS_CONNECTIONS.get(key)                  
# <end id="get-connection"/>


# 代码清单 10-2
# <start id="get-sharded-connection"/>
def get_sharded_connection(component, key, shard_count, wait=1):
    # 计算出 “<组件名>:<分片数字>” 格式的分片 ID 。
    shard = shard_key(component, 'x'+str(key), shard_count, 2)  
    # 返回连接。
    return get_redis_connection(shard, wait)                   
# <end id="get-sharded-connection"/>


# <start id="no-decorator-example"/>
def log_recent(conn, app, message):
    'the old log_recent() code'

log_recent = redis_connection('logs')(log_recent)   # 通过反复执行 3 次这行代码，可以达到和装饰器一样的效果
# <end id="no-decorator-example"/>

# 代码清单 10-3
# <start id="shard-aware-decorator"/>
# 装饰器接受组件名以及预期的分片数量作为参数。
def sharded_connection(component, shard_count, wait=1):        
    # 创建一个包装器，使用它去装饰传入的函数。
    def wrapper(function):                                     
        # 从原始函数里面复制一些有用的元信息到配置处理器。
        @functools.wraps(function)                            
        # 创建一个函数，它负责计算键的分片 ID ，并对连接管理器进行设置。
        def call(key, *args, **kwargs):                        
            # 获取分片连接。
            conn = get_sharded_connection(                     
                component, key, shard_count, wait)            
            # 实际地调用被装饰的函数，并将分片连接以及其他参数传递给它。
            return function(conn, key, *args, **kwargs)        
        # 返回被包装后的函数。
        return call                                             
    # 返回一个函数，它可以对需要分片连接的函数进行包装。
    return wrapper                                             
# <end id="shard-aware-decorator"/>


# 代码清单 10-4
# <start id="sharded-count-unique"/>
# 将 count_visit() 函数分片到 16 台机器上面执行，
# 执行所得的结果将被自动地分片到每台机器的多个数据库键上面。
@sharded_connection('unique', 16)                      
def count_visit(conn, session_id):
    today = date.today()
    key = 'unique:%s'%today.isoformat()
    # 经过修改的 get_expected() 调用。
    conn2, expected = get_expected(key, today)        

    id = int(session_id.replace('-', '')[:15], 16)
    if shard_sadd(conn, key, id, expected, SHARD_SIZE):
        # 使用 get_expected() 函数返回的非分片（nonsharded）连接，
        # 对唯一计数器执行自增操作。
        conn2.incr(key)                               

# 对 get_expected() 函数使用非分片连接。
@redis_connection('unique')                            
def get_expected(conn, key, today):
    'all of the same function body as before, except the last line'
    # 返回非分片连接，
    # 使得 count_visit() 函数可以在有需要的时候，
    # 对唯一计数器执行自增操作。
    return conn, EXPECTED[key]                        
# <end id="sharded-count-unique"/>


# 代码清单 10-5
# <start id="search-with-values"/>
# 这个函数接受的参数与 search_and_sort() 函数接受的参数完全相同。
def search_get_values(conn, query, id=None, ttl=300, sort="-updated", 
                      start=0, num=20):                               
    # 首先取得搜索操作和排序操作的执行结果。
    count, docids, id = search_and_sort(                           
        conn, query, id, ttl, sort, 0, start+num)                  

    key = "kb:doc:%s"
    sort = sort.lstrip('-')

    pipe = conn.pipeline(False)
    # 根据结果的排序方式来获取数据。
    for docid in docids:                                           
        pipe.hget(key%docid, sort)                                 
    sort_column = pipe.execute()                                   

    # 将文档 ID 以及对文档进行排序产生的数据进行配对（pair up）。
    data_pairs = zip(docids, sort_column)                          
    # 返回结果包含的文档数量、排序之后的搜索结果以及结果的缓存 ID 。
    return count, data_pairs, id                                   
# <end id="search-with-values"/>


# 代码清单 10-6
# <start id="search-on-shards"/>
# 程序为了获知自己要连接的服务器，
# 会假定所有分片服务器的信息都记录在一个标准的配置位置里面。
def get_shard_results(component, shards, query, ids=None, ttl=300,  
                  sort="-updated", start=0, num=20, wait=1):        

    # 准备一些结构，用于储存之后获取的数据。
    count = 0      
    data = []      
    # 尝试使用已被缓存的搜索结果；
    # 如果没有缓存结果可用，那么重新执行查询。
    ids = ids or shards * [None]       
    for shard in xrange(shards):
        # 获取或者创建一个连向指定分片的连接。
        conn = get_redis_connection('%s:%s'%(component, shard), wait)
        # 获取搜索结果以及它们的排序数据。
        c, d, i = search_get_values(                        
            conn, query, ids[shard], ttl, sort, start, num) 

        # 将这个分片的计算结果与其他分片的计算结果进行合并。
        count += c          
        data.extend(d)     
        ids[shard] = i      

    # 把所有分片的原始（raw）计算结果返回给调用者。
    return count, data, ids    
# <end id="search-on-shards"/>

def get_values_thread(component, shard, wait, rqueue, *args, **kwargs):
    conn = get_redis_connection('%s:%s'%(component, shard), wait)
    count, results, id = search_get_values(conn, *args, **kwargs)
    rqueue.put((shard, count, results, id))

def get_shard_results_thread(component, shards, query, ids=None, ttl=300,
                  sort="-updated", start=0, num=20, wait=1, timeout=.5):

    ids = ids or shards * [None]
    rqueue = Queue()

    for shard in xrange(shards):
        t = threading.Thread(target=get_values_thread, args=(
            component, shard, wait, rqueue, query, ids[shard],
            ttl, sort, start, num))
        t.setDaemon(1)
        t.start()

    received = 0
    count = 0
    data = []
    deadline = time.time() + timeout
    while received < shards and time.time() < deadline:
        try:
            sh, c, r, i = rqueue.get(timeout=max(deadline-time.time(), .001))
        except Empty:
            break
        else:
            count += c
            data.extend(r)
            ids[sh] = i

    return count, data, ids


# 代码清单 10-7
# <start id="merge-sharded-results"/>
def to_numeric_key(data):
    try:
        # 这里之所以使用 Decimal 数字类型，
        # 是因为这种类型可以合理地对整数和浮点数进行转换，
        # 并在值缺失或者不是数字值的时候，
        # 返回默认值 0 。
        return Decimal(data[1] or '0')     
    except:
        return Decimal('0')               

def to_string_key(data):
    # 总是返回一个字符串，即使在值缺失的情况下，也是如此。
    return data[1] or ''                   

# 这个函数需要接受所有分片参数和搜索参数，
# 这些参数大部分都会被传给底层的函数，
# 而这个函数本身只会用到 sort 参数以及搜索偏移量。
def search_shards(component, shards, query, ids=None, ttl=300,     
                  sort="-updated", start=0, num=20, wait=1):       

    # 获取未经排序的分片搜索结果。
    count, data, ids = get_shard_results(                           
        component, shards, query, ids, ttl, sort, start, num, wait) 

    # 准备好进行排序所需的各个参数。
    reversed = sort.startswith('-')                    
    sort = sort.strip('-')                             
    key = to_numeric_key                               
    if sort not in ('updated', 'id', 'created'):        
        key = to_string_key                             

    # 根据 sort 参数对搜索结果进行排序。
    data.sort(key=key, reverse=reversed)               

    results = []
    # 只获取用户指定的那一页搜索结果。
    for docid, score in data[start:start+num]:         
        results.append(docid)                          

    # 返回被选中的结果，其中包括由每个分片的缓存 ID 组成的序列。
    return count, results, ids                         
# <end id="merge-sharded-results"/>


# 代码清单 10-8
# <start id="zset-search-with-values"/>
# 这个函数接受 search_and_zsort() 函数所需的全部参数。
def search_get_zset_values(conn, query, id=None, ttl=300, update=1, 
                    vote=0, start=0, num=20, desc=True):            

    # 调用底层的 search_and_zsort() 函数，
    # 获取搜索结果的缓存 ID 以及结果包含的文档数量。
    count, r, id = search_and_zsort(                                
        conn, query, id, ttl, update, vote, 0, 1, desc)             

    # 获取指定的搜索结果以及这些结果的分值。
    if desc:                                                        
        data = conn.zrevrange(id, 0, start + num - 1, withscores=True)
    else:                                                          
        data = conn.zrange(id, 0, start + num - 1, withscores=True) 

    # 返回搜索结果的数量、搜索结果本身、搜索结果的分值以及搜索结果的缓存 ID 。
    return count, data, id                                          
# <end id="zset-search-with-values"/>


# 代码清单 10-9
# <start id="search-shards-zset"/>
# 函数需要接受所有分片参数以及所有搜索参数。
def search_shards_zset(component, shards, query, ids=None, ttl=300,   
                update=1, vote=0, start=0, num=20, desc=True, wait=1):

    # 准备一些结构，用于储存之后获取到的数据。
    count = 0                       
    data = []                       
    # 尝试使用已有的缓存结果；
    # 如果没有缓存结果可用，那么开始一次新的搜索。
    ids = ids or shards * [None]    
    for shard in xrange(shards):
        # 获取或者创建指向每个分片的连接。
        conn = get_redis_connection('%s:%s'%(component, shard), wait) 
        # 在分片上面进行搜索，并取得搜索结果的分值。
        c, d, i = search_get_zset_values(conn, query, ids[shard],     
            ttl, update, vote, start, num, desc)                      

        # 对每个分片的搜索结果进行合并。
        count += c      
        data.extend(d)  
        ids[shard] = i  

    # 定义一个简单的排序辅助函数，让它只返回与分值有关的信息。
    def key(result):       
        return result[1]   

    # 对所有搜索结果进行排序。
    data.sort(key=key, reversed=desc)   
    results = []
    # 从结果里面提取出文档 ID ，并丢弃与之关联的分值。
    for docid, score in data[start:start+num]:  
        results.append(docid)                  

    # 将搜索结果返回给调用者。
    return count, results, ids                  
# <end id="search-shards-zset"/>


# 代码清单 10-11
# <start id="sharded-api-base"/>
class KeyShardedConnection(object):
    # 对象使用组件名字以及分片数量进行初始化。
    def __init__(self, component, shards):        
        self.component = component                 
        self.shards = shards                      
    # 当用户尝试从对象里面获取一个元素的时候，
    # 这个方法就会被调用，
    # 而调用这个方法时传入的参数就是用户请求的元素。
    def __getitem__(self, key):                    
        # 根据传入的键以及之前已知的组件名字和分片数量，
        # 获取分片连接。
        return get_sharded_connection(             
            self.component, key, self.shards)     
# <end id="sharded-api-base"/>


# 代码清单 10-10
# <start id="sharded-api-example"/>
# 创建一个连接，这个连接包含对拥有指定分片数量的组件进行分片所需的相关信息。
sharded_timelines = KeyShardedConnection('timelines', 8)   

def follow_user(conn, uid, other_uid):
    fkey1 = 'following:%s'%uid
    fkey2 = 'followers:%s'%other_uid

    if conn.zscore(fkey1, other_uid):
        print "already followed", uid, other_uid
        return None

    now = time.time()

    pipeline = conn.pipeline(True)
    pipeline.zadd(fkey1, other_uid, now)
    pipeline.zadd(fkey2, uid, now)
    pipeline.zcard(fkey1)
    pipeline.zcard(fkey2)
    following, followers = pipeline.execute()[-2:]
    pipeline.hset('user:%s'%uid, 'following', following)
    pipeline.hset('user:%s'%other_uid, 'followers', followers)
    pipeline.execute()

    pkey = 'profile:%s'%other_uid
    # 从正在关注的用户的个人时间线里面，取出最新的状态消息。
    status_and_score = sharded_timelines[pkey].zrevrange(   
        pkey, 0, HOME_TIMELINE_SIZE-1, withscores=True)     

    if status_and_score:
        hkey = 'home:%s'%uid
        # 根据被分片的键获取一个连接，然后通过连接获取一个流水线对象。
        pipe = sharded_timelines[hkey].pipeline(True)       
        # 将一系列状态消息添加到位于分片上面的定制时间线有序集合里面，
        # 并在添加操作完成之后，对有序集合进行修剪。
        pipe.zadd(hkey, **dict(status_and_score))           
        pipe.zremrangebyrank(hkey, 0, -HOME_TIMELINE_SIZE-1)
        # 执行事务。
        pipe.execute()                                      

    return True
# <end id="sharded-api-example"/>


# 代码清单 10-13
# <start id="key-data-sharded-api"/>
class KeyDataShardedConnection(object):
    # 对象使用组件名和分片数量进行初始化。
    def __init__(self, component, shards):         
        self.component = component                 
        self.shards = shards                       
    # 当一对 ID 作为字典查找操作的其中一个参数被传入时，
    # 这个方法将被调用。
    def __getitem__(self, ids):                   
        # 取出两个 ID ，并确保它们都是整数。
        id1, id2 = map(int, ids)                   
        # 如果第二个 ID 比第一个 ID 要小，
        # 那么对调两个 ID 的位置，
        # 从而确保第一个 ID 总是小于或等于第二个 ID 。
        if id2 < id1:                              
            id1, id2 = id2, id1                    
        # 基于两个 ID 构建出一个键。
        key = "%s:%s"%(id1, id2)                    
        # 使用构建出的键以及之前已知的组件名和分片数量，
        # 获取分片连接。
        return get_sharded_connection(             
            self.component, key, self.shards)       
# <end id="key-data-sharded-api"/>


_follow_user = follow_user
# 代码清单 10-12
# <start id="sharded-api-example2"/>
# 创建一个连接，
# 这个连接包含对拥有指定分片数量的组件进行分片所需的相关信息。
sharded_timelines = KeyShardedConnection('timelines', 8)        
sharded_followers = KeyDataShardedConnection('followers', 16)   

def follow_user(conn, uid, other_uid):
    fkey1 = 'following:%s'%uid
    fkey2 = 'followers:%s'%other_uid

    # 根据 uid 和 other_uid 获取连接对象。
    sconn = sharded_followers[uid, other_uid]          
    # 检查 other_uid 代表的用户是否已经关注了 uid 代表的用户。
    if sconn.zscore(fkey1, other_uid):                 
        return None

    now = time.time()
    spipe = sconn.pipeline(True)
    # 把关注者的信息以及被关注者的信息添加到有序集合里面。
    spipe.zadd(fkey1, other_uid, now)                  
    spipe.zadd(fkey2, uid, now)                        
    following, followers = spipe.execute()

    pipeline = conn.pipeline(True)
    # 为执行关注操作的用户以及被关注的用户更新关注者信息和正在关注信息。
    pipeline.hincrby('user:%s'%uid, 'following', int(following))      
    pipeline.hincrby('user:%s'%other_uid, 'followers', int(followers))
    pipeline.execute()

    pkey = 'profile:%s'%other_uid
    status_and_score = sharded_timelines[pkey].zrevrange(
        pkey, 0, HOME_TIMELINE_SIZE-1, withscores=True)

    if status_and_score:
        hkey = 'home:%s'%uid
        pipe = sharded_timelines[hkey].pipeline(True)
        pipe.zadd(hkey, **dict(status_and_score))
        pipe.zremrangebyrank(hkey, 0, -HOME_TIMELINE_SIZE-1)
        pipe.execute()

    return True
# <end id="sharded-api-example2"/>


# 代码清单 10-14
# <start id="sharded-zrangebyscore"/>
# 函数接受组件名称、分片数量以及那些可以在分片环境下产生正确行为的参数作为参数。
def sharded_zrangebyscore(component, shards, key, min, max, num):  
    data = []
    for shard in xrange(shards):
        # 获取指向当前分片的分片连接。
        conn = get_redis_connection("%s:%s"%(component, shard))     
        # 从 Redis 分片上面取出数据。
        data.extend(conn.zrangebyscore(                             
            key, min, max, start=0, num=num, withscores=True))      

    # 首先基于分值对数据进行排序，然后再基于成员进行排序。
    def key(pair):                     
        return pair[1], pair[0]        
    data.sort(key=key)                 

    # 根据用户请求的数量返回元素。
    return data[:num]                  
# <end id="sharded-zrangebyscore"/>


# 代码清单 10-15
# <start id="sharded-syndicate-posts"/>
def syndicate_status(uid, post, start=0, on_lists=False):
    root = 'followers'
    key = 'followers:%s'%uid
    base = 'home:%s'
    if on_lists:
        root = 'list:out'
        key = 'list:out:%s'%uid
        base = 'list:statuses:%s'

    # 通过 ZRANGEBYSCORE 调用，找出下一组关注者。
    followers = sharded_zrangebyscore(root,                         
        sharded_followers.shards, key, start, 'inf', POSTS_PER_PASS)

    # 基于预先分片的结果对个人信息进行分组，
    # 并把分组后的信息储存到预先准备好的结构里面。
    to_send = defaultdict(list)                            
    for follower, start in followers:
        # 构造出储存时间线的键。
        timeline = base % follower                          
        # 找到负责储存这个时间线的分片。
        shard = shard_key('timelines',                     
            timeline, sharded_timelines.shards, 2)         
        # 把时间线的键添加到位于同一个分片的其他时间线的后面。
        to_send[shard].append(timeline)                    

    for timelines in to_send.itervalues():
        # 根据储存这组时间线的服务器，
        # 找出连向它的连接，
        # 然后创建一个流水线对象。
        pipe = sharded_timelines[timelines[0]].pipeline(False) 
        for timeline in timelines:
            # 把新发送的消息添加到时间线上面，
            # 并移除过于陈旧的消息。
            pipe.zadd(timeline, **post)                
            pipe.zremrangebyrank(                      
                timeline, 0, -HOME_TIMELINE_SIZE-1)    
        pipe.execute()

    conn = redis.Redis()
    if len(followers) >= POSTS_PER_PASS:
        execute_later(conn, 'default', 'syndicate_status',
            [uid, post, start, on_lists])

    elif not on_lists:
        execute_later(conn, 'default', 'syndicate_status',
            [uid, post, 0, True])
# <end id="sharded-syndicate-posts"/>

def _fake_shards_for(conn, component, count, actual):
    assert actual <= 4
    for i in xrange(count):
        m = i % actual
        conn.set('config:redis:%s:%i'%(component, i), json.dumps({'db':14 - m}))

class TestCh10(unittest.TestCase):
    def _flush(self):
        self.conn.flushdb()
        redis.Redis(db=14).flushdb()
        redis.Redis(db=13).flushdb()
        redis.Redis(db=12).flushdb()
        redis.Redis(db=11).flushdb()
        
    def setUp(self):
        self.conn = redis.Redis(db=15)
        self._flush()
        global config_connection
        config_connection = self.conn
        self.conn.set('config:redis:test', json.dumps({'db':15}))

    def tearDown(self):
        self._flush()

    def test_get_sharded_connections(self):
        _fake_shards_for(self.conn, 'shard', 2, 2)

        for i in xrange(10):
            get_sharded_connection('shard', i, 2).sadd('foo', i)

        s0 = redis.Redis(db=14).scard('foo')
        s1 = redis.Redis(db=13).scard('foo')
        self.assertTrue(s0 < 10)
        self.assertTrue(s1 < 10)
        self.assertEquals(s0 + s1, 10)

    def test_count_visit(self):
        shards = {'db':13}, {'db':14}
        self.conn.set('config:redis:unique', json.dumps({'db':15}))
        for i in xrange(16):
            self.conn.set('config:redis:unique:%s'%i, json.dumps(shards[i&1]))
    
        for i in xrange(100):
            count_visit(str(uuid.uuid4()))
        base = 'unique:%s'%date.today().isoformat()
        total = 0
        for c in shards:
            conn = redis.Redis(**c)
            keys = conn.keys(base + ':*')
            for k in keys:
                cnt = conn.scard(k)
                total += cnt
                self.assertTrue(cnt < k)
        self.assertEquals(total, 100)
        self.assertEquals(self.conn.get(base), '100')

    def test_sharded_search(self):
        _fake_shards_for(self.conn, 'search', 2, 2)
        
        docs = 'hello world how are you doing'.split(), 'this world is doing fine'.split()
        for i in xrange(50):
            c = get_sharded_connection('search', i, 2)
            index_document(c, i, docs[i&1], {'updated':time.time() + i, 'id':i, 'created':time.time() + i})
            r = search_and_sort(c, docs[i&1], sort='-id')
            self.assertEquals(r[1][0], str(i))

        total = 0
        for shard in (0,1):
            count = search_get_values(get_redis_connection('search:%s'%shard),['this', 'world'], num=50)[0]
            total += count
            self.assertTrue(count < 50)
            self.assertTrue(count > 0)
        
        self.assertEquals(total, 25)
        
        count, r, id = get_shard_results('search', 2, ['world', 'doing'], num=50)
        self.assertEquals(count, 50)
        self.assertEquals(count, len(r))
        
        self.assertEquals(get_shard_results('search', 2, ['this', 'doing'], num=50)[0], 25)

        count, r, id = get_shard_results_thread('search', 2, ['this', 'doing'], num=50)
        self.assertEquals(count, 25)
        self.assertEquals(count, len(r))
        r.sort(key=lambda x:x[1], reverse=True)
        r = list(zip(*r)[0])
        
        count, r2, id = search_shards('search', 2, ['this', 'doing'])
        self.assertEquals(count, 25)
        self.assertEquals(len(r2), 20)
        self.assertEquals(r2, r[:20])
        
    def test_sharded_follow_user(self):
        _fake_shards_for(self.conn, 'timelines', 8, 4)

        sharded_timelines['profile:1'].zadd('profile:1', 1, time.time())
        for u2 in xrange(2, 11):
            sharded_timelines['profile:%i'%u2].zadd('profile:%i'%u2, u2, time.time() + u2)
            _follow_user(self.conn, 1, u2)
            _follow_user(self.conn, u2, 1)
        
        self.assertEquals(self.conn.zcard('followers:1'), 9)
        self.assertEquals(self.conn.zcard('following:1'), 9)
        self.assertEquals(sharded_timelines['home:1'].zcard('home:1'), 9)
        
        for db in xrange(14, 10, -1):
            self.assertTrue(len(redis.Redis(db=db).keys()) > 0)
        for u2 in xrange(2, 11):
            self.assertEquals(self.conn.zcard('followers:%i'%u2), 1)
            self.assertEquals(self.conn.zcard('following:%i'%u2), 1)
            self.assertEquals(sharded_timelines['home:%i'%u2].zcard('home:%i'%u2), 1)

    def test_sharded_follow_user_and_syndicate_status(self):
        _fake_shards_for(self.conn, 'timelines', 8, 4)
        _fake_shards_for(self.conn, 'followers', 4, 4)
        sharded_followers.shards = 4
    
        sharded_timelines['profile:1'].zadd('profile:1', 1, time.time())
        for u2 in xrange(2, 11):
            sharded_timelines['profile:%i'%u2].zadd('profile:%i'%u2, u2, time.time() + u2)
            follow_user(self.conn, 1, u2)
            follow_user(self.conn, u2, 1)
        
        allkeys = defaultdict(int)
        for db in xrange(14, 10, -1):
            c = redis.Redis(db=db)
            for k in c.keys():
                allkeys[k] += c.zcard(k)

        for k, v in allkeys.iteritems():
            part, _, owner = k.partition(':')
            if part in ('following', 'followers', 'home'):
                self.assertEquals(v, 9 if owner == '1' else 1)
            elif part == 'profile':
                self.assertEquals(v, 1)

        self.assertEquals(len(sharded_zrangebyscore('followers', 4, 'followers:1', '0', 'inf', 100)), 9)
        syndicate_status(1, {'11':time.time()})
        self.assertEquals(len(sharded_zrangebyscore('timelines', 4, 'home:2', '0', 'inf', 100)), 2)



if __name__ == '__main__':
    unittest.main()
