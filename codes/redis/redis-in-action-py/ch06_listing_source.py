# coding: utf-8

import bisect
from collections import defaultdict, deque
import json
import math
import os
import time
import unittest
import uuid
import zlib

import redis

QUIT = False
pipe = inv = item = buyer = seller = inventory = None


# 代码清单 6-1
# <start id="_1314_14473_8380"/>
def add_update_contact(conn, user, contact):
    ac_list = 'recent:' + user
    # 准备执行原子操作。
    pipeline = conn.pipeline(True) 
    # 如果联系人已经存在，那么移除他。
    pipeline.lrem(ac_list, contact) 
    # 将联系人推入到列表的最前端。
    pipeline.lpush(ac_list, contact) 
    # 只保留列表里面的前100个联系人。
    pipeline.ltrim(ac_list, 0, 99)   
    # 实际地执行以上操作。
    pipeline.execute()               
# <end id="_1314_14473_8380"/>


# <start id="_1314_14473_8383"/>
def remove_contact(conn, user, contact):
    conn.lrem('recent:' + user, contact)
# <end id="_1314_14473_8383"/>


# 代码清单 6-2
# <start id="_1314_14473_8386"/>
def fetch_autocomplete_list(conn, user, prefix):
    # 获取自动补完列表。
    candidates = conn.lrange('recent:' + user, 0, -1) 
    matches = []
    # 检查每个候选联系人。
    for candidate in candidates:                    
        if candidate.lower().startswith(prefix):  
            # 发现一个匹配的联系人。
            matches.append(candidate)         
    # 返回所有匹配的联系人。
    return matches                           
# <end id="_1314_14473_8386"/>


# 代码清单 6-3
# <start id="_1314_14473_8396"/>
# 准备一个由已知字符组成的列表。
valid_characters = '`abcdefghijklmnopqrstuvwxyz{'     

def find_prefix_range(prefix):
    # 在字符列表中查找前缀字符所处的位置。
    posn = bisect.bisect_left(valid_characters, prefix[-1:]) 
    # 找到前驱字符。
    suffix = valid_characters[(posn or 1) - 1]  
    # 返回范围。
    return prefix[:-1] + suffix + '{', prefix + '{'         
# <end id="_1314_14473_8396"/>


# 代码清单 6-4
# <start id="_1314_14473_8399"/>
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
        # 如果自动补完有序集合已经被其他客户端修改过了，那么进行重试。
        except redis.exceptions.WatchError:               
            continue                                     

    # 如果有其他自动补完操作正在执行，
    # 那么从获取到的元素里面移除起始元素和终结元素。
    return [item for item in items if '{' not in item]  
# <end id="_1314_14473_8399"/>


# 代码清单 6-5
# <start id="_1314_14473_8403"/>
def join_guild(conn, guild, user):
    conn.zadd('members:' + guild, user, 0)

def leave_guild(conn, guild, user):
    conn.zrem('members:' + guild, user)
# <end id="_1314_14473_8403"/>
#END


# 代码清单 6-6
# <start id="_1314_14473_8431"/>
def list_item(conn, itemid, sellerid, price):
    #...
            # 监视卖家包裹发生的变动。
            pipe.watch(inv)                            
            # 确保被出售的物品仍然存在于卖家的包裹里面。
            if not pipe.sismember(inv, itemid):        
                pipe.unwatch()                        
                return None

            # 将物品添加到市场里面。
            pipe.multi()                          
            pipe.zadd("market:", item, price)     
            pipe.srem(inv, itemid)                 
            pipe.execute()                         
            return True
    #...
# <end id="_1314_14473_8431"/>


# 代码清单 6-7
# <start id="_1314_14473_8435"/>
def purchase_item(conn, buyerid, itemid, sellerid, lprice):
    #...
            # 监视市场以及买家个人信息发生的变化。
            pipe.watch("market:", buyer)             

            # 检查物品是否已经售出、物品的价格是否已经发生了变化，
            # 以及买家是否有足够的金钱来购买这件物品。
            price = pipe.zscore("market:", item)     
            funds = int(pipe.hget(buyer, 'funds'))    
            if price != lprice or price > funds:     
                pipe.unwatch()                       
                return None

            # 将买家支付的货款转移给卖家，并将被卖出的物品转移给买家。
            pipe.multi()                              
            pipe.hincrby(seller, 'funds', int(price)) 
            pipe.hincrby(buyerid, 'funds', int(-price))
            pipe.sadd(inventory, itemid)               
            pipe.zrem("market:", item)                 
            pipe.execute()                            
            return True

    #...
# <end id="_1314_14473_8435"/>


# 代码清单 6-8
# <start id="_1314_14473_8641"/>
def acquire_lock(conn, lockname, acquire_timeout=10):
    # 128位随机标识符。
    identifier = str(uuid.uuid4())                     

    end = time.time() + acquire_timeout
    while time.time() < end:
        # 尝试取得锁。
        if conn.setnx('lock:' + lockname, identifier): 
            return identifier

        time.sleep(.001)

    return False
# <end id="_1314_14473_8641"/>


# 代码清单 6-9
# <start id="_1314_14473_8645"/>
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
        # 释放锁。
        release_lock(conn, 'market:', locked)   
# <end id="_1314_14473_8645"/>


# 代码清单 6-10
# <start id="_1314_14473_8650"/>
def release_lock(conn, lockname, identifier):
    pipe = conn.pipeline(True)
    lockname = 'lock:' + lockname

    while True:
        try:
            # 检查并确认进程还持有着锁。
            pipe.watch(lockname)                  
            if pipe.get(lockname) == identifier:  
                # 释放锁。
                pipe.multi()                  
                pipe.delete(lockname)      
                pipe.execute()             
                return True                    

            pipe.unwatch()
            break

        # 有其他客户端修改了锁；重试。
        except redis.exceptions.WatchError:     
            pass                                 

    # 进程已经失去了锁。
    return False                                
# <end id="_1314_14473_8650"/>


# 代码清单 6-11
# <start id="_1314_14473_8790"/>
def acquire_lock_with_timeout(
    conn, lockname, acquire_timeout=10, lock_timeout=10):
    # 128位随机标识符。
    identifier = str(uuid.uuid4())                   
    lockname = 'lock:' + lockname
    # 确保传给EXPIRE的都是整数。
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
# <end id="_1314_14473_8790"/>


# 代码清单 6-12 
# <start id="_1314_14473_8986"/>
def acquire_semaphore(conn, semname, limit, timeout=10):
    # 128位随机标识符。
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
# <end id="_1314_14473_8986"/>


# 代码清单 6-13
# <start id="_1314_14473_8990"/>
def release_semaphore(conn, semname, identifier):
    # 如果信号量已经被正确地释放，那么返回True；
    # 返回False则表示该信号量已经因为过期而被删除了。
    return conn.zrem(semname, identifier)                   
# <end id="_1314_14473_8990"/>


# 代码清单 6-14
# <start id="_1314_14473_9004"/>
def acquire_fair_semaphore(conn, semname, limit, timeout=10):
    # 128位随机标识符。
    identifier = str(uuid.uuid4())                           
    czset = semname + ':owner'
    ctr = semname + ':counter'

    now = time.time()
    pipeline = conn.pipeline(True)
    # 删除超时的信号量。
    pipeline.zremrangebyscore(semname, '-inf', now - timeout)  
    pipeline.zinterstore(czset, {czset: 1, semname: 0})      

    # 对计数器执行自增操作，并获取操作执行之后的值。
    pipeline.incr(ctr)                                       
    counter = pipeline.execute()[-1]                         

    # 尝试获取信号量。
    pipeline.zadd(semname, identifier, now)                   
    pipeline.zadd(czset, identifier, counter)                

    # 通过检查排名来判断客户端是否取得了信号量。
    pipeline.zrank(czset, identifier)                         
    if pipeline.execute()[-1] < limit:                       
        # 客户端成功取得了信号量。
        return identifier                                    

    # 客户端未能取得信号量，清理无用数据。
    pipeline.zrem(semname, identifier)                        
    pipeline.zrem(czset, identifier)                         
    pipeline.execute()
    return None
# <end id="_1314_14473_9004"/>


# 代码清单 6-15
# <start id="_1314_14473_9014"/>
def release_fair_semaphore(conn, semname, identifier):
    pipeline = conn.pipeline(True)
    pipeline.zrem(semname, identifier)
    pipeline.zrem(semname + ':owner', identifier)
    # 返回True表示信号量已被正确地释放，
    # 返回False则表示想要释放的信号量已经因为超时而被删除了。
    return pipeline.execute()[0]                             
# <end id="_1314_14473_9014"/>


# 代码清单 6-16
# <start id="_1314_14473_9022"/>
def refresh_fair_semaphore(conn, semname, identifier):
    # 更新客户端持有的信号量。
    if conn.zadd(semname, identifier, time.time()):          
        # 告知调用者，客户端已经失去了信号量。
        release_fair_semaphore(conn, semname, identifier)   
        return False                                      
    # 客户端仍然持有信号量。
    return True                                              
# <end id="_1314_14473_9022"/>


# 代码清单 6-17
# <start id="_1314_14473_9031"/>
def acquire_semaphore_with_lock(conn, semname, limit, timeout=10):
    identifier = acquire_lock(conn, semname, acquire_timeout=.01)
    if identifier:
        try:
            return acquire_fair_semaphore(conn, semname, limit, timeout)
        finally:
            release_lock(conn, semname, identifier)
# <end id="_1314_14473_9031"/>


# 代码清单 6-18
# <start id="_1314_14473_9056"/>
def send_sold_email_via_queue(conn, seller, item, price, buyer):
    # 准备好待发送邮件。
    data = {
        'seller_id': seller,                 
        'item_id': item,                      
        'price': price,                         
        'buyer_id': buyer,                      
        'time': time.time()                    
    }
    # 将待发送邮件推入到队列里面。
    conn.rpush('queue:email', json.dumps(data)) 
# <end id="_1314_14473_9056"/>


# 代码清单 6-19
# <start id="_1314_14473_9060"/>
def process_sold_email_queue(conn):
    while not QUIT:
        # 尝试获取一封待发送邮件。
        packed = conn.blpop(['queue:email'], 30)                  
        # 队列里面暂时还没有待发送邮件，重试。
        if not packed:                                          
            continue

        # 从JSON对象中解码出邮件信息。
        to_send = json.loads(packed[1])                       
        try:
            # 使用预先编写好的邮件发送函数来发送邮件。
            fetch_data_and_send_sold_email(to_send)            
        except EmailSendError as err:
            log_error("Failed to send sold email", err, to_send)
        else:
            log_success("Sent sold email", to_send)
# <end id="_1314_14473_9060"/>


# 代码清单 6-20
# <start id="_1314_14473_9066"/>
def worker_watch_queue(conn, queue, callbacks):
    while not QUIT:
        # 尝试从队列里面取出一项待执行任务。
        packed = conn.blpop([queue], 30)                   
        # 队列为空，没有任务需要执行；重试。
        if not packed:                                     
            continue                                      

        # 解码任务信息。
        name, args = json.loads(packed[1])                
        # 没有找到任务指定的回调函数，用日志记录错误并重试。
        if name not in callbacks:                         
            log_error("Unknown callback %s"%name)        
            continue                                      
        # 执行任务。
        callbacks[name](*args)                            
# <end id="_1314_14473_9066"/>


# 代码清单 6-21
# <start id="_1314_14473_9074"/>
def worker_watch_queues(conn, queues, callbacks):   # 实现优先级特性要修改的第一行代码。
    while not QUIT:
        packed = conn.blpop(queues, 30)             # 实现优先级特性要修改的第二行代码。
        if not packed:
            continue

        name, args = json.loads(packed[1])
        if name not in callbacks:
            log_error("Unknown callback %s"%name)
            continue
        callbacks[name](*args)
# <end id="_1314_14473_9074"/>


# 代码清单 6-22
# <start id="_1314_14473_9094"/>
def execute_later(conn, queue, name, args, delay=0):
    # 创建唯一标识符。
    identifier = str(uuid.uuid4())                        
    # 准备好需要入队的任务。
    item = json.dumps([identifier, queue, name, args])  
    if delay > 0:
        # 延迟执行这个任务。
        conn.zadd('delayed:', item, time.time() + delay) 
    else:
        # 立即执行这个任务。
        conn.rpush('queue:' + queue, item)                 
    # 返回标识符。
    return identifier                                    
# <end id="_1314_14473_9094"/>


# 代码清单 6-23
# <start id="_1314_14473_9099"/>
def poll_queue(conn):
    while not QUIT:
        # 获取队列中的第一个任务。
        item = conn.zrange('delayed:', 0, 0, withscores=True)   
        # 队列没有包含任何任务，或者任务的执行时间未到。
        if not item or item[0][1] > time.time():               
            time.sleep(.01)                                    
            continue                                            

        # 解码要被执行的任务，弄清楚它应该被推入到哪个任务队列里面。
        item = item[0][0]                                      
        identifier, queue, function, args = json.loads(item)   

        # 为了对任务进行移动，尝试获取锁。
        locked = acquire_lock(conn, identifier)                
        # 获取锁失败，跳过后续步骤并重试。
        if not locked:                                         
            continue                                          

        # 将任务推入到适当的任务队列里面。
        if conn.zrem('delayed:', item):                       
            conn.rpush('queue:' + queue, item)                 

        # 释放锁。
        release_lock(conn, identifier, locked)                 
# <end id="_1314_14473_9099"/>


# 代码清单 6-24
# <start id="_1314_14473_9124"/>
def create_chat(conn, sender, recipients, message, chat_id=None):
    # 获得新的群组ID。
    chat_id = chat_id or str(conn.incr('ids:chat:'))     

    # 创建一个由用户和分值组成的字典，字典里面的信息将被添加到有序集合里面。
    recipients.append(sender)                           
    recipientsd = dict((r, 0) for r in recipients)       

    pipeline = conn.pipeline(True)
    # 将所有参与群聊的用户添加到有序集合里面。
    pipeline.zadd('chat:' + chat_id, **recipientsd)      
    # 初始化已读有序集合。
    for rec in recipients:                                
        pipeline.zadd('seen:' + rec, chat_id, 0)          
    pipeline.execute()

    # 发送消息。
    return send_message(conn, chat_id, sender, message)  
# <end id="_1314_14473_9124"/>


# 代码清单 6-25
# <start id="_1314_14473_9127"/>
def send_message(conn, chat_id, sender, message):
    identifier = acquire_lock(conn, 'chat:' + chat_id)
    if not identifier:
        raise Exception("Couldn't get the lock")
    try:
        # 筹备待发送的消息。
        mid = conn.incr('ids:' + chat_id) 
        ts = time.time()                                 
        packed = json.dumps({                            
            'id': mid,                                   
            'ts': ts,                                   
            'sender': sender,                           
            'message': message,                         
        })                                              

        # 将消息发送至群组。
        conn.zadd('msgs:' + chat_id, packed, mid)     
    finally:
        release_lock(conn, 'chat:' + chat_id, identifier)
    return chat_id
# <end id="_1314_14473_9127"/>


# 代码清单 6-26
# <start id="_1314_14473_9132"/>
def fetch_pending_messages(conn, recipient):
    # 获取最后接收到的消息的ID。
    seen = conn.zrange('seen:' + recipient, 0, -1, withscores=True) 

    pipeline = conn.pipeline(True)

    # 获取所有未读消息。
    for chat_id, seen_id in seen:                              
        pipeline.zrangebyscore(                              
            'msgs:' + chat_id, seen_id+1, 'inf')               
    # 这些数据将被返回给函数调用者。
    chat_info = zip(seen, pipeline.execute())                 

    for i, ((chat_id, seen_id), messages) in enumerate(chat_info):
        if not messages:
            continue
        messages[:] = map(json.loads, messages)
        # 使用最新收到的消息来更新群组有序集合。
        seen_id = messages[-1]['id']                         
        conn.zadd('chat:' + chat_id, recipient, seen_id)       

        # 找出那些所有人都已经阅读过的消息。
        min_id = conn.zrange(                                
            'chat:' + chat_id, 0, 0, withscores=True)          

        # 更新已读消息有序集合。
        pipeline.zadd('seen:' + recipient, chat_id, seen_id)   
        if min_id:
            # 清除那些已经被所有人阅读过的消息。
            pipeline.zremrangebyscore(                        
                'msgs:' + chat_id, 0, min_id[0][1])             
        chat_info[i] = (chat_id, messages)
    pipeline.execute()

    return chat_info
# <end id="_1314_14473_9132"/>


# 代码清单 6-27
# <start id="_1314_14473_9135"/>
def join_chat(conn, chat_id, user):
    # 取得最新群组消息的ID。
    message_id = int(conn.get('ids:' + chat_id))            

    pipeline = conn.pipeline(True)
    # 将用户添加到群组成员列表里面。
    pipeline.zadd('chat:' + chat_id, user, message_id)         
    # 将群组添加到用户的已读列表里面。
    pipeline.zadd('seen:' + user, chat_id, message_id)        
    pipeline.execute()
# <end id="_1314_14473_9135"/>


# 代码清单 6-28
# <start id="_1314_14473_9136"/>
def leave_chat(conn, chat_id, user):
    pipeline = conn.pipeline(True)
    # 从群组里面移除给定的用户。
    pipeline.zrem('chat:' + chat_id, user)                     
    pipeline.zrem('seen:' + user, chat_id)                     
    # 查看群组剩余成员的数量。
    pipeline.zcard('chat:' + chat_id)                          

    if not pipeline.execute()[-1]:
        # 删除群组。
        pipeline.delete('msgs:' + chat_id)                    
        pipeline.delete('ids:' + chat_id)                     
        pipeline.execute()
    else:
        # 查找那些已经被所有成员阅读过的消息。
        oldest = conn.zrange(                                  
            'chat:' + chat_id, 0, 0, withscores=True)          
        # 删除那些已经被所有成员阅读过的消息。
        conn.zremrangebyscore('msgs:' + chat_id, 0, oldest[0][1])
# <end id="_1314_14473_9136"/>


# 代码清单 6-29
# <start id="_1314_15044_3669"/>
# 本地聚合数据字典。
aggregates = defaultdict(lambda: defaultdict(int))     

def daily_country_aggregate(conn, line):
    if line:
        line = line.split()
        # 提取日志行中的信息。
        ip = line[0]                                    
        day = line[1]                                   
        # 根据IP地址判断用户所在国家。
        country = find_city_by_ip_local(ip)[2]        
        # 对本地聚合数据执行自增操作。
        aggregates[day][country] += 1                  
        return

    # 当天的日志文件已经处理完毕，将聚合计算的结果写入到Redis里面。
    for day, aggregate in aggregates.items():          
        conn.zadd('daily:country:' + day, **aggregate) 
        del aggregates[day]                           
# <end id="_1314_15044_3669"/>


# 代码清单 6-30
# <start id="_1314_14473_9209"/>
def copy_logs_to_redis(conn, path, channel, count=10,
                       limit=2**30, quit_when_done=True):
    bytes_in_redis = 0
    waiting = deque()
    # 创建用于向客户端发送消息的群组。
    create_chat(conn, 'source', map(str, range(count)), '', channel) 
    count = str(count)
    # 遍历所有日志文件。
    for logfile in sorted(os.listdir(path)):             
        full_path = os.path.join(path, logfile)

        fsize = os.stat(full_path).st_size
        # 如果程序需要更多空间，那么清除已经处理完毕的文件。
        while bytes_in_redis + fsize > limit:              
            cleaned = _clean(conn, channel, waiting, count)
            if cleaned:                                  
                bytes_in_redis -= cleaned                
            else:                                       
                time.sleep(.25)                          

        # 将文件上传至Redis。
        with open(full_path, 'rb') as inp:           
            block = ' '                          
            while block:                                 
                block = inp.read(2**17)                  
                conn.append(channel+logfile, block)      

        # 提醒监听者，文件已经准备就绪。
        send_message(conn, channel, 'source', logfile)    

        # 对本地记录的Redis内存占用量相关信息进行更新。
        bytes_in_redis += fsize                          
        waiting.append((logfile, fsize))                  

    # 所有日志文件已经处理完毕，向监听者报告此事。
    if quit_when_done:                                    
        send_message(conn, channel, 'source', ':done')    

    # 在工作完成之后，清理无用的日志文件。
    while waiting:                                        
        cleaned = _clean(conn, channel, waiting, count)   
        if cleaned:                                       
            bytes_in_redis -= cleaned                     
        else:                                             
            time.sleep(.25)                             

# 对Redis进行清理的详细步骤。
def _clean(conn, channel, waiting, count):                
    if not waiting:                                        
        return 0                                           
    w0 = waiting[0][0]                                     
    if conn.get(channel + w0 + ':done') == count:          
        conn.delete(channel + w0, channel + w0 + ':done')  
        return waiting.popleft()[1]                        
    return 0                                               
# <end id="_1314_14473_9209"/>


# 代码清单 6-31
# <start id="_1314_14473_9213"/>
def process_logs_from_redis(conn, id, callback):
    while 1:
        # 获取文件列表。
        fdata = fetch_pending_messages(conn, id)                    

        for ch, mdata in fdata:
            for message in mdata:
                logfile = message['message']

                # 所有日志行已经处理完毕。
                if logfile == ':done':                                
                    return                                            
                elif not logfile:
                    continue

                # 选择一个块读取器（block reader）。
                block_reader = readblocks                             
                if logfile.endswith('.gz'):                           
                    block_reader = readblocks_gz                      

                # 遍历日志行。
                for line in readlines(conn, ch+logfile, block_reader):
                    # 将日志行传递给回调函数。
                    callback(conn, line)                              
                # 强制地刷新聚合数据缓存。
                callback(conn, None)                                 

                # 报告日志已经处理完毕。
                conn.incr(ch + logfile + ':done')                    

        if not fdata:
            time.sleep(.1)
# <end id="_1314_14473_9213"/>


# 代码清单 6-32
# <start id="_1314_14473_9221"/>
def readlines(conn, key, rblocks):
    out = ''
    for block in rblocks(conn, key):
        out += block
        # 查找位于文本最右端的断行符；如果断行符不存在，那么rfind()返回-1。
        posn = out.rfind('\n')                      
        # 找到一个断行符。
        if posn >= 0:                               
            # 根据断行符来分割日志行。
            for line in out[:posn].split('\n'):    
                # 向调用者返回每个行。
                yield line + '\n'                  
            # 保留余下的数据。
            out = out[posn+1:]                     
        # 所有数据块已经处理完毕。
        if not block:                              
            yield out
            break
# <end id="_1314_14473_9221"/>


# 代码清单 6-33
# <start id="_1314_14473_9225"/>
def readblocks(conn, key, blocksize=2**17):
    lb = blocksize
    pos = 0
    # 尽可能地读取更多数据，直到出现不完整读操作（partial read）为止。
    while lb == blocksize:                                 
        # 获取数据块。
        block = conn.substr(key, pos, pos + blocksize - 1) 
        # 准备进行下一次遍历。
        yield block                                        
        lb = len(block)                                    
        pos += lb                                          
    yield ''
# <end id="_1314_14473_9225"/>


# 代码清单 6-34
# <start id="_1314_14473_9229"/>
def readblocks_gz(conn, key):
    inp = ''
    decoder = None
    # 从Redis里面读入原始数据。
    for block in readblocks(conn, key, 2**17):                 
        if not decoder:
            inp += block
            try:
                # 分析头信息以便取得被压缩数据。
                if inp[:3] != "\x1f\x8b\x08":                
                    raise IOError("invalid gzip data")         
                i = 10                                          
                flag = ord(inp[3])                              
                if flag & 4:                                    
                    i += 2 + ord(inp[i]) + 256*ord(inp[i+1])    
                if flag & 8:                                    
                    i = inp.index('\0', i) + 1                  
                if flag & 16:                                   
                    i = inp.index('\0', i) + 1                  
                if flag & 2:                                   
                    i += 2                                     

                # 程序读取的头信息并不完整。
                if i > len(inp):                               
                    raise IndexError("not enough data")         
            except (IndexError, ValueError):                    
                continue                                       

            else:
                # 已经找到头信息，准备好相应的解压程序。
                block = inp[i:]                                 
                inp = None                                      
                decoder = zlib.decompressobj(-zlib.MAX_WBITS)   
                if not block:
                    continue

        # 所有数据已经处理完毕，向调用者返回最后剩下的数据块。
        if not block:                                           
            yield decoder.flush()                               
            break

        # 向调用者返回解压后的数据块。
        yield decoder.decompress(block)                         
# <end id="_1314_14473_9229"/>

class TestCh06(unittest.TestCase):
    def setUp(self):
        import redis
        self.conn = redis.Redis(db=15)

    def tearDown(self):
        self.conn.flushdb()
        del self.conn
        print
        print

    def test_add_update_contact(self):
        import pprint
        conn = self.conn
        conn.delete('recent:user')

        print "Let's add a few contacts..."
        for i in xrange(10):
            add_update_contact(conn, 'user', 'contact-%i-%i'%(i//3, i))
        print "Current recently contacted contacts"
        contacts = conn.lrange('recent:user', 0, -1)
        pprint.pprint(contacts)
        self.assertTrue(len(contacts) >= 10)
        print

        print "Let's pull one of the older ones up to the front"
        add_update_contact(conn, 'user', 'contact-1-4')
        contacts = conn.lrange('recent:user', 0, 2)
        print "New top-3 contacts:"
        pprint.pprint(contacts)
        self.assertEquals(contacts[0], 'contact-1-4')
        print

        print "Let's remove a contact..."
        print remove_contact(conn, 'user', 'contact-2-6')
        contacts = conn.lrange('recent:user', 0, -1)
        print "New contacts:"
        pprint.pprint(contacts)
        self.assertTrue(len(contacts) >= 9)
        print

        print "And let's finally autocomplete on "
        all = conn.lrange('recent:user', 0, -1)
        contacts = fetch_autocomplete_list(conn, 'user', 'c')
        self.assertTrue(all == contacts)
        equiv = [c for c in all if c.startswith('contact-2-')]
        contacts = fetch_autocomplete_list(conn, 'user', 'contact-2-')
        equiv.sort()
        contacts.sort()
        self.assertEquals(equiv, contacts)
        conn.delete('recent:user')

    def test_address_book_autocomplete(self):
        self.conn.delete('members:test')
        print "the start/end range of 'abc' is:", find_prefix_range('abc')
        print

        print "Let's add a few people to the guild"
        for name in ['jeff', 'jenny', 'jack', 'jennifer']:
            join_guild(self.conn, 'test', name)
        print
        print "now let's try to find users with names starting with 'je':"
        r = autocomplete_on_prefix(self.conn, 'test', 'je')
        print r
        self.assertTrue(len(r) == 3)
        print "jeff just left to join a different guild..."
        leave_guild(self.conn, 'test', 'jeff')
        r = autocomplete_on_prefix(self.conn, 'test', 'je')
        print r
        self.assertTrue(len(r) == 2)
        self.conn.delete('members:test')

    def test_distributed_locking(self):
        self.conn.delete('lock:testlock')
        print "Getting an initial lock..."
        self.assertTrue(acquire_lock_with_timeout(self.conn, 'testlock', 1, 1))
        print "Got it!"
        print "Trying to get it again without releasing the first one..."
        self.assertFalse(acquire_lock_with_timeout(self.conn, 'testlock', .01, 1))
        print "Failed to get it!"
        print
        print "Waiting for the lock to timeout..."
        time.sleep(2)
        print "Getting the lock again..."
        r = acquire_lock_with_timeout(self.conn, 'testlock', 1, 1)
        self.assertTrue(r)
        print "Got it!"
        print "Releasing the lock..."
        self.assertTrue(release_lock(self.conn, 'testlock', r))
        print "Released it..."
        print
        print "Acquiring it again..."
        self.assertTrue(acquire_lock_with_timeout(self.conn, 'testlock', 1, 1))
        print "Got it!"
        self.conn.delete('lock:testlock')

    def test_counting_semaphore(self):
        self.conn.delete('testsem', 'testsem:owner', 'testsem:counter')
        print "Getting 3 initial semaphores with a limit of 3..."
        for i in xrange(3):
            self.assertTrue(acquire_fair_semaphore(self.conn, 'testsem', 3, 1))
        print "Done!"
        print "Getting one more that should fail..."
        self.assertFalse(acquire_fair_semaphore(self.conn, 'testsem', 3, 1))
        print "Couldn't get it!"
        print
        print "Lets's wait for some of them to time out"
        time.sleep(2)
        print "Can we get one?"
        r = acquire_fair_semaphore(self.conn, 'testsem', 3, 1)
        self.assertTrue(r)
        print "Got one!"
        print "Let's release it..."
        self.assertTrue(release_fair_semaphore(self.conn, 'testsem', r))
        print "Released!"
        print
        print "And let's make sure we can get 3 more!"
        for i in xrange(3):
            self.assertTrue(acquire_fair_semaphore(self.conn, 'testsem', 3, 1))
        print "We got them!"
        self.conn.delete('testsem', 'testsem:owner', 'testsem:counter')

    def test_delayed_tasks(self):
        import threading
        self.conn.delete('queue:tqueue', 'delayed:')
        print "Let's start some regular and delayed tasks..."
        for delay in [0, .5, 0, 1.5]:
            self.assertTrue(execute_later(self.conn, 'tqueue', 'testfn', [], delay))
        r = self.conn.llen('queue:tqueue')
        print "How many non-delayed tasks are there (should be 2)?", r
        self.assertEquals(r, 2)
        print
        print "Let's start up a thread to bring those delayed tasks back..."
        t = threading.Thread(target=poll_queue, args=(self.conn,))
        t.setDaemon(1)
        t.start()
        print "Started."
        print "Let's wait for those tasks to be prepared..."
        time.sleep(2)
        global QUIT
        QUIT = True
        t.join()
        r = self.conn.llen('queue:tqueue')
        print "Waiting is over, how many tasks do we have (should be 4)?", r
        self.assertEquals(r, 4)
        self.conn.delete('queue:tqueue', 'delayed:')

    def test_multi_recipient_messaging(self):
        self.conn.delete('ids:chat:', 'msgs:1', 'ids:1', 'seen:joe', 'seen:jeff', 'seen:jenny')

        print "Let's create a new chat session with some recipients..."
        chat_id = create_chat(self.conn, 'joe', ['jeff', 'jenny'], 'message 1')
        print "Now let's send a few messages..."
        for i in xrange(2, 5):
            send_message(self.conn, chat_id, 'joe', 'message %s'%i)
        print
        print "And let's get the messages that are waiting for jeff and jenny..."
        r1 = fetch_pending_messages(self.conn, 'jeff')
        r2 = fetch_pending_messages(self.conn, 'jenny')
        print "They are the same?", r1==r2
        self.assertEquals(r1, r2)
        print "Those messages are:"
        import pprint
        pprint.pprint(r1)
        self.conn.delete('ids:chat:', 'msgs:1', 'ids:1', 'seen:joe', 'seen:jeff', 'seen:jenny')

    def test_file_distribution(self):
        import gzip, shutil, tempfile, threading
        self.conn.delete('test:temp-1.txt', 'test:temp-2.txt', 'test:temp-3.txt', 'msgs:test:', 'seen:0', 'seen:source', 'ids:test:', 'chat:test:')

        dire = tempfile.mkdtemp()
        try:
            print "Creating some temporary 'log' files..."
            with open(dire + '/temp-1.txt', 'wb') as f:
                f.write('one line\n')
            with open(dire + '/temp-2.txt', 'wb') as f:
                f.write(10000 * 'many lines\n')
            out = gzip.GzipFile(dire + '/temp-3.txt.gz', mode='wb')
            for i in xrange(100000):
                out.write('random line %s\n'%(os.urandom(16).encode('hex'),))
            out.close()
            size = os.stat(dire + '/temp-3.txt.gz').st_size
            print "Done."
            print
            print "Starting up a thread to copy logs to redis..."
            t = threading.Thread(target=copy_logs_to_redis, args=(self.conn, dire, 'test:', 1, size))
            t.setDaemon(1)
            t.start()

            print "Let's pause to let some logs get copied to Redis..."
            time.sleep(.25)
            print
            print "Okay, the logs should be ready. Let's process them!"

            index = [0]
            counts = [0, 0, 0]
            def callback(conn, line):
                if line is None:
                    print "Finished with a file %s, linecount: %s"%(index[0], counts[index[0]])
                    index[0] += 1
                elif line or line.endswith('\n'):
                    counts[index[0]] += 1

            print "Files should have 1, 10000, and 100000 lines"
            process_logs_from_redis(self.conn, '0', callback)
            self.assertEquals(counts, [1, 10000, 100000])

            print
            print "Let's wait for the copy thread to finish cleaning up..."
            t.join()
            print "Done cleaning out Redis!"

        finally:
            print "Time to clean up files..."
            shutil.rmtree(dire)
            print "Cleaned out files!"
        self.conn.delete('test:temp-1.txt', 'test:temp-2.txt', 'test:temp-3.txt', 'msgs:test:', 'seen:0', 'seen:source', 'ids:test:', 'chat:test:')

if __name__ == '__main__':
    unittest.main()
