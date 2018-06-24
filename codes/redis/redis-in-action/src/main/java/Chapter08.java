import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.lang.reflect.Method;
import java.util.*;

public class Chapter08 {
    private static int HOME_TIMELINE_SIZE = 1000;
    private static int POSTS_PER_PASS = 1000;
    private static int REFILL_USERS_STEP = 50;

    public static final void main(String[] args)
        throws InterruptedException
    {
        new Chapter08().run();
    }

    public void run()
        throws InterruptedException
    {
        Jedis conn = new Jedis("localhost");
        conn.select(15);
        conn.flushDB();

        testCreateUserAndStatus(conn);
        conn.flushDB();

        testFollowUnfollowUser(conn);
        conn.flushDB();

        testSyndicateStatus(conn);
        conn.flushDB();

        testRefillTimeline(conn);
    }

    public void testCreateUserAndStatus(Jedis conn) {
        System.out.println("\n----- testCreateUserAndStatus -----");

        assert createUser(conn, "TestUser", "Test User") == 1;
        assert createUser(conn, "TestUser", "Test User2") == -1;

        assert createStatus(conn, 1, "This is a new status message") == 1;
        assert "1".equals(conn.hget("user:1", "posts"));
    }

    public void testFollowUnfollowUser(Jedis conn) {
        System.out.println("\n----- testFollowUnfollowUser -----");

        assert createUser(conn, "TestUser", "Test User") == 1;
        assert createUser(conn, "TestUser2", "Test User2") == 2;

        assert followUser(conn, 1, 2);
        assert conn.zcard("followers:2") == 1;
        assert conn.zcard("followers:1") == 0;
        assert conn.zcard("following:1") == 1;
        assert conn.zcard("following:2") == 0;
        assert "1".equals(conn.hget("user:1", "following"));
        assert "0".equals(conn.hget("user:2", "following"));
        assert "0".equals(conn.hget("user:1", "followers"));
        assert "1".equals(conn.hget("user:2", "followers"));

        assert !unfollowUser(conn, 2, 1);
        assert unfollowUser(conn, 1, 2);
        assert conn.zcard("followers:2") == 0;
        assert conn.zcard("followers:1") == 0;
        assert conn.zcard("following:1") == 0;
        assert conn.zcard("following:2") == 0;
        assert "0".equals(conn.hget("user:1", "following"));
        assert "0".equals(conn.hget("user:2", "following"));
        assert "0".equals(conn.hget("user:1", "followers"));
        assert "0".equals(conn.hget("user:2", "followers"));
    }

    public void testSyndicateStatus(Jedis conn)
        throws InterruptedException
    {
        System.out.println("\n----- testSyndicateStatus -----");

        assert createUser(conn, "TestUser", "Test User") == 1;
        assert createUser(conn, "TestUser2", "Test User2") == 2;

        assert followUser(conn, 1, 2);
        assert conn.zcard("followers:2") == 1;
        assert "1".equals(conn.hget("user:1", "following"));
        assert postStatus(conn, 2, "this is some message content") == 1;
        assert getStatusMessages(conn, 1).size() == 1;

        for(int i = 3; i < 11; i++) {
            assert createUser(conn, "TestUser" + i, "Test User" + i) == i;
            followUser(conn, i, 2);
        }

        POSTS_PER_PASS = 5;

        assert postStatus(conn, 2, "this is some other message content") == 2;
        Thread.sleep(100);
        assert getStatusMessages(conn, 9).size() == 2;

        assert unfollowUser(conn, 1, 2);
        assert getStatusMessages(conn, 1).size() == 0;
    }

    public void testRefillTimeline(Jedis conn)
        throws InterruptedException
    {
        System.out.println("\n----- testRefillTimeline -----");

        assert createUser(conn, "TestUser", "Test User") == 1;
        assert createUser(conn, "TestUser2", "Test User2") == 2;
        assert createUser(conn, "TestUser3", "Test User3") == 3;

        assert followUser(conn, 1, 2);
        assert followUser(conn, 1, 3);

        HOME_TIMELINE_SIZE = 5;

        for (int i = 0; i < 10; i++) {
            assert postStatus(conn, 2, "message") != -1;
            assert postStatus(conn, 3, "message") != -1;
            Thread.sleep(50);
        }

        assert getStatusMessages(conn, 1).size() == 5;
        assert unfollowUser(conn, 1, 2);
        assert getStatusMessages(conn, 1).size() < 5;

        refillTimeline(conn, "following:1", "home:1");
        List<Map<String,String>> messages = getStatusMessages(conn, 1);
        assert messages.size() == 5;
        for (Map<String,String> message : messages) {
            assert "3".equals(message.get("uid"));
        }

        long statusId = Long.valueOf(messages.get(messages.size() -1).get("id"));
        assert deleteStatus(conn, 3, statusId);
        assert getStatusMessages(conn, 1).size() == 4;
        assert conn.zcard("home:1") == 5;
        cleanTimelines(conn, 3, statusId);
        assert conn.zcard("home:1") == 4;
    }

    public String acquireLockWithTimeout(
        Jedis conn, String lockName, int acquireTimeout, int lockTimeout)
    {
        String id = UUID.randomUUID().toString();
        lockName = "lock:" + lockName;

        long end = System.currentTimeMillis() + (acquireTimeout * 1000);
        while (System.currentTimeMillis() < end) {
            if (conn.setnx(lockName, id) >= 1) {
                conn.expire(lockName, lockTimeout);
                return id;
            }else if (conn.ttl(lockName) <= 0){
                conn.expire(lockName, lockTimeout);
            }

            try{
                Thread.sleep(1);
            }catch(InterruptedException ie){
                Thread.interrupted();
            }
        }

        return null;
    }

    public boolean releaseLock(Jedis conn, String lockName, String identifier) {
        lockName = "lock:" + lockName;
        while (true) {
            conn.watch(lockName);
            if (identifier.equals(conn.get(lockName))) {
                Transaction trans = conn.multi();
                trans.del(lockName);
                List<Object> result = trans.exec();
                // null response indicates that the transaction was aborted due
                // to the watched key changing.
                if (result == null){
                    continue;
                }
                return true;
            }

            conn.unwatch();
            break;
        }

        return false;
    }

    public long createUser(Jedis conn, String login, String name) {
        String llogin = login.toLowerCase();
        String lock = acquireLockWithTimeout(conn, "user:" + llogin, 10, 1);
        if (lock == null){
            return -1;
        }

        if (conn.hget("users:", llogin) != null) {
            return -1;
        }

        long id = conn.incr("user:id:");
        Transaction trans = conn.multi();
        trans.hset("users:", llogin, String.valueOf(id));
        Map<String,String> values = new HashMap<String,String>();
        values.put("login", login);
        values.put("id", String.valueOf(id));
        values.put("name", name);
        values.put("followers", "0");
        values.put("following", "0");
        values.put("posts", "0");
        values.put("signup", String.valueOf(System.currentTimeMillis()));
        trans.hmset("user:" + id, values);
        trans.exec();
        releaseLock(conn, "user:" + llogin, lock);
        return id;
    }

    @SuppressWarnings("unchecked")
    public boolean followUser(Jedis conn, long uid, long otherUid) {
        String fkey1 = "following:" + uid;
        String fkey2 = "followers:" + otherUid;

        if (conn.zscore(fkey1, String.valueOf(otherUid)) != null) {
            return false;
        }

        long now = System.currentTimeMillis();

        Transaction trans = conn.multi();
        trans.zadd(fkey1, now, String.valueOf(otherUid));
        trans.zadd(fkey2, now, String.valueOf(uid));
        trans.zcard(fkey1);
        trans.zcard(fkey2);
        trans.zrevrangeWithScores("profile:" + otherUid, 0, HOME_TIMELINE_SIZE - 1);

        List<Object> response = trans.exec();
        long following = (Long)response.get(response.size() - 3);
        long followers = (Long)response.get(response.size() - 2);
        Set<Tuple> statuses = (Set<Tuple>)response.get(response.size() - 1);

        trans = conn.multi();
        trans.hset("user:" + uid, "following", String.valueOf(following));
        trans.hset("user:" + otherUid, "followers", String.valueOf(followers));
        if (statuses.size() > 0) {
            for (Tuple status : statuses){
                trans.zadd("home:" + uid, status.getScore(), status.getElement());
            }
        }
        trans.zremrangeByRank("home:" + uid, 0, 0 - HOME_TIMELINE_SIZE - 1);
        trans.exec();

        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean unfollowUser(Jedis conn, long uid, long otherUid) {
        String fkey1 = "following:" + uid;
        String fkey2 = "followers:" + otherUid;

        if (conn.zscore(fkey1, String.valueOf(otherUid)) == null) {
            return false;
        }

        Transaction trans = conn.multi();
        trans.zrem(fkey1, String.valueOf(otherUid));
        trans.zrem(fkey2, String.valueOf(uid));
        trans.zcard(fkey1);
        trans.zcard(fkey2);
        trans.zrevrange("profile:" + otherUid, 0, HOME_TIMELINE_SIZE - 1);

        List<Object> response = trans.exec();
        long following = (Long)response.get(response.size() - 3);
        long followers = (Long)response.get(response.size() - 2);
        Set<String> statuses = (Set<String>)response.get(response.size() - 1);

        trans = conn.multi();
        trans.hset("user:" + uid, "following", String.valueOf(following));
        trans.hset("user:" + otherUid, "followers", String.valueOf(followers));
        if (statuses.size() > 0){
            for (String status : statuses) {
                trans.zrem("home:" + uid, status);
            }
        }

        trans.exec();
        return true;
    }

    public long createStatus(Jedis conn, long uid, String message) {
        return createStatus(conn, uid, message, null);
    }
    public long createStatus(
        Jedis conn, long uid, String message, Map<String,String> data)
    {
        Transaction trans = conn.multi();
        trans.hget("user:" + uid, "login");
        trans.incr("status:id:");

        List<Object> response = trans.exec();
        String login = (String)response.get(0);
        long id = (Long)response.get(1);

        if (login == null) {
            return -1;
        }

        if (data == null){
            data = new HashMap<String,String>();
        }
        data.put("message", message);
        data.put("posted", String.valueOf(System.currentTimeMillis()));
        data.put("id", String.valueOf(id));
        data.put("uid", String.valueOf(uid));
        data.put("login", login);

        trans = conn.multi();
        trans.hmset("status:" + id, data);
        trans.hincrBy("user:" + uid, "posts", 1);
        trans.exec();
        return id;
    }

    public long postStatus(Jedis conn, long uid, String message) {
        return postStatus(conn, uid, message, null);
    }
    public long postStatus(
        Jedis conn, long uid, String message, Map<String,String> data)
    {
        long id = createStatus(conn, uid, message, data);
        if (id == -1){
            return -1;
        }

        String postedString = conn.hget("status:" + id, "posted");
        if (postedString == null) {
            return -1;
        }

        long posted = Long.parseLong(postedString);
        conn.zadd("profile:" + uid, posted, String.valueOf(id));

        syndicateStatus(conn, uid, id, posted, 0);
        return id;
    }

    public void syndicateStatus(
        Jedis conn, long uid, long postId, long postTime, double start)
    {
        Set<Tuple> followers = conn.zrangeByScoreWithScores(
            "followers:" + uid,
            String.valueOf(start), "inf",
            0, POSTS_PER_PASS);

        Transaction trans = conn.multi();
        for (Tuple tuple : followers){
            String follower = tuple.getElement();
            start = tuple.getScore();
            trans.zadd("home:" + follower, postTime, String.valueOf(postId));
            trans.zrange("home:" + follower, 0, -1);
            trans.zremrangeByRank(
                "home:" + follower, 0, 0 - HOME_TIMELINE_SIZE - 1);
        }
        trans.exec();

        if (followers.size() >= POSTS_PER_PASS) {
            try{
                Method method = getClass().getDeclaredMethod(
                    "syndicateStatus", Jedis.class, Long.TYPE, Long.TYPE, Long.TYPE, Double.TYPE);
                executeLater("default", method, uid, postId, postTime, start);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public boolean deleteStatus(Jedis conn, long uid, long statusId) {
        String key = "status:" + statusId;
        String lock = acquireLockWithTimeout(conn, key, 1, 10);
        if (lock == null) {
            return false;
        }

        try{
            if (!String.valueOf(uid).equals(conn.hget(key, "uid"))) {
                return false;
            }

            Transaction trans = conn.multi();
            trans.del(key);
            trans.zrem("profile:" + uid, String.valueOf(statusId));
            trans.zrem("home:" + uid, String.valueOf(statusId));
            trans.hincrBy("user:" + uid, "posts", -1);
            trans.exec();

            return true;
        }finally{
            releaseLock(conn, key, lock);
        }
    }

    public List<Map<String,String>> getStatusMessages(Jedis conn, long uid) {
        return getStatusMessages(conn, uid, 1, 30);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,String>> getStatusMessages(
        Jedis conn, long uid, int page, int count)
    {
        Set<String> statusIds = conn.zrevrange(
            "home:" + uid, (page - 1) * count, page * count - 1);

        Transaction trans = conn.multi();
        for (String id : statusIds) {
            trans.hgetAll("status:" + id);
        }

        List<Map<String,String>> statuses = new ArrayList<Map<String,String>>();
        for (Object result : trans.exec()) {
            Map<String,String> status = (Map<String,String>)result;
            if (status != null && status.size() > 0){
                statuses.add(status);
            }
        }
        return statuses;
    }

    public void refillTimeline(Jedis conn, String incoming, String timeline) {
        refillTimeline(conn, incoming, timeline, 0);
    }

    @SuppressWarnings("unchecked")
    public void refillTimeline(
            Jedis conn, String incoming, String timeline, double start)
    {
        if (start == 0 && conn.zcard(timeline) >= 750) {
            return;
        }

        Set<Tuple> users = conn.zrangeByScoreWithScores(
            incoming, String.valueOf(start), "inf", 0, REFILL_USERS_STEP);

        Pipeline pipeline = conn.pipelined();
        for (Tuple tuple : users){
            String uid = tuple.getElement();
            start = tuple.getScore();
            pipeline.zrevrangeWithScores(
                "profile:" + uid, 0, HOME_TIMELINE_SIZE - 1);
        }

        List<Object> response = pipeline.syncAndReturnAll();
        List<Tuple> messages = new ArrayList<Tuple>();
        for (Object results : response) {
            messages.addAll((Set<Tuple>)results);
        }

        Collections.sort(messages);
        messages = messages.subList(0, HOME_TIMELINE_SIZE);

        Transaction trans = conn.multi();
        if (messages.size() > 0) {
            for (Tuple tuple : messages) {
                trans.zadd(timeline, tuple.getScore(), tuple.getElement());
            }
        }
        trans.zremrangeByRank(timeline, 0, 0 - HOME_TIMELINE_SIZE - 1);
        trans.exec();

        if (users.size() >= REFILL_USERS_STEP) {
            try{
                Method method = getClass().getDeclaredMethod(
                    "refillTimeline", Jedis.class, String.class, String.class, Double.TYPE);
                executeLater("default", method, incoming, timeline, start);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public void cleanTimelines(Jedis conn, long uid, long statusId) {
        cleanTimelines(conn, uid, statusId, 0, false);
    }
    public void cleanTimelines(
        Jedis conn, long uid, long statusId, double start, boolean onLists)
    {
        String key = "followers:" + uid;
        String base = "home:";
        if (onLists) {
            key = "list:out:" + uid;
            base = "list:statuses:";
        }
        Set<Tuple> followers = conn.zrangeByScoreWithScores(
            key, String.valueOf(start), "inf", 0, POSTS_PER_PASS);

        Transaction trans = conn.multi();
        for (Tuple tuple : followers) {
            start = tuple.getScore();
            String follower = tuple.getElement();
            trans.zrem(base + follower, String.valueOf(statusId));
        }
        trans.exec();

        Method method = null;
        try{
            method = getClass().getDeclaredMethod(
                "cleanTimelines", Jedis.class,
                Long.TYPE, Long.TYPE, Double.TYPE, Boolean.TYPE);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

        if (followers.size() >= POSTS_PER_PASS) {
            executeLater("default", method, uid, statusId, start, onLists);

        }else if (!onLists) {
            executeLater("default", method, uid, statusId, 0, true);
        }
    }

    public void executeLater(String queue, Method method, Object... args) {
        MethodThread thread = new MethodThread(this, method, args);
        thread.start();
    }

    public class MethodThread
        extends Thread
    {
        private Object instance;
        private Method method;
        private Object[] args;

        public MethodThread(Object instance, Method method, Object... args) {
            this.instance = instance;
            this.method = method;
            this.args = args;
        }

        public void run() {
            Jedis conn = new Jedis("localhost");
            conn.select(15);

            Object[] args = new Object[this.args.length + 1];
            System.arraycopy(this.args, 0, args, 1, this.args.length);
            args[0] = conn;

            try{
                method.invoke(instance, args);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
