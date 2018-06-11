# coding: utf-8

import bisect
import math
import threading
import time
import unittest
import uuid

import redis

# 代码清单 11-1
# <start id="script-load"/>
def script_load(script):
    # 将 SCRIPT LOAD 命令返回的已缓存脚本 SHA1 校验和储存到一个列表里面，
    # 以便之后在 call() 函数内部对其进行修改。
    sha = [None]                
    # 在调用已载入脚本的时候，
    # 用户需要将 Redis 连接、脚本要处理的键以及脚本的其他参数传递给脚本。
    def call(conn, keys=[], args=[], force_eval=False):  
        if not force_eval:
            # 程序只会在 SHA1 校验和未被缓存的情况下尝试载入脚本。
            if not sha[0]:   
                # 如果 SHA1 校验和未被缓存，那么载入给定的脚本
                sha[0] = conn.execute_command(              
                    "SCRIPT", "LOAD", script, parse="LOAD") 
    
            try:
                # 使用已缓存的 SHA1 校验和执行命令。
                return conn.execute_command(                    
                    "EVALSHA", sha[0], len(keys), *(keys+args)) 
        
            except redis.exceptions.ResponseError as msg:
                # 如果错误与脚本缺失无关，那么重新抛出异常。
                if not msg.args[0].startswith("NOSCRIPT"):      
                    raise                                       
        
        # 当程序接收到脚本错误的时候，
        # 又或者程序需要强制执行脚本的时候，
        # 它会使用 EVAL 命令直接执行给定的脚本。
        # EVAL 命令在执行完脚本之后，
        # 会自动地把脚本缓存起来，
        # 而缓存产生的 SHA1 校验和跟使用 EVALSHA 命令缓存脚本产生的 SHA1 校验和是完全相同的。
        return conn.execute_command(                   
            "EVAL", script, len(keys), *(keys+args))   
    
    # 返回一个函数，这个函数在被调用的时候会自动载入并执行脚本。
    return call            
# <end id="script-load"/>


'''
# <start id="show-script-load"/>
>>> ret_1 = script_load("return 1")     # 在大多数情况下，我们在载入脚本之后都会储存起脚本载入程序返回的函数引用。
>>> ret_1(conn)                         # 在此之后，我们就可以通过传入连接对象以及脚本需要的其他参数来调用函数。
1L                                      # 只要条件允许，就将脚本返回的结果转换成相应的 Python 类型。
# <end id="show-script-load"/>
'''


# 代码清单 11-2
# <start id="ch08-post-status"/>
def create_status(conn, uid, message, **data):
    pipeline = conn.pipeline(True)
    # 根据用户 ID 获取用户的用户名。
    pipeline.hget('user:%s' % uid, 'login') 
    # 为这条状态消息创建一个新的 ID 。
    pipeline.incr('status:id:')             
    login, id = pipeline.execute()

    # 在发布状态消息之前，先检查用户的账号是否存在。
    if not login:                          
        return None                        

    # 准备并设置状态消息的各项信息。
    data.update({
        'message': message,               
        'posted': time.time(),             
        'id': id,                          
        'uid': uid,                        
        'login': login,                    
    })
    pipeline.hmset('status:%s' % id, data)  
    # 更新用户的已发送状态消息数量。
    pipeline.hincrby('user:%s' % uid, 'posts')
    pipeline.execute()
    # 返回新创建的状态消息的 ID 。
    return id                              
# <end id="ch08-post-status"/>


_create_status = create_status
# 代码清单 11-3
# <start id="post-status-lua"/>
# 这个函数接受的参数和原版消息发布函数接受的参数一样。
def create_status(conn, uid, message, **data):         
    # 准备好对状态消息进行设置所需的各个参数和属性。
    args = [                                            
        'message', message,                             
        'posted', time.time(),                          
        'uid', uid,                                     
    ]
    for key, value in data.iteritems():                 
        args.append(key)                               
        args.append(value)                             

    return create_status_lua(                          
        conn, ['user:%s' % uid, 'status:id:'], args)   

create_status_lua = script_load('''
-- 根据用户 ID ，获取用户的用户名。
-- 记住，Lua 表格的索引是从 1 开始的，
-- 而不是像 Python 和很多其他语言那样从 0 开始。
local login = redis.call('hget', KEYS[1], 'login')    
-- 如果用户并未登录，那么向调用者说明这一情况。
if not login then                                    
    return false                                       
end
-- 获取一个新的状态消息 ID 。
local id = redis.call('incr', KEYS[2])                 
-- 准备好负责储存状态消息的键。
local key = string.format('status:%s', id)             

-- 为状态消息执行数据设置操作。
redis.call('hmset', key,                               
    'login', login,                                     
    'id', id,                                         
    unpack(ARGV))                                       
--  对用户的已发布消息计数器执行自增操作。
redis.call('hincrby', KEYS[1], 'posts', 1)             

-- 返回状态消息的 ID 。
return id                                              
''')
# <end id="post-status-lua"/>


# 代码清单 11-4
# <start id="old-lock"/>
def acquire_lock_with_timeout(
    conn, lockname, acquire_timeout=10, lock_timeout=10):
    # 128 位随机标识符。
    identifier = str(uuid.uuid4())                     
    lockname = 'lock:' + lockname
    # 确保传给 EXPIRE 的都是整数。
    lock_timeout = int(math.ceil(lock_timeout))        
    
    end = time.time() + acquire_timeout
    while time.time() < end:
        # 获取锁并设置过期时间。
        if conn.setnx(lockname, identifier):           
            conn.expire(lockname, lock_timeout)        
            return identifier
        # 检查过期时间，并在有需要时对其进行更新。
        elif not conn.ttl(lockname):                   
            conn.expire(lockname, lock_timeout)        
    
        time.sleep(.001)
    
    return False
# <end id="old-lock"/>


_acquire_lock_with_timeout = acquire_lock_with_timeout
# 代码清单 11-5
# <start id="lock-in-lua"/>
def acquire_lock_with_timeout(
    conn, lockname, acquire_timeout=10, lock_timeout=10):
    identifier = str(uuid.uuid4())                      
    lockname = 'lock:' + lockname
    lock_timeout = int(math.ceil(lock_timeout))      
    
    acquired = False
    end = time.time() + acquire_timeout
    while time.time() < end and not acquired:
        # 执行实际的锁获取操作，通过检查确保 Lua 调用已经执行成功。
        acquired = acquire_lock_with_timeout_lua(                  
            conn, [lockname], [lock_timeout, identifier]) == 'OK'  
    
        time.sleep(.001 * (not acquired))
    
    return acquired and identifier

acquire_lock_with_timeout_lua = script_load('''
-- 检测锁是否已经存在。（再次提醒，Lua 表格的索引是从 1 开始的。）
if redis.call('exists', KEYS[1]) == 0 then             
    -- 使用给定的过期时间以及标识符去设置键。
    return redis.call('setex', KEYS[1], unpack(ARGV))  
end
''')
# <end id="lock-in-lua"/>


def release_lock(conn, lockname, identifier):
    pipe = conn.pipeline(True)
    lockname = 'lock:' + lockname
    
    while True:
        try:
            pipe.watch(lockname)                  #A
            if pipe.get(lockname) == identifier:  #A
                pipe.multi()                      #B
                pipe.delete(lockname)             #B
                pipe.execute()                    #B
                return True                       #B
    
            pipe.unwatch()
            break
    
        except redis.exceptions.WatchError:       #C
            pass                                  #C
    
    return False                                  #D


_release_lock = release_lock
# 代码清单 11-6
# <start id="release-lock-in-lua"/>
def release_lock(conn, lockname, identifier):
    lockname = 'lock:' + lockname
    # 调用负责释放锁的 Lua 函数。
    return release_lock_lua(conn, [lockname], [identifier]) 

release_lock_lua = script_load('''
-- 检查锁是否匹配。
if redis.call('get', KEYS[1]) == ARGV[1] then              
    -- 删除锁并确保脚本总是返回真值。
    return redis.call('del', KEYS[1]) or true              
end
''')
# <end id="release-lock-in-lua"/>

# 代码清单 11-7
# <start id="old-acquire-semaphore"/>
def acquire_semaphore(conn, semname, limit, timeout=10):
    # 128 位随机标识符。
    identifier = str(uuid.uuid4())                            
    now = time.time()

    pipeline = conn.pipeline(True)
    # 清理过期的信号量持有者。
    pipeline.zremrangebyscore(semname, '-inf', now - timeout) 
    # 尝试获取信号量。
    pipeline.zadd(semname, identifier, now)                   
    # 检查是否成功取得了信号量。
    pipeline.zrank(semname, identifier)                       
    if pipeline.execute()[-1] < limit:                         
        return identifier

    # 获取信号量失败，删除之前添加的标识符。
    conn.zrem(semname, identifier)                             
    return None
# <end id="old-acquire-semaphore"/>


_acquire_semaphore = acquire_semaphore
# 代码清单 11-8
# <start id="acquire-semaphore-lua"/>
def acquire_semaphore(conn, semname, limit, timeout=10):
    # 取得当前时间戳，用于处理超时信号量。
    now = time.time()                                           
    # 把所有必须的参数传递给 Lua 函数，实际地执行信号量获取操作。
    return acquire_semaphore_lua(conn, [semname],               
        [now-timeout, limit, now, str(uuid.uuid4())])           

acquire_semaphore_lua = script_load('''
-- 清除所有已过期的信号量。
redis.call('zremrangebyscore', KEYS[1], '-inf', ARGV[1])        

-- 如果还有剩余的信号量可用，那么获取信号量。
if redis.call('zcard', KEYS[1]) < tonumber(ARGV[2]) then        
    -- 把时间戳添加到超时有序集合里面。
    redis.call('zadd', KEYS[1], ARGV[3], ARGV[4])              
    return ARGV[4]
end
''')
# <end id="acquire-semaphore-lua"/>

def release_semaphore(conn, semname, identifier):
    return conn.zrem(semname, identifier)

# 代码清单 11-9
# <start id="refresh-semaphore-lua"/>
def refresh_semaphore(conn, semname, identifier):
    return refresh_semaphore_lua(conn, [semname],
        # 如果信号量没有被刷新，那么 Lua 脚本将返回空值，
        # 而 Python 会将这个空值转换成 None 并返回给调用者。
        [identifier, time.time()]) != None        

refresh_semaphore_lua = script_load('''
-- 如果信号量仍然存在，那么对它的时间戳进行更新。
if redis.call('zscore', KEYS[1], ARGV[1]) then                  
    return redis.call('zadd', KEYS[1], ARGV[2], ARGV[1]) or true 
end
''')
# <end id="refresh-semaphore-lua"/>

valid_characters = '`abcdefghijklmnopqrstuvwxyz{'           

def find_prefix_range(prefix):
    posn = bisect.bisect_left(valid_characters, prefix[-1:]) 
    suffix = valid_characters[(posn or 1) - 1]               
    return prefix[:-1] + suffix + '{', prefix + '{'         

# 代码清单 11-10
# <start id="old-autocomplete-code"/>
def autocomplete_on_prefix(conn, guild, prefix):
    # 根据给定的前缀计算出查找范围的起点和终点。
    start, end = find_prefix_range(prefix)               
    identifier = str(uuid.uuid4())                       
    start += identifier                                   
    end += identifier                                     
    zset_name = 'members:' + guild

    # 将范围的起始元素和结束元素添加到有序集合里面。
    conn.zadd(zset_name, start, 0, end, 0)               
    pipeline = conn.pipeline(True)
    while 1:
        try:
            pipeline.watch(zset_name)
            # 找到两个被插入元素在有序集合中的排名。
            sindex = pipeline.zrank(zset_name, start)     
            eindex = pipeline.zrank(zset_name, end)      
            erange = min(sindex + 9, eindex - 2)          
            pipeline.multi()
            # 获取范围内的值，然后删除之前插入的起始元素和结束元素。
            pipeline.zrem(zset_name, start, end)         
            pipeline.zrange(zset_name, sindex, erange)    
            items = pipeline.execute()[-1]                
            break
        # 如果自动补完有序集合已经被其他客户端修改过了，
        # 那么进行重试。
        except redis.exceptions.WatchError:               
            continue                                      

    # 如果有其他自动补完操作正在执行，
    # 那么从获取到的元素里面移除起始元素和终结元素。
    return [item for item in items if '{' not in item]    
# <end id="old-autocomplete-code"/>


_autocomplete_on_prefix = autocomplete_on_prefix
# 代码清单 11-11
# <start id="autocomplete-on-prefix-lua"/>
def autocomplete_on_prefix(conn, guild, prefix):
    # 取得范围和标识符。
    start, end = find_prefix_range(prefix)                 
    identifier = str(uuid.uuid4())                         
    
    # 使用 Lua 脚本从 Redis 里面获取数据。
    items = autocomplete_on_prefix_lua(conn,               
        ['members:' + guild],                              
        [start+identifier, end+identifier])                 
    
    # 过滤掉所有不想要的元素。
    return [item for item in items if '{' not in item]      

autocomplete_on_prefix_lua = script_load('''
--  把标记起始范围和结束范围的元素添加到有序集合里面。
redis.call('zadd', KEYS[1], 0, ARGV[1], 0, ARGV[2])             
-- 在有序集合里面找到范围元素的位置。
local sindex = redis.call('zrank', KEYS[1], ARGV[1])            
local eindex = redis.call('zrank', KEYS[1], ARGV[2])            
-- 计算出想要获取的元素所处的范围。
eindex = math.min(sindex + 9, eindex - 2)                       

-- 移除范围元素。
redis.call('zrem', KEYS[1], unpack(ARGV))                       
-- 获取并返回结果。
return redis.call('zrange', KEYS[1], sindex, eindex)            
''')
# <end id="autocomplete-on-prefix-lua"/>


# 代码清单 11-12
# <start id="ch06-purchase-item-with-lock"/>
def purchase_item_with_lock(conn, buyerid, itemid, sellerid):
    buyer = "users:%s" % buyerid
    seller = "users:%s" % sellerid
    item = "%s.%s" % (itemid, sellerid)
    inventory = "inventory:%s" % buyerid

    # 尝试获取锁。
    locked = acquire_lock(conn, 'market:')    
    if not locked:
        return False

    pipe = conn.pipeline(True)
    try:
        # 检查物品是否已经售出，以及买家是否有足够的金钱来购买物品。
        pipe.zscore("market:", item)          
        pipe.hget(buyer, 'funds')              
        price, funds = pipe.execute()         
        if price is None or price > funds:    
            return None                       

        # 将买家支付的货款转移给卖家，并将售出的物品转移给买家。
        pipe.hincrby(seller, 'funds', int(price))  
        pipe.hincrby(buyer, 'funds', int(-price))  
        pipe.sadd(inventory, itemid)               
        pipe.zrem("market:", item)                
        pipe.execute()                             
        return True
    finally:
        # 释放锁
        release_lock(conn, 'market:', locked)     
# <end id="ch06-purchase-item-with-lock"/>


# 代码清单 11-13
# <start id="purchase-item-lua"/>
def purchase_item(conn, buyerid, itemid, sellerid):
    # 准备好执行 Lua 脚本所需的所有键和参数。
    buyer = "users:%s" % buyerid                       
    seller = "users:%s" % sellerid                     
    item = "%s.%s"%(itemid, sellerid)                  
    inventory = "inventory:%s" % buyerid               

    return purchase_item_lua(conn,
        ['market:', buyer, seller, inventory], [item, itemid])

purchase_item_lua = script_load('''
-- 获取物品的价格以及买家可用的金钱数量。
local price = tonumber(redis.call('zscore', KEYS[1], ARGV[1]))  
local funds = tonumber(redis.call('hget', KEYS[2], 'funds'))    

-- 如果物品仍在销售，并且买家也有足够的金钱，那么对物品和金钱进行相应的转移。
if price and funds and funds >= price then                      
    redis.call('hincrby', KEYS[3], 'funds', price)              
    redis.call('hincrby', KEYS[2], 'funds', -price)            
    redis.call('sadd', KEYS[4], ARGV[2])                        
    redis.call('zrem', KEYS[1], ARGV[1])                       
    -- 返回真值表示购买操作执行成功。
    return true                                                
end
''')
# <end id="purchase-item-lua"/>

def list_item(conn, itemid, sellerid, price):
    inv = "inventory:%s" % sellerid
    item = "%s.%s" % (itemid, sellerid)
    return list_item_lua(conn, [inv, 'market:'], [itemid, item, price])

list_item_lua = script_load('''
if redis.call('sismember', KEYS[1], ARGV[1]) ~= 0 then
    redis.call('zadd', KEYS[2], ARGV[2], ARGV[3])
    redis.call('srem', KEYS[1], ARGV[1])
    return true
end
''')


# 代码清单 11-14
# <start id="sharded-list-push"/>
def sharded_push_helper(conn, key, *items, **kwargs):
    # 把元素组成的序列转换成列表。
    items = list(items)                                
    total = 0
    # 仍然有元素需要推入……
    while items:                                       
        # ……通过调用 Lua 脚本，把元素推入到分片列表里面。
        pushed = sharded_push_lua(conn,                
            [key+':', key+':first', key+':last'],      
            # 这个程序目前每次最多只会推入 64 个元素，
            # 读者可以根据自己的压缩列表最大长度来调整这个数值。
            [kwargs['cmd']] + items[:64])              
        # 计算被推入的元素数量。
        total += pushed                                
        # 移除那些已经被推入到分片列表里面的元素。
        del items[:pushed]                             
    # 返回被推入元素的总数量。
    return total                                       

def sharded_lpush(conn, key, *items):
    # 调用 sharded_push_helper() 函数，
    # 并通过指定的参数告诉它应该执行左端推入操作还是右端推入操作。
    return sharded_push_helper(conn, key, *items, cmd='lpush')

def sharded_rpush(conn, key, *items):
    # 调用 sharded_push_helper() 函数，
    # 并通过指定的参数告诉它应该执行左端推入操作还是右端推入操作。
    return sharded_push_helper(conn, key, *items, cmd='rpush')

sharded_push_lua = script_load('''
-- 确定每个列表分片的最大长度。
local max = tonumber(redis.call(                            
    'config', 'get', 'list-max-ziplist-entries')[2])        
-- 如果没有元素需要进行推入，又或者压缩列表的最大长度太小，那么返回 0 。
if #ARGV < 2 or max < 2 then return 0 end                  

-- 弄清楚程序要对列表的左端还是右端进行推入，然后取得那一端对应的分片。
local skey = ARGV[1] == 'lpush' and KEYS[2] or KEYS[3]     
local shard = redis.call('get', skey) or '0'              

while 1 do
    -- 取得分片的当前长度。
    local current = tonumber(redis.call('llen', KEYS[1]..shard))   
    -- 计算出在不超过限制的情况下，可以将多少个元素推入到目前的列表里面。
    -- 此外，在列表里面保留一个节点的空间以便处理之后可能发生的阻塞弹出操作。
    local topush = math.min(#ARGV - 1, max - current - 1)          
    -- 在条件允许的情况下，向列表推入尽可能多的元素。
    if topush > 0 then                                              
        redis.call(ARGV[1], KEYS[1]..shard, unpack(ARGV, 2, topush+1))
        return topush                                                 
    end
    -- 否则的话，生成一个新的分片并继续进行未完成的推入工作。
    shard = redis.call(ARGV[1] == 'lpush' and 'decr' or 'incr', skey) 
end
''')
# <end id="sharded-list-push"/>

def sharded_llen(conn, key):
    return sharded_llen_lua(conn, [key+':', key+':first', key+':last'])

sharded_llen_lua = script_load('''
local shardsize = tonumber(redis.call(
    'config', 'get', 'list-max-ziplist-entries')[2])

local first = tonumber(redis.call('get', KEYS[2]) or '0')
local last = tonumber(redis.call('get', KEYS[3]) or '0')

local total = 0
total = total + tonumber(redis.call('llen', KEYS[1]..first))
if first ~= last then
    total = total + (last - first - 1) * (shardsize-1)
    total = total + tonumber(redis.call('llen', KEYS[1]..last))
end

return total
''')


# 代码清单 11-15
# <start id="sharded-list-pop-lua"/>
def sharded_lpop(conn, key):
    return sharded_list_pop_lua(
        conn, [key+':', key+':first', key+':last'], ['lpop'])

def sharded_rpop(conn, key):
    return sharded_list_pop_lua(
        conn, [key+':', key+':first', key+':last'], ['rpop'])

sharded_list_pop_lua = script_load('''
-- 找到需要执行弹出操作的分片。
local skey = ARGV[1] == 'lpop' and KEYS[2] or KEYS[3]          
-- 找到不需要执行弹出操作的分片。
local okey = ARGV[1] ~= 'lpop' and KEYS[2] or KEYS[3]          
-- 获取需要执行弹出操作的分片的 ID 。
local shard = redis.call('get', skey) or '0'                 

-- 从分片对应的列表里面弹出一个元素。
local ret = redis.call(ARGV[1], KEYS[1]..shard)                 
-- 如果程序因为分片为空而没有得到弹出元素，
-- 又或者弹出操作使得分片变空了，那么对分片端点进行清理。
if not ret or redis.call('llen', KEYS[1]..shard) == '0' then    
    -- 获取不需要执行弹出操作的分片的 ID 。
    local oshard = redis.call('get', okey) or '0'               

    -- 如果分片列表的两端相同，那么说明它已经不包含任何元素，操作执行完毕。
    if shard == oshard then                                    
        return ret                                              
    end

    -- 根据被弹出的元素来自列表的左端还是右端，
    -- 决定应该增加还是减少分片的 ID 。
    local cmd = ARGV[1] == 'lpop' and 'incr' or 'decr'          
    -- 调整分片的端点（endpoint）。
    shard = redis.call(cmd, skey)                               
    if not ret then
        -- 如果之前没有取得弹出元素，那么尝试对新分片进行弹出。
        ret = redis.call(ARGV[1], KEYS[1]..shard)              
    end
end
return ret
''')
# <end id="sharded-list-pop-lua"/>


# 代码清单 11-16
# <start id="sharded-blocking-list-pop"/>
# 预先定义好的伪元素，读者也可以按自己的需要，
# 把这个伪元素替换成某个不可能出现在分片列表里面的值。
DUMMY = str(uuid.uuid4())                                          

# 定义一个辅助函数，
# 这个函数会为左端阻塞弹出操作以及右端阻塞弹出操作执行实际的弹出动作。
def sharded_bpop_helper(conn, key, timeout, pop, bpop, endp, push): 
    # 准备好流水线对象和超时信息。
    pipe = conn.pipeline(False)                                    
    timeout = max(timeout, 0) or 2**64                             
    end = time.time() + timeout                                     
    
    while time.time() < end:
        # 尝试执行一次非阻塞弹出，
        # 如果这个操作成功取得了一个弹出值，
        # 并且这个值并不是伪元素，那么返回这个值。
        result = pop(conn, key)                                     
        if result not in (None, DUMMY):                             
            return result                                          
    
        # 取得程序认为需要对其执行弹出操作的分片。
        shard = conn.get(key + endp) or '0'                        
        # 运行 Lua 脚本辅助程序，
        # 它会在程序尝试从错误的分片里面弹出元素的时候，
        # 将一个伪元素推入到那个分片里面。
        sharded_bpop_helper_lua(pipe, [key + ':', key + endp],    
            # 因为程序不能在流水线里面执行一个可能会失败的 EVALSHA 调用，
            # 所以这里需要使用 force_eval 参数，
            # 确保程序调用的是 EVAL 命令而不是 EVALSHA 命令。
            [shard, push, DUMMY], force_eval=True)                 
        #  使用用户传入的 BLPOP 命令或 BRPOP 命令，对列表执行阻塞弹出操作。
        getattr(pipe, bpop)(key + ':' + shard, 1)                  
    
        # 如果命令返回了一个元素，那么程序执行完毕；否则的话，进行重试。
        result = (pipe.execute()[-1] or [None])[-1]                
        if result not in (None, DUMMY):                            
            return result                                          

# 这个函数负责调用底层的阻塞弹出操作。
def sharded_blpop(conn, key, timeout=0):                             
    return sharded_bpop_helper(                                      
        conn, key, timeout, sharded_lpop, 'blpop', ':first', 'lpush') 

# 这个函数负责调用底层的阻塞弹出操作。
def sharded_brpop(conn, key, timeout=0):                             
    return sharded_bpop_helper(                                      
        conn, key, timeout, sharded_rpop, 'brpop', ':last', 'rpush')  

sharded_bpop_helper_lua = script_load('''
--  找到程序想要对其执行弹出操作的列表端，并取得这个列表端对应的分片。
local shard = redis.call('get', KEYS[2]) or '0'                     
-- 如果程序接下来要从错误的分片里面弹出元素，那么将伪元素推入到那个分片里面。
if shard ~= ARGV[1] then                                            
    redis.call(ARGV[2], KEYS[1]..ARGV[1], ARGV[3])                  
end
''')
# <end id="sharded-blocking-list-pop"/>

class TestCh11(unittest.TestCase):
    def setUp(self):
        self.conn = redis.Redis(db=15)
        self.conn.flushdb()
    def tearDown(self):
        self.conn.flushdb()

    def test_load_script(self):
        self.assertEquals(script_load("return 1")(self.conn), 1)

    def test_create_status(self):
        self.conn.hset('user:1', 'login', 'test')
        sid = _create_status(self.conn, 1, 'hello')
        sid2 = create_status(self.conn, 1, 'hello')
        
        self.assertEquals(self.conn.hget('user:1', 'posts'), '2')
        data = self.conn.hgetall('status:%s'%sid)
        data2 = self.conn.hgetall('status:%s'%sid2)
        data.pop('posted'); data.pop('id')
        data2.pop('posted'); data2.pop('id')
        self.assertEquals(data, data2)

    def test_locking(self):
        identifier = acquire_lock_with_timeout(self.conn, 'test', 1, 5)
        self.assertTrue(identifier)
        self.assertFalse(acquire_lock_with_timeout(self.conn, 'test', 1, 5))
        release_lock(self.conn, 'test', identifier)
        self.assertTrue(acquire_lock_with_timeout(self.conn, 'test', 1, 5))
    
    def test_semaphore(self):
        ids = []
        for i in xrange(5):
            ids.append(acquire_semaphore(self.conn, 'test', 5, timeout=1))
        self.assertTrue(None not in ids)
        self.assertFalse(acquire_semaphore(self.conn, 'test', 5, timeout=1))
        time.sleep(.01)
        id = acquire_semaphore(self.conn, 'test', 5, timeout=0)
        self.assertTrue(id)
        self.assertFalse(refresh_semaphore(self.conn, 'test', ids[-1]))
        self.assertFalse(release_semaphore(self.conn, 'test', ids[-1]))

        self.assertTrue(refresh_semaphore(self.conn, 'test', id))
        self.assertTrue(release_semaphore(self.conn, 'test', id))
        self.assertFalse(release_semaphore(self.conn, 'test', id))

    def test_autocomplet_on_prefix(self):
        for word in 'these are some words that we will be autocompleting on'.split():
            self.conn.zadd('members:test', word, 0)
        
        self.assertEquals(autocomplete_on_prefix(self.conn, 'test', 'th'), ['that', 'these'])
        self.assertEquals(autocomplete_on_prefix(self.conn, 'test', 'w'), ['we', 'will', 'words'])
        self.assertEquals(autocomplete_on_prefix(self.conn, 'test', 'autocompleting'), ['autocompleting'])

    def test_marketplace(self):
        self.conn.sadd('inventory:1', '1')
        self.conn.hset('users:2', 'funds', 5)
        self.assertFalse(list_item(self.conn, 2, 1, 10))
        self.assertTrue(list_item(self.conn, 1, 1, 10))
        self.assertFalse(purchase_item(self.conn, 2, '1', 1))
        self.conn.zadd('market:', '1.1', 4)
        self.assertTrue(purchase_item(self.conn, 2, '1', 1))

    def test_sharded_list(self):
        self.assertEquals(sharded_lpush(self.conn, 'lst', *range(100)), 100)
        self.assertEquals(sharded_llen(self.conn, 'lst'), 100)

        self.assertEquals(sharded_lpush(self.conn, 'lst2', *range(1000)), 1000)
        self.assertEquals(sharded_llen(self.conn, 'lst2'), 1000)
        self.assertEquals(sharded_rpush(self.conn, 'lst2', *range(-1, -1001, -1)), 1000)
        self.assertEquals(sharded_llen(self.conn, 'lst2'), 2000)

        self.assertEquals(sharded_lpop(self.conn, 'lst2'), '999')
        self.assertEquals(sharded_rpop(self.conn, 'lst2'), '-1000')
        
        for i in xrange(999):
            r = sharded_lpop(self.conn, 'lst2')
        self.assertEquals(r, '0')

        results = []
        def pop_some(conn, fcn, lst, count, timeout):
            for i in xrange(count):
                results.append(sharded_blpop(conn, lst, timeout))
        
        t = threading.Thread(target=pop_some, args=(self.conn, sharded_blpop, 'lst3', 10, 1))
        t.setDaemon(1)
        t.start()
        
        self.assertEquals(sharded_rpush(self.conn, 'lst3', *range(4)), 4)
        time.sleep(2)
        self.assertEquals(sharded_rpush(self.conn, 'lst3', *range(4, 8)), 4)
        time.sleep(2)
        self.assertEquals(results, ['0', '1', '2', '3', None, '4', '5', '6', '7', None])

if __name__ == '__main__':
    unittest.main()
