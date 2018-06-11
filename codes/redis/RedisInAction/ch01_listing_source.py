# coding: utf-8

import time
import unittest


# 代码清单 1-1
'''
$ redis-cli                                 # 启动redis-cli 客户端
redis 127.0.0.1:6379> set hello world       # 将键 hello 的值设置为 world 。
OK                                          # SET 命令在执行成功时返回 OK ，Python 客户端会将这个 OK 转换成 True
redis 127.0.0.1:6379> get hello             # 获取储存在键 hello 中的值。
"world"                                     # 键的值仍然是 world ，跟我们刚才设置的一样。
redis 127.0.0.1:6379> del hello             # 删除这个键值对。
(integer) 1                                 # 在对值进行删除的时候，DEL 命令将返回被成功删除的值的数量。
redis 127.0.0.1:6379> get hello             # 因为键的值已经不存在，所以尝试获取键的值将得到一个 nil ，
(nil)                                       # Python 客户端会将这个 nil 转换成 None。
redis 127.0.0.1:6379> 
'''


# 代码清单 1-2
'''
redis 127.0.0.1:6379> rpush list-key item   # 在向列表推入新元素之后，该命令会返回列表的当前长度。
(integer) 1                                 #
redis 127.0.0.1:6379> rpush list-key item2  #
(integer) 2                                 #
redis 127.0.0.1:6379> rpush list-key item   #
(integer) 3                                 #
redis 127.0.0.1:6379> lrange list-key 0 -1  # 使用0为范围的起始索引，-1为范围的结束索引，
1) "item"                                   # 可以取出列表包含的所有元素。
2) "item2"                                  #
3) "item"                                   #
redis 127.0.0.1:6379> lindex list-key 1     # 使用LINDEX可以从列表里面取出单个元素。
"item2"                                     #
redis 127.0.0.1:6379> lpop list-key         # 从列表里面弹出一个元素，被弹出的元素不再存在于列表。
"item"                                      #
redis 127.0.0.1:6379> lrange list-key 0 -1  #
1) "item2"                                  #
2) "item"                                   #
redis 127.0.0.1:6379> 
'''


# 代码清单 1-3
'''
redis 127.0.0.1:6379> sadd set-key item     # 在尝试将一个元素添加到集合的时候，
(integer) 1                                 # 命令返回1表示这个元素被成功地添加到了集合里面，
redis 127.0.0.1:6379> sadd set-key item2    # 而返回0则表示这个元素已经存在于集合中。
(integer) 1                                 #
redis 127.0.0.1:6379> sadd set-key item3    #
(integer) 1                                 #
redis 127.0.0.1:6379> sadd set-key item     #
(integer) 0                                 #
redis 127.0.0.1:6379> smembers set-key      # 获取集合包含的所有元素将得到一个由元素组成的序列，
1) "item"                                   # Python客户端会将这个序列转换成Python集合。
2) "item2"                                  #
3) "item3"                                  #
redis 127.0.0.1:6379> sismember set-key item4   # 检查一个元素是否存在于集合中，
(integer) 0                                     # Python客户端会返回一个布尔值来表示检查结果。
redis 127.0.0.1:6379> sismember set-key item    #
(integer) 1                                     #
redis 127.0.0.1:6379> srem set-key item2    # 在使用命令移除集合中的元素时，命令会返回被移除的元素数量。
(integer) 1                                 #
redis 127.0.0.1:6379> srem set-key item2    #
(integer) 0                                 #
redis 127.0.0.1:6379>  smembers set-key
1) "item"
2) "item3"
redis 127.0.0.1:6379> 
'''


# 代码清单 1-4
'''
redis 127.0.0.1:6379> hset hash-key sub-key1 value1 # 在尝试添加键值对到散列的时候，
(integer) 1                                         # 命令会返回一个值来表示给定的键是否已经存在于散列里面。
redis 127.0.0.1:6379> hset hash-key sub-key2 value2 #
(integer) 1                                         #
redis 127.0.0.1:6379> hset hash-key sub-key1 value1 #
(integer) 0                                         #
redis 127.0.0.1:6379> hgetall hash-key              # 获取散列包含的所有键值对，
1) "sub-key1"                                       # Python客户端会将这些键值对转换为Python字典。
2) "value1"                                         #
3) "sub-key2"                                       #
4) "value2"                                         #
redis 127.0.0.1:6379> hdel hash-key sub-key2        # 在删除键值对的时候，
(integer) 1                                         # 命令会返回一个值来表示给定的键在移除之前是否存在于散列里面。
redis 127.0.0.1:6379> hdel hash-key sub-key2        #
(integer) 0                                         #
redis 127.0.0.1:6379> hget hash-key sub-key1        # 从散列里面单独取出一个域。
"value1"                                            #
redis 127.0.0.1:6379> hgetall hash-key
1) "sub-key1"
2) "value1"
'''


# 代码清单 1-5 
'''
redis 127.0.0.1:6379> zadd zset-key 728 member1     # 在尝试向有序集合添加元素的时候，
(integer) 1                                         # 命令会返回新添加元素的数量。
redis 127.0.0.1:6379> zadd zset-key 982 member0     #
(integer) 1                                         #
redis 127.0.0.1:6379> zadd zset-key 982 member0     #
(integer) 0                                         #
redis 127.0.0.1:6379> zrange zset-key 0 -1 withscores   # 获取有序集合包含的所有元素，
1) "member1"                                            # 这些元素会按照分值进行排序，
2) "728"                                                # Python客户端会将这些分值转换成浮点数。
3) "member0"                                            #
4) "982"                                                #
redis 127.0.0.1:6379> zrangebyscore zset-key 0 800 withscores   # 也可以根据分值来获取有序集合的其中一部分元素。
1) "member1"                                                    #
2) "728"                                                        #
redis 127.0.0.1:6379> zrem zset-key member1     # 在移除有序集合元素的时候，
(integer) 1                                     # 命令会返回被移除元素的数量。
redis 127.0.0.1:6379> zrem zset-key member1     #
(integer) 0                                     #
redis 127.0.0.1:6379> zrange zset-key 0 -1 withscores
1) "member0"
2) "982"
'''


# 代码清单 1-6
# <start id="upvote-code"/>
# 准备好需要用到的常量。
ONE_WEEK_IN_SECONDS = 7 * 86400
VOTE_SCORE = 432

def article_vote(conn, user, article):

    # 计算文章的投票截止时间。
    cutoff = time.time() - ONE_WEEK_IN_SECONDS

    # 检查是否还可以对文章进行投票
    #（虽然使用散列也可以获取文章的发布时间，
    # 但有序集合返回的文章发布时间为浮点数，
    # 可以不进行转换直接使用）。
    if conn.zscore('time:', article) < cutoff:
        return

    # 从article:id标识符（identifier）里面取出文章的ID。
    article_id = article.partition(':')[-1]

    # 如果用户是第一次为这篇文章投票，那么增加这篇文章的投票数量和评分。
    if conn.sadd('voted:' + article_id, user):
        conn.zincrby('score:', article, VOTE_SCORE)
        conn.hincrby(article, 'votes', 1)
# <end id="upvote-code"/>


# 代码清单 1-7
# <start id="post-article-code"/>
def post_article(conn, user, title, link):
    # 生成一个新的文章ID。
    article_id = str(conn.incr('article:'))

    voted = 'voted:' + article_id
    # 将发布文章的用户添加到文章的已投票用户名单里面，
    # 然后将这个名单的过期时间设置为一周（第3章将对过期时间作更详细的介绍）。
    conn.sadd(voted, user)
    conn.expire(voted, ONE_WEEK_IN_SECONDS)

    now = time.time()
    article = 'article:' + article_id
    # 将文章信息存储到一个散列里面。
    conn.hmset(article, {
        'title': title,
        'link': link,
        'poster': user,
        'time': now,
        'votes': 1,
    })

    # 将文章添加到根据发布时间排序的有序集合和根据评分排序的有序集合里面。
    conn.zadd('score:', article, now + VOTE_SCORE)
    conn.zadd('time:', article, now) 

    return article_id
# <end id="post-article-code"/>


# 代码清单 1-8
# <start id="fetch-articles-code"/>
ARTICLES_PER_PAGE = 25

def get_articles(conn, page, order='score:'):
    # 设置获取文章的起始索引和结束索引。
    start = (page-1) * ARTICLES_PER_PAGE
    end = start + ARTICLES_PER_PAGE - 1

    # 获取多个文章ID。
    ids = conn.zrevrange(order, start, end)
    articles = []
    # 根据文章ID获取文章的详细信息。
    for id in ids:
        article_data = conn.hgetall(id)
        article_data['id'] = id
        articles.append(article_data)

    return articles
# <end id="fetch-articles-code"/>


# 代码清单 1-9
# <start id="add-remove-groups"/>
def add_remove_groups(conn, article_id, to_add=[], to_remove=[]):
    # 构建存储文章信息的键名。
    article = 'article:' + article_id
    for group in to_add:
        # 将文章添加到它所属的群组里面。
        conn.sadd('group:' + group, article)
    for group in to_remove:
        # 从群组里面移除文章。
        conn.srem('group:' + group, article)
# <end id="add-remove-groups"/>


# 代码清单 1-10
# <start id="fetch-articles-group"/>
def get_group_articles(conn, group, page, order='score:'):
    # 为每个群组的每种排列顺序都创建一个键。
    key = order + group
    # 检查是否有已缓存的排序结果，如果没有的话就现在进行排序。
    if not conn.exists(key): 
        # 根据评分或者发布时间，对群组文章进行排序。
        conn.zinterstore(key,
            ['group:' + group, order],
            aggregate='max',
        )
        # 让Redis在60秒钟之后自动删除这个有序集合。
        conn.expire(key, 60)
    # 调用之前定义的get_articles()函数来进行分页并获取文章数据。
    return get_articles(conn, page, key)
# <end id="fetch-articles-group"/>

#--------------- 以下是用于测试代码的辅助函数 --------------------------------

class TestCh01(unittest.TestCase):
    def setUp(self):
        import redis
        self.conn = redis.Redis(db=15)

    def tearDown(self):
        del self.conn
        print
        print

    def test_article_functionality(self):
        conn = self.conn
        import pprint

        article_id = str(post_article(conn, 'username', 'A title', 'http://www.google.com'))
        print "We posted a new article with id:", article_id
        print
        self.assertTrue(article_id)

        print "Its HASH looks like:"
        r = conn.hgetall('article:' + article_id)
        print r
        print
        self.assertTrue(r)

        article_vote(conn, 'other_user', 'article:' + article_id)
        print "We voted for the article, it now has votes:",
        v = int(conn.hget('article:' + article_id, 'votes'))
        print v
        print
        self.assertTrue(v > 1)

        print "The currently highest-scoring articles are:"
        articles = get_articles(conn, 1)
        pprint.pprint(articles)
        print

        self.assertTrue(len(articles) >= 1)

        add_remove_groups(conn, article_id, ['new-group'])
        print "We added the article to a new group, other articles include:"
        articles = get_group_articles(conn, 'new-group', 1)
        pprint.pprint(articles)
        print
        self.assertTrue(len(articles) >= 1)

        to_del = (
            conn.keys('time:*') + conn.keys('voted:*') + conn.keys('score:*') + 
            conn.keys('article:*') + conn.keys('group:*')
        )
        if to_del:
            conn.delete(*to_del)

if __name__ == '__main__':
    unittest.main()
