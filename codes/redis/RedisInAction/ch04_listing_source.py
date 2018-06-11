# coding: utf-8

import os
import time
import unittest
import uuid

import redis


# 代码清单 4-1
'''
# <start id="persistence-options"/>
save 60 1000                        # 快照持久化选项。
stop-writes-on-bgsave-error no      #
rdbcompression yes                  #
dbfilename dump.rdb                 #

appendonly no                       # 只追加文件持久化选项。
appendfsync everysec                #
no-appendfsync-on-rewrite no        #
auto-aof-rewrite-percentage 100     #
auto-aof-rewrite-min-size 64mb      #

dir ./                              # 共享选项，这个选项决定了快照文件和只追加文件的保存位置。
'''


# 代码清单 4-2
# <start id="process-logs-progress"/>
# 日志处理函数接受的其中一个参数为回调函数，
# 这个回调函数接受一个Redis连接和一个日志行作为参数，
# 并通过调用流水线对象的方法来执行Redis命令。
def process_logs(conn, path, callback):
    # 获取文件当前的处理进度。
    current_file, offset = conn.mget( 
        'progress:file', 'progress:position') 

    pipe = conn.pipeline()

    # 通过使用闭包（closure）来减少重复代码
    def update_progress():    
        # 更新正在处理的日志文件的名字和偏移量。
        pipe.mset({
            'progress:file': fname,
            'progress:position': offset 
        })
        # 这个语句负责执行实际的日志更新操作，
        # 并将日志文件的名字和目前的处理进度记录到Redis里面。
        pipe.execute()

    # 有序地遍历各个日志文件。
    for fname in sorted(os.listdir(path)): 
        # 略过所有已处理的日志文件。
        if fname < current_file:
            continue

        inp = open(os.path.join(path, fname), 'rb')
        # 在接着处理一个因为系统崩溃而未能完成处理的日志文件时，略过已处理的内容。
        if fname == current_file:
            inp.seek(int(offset, 10)) 
        else:
            offset = 0

        current_file = None

        # 枚举函数遍历一个由文件行组成的序列，
        # 并返回任意多个二元组，
        # 每个二元组包含了行号lno和行数据line，
        # 其中行号从0开始。
        for lno, line in enumerate(inp):
            # 处理日志行。
            callback(pipe, line)
            # 更新已处理内容的偏移量。
            offset += int(offset) + len(line) 

            # 每当处理完1000个日志行或者处理完整个日志文件的时候，
            # 都更新一次文件的处理进度。
            if not (lno+1) % 1000: 
                update_progress()

        update_progress()

        inp.close()
# <end id="process-logs-progress"/>


# 代码清单 4-3
# <start id="wait-for-sync"/>
def wait_for_sync(mconn, sconn):
    identifier = str(uuid.uuid4())
    # 将令牌添加至主服务器。
    mconn.zadd('sync:wait', identifier, time.time()) 

    # 如果有必要的话，等待从服务器完成同步。
    while sconn.info()['master_link_status'] != 'up': 
        time.sleep(.001)

    # 等待从服务器接收数据更新。
    while not sconn.zscore('sync:wait', identifier): 
        time.sleep(.001)

    # 最多只等待一秒钟。
    deadline = time.time() + 1.01  
    while time.time() < deadline: 
        # 检查数据更新是否已经被同步到了磁盘。
        if sconn.info()['aof_pending_bio_fsync'] == 0:
            break
        time.sleep(.001)

    # 清理刚刚创建的新令牌以及之前可能留下的旧令牌。
    mconn.zrem('sync:wait', identifier)
    mconn.zremrangebyscore('sync:wait', 0, time.time()-900) 
# <end id="wait-for-sync"/>


# 代码清单 4-4
'''
# <start id="master-failover"/>
user@vpn-master ~:$ ssh root@machine-b.vpn                          # 通过VPN网络连接机器B。
Last login: Wed Mar 28 15:21:06 2012 from ...                       #
root@machine-b ~:$ redis-cli                                        # 启动命令行Redis客户端来执行几个简单的操作。
redis 127.0.0.1:6379> SAVE                                          # 执行SAVE命令，
OK                                                                  # 并在命令完成之后，
redis 127.0.0.1:6379> QUIT                                          # 使用QUIT命令退出客户端。
root@machine-b ~:$ scp \\                                           # 将快照文件发送至新的主服务器——机器C。
> /var/local/redis/dump.rdb machine-c.vpn:/var/local/redis/         #
dump.rdb                      100%   525MB  8.1MB/s   01:05         #
root@machine-b ~:$ ssh machine-c.vpn                                # 连接新的主服务器并启动Redis。
Last login: Tue Mar 27 12:42:31 2012 from ...                       #
root@machine-c ~:$ sudo /etc/init.d/redis-server start              #
Starting Redis server...                                            #
root@machine-c ~:$ exit
root@machine-b ~:$ redis-cli                                        # 告知机器B的Redis，让它将机器C用作新的主服务器。
redis 127.0.0.1:6379> SLAVEOF machine-c.vpn 6379                    #
OK                                                                  #
redis 127.0.0.1:6379> QUIT
root@machine-b ~:$ exit
user@vpn-master ~:$
# <end id="master-failover"/>
#A Connect to machine B on our vpn network
#B Start up the command line redis client to do a few simple operations
#C Start a SAVE, and when it is done, QUIT so that we can continue
#D Copy the snapshot over to the new master, machine C
#E Connect to the new master and start Redis
#F Tell machine B's Redis that it should use C as the new master
#END
'''

# 代码清单 4-5
# <start id="_1313_14472_8342"/>
def list_item(conn, itemid, sellerid, price):
    inventory = "inventory:%s"%sellerid
    item = "%s.%s"%(itemid, sellerid)
    end = time.time() + 5
    pipe = conn.pipeline()

    while time.time() < end:
        try:
            # 监视用户包裹发生的变化。
            pipe.watch(inventory)
            # 验证用户是否仍然持有指定的物品。
            if not pipe.sismember(inventory, itemid):
                # 如果指定的物品不在用户的包裹里面，
                # 那么停止对包裹键的监视并返回一个空值。
                pipe.unwatch() 
                return None

            # 将指定的物品添加到物品买卖市场里面。
            pipe.multi()
            pipe.zadd("market:", item, price) 
            pipe.srem(inventory, itemid)
            # 如果执行execute方法没有引发WatchError异常，
            # 那么说明事务执行成功，
            # 并且对包裹键的监视也已经结束。
            pipe.execute()   
            return True
        # 用户的包裹已经发生了变化；重试。
        except redis.exceptions.WatchError: 
            pass
    return False
# <end id="_1313_14472_8342"/>


# 代码清单 4-6
# <start id="_1313_14472_8353"/>
def purchase_item(conn, buyerid, itemid, sellerid, lprice):
    buyer = "users:%s"%buyerid
    seller = "users:%s"%sellerid
    item = "%s.%s"%(itemid, sellerid)
    inventory = "inventory:%s"%buyerid
    end = time.time() + 10
    pipe = conn.pipeline()

    while time.time() < end:
        try:
            # 对物品买卖市场以及买家账号信息的变化进行监视。
            pipe.watch("market:", buyer)

            # 检查指定物品的价格是否出现了变化，
            # 以及买家是否有足够的钱来购买指定的物品。
            price = pipe.zscore("market:", item) 
            funds = int(pipe.hget(buyer, "funds"))  
            if price != lprice or price > funds:  
                pipe.unwatch()   
                return None

            # 将买家支付的货款转移给卖家，并将卖家出售的物品移交给买家。
            pipe.multi()
            pipe.hincrby(seller, "funds", int(price)) 
            pipe.hincrby(buyer, "funds", int(-price)) 
            pipe.sadd(inventory, itemid)  
            pipe.zrem("market:", item)  
            pipe.execute()      
            return True
        # 如果买家的账号或者物品买卖市场出现了变化，那么进行重试。
        except redis.exceptions.WatchError:
            pass

    return False
# <end id="_1313_14472_8353"/>


# 代码清单 4-7
# <start id="update-token"/>
def update_token(conn, token, user, item=None):
    # 获取时间戳。
    timestamp = time.time()
    # 创建令牌与已登录用户之间的映射。
    conn.hset('login:', token, user)
    # 记录令牌最后一次出现的时间。
    conn.zadd('recent:', token, timestamp)
    if item:
        # 把用户浏览过的商品记录起来。
        conn.zadd('viewed:' + token, item, timestamp) 
        # 移除旧商品，只记录最新浏览的25件商品。
        conn.zremrangebyrank('viewed:' + token, 0, -26) 
        # 更新给定商品的被浏览次数。
        conn.zincrby('viewed:', item, -1) 
# <end id="update-token"/>


# 代码清单 4-8
# <start id="update-token-pipeline"/>
def update_token_pipeline(conn, token, user, item=None):
    timestamp = time.time()
    # 设置流水线。
    pipe = conn.pipeline(False)                         #A
    pipe.hset('login:', token, user)
    pipe.zadd('recent:', token, timestamp)
    if item:
        pipe.zadd('viewed:' + token, item, timestamp)
        pipe.zremrangebyrank('viewed:' + token, 0, -26)
        pipe.zincrby('viewed:', item, -1)
    # 执行那些被流水线包裹的命令。
    pipe.execute()                                      #B
# <end id="update-token-pipeline"/>


# 代码清单 4-9
# <start id="simple-pipeline-benchmark-code"/>
def benchmark_update_token(conn, duration):
    # 测试会分别执行update_token()函数和update_token_pipeline()函数。
    for function in (update_token, update_token_pipeline): 
        # 设置计数器以及测试结束的条件。
        count = 0                                               #B
        start = time.time()                                     #B
        end = start + duration                                  #B
        while time.time() < end:
            count += 1
            # 调用两个函数的其中一个。
            function(conn, 'token', 'user', 'item')             #C
        # 计算函数的执行时长。
        delta = time.time() - start                             #D
        # 打印测试结果。
        print function.__name__, count, delta, count / delta    #E
# <end id="simple-pipeline-benchmark-code"/>


# 代码清单 4-10
'''
# <start id="redis-benchmark"/>
$ redis-benchmark  -c 1 -q                               # 给定“-q”选项可以让程序简化输出结果，
PING (inline): 34246.57 requests per second              # 给定“-c 1”选项让程序只使用一个客户端来进行测试。
PING: 34843.21 requests per second
MSET (10 keys): 24213.08 requests per second
SET: 32467.53 requests per second
GET: 33112.59 requests per second
INCR: 32679.74 requests per second
LPUSH: 33333.33 requests per second
LPOP: 33670.04 requests per second
SADD: 33222.59 requests per second
SPOP: 34482.76 requests per second
LPUSH (again, in order to bench LRANGE): 33222.59 requests per second
LRANGE (first 100 elements): 22988.51 requests per second
LRANGE (first 300 elements): 13888.89 requests per second
LRANGE (first 450 elements): 11061.95 requests per second
LRANGE (first 600 elements): 9041.59 requests per second
# <end id="redis-benchmark"/>
#A We run with the '-q' option to get simple output, and '-c 1' to use a single client
#END
'''

#--------------- 以下是用于测试代码的辅助函数 --------------------------------

class TestCh04(unittest.TestCase):
    def setUp(self):
        import redis
        self.conn = redis.Redis(db=15)
        self.conn.flushdb()

    def tearDown(self):
        self.conn.flushdb()
        del self.conn
        print
        print

    # We can't test process_logs, as that would require writing to disk, which
    # we don't want to do.

    # We also can't test wait_for_sync, as we can't guarantee that there are
    # multiple Redis servers running with the proper configuration

    def test_list_item(self):
        import pprint
        conn = self.conn

        print "We need to set up just enough state so that a user can list an item"
        seller = 'userX'
        item = 'itemX'
        conn.sadd('inventory:' + seller, item)
        i = conn.smembers('inventory:' + seller)
        print "The user's inventory has:", i
        self.assertTrue(i)
        print

        print "Listing the item..."
        l = list_item(conn, item, seller, 10)
        print "Listing the item succeeded?", l
        self.assertTrue(l)
        r = conn.zrange('market:', 0, -1, withscores=True)
        print "The market contains:"
        pprint.pprint(r)
        self.assertTrue(r)
        self.assertTrue(any(x[0] == 'itemX.userX' for x in r))

    def test_purchase_item(self):
        self.test_list_item()
        conn = self.conn
        
        print "We need to set up just enough state so a user can buy an item"
        buyer = 'userY'
        conn.hset('users:userY', 'funds', 125)
        r = conn.hgetall('users:userY')
        print "The user has some money:", r
        self.assertTrue(r)
        self.assertTrue(r.get('funds'))
        print

        print "Let's purchase an item"
        p = purchase_item(conn, 'userY', 'itemX', 'userX', 10)
        print "Purchasing an item succeeded?", p
        self.assertTrue(p)
        r = conn.hgetall('users:userY')
        print "Their money is now:", r
        self.assertTrue(r)
        i = conn.smembers('inventory:' + buyer)
        print "Their inventory is now:", i
        self.assertTrue(i)
        self.assertTrue('itemX' in i)
        self.assertEquals(conn.zscore('market:', 'itemX.userX'), None)

    def test_benchmark_update_token(self):
        benchmark_update_token(self.conn, 5)

if __name__ == '__main__':
    unittest.main()
