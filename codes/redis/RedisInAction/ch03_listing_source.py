# coding: utf-8

import threading
import time
import unittest

import redis

ONE_WEEK_IN_SECONDS = 7 * 86400
VOTE_SCORE = 432
ARTICLES_PER_PAGE = 25


# 代码清单 3-1
'''
# <start id="string-calls-1"/>
>>> conn = redis.Redis()
>>> conn.get('key')             # 尝试获取一个不存在的键将得到一个None值，终端不会显示这个值。
>>> conn.incr('key')            # 我们既可以对不存在的键执行自增操作，
1                               # 也可以通过可选的参数来指定自增操作的增量。
>>> conn.incr('key', 15)        #
16                              #
>>> conn.decr('key', 5)         # 和自增操作一样，
11                              # 执行自减操作的函数也可以通过可选的参数来指定减量。
>>> conn.get('key')             # 在尝试获取一个键的时候，命令以字符串格式返回被存储的整数。
'11'                            #
>>> conn.set('key', '13')       # 即使在设置键时输入的值为字符串，
True                            # 但只要这个值可以被解释为整数，
>>> conn.incr('key')            # 我们就可以把它当作整数来处理。
14                              #
# <end id="string-calls-1"/>
'''


# 代码清单 3-2
'''
# <start id="string-calls-2"/>
>>> conn.append('new-string-key', 'hello ')     # 将字符串'hello'追加到目前并不存在的'new-string-key'键里。
6L                                              # APPEND命令在执行之后会返回字符串当前的长度。
>>> conn.append('new-string-key', 'world!')
12L                                             #
>>> conn.substr('new-string-key', 3, 7)         # Redis的索引以0为开始，在进行范围访问时，范围的终点（endpoint）默认也包含在这个范围之内。
'lo wo'                                         # 字符串'lo wo'位于字符串'hello world!'的中间。
>>> conn.setrange('new-string-key', 0, 'H')     # 对字符串执行范围设置操作。
12                                              # SETRANGE命令在执行之后同样会返回字符串的当前总长度。
>>> conn.setrange('new-string-key', 6, 'W')
12
>>> conn.get('new-string-key')                  # 查看字符串的当前值。
'Hello World!'                                  # 前面执行的两个SETRANGE命令成功地将字母h和w从原来的小写改成了大写。
>>> conn.setrange('new-string-key', 11, ', how are you?')   # SETRANGE命令既可以用于替换字符串里已有的内容，又可以用于增长字符串。
25
>>> conn.get('new-string-key')
'Hello World, how are you?'                     # 前面执行的SETRANGE命令移除了字符串末尾的感叹号，并将更多字符追加到了字符串末尾。
>>> conn.setbit('another-key', 2, 1)            # 对超出字符串长度的二进制位进行设置时，超出的部分会被填充为空字节。
0                                               # SETBIT命令会返回二进制位被设置之前的值。
>>> conn.setbit('another-key', 7, 1)            # 在对Redis存储的二进制位进行解释（interpret）时，
0                                               # 请记住Redis存储的二进制位是按照偏移量从高到低排列的。
>>> conn.get('another-key')                     #
'!'                                             # 通过将第2个二进制位以及第7个二进制位的值设置为1，键的值将变为‘!’，即字符33 。
# <end id="string-calls-2"/>
'''


# 代码清单 3-3
'''
# <start id="list-calls-1"/>
>>> conn.rpush('list-key', 'last')          # 在向列表推入元素时，
1L                                          # 推入操作执行完毕之后会返回列表当前的长度。
>>> conn.lpush('list-key', 'first')         # 可以很容易地对列表的两端执行推入操作。
2L
>>> conn.rpush('list-key', 'new last')
3L
>>> conn.lrange('list-key', 0, -1)          # 从语义上来说，列表的左端为开头，右端为结尾。
['first', 'last', 'new last']               #
>>> conn.lpop('list-key')                   # 通过重复地弹出列表左端的元素，
'first'                                     # 可以按照从左到右的顺序来获取列表中的元素。
>>> conn.lpop('list-key')                   #
'last'                                      #
>>> conn.lrange('list-key', 0, -1)
['new last']
>>> conn.rpush('list-key', 'a', 'b', 'c')   # 可以同时推入多个元素。
4L
>>> conn.lrange('list-key', 0, -1)
['new last', 'a', 'b', 'c']
>>> conn.ltrim('list-key', 2, -1)           # 可以从列表的左端、右端或者左右两端删减任意数量的元素。
True                                        #
>>> conn.lrange('list-key', 0, -1)          #
['b', 'c']                                  #
# <end id="list-calls-1"/>
'''


# 代码清单 3-4
'''
# <start id="list-calls-2"/>
>>> conn.rpush('list', 'item1')             # 将一些元素添加到两个列表里面。
1                                           #
>>> conn.rpush('list', 'item2')             #
2                                           #
>>> conn.rpush('list2', 'item3')            #
1                                           #
>>> conn.brpoplpush('list2', 'list', 1)     # 将一个元素从一个列表移动到另一个列表，
'item3'                                     # 并返回被移动的元素。
>>> conn.brpoplpush('list2', 'list', 1)     # 当列表不包含任何元素时，阻塞弹出操作会在给定的时限内等待可弹出的元素出现，并在时限到达后返回None（交互终端不会打印这个值）。
>>> conn.lrange('list', 0, -1)              # 弹出“list2”最右端的元素，
['item3', 'item1', 'item2']                 # 并将弹出的元素推入到“list”的左端。
>>> conn.brpoplpush('list', 'list2', 1)
'item2'
>>> conn.blpop(['list', 'list2'], 1)        # BLPOP命令会从左到右地检查传入的列表，
('list', 'item3')                           # 并对最先遇到的非空列表执行弹出操作。
>>> conn.blpop(['list', 'list2'], 1)        #
('list', 'item1')                           #
>>> conn.blpop(['list', 'list2'], 1)        #
('list2', 'item2')                          #
>>> conn.blpop(['list', 'list2'], 1)        #
>>>
# <end id="list-calls-2"/>
'''

# <start id="exercise-update-token"/>
def update_token(conn, token, user, item=None):
    timestamp = time.time()
    conn.hset('login:', token, user)
    conn.zadd('recent:', token, timestamp)
    if item:
        key = 'viewed:' + token
        # 如果指定的元素存在于列表当中，那么移除它
        conn.lrem(key, item)
        # 将元素推入到列表的右端，使得 ZRANGE 和 LRANGE 可以取得相同的结果
        conn.rpush(key, item)
        # 对列表进行修剪，让它最多只能保存 25 个元素
        conn.ltrim(key, -25, -1)
    conn.zincrby('viewed:', item, -1)
# <end id="exercise-update-token"/>


# 代码清单 3-5
'''
# <start id="set-calls-1"/>
>>> conn.sadd('set-key', 'a', 'b', 'c')         # SADD命令会将那些目前并不存在于集合里面的元素添加到集合里面，
3                                               # 并返回被添加元素的数量。
>>> conn.srem('set-key', 'c', 'd')              # srem函数在元素被成功移除时返回True，
True                                            # 移除失败时返回False；
>>> conn.srem('set-key', 'c', 'd')              # 注意这是Python客户端的一个bug，
False                                           # 实际上Redis的SREM命令返回的是被移除元素的数量，而不是布尔值。
>>> conn.scard('set-key')                       # 查看集合包含的元素数量。
2                                               #
>>> conn.smembers('set-key')                    # 获取集合包含的所有元素。
set(['a', 'b'])                                 #
>>> conn.smove('set-key', 'set-key2', 'a')      # 可以很容易地将元素从一个集合移动到另一个集合。
True                                            #
>>> conn.smove('set-key', 'set-key2', 'c')      # 在执行SMOVE命令时，
False                                           # 如果用户想要移动的元素不存在于第一个集合里，
>>> conn.smembers('set-key2')                   # 那么移动操作就不会执行。
set(['a'])                                      #
# <end id="set-calls-1"/>
'''


# 代码清单 3-6
'''
# <start id="set-calls-2"/>
>>> conn.sadd('skey1', 'a', 'b', 'c', 'd')  # 首先将一些元素添加到两个集合里面。
4                                           #
>>> conn.sadd('skey2', 'c', 'd', 'e', 'f')  #
4                                           #
>>> conn.sdiff('skey1', 'skey2')            # 计算从第一个集合中移除第二个集合所有元素之后的结果。
set(['a', 'b'])                             #
>>> conn.sinter('skey1', 'skey2')           # 还可以找出同时存在于两个集合中的元素。
set(['c', 'd'])                             #
>>> conn.sunion('skey1', 'skey2')           # 可以找出两个结合中的所有元素。
set(['a', 'c', 'b', 'e', 'd', 'f'])         #
# <end id="set-calls-2"/>
'''


# 代码清单 3-7
'''
# <start id="hash-calls-1"/>
>>> conn.hmset('hash-key', {'k1':'v1', 'k2':'v2', 'k3':'v3'})   # 使用HMSET命令可以一次将多个键值对添加到散列里面。
True                                                            #
>>> conn.hmget('hash-key', ['k2', 'k3'])                        #  使用HMGET命令可以一次获取多个键的值。
['v2', 'v3']                                                    #
>>> conn.hlen('hash-key')                                       # HLEN命令通常用于调试一个包含非常多键值对的散列。
3                                                               #
>>> conn.hdel('hash-key', 'k1', 'k3')                           # HDEL命令在成功地移除了至少一个键值对时返回True，
True                                                            # 因为HDEL命令已经可以同时删除多个键值对了，所以Redis没有实现HMDEL命令。
# <end id="hash-calls-1"/>
'''


# 代码清单 3-8
'''
# <start id="hash-calls-2"/>
>>> conn.hmset('hash-key2', {'short':'hello', 'long':1000*'1'}) # 在考察散列的时候，我们可以只取出散列包含的键，而不必传输大的键值。
True                                                            #
>>> conn.hkeys('hash-key2')                                     #
['long', 'short']                                               #
>>> conn.hexists('hash-key2', 'num')                            # 检查给定的键是否存在于散列中。
False                                                           #
>>> conn.hincrby('hash-key2', 'num')                            # 和字符串一样，
1L                                                              # 对散列中一个尚未存在的键执行自增操作时，
>>> conn.hexists('hash-key2', 'num')                            # Redis会将键的值当作0来处理。
True                                                            #
# <end id="hash-calls-2"/>
'''


# 代码清单 3-9
'''
# <start id="zset-calls-1"/>
>>> conn.zadd('zset-key', 'a', 3, 'b', 2, 'c', 1)   # 在Python客户端执行ZADD命令需要先输入成员、后输入分值，
3                                                   # 这跟Redis标准的先输入分值、后输入成员的做法正好相反。
>>> conn.zcard('zset-key')                          # 取得有序集合的大小可以让我们在某些情况下知道是否需要对有序集合进行修剪。
3                                                   #
>>> conn.zincrby('zset-key', 'c', 3)                # 跟字符串和散列一样，
4.0                                                 # 有序集合的成员也可以执行自增操作。
>>> conn.zscore('zset-key', 'b')                    # 获取单个成员的分值对于实现计数器或者排行榜之类的功能非常有用。
2.0                                                 #
>>> conn.zrank('zset-key', 'c')                     # 获取指定成员的排名（排名以0为开始），
2                                                   # 之后可以根据这个排名来决定ZRANGE的访问范围。
>>> conn.zcount('zset-key', 0, 3)                   # 对于某些任务来说，
2L                                                  # 统计给定分值范围内的元素数量非常有用。
>>> conn.zrem('zset-key', 'b')                      # 从有序集合里面移除成员和添加成员一样容易。
True                                                #
>>> conn.zrange('zset-key', 0, -1, withscores=True) # 在进行调试时，我们通常会使用ZRANGE取出有序集合里包含的所有元素，
[('a', 3.0), ('c', 4.0)]                            # 但是在实际用例中，通常一次只会取出一小部分元素。
# <end id="zset-calls-1"/>
'''


# 代码清单 3-10
'''
# <start id="zset-calls-2"/>
>>> conn.zadd('zset-1', 'a', 1, 'b', 2, 'c', 3)                         # 首先创建两个有序集合。
3                                                                       #
>>> conn.zadd('zset-2', 'b', 4, 'c', 1, 'd', 0)                         #
3                                                                       #
>>> conn.zinterstore('zset-i', ['zset-1', 'zset-2'])                    # 因为ZINTERSTORE和ZUNIONSTORE默认使用的聚合函数为sum，
2L                                                                      # 所以多个有序集合里成员的分值将被加起来。
>>> conn.zrange('zset-i', 0, -1, withscores=True)                       #
[('c', 4.0), ('b', 6.0)]                                                #
>>> conn.zunionstore('zset-u', ['zset-1', 'zset-2'], aggregate='min')   # 用户可以在执行并集运算和交集运算的时候传入不同的聚合函数，
4L                                                                      # 共有 sum、min、max 三个聚合函数可选。
>>> conn.zrange('zset-u', 0, -1, withscores=True)                       #
[('d', 0.0), ('a', 1.0), ('c', 1.0), ('b', 2.0)]                        #
>>> conn.sadd('set-1', 'a', 'd')                                        # 用户还可以把集合作为输入传给ZINTERSTORE和ZUNIONSTORE，
2                                                                       # 命令会将集合看作是成员分值全为1的有序集合来处理。
>>> conn.zunionstore('zset-u2', ['zset-1', 'zset-2', 'set-1'])          #
4L                                                                      #
>>> conn.zrange('zset-u2', 0, -1, withscores=True)                      #
[('d', 1.0), ('a', 2.0), ('c', 4.0), ('b', 6.0)]                        #
# <end id="zset-calls-2"/>
'''

def publisher(n):
    time.sleep(1)
    for i in xrange(n):
        conn.publish('channel', i)
        time.sleep(1)

def run_pubsub():
    threading.Thread(target=publisher, args=(3,)).start()
    pubsub = conn.pubsub()
    pubsub.subscribe(['channel'])
    count = 0
    for item in pubsub.listen():
        print item
        count += 1
        if count == 4:
            pubsub.unsubscribe()
        if count == 5:
            break


# 代码清单 3-11
'''
# <start id="pubsub-calls-1"/>
>>> def publisher(n):
...     time.sleep(1)                                                   # 函数在刚开始执行时会先休眠，让订阅者有足够的时间来连接服务器并监听消息。
...     for i in xrange(n):
...         conn.publish('channel', i)                                  # 在发布消息之后进行短暂的休眠，
...         time.sleep(1)                                               # 让消息可以一条接一条地出现。
...
>>> def run_pubsub():
...     threading.Thread(target=publisher, args=(3,)).start()
...     pubsub = conn.pubsub()
...     pubsub.subscribe(['channel'])
...     count = 0
...     for item in pubsub.listen():
...         print item
...         count += 1
...         if count == 4:
...             pubsub.unsubscribe()
...         if count == 5:
...             break
... 

>>> def run_pubsub():
...     threading.Thread(target=publisher, args=(3,)).start()           # 启动发送者线程发送三条消息。
...     pubsub = conn.pubsub()                                          # 创建发布与订阅对象，并让它订阅给定的频道。
...     pubsub.subscribe(['channel'])                                   #
...     count = 0
...     for item in pubsub.listen():                                    # 通过遍历pubsub.listen()函数的执行结果来监听订阅消息。
...         print item                                                  # 打印接收到的每条消息。
...         count += 1                                                  # 在接收到一条订阅反馈消息和三条发布者发送的消息之后，
...         if count == 4:                                              # 执行退订操作，停止监听新消息。
...             pubsub.unsubscribe()                                    #
...         if count == 5:                                              # 当客户端接收到退订反馈消息时，
...             break                                                   # 需要停止接收消息。
...
>>> run_pubsub()                                                        # 实际运行函数并观察它们的行为。
{'pattern': None, 'type': 'subscribe', 'channel': 'channel', 'data': 1L}# 在刚开始订阅一个频道的时候，客户端会接收到一条关于被订阅频道的反馈消息。
{'pattern': None, 'type': 'message', 'channel': 'channel', 'data': '0'} # 这些结构就是我们在遍历pubsub.listen()函数时得到的元素。
{'pattern': None, 'type': 'message', 'channel': 'channel', 'data': '1'} #
{'pattern': None, 'type': 'message', 'channel': 'channel', 'data': '2'} #
{'pattern': None, 'type': 'unsubscribe', 'channel': 'channel', 'data':  # 在退订频道时，客户端会接收到一条反馈消息，
0L}                                                                     # 告知被退订的是哪个频道，以及客户端目前仍在订阅的频道数量。
# <end id="pubsub-calls-1"/>
'''


# 代码清单 3-12
'''
# <start id="sort-calls"/>
>>> conn.rpush('sort-input', 23, 15, 110, 7)                    # 首先将一些元素添加到列表里面。
4                                                               #
>>> conn.sort('sort-input')                                     # 根据数字大小对元素进行排序。
['7', '15', '23', '110']                                        #
>>> conn.sort('sort-input', alpha=True)                         # 根据字母表顺序对元素进行排序。
['110', '15', '23', '7']                                        #
>>> conn.hset('d-7', 'field', 5)                                # 添加一些用于执行排序操作和获取操作的附加数据。
1L                                                              #
>>> conn.hset('d-15', 'field', 1)                               #
1L                                                              #
>>> conn.hset('d-23', 'field', 9)                               #
1L                                                              #
>>> conn.hset('d-110', 'field', 3)                              #
1L                                                              #
>>> conn.sort('sort-input', by='d-*->field')                    # 将散列的域（field）用作权重，对sort-input列表进行排序。
['15', '110', '7', '23']                                        #
>>> conn.sort('sort-input', by='d-*->field', get='d-*->field')  # 获取外部数据作为返回值，而不返回被排序的元素。
['1', '3', '5', '9']                                            #
# <end id="sort-calls"/>
'''


# 代码清单 3-13
'''
# <start id="simple-pipeline-notrans"/>
>>> def notrans():
...     print conn.incr('notrans:')                     # 对‘notrans:’计数器执行自增操作并打印操作的执行结果。
...     time.sleep(.1)                                  # 等待100毫秒。
...     conn.incr('notrans:', -1)                       # 对‘notrans:’计数器执行自减操作。
...
>>> if 1:
...     for i in xrange(3):                             # 启动三个线程来执行没有被事务包裹的自增、休眠和自减操作。
...         threading.Thread(target=notrans).start()    #
...     time.sleep(.5)                                  # 等待500毫秒，让操作有足够的时间完成。
...
1                                                       # 因为没有使用事务，
2                                                       # 所以三个线程执行的各个命令会互相交错，
3                                                       # 使得计数器的值持续地增大。
# <end id="simple-pipeline-notrans"/>
'''


# 代码清单 3-14
'''
# <start id="simple-pipeline-trans"/>
>>> def trans():
...     pipeline = conn.pipeline()                      # 创建一个事务型（transactional）流水线对象。
...     pipeline.incr('trans:')                         # 把针对‘trans:’计数器的自增操作放入队列。
...     time.sleep(.1)                                  # 等待100毫秒。
...     pipeline.incr('trans:', -1)                     # 把针对‘trans:’计数器的自减操作放入队列。
...     print pipeline.execute()[0]                     # 执行事务包含的命令并打印自增操作的执行结果。
...
>>> if 1:
...     for i in xrange(3):                             # 启动三个线程来执行被事务包裹的自增、休眠和自减三个操作。
...         threading.Thread(target=trans).start()      #
...     time.sleep(.5)                                  # 等待500毫秒，让操作有足够的时间完成。
...
1                                                       # 因为每组自增、休眠和自减操作都在事务里面执行，
1                                                       # 所以命令之间不会互相交错，
1                                                       # 因此所有事务的执行结果都是1。
# <end id="simple-pipeline-trans"/>
'''


# <start id="exercise-fix-article-vote"/>
def article_vote(conn, user, article):
    # 在进行投票之前，先检查这篇文章是否仍然处于可投票的时间之内
    cutoff = time.time() - ONE_WEEK_IN_SECONDS
    posted = conn.zscore('time:', article)
    if posted < cutoff:
        return

    article_id = article.partition(':')[-1]
    pipeline = conn.pipeline()
    pipeline.sadd('voted:' + article_id, user)
    # 为文章的投票设置过期时间
    pipeline.expire('voted:' + article_id, int(posted-cutoff))
    if pipeline.execute()[0]:
        # 因为客户端可能会在执行 SADD/EXPIRE 之间或者执行 ZINCRBY/HINCRBY 之间掉线
        # 所以投票可能会不被计数，但这总比在执行 ZINCRBY/HINCRBY 之间失败并导致不完整的计数要好
        pipeline.zincrby('score:', article, VOTE_SCORE)
        pipeline.hincrby(article, 'votes', 1)
        pipeline.execute()
# <end id="exercise-fix-article-vote"/>

# 从技术上来将，上面的 article_vote() 函数仍然有一些问题，
# 这些问题可以通过下面展示的这段代码来解决，
# 这段代码里面用到了本书第 4 章才会介绍的技术

def article_vote(conn, user, article):
    cutoff = time.time() - ONE_WEEK_IN_SECONDS
    posted = conn.zscore('time:', article)
    article_id = article.partition(':')[-1]
    voted = 'voted:' + article_id

    pipeline = conn.pipeline()
    while posted > cutoff:
        try:
            pipeline.watch(voted)
            if not pipeline.sismember(voted, user):
                pipeline.multi()
                pipeline.sadd(voted, user)
                pipeline.expire(voted, int(posted-cutoff))
                pipeline.zincrby('score:', article, VOTE_SCORE)
                pipeline.hincrby(article, 'votes', 1)
                pipeline.execute()
            else:
                pipeline.unwatch()
            return
        except redis.exceptions.WatchError:
            cutoff = time.time() - ONE_WEEK_IN_SECONDS

# <start id="exercise-fix-get_articles"/>
def get_articles(conn, page, order='score:'):
    start = max(page-1, 0) * ARTICLES_PER_PAGE
    end = start + ARTICLES_PER_PAGE - 1

    ids = conn.zrevrangebyscore(order, start, end)

    pipeline = conn.pipeline()
    # 将等待执行的多个 HGETALL 调用放入流水线
    map(pipeline.hgetall, ids)                              #A

    articles = []
    # 执行被流水线包含的多个 HGETALL 命令，
    # 并将执行所得的多个 id 添加到 articles 变量里面
    for id, article_data in zip(ids, pipeline.execute()):   #B
        article_data['id'] = id
        articles.append(article_data)

    return articles
# <end id="exercise-fix-get_articles"/>


# 代码清单 3-15
'''
# <start id="other-calls-1"/>
>>> conn.set('key', 'value')                    # 设置一个简单的字符串值，作为过期时间的设置对象。
True                                            #
>>> conn.get('key')                             #
'value'                                         #
>>> conn.expire('key', 2)                       # 如果我们为键设置了过期时间，那么当键过期后，
True                                            # 我们再尝试去获取键时，会发现键已经被删除了。
>>> time.sleep(2)                               #
>>> conn.get('key')                             #
>>> conn.set('key', 'value2')
True
>>> conn.expire('key', 100); conn.ttl('key')    # 还可以很容易地查到键距离过期时间还有多久。
True                                            #
100                                             #
# <end id="other-calls-1"/>
'''

# <start id="exercise-no-recent-zset"/>
THIRTY_DAYS = 30*86400
def check_token(conn, token):
    # 为了能够对登录令牌进行过期，我们将把它存储为字符串值
    return conn.get('login:' + token)

def update_token(conn, token, user, item=None):
    # 在一次命令调用里面，同时为字符串键设置值和过期时间
    conn.setex('login:' + token, user, THIRTY_DAYS)
    key = 'viewed:' + token
    if item:
        conn.lrem(key, item)
        conn.rpush(key, item)
        conn.ltrim(key, -25, -1)
    # 跟字符串不一样，Redis 并没有提供能够在操作列表的同时，
    # 为列表设置过期时间的命令，
    # 所以我们需要在这里调用 EXPIRE 命令来为列表设置过期时间
    conn.expire(key, THIRTY_DAYS)
    conn.zincrby('viewed:', item, -1)

def add_to_cart(conn, session, item, count):
    key = 'cart:' + session
    if count <= 0:
        conn.hrem(key, item)
    else:
        conn.hset(key, item, count)
    # 散列也和列表一样，需要通过调用 EXPIRE 命令来设置过期时间
    conn.expire(key, THIRTY_DAYS)
# <end id="exercise-no-recent-zset"/>
