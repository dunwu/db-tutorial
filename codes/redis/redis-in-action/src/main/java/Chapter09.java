import org.javatuples.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ZParams;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;

public class Chapter09 {
    private static final String[] COUNTRIES = (
        "ABW AFG AGO AIA ALA ALB AND ARE ARG ARM ASM ATA ATF ATG AUS AUT AZE BDI " +
        "BEL BEN BES BFA BGD BGR BHR BHS BIH BLM BLR BLZ BMU BOL BRA BRB BRN BTN " +
        "BVT BWA CAF CAN CCK CHE CHL CHN CIV CMR COD COG COK COL COM CPV CRI CUB " +
        "CUW CXR CYM CYP CZE DEU DJI DMA DNK DOM DZA ECU EGY ERI ESH ESP EST ETH " +
        "FIN FJI FLK FRA FRO FSM GAB GBR GEO GGY GHA GIB GIN GLP GMB GNB GNQ GRC " +
        "GRD GRL GTM GUF GUM GUY HKG HMD HND HRV HTI HUN IDN IMN IND IOT IRL IRN " +
        "IRQ ISL ISR ITA JAM JEY JOR JPN KAZ KEN KGZ KHM KIR KNA KOR KWT LAO LBN " +
        "LBR LBY LCA LIE LKA LSO LTU LUX LVA MAC MAF MAR MCO MDA MDG MDV MEX MHL " +
        "MKD MLI MLT MMR MNE MNG MNP MOZ MRT MSR MTQ MUS MWI MYS MYT NAM NCL NER " +
        "NFK NGA NIC NIU NLD NOR NPL NRU NZL OMN PAK PAN PCN PER PHL PLW PNG POL " +
        "PRI PRK PRT PRY PSE PYF QAT REU ROU RUS RWA SAU SDN SEN SGP SGS SHN SJM " +
        "SLB SLE SLV SMR SOM SPM SRB SSD STP SUR SVK SVN SWE SWZ SXM SYC SYR TCA " +
        "TCD TGO THA TJK TKL TKM TLS TON TTO TUN TUR TUV TWN TZA UGA UKR UMI URY " +
        "USA UZB VAT VCT VEN VGB VIR VNM VUT WLF WSM YEM ZAF ZMB ZWE").split(" ");

    private static final Map<String,String[]> STATES = new HashMap<String,String[]>();
    static {
        STATES.put("CAN", "AB BC MB NB NL NS NT NU ON PE QC SK YT".split(" "));
        STATES.put("USA", (
            "AA AE AK AL AP AR AS AZ CA CO CT DC DE FL FM GA GU HI IA ID IL IN " +
            "KS KY LA MA MD ME MH MI MN MO MP MS MT NC ND NE NH NJ NM NV NY OH " +
            "OK OR PA PR PW RI SC SD TN TX UT VA VI VT WA WI WV WY").split(" "));
    }

    private static final SimpleDateFormat ISO_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:00:00");
    static{
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final void main(String[] args) {
        new Chapter09().run();
    }

    public void run() {
        Jedis conn = new Jedis("localhost");
        conn.select(15);
        conn.flushDB();

        testLongZiplistPerformance(conn);
        testShardKey(conn);
        testShardedHash(conn);
        testShardedSadd(conn);
        testUniqueVisitors(conn);
        testUserLocation(conn);
    }

    public void testLongZiplistPerformance(Jedis conn) {
        System.out.println("\n----- testLongZiplistPerformance -----");

        longZiplistPerformance(conn, "test", 5, 10, 10);
        assert conn.llen("test") == 5;
    }

    public void testShardKey(Jedis conn) {
        System.out.println("\n----- testShardKey -----");

        String base = "test";
        assert "test:0".equals(shardKey(base, "1", 2, 2));
        assert "test:1".equals(shardKey(base, "125", 1000, 100));

        for (int i = 0; i < 50; i++) {
            String key = shardKey(base, "hello:" + i, 1000, 100);
            String[] parts = key.split(":");
            assert Integer.parseInt(parts[parts.length - 1]) < 20;

            key = shardKey(base, String.valueOf(i), 1000, 100);
            parts = key.split(":");
            assert Integer.parseInt(parts[parts.length - 1]) < 10;
        }
    }

    public void testShardedHash(Jedis conn) {
        System.out.println("\n----- testShardedHash -----");

        for (int i = 0; i < 50; i++) {
            String istr = String.valueOf(i);
            shardHset(conn, "test", "keyname:" + i, istr, 1000, 100);
            assert istr.equals(shardHget(conn, "test", "keyname:" + i, 1000, 100));
            shardHset(conn, "test2", istr, istr, 1000, 100);
            assert istr.equals(shardHget(conn, "test2", istr, 1000, 100));
        }
    }

    public void testShardedSadd(Jedis conn) {
        System.out.println("\n----- testShardedSadd -----");

        for (int i = 0; i < 50; i++) {
            shardSadd(conn, "testx", String.valueOf(i), 50, 50);
        }
        assert conn.scard("testx:0") + conn.scard("testx:1") == 50;
    }

    public void testUniqueVisitors(Jedis conn) {
        System.out.println("\n----- testUniqueVisitors -----");

        DAILY_EXPECTED = 10000;

        for (int i = 0; i < 179; i++) {
            countVisit(conn, UUID.randomUUID().toString());
        }
        assert "179".equals(conn.get("unique:" + ISO_FORMAT.format(new Date())));

        conn.flushDB();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        conn.set("unique:" + ISO_FORMAT.format(yesterday.getTime()), "1000");
        for (int i = 0; i < 183; i++) {
            countVisit(conn, UUID.randomUUID().toString());
        }
        assert "183".equals(conn.get("unique:" + ISO_FORMAT.format(new Date())));
    }

    public void testUserLocation(Jedis conn) {
        System.out.println("\n----- testUserLocation -----");

        int i = 0;
        for (String country : COUNTRIES) {
            if (STATES.containsKey(country)){
                for (String state : STATES.get(country)) {
                    setLocation(conn, i, country, state);
                    i++;
                }
            }else{
                setLocation(conn, i, country, "");
                i++;
            }
        }

        Pair<Map<String,Long>,Map<String,Map<String,Long>>> _aggs =
            aggregateLocation(conn);

        long[] userIds = new long[i + 1];
        for (int j = 0; j <= i; j++) {
            userIds[j] = j;
        }
        Pair<Map<String,Long>,Map<String,Map<String,Long>>> aggs =
            aggregateLocationList(conn, userIds);

        assert _aggs.equals(aggs);

        Map<String,Long> countries = aggs.getValue0();
        Map<String,Map<String,Long>> states = aggs.getValue1();
        for (String country : aggs.getValue0().keySet()){
            if (STATES.containsKey(country)) {
                assert STATES.get(country).length == countries.get(country);
                for (String state : STATES.get(country)){
                    assert states.get(country).get(state) == 1;
                }
            }else{
                assert countries.get(country) == 1;
            }
        }
    }

    public double longZiplistPerformance(
            Jedis conn, String key, int length, int passes, int psize)
    {
        conn.del(key);
        for (int i = 0; i < length; i++) {
            conn.rpush(key, String.valueOf(i));
        }

        Pipeline pipeline = conn.pipelined();
        long time = System.currentTimeMillis();
        for (int p = 0; p < passes; p++) {
            for (int pi = 0; pi < psize; pi++) {
                pipeline.rpoplpush(key, key);
            }
            pipeline.sync();
        }

        return (passes * psize) / (System.currentTimeMillis() - time);
    }

    public String shardKey(String base, String key, long totalElements, int shardSize) {
        long shardId = 0;
        if (isDigit(key)) {
            shardId = Integer.parseInt(key, 10) / shardSize;
        }else{
            CRC32 crc = new CRC32();
            crc.update(key.getBytes());
            long shards = 2 * totalElements / shardSize;
            shardId = Math.abs(((int)crc.getValue()) % shards);
        }
        return base + ':' + shardId;
    }

    public Long shardHset(
        Jedis conn, String base, String key, String value, long totalElements, int shardSize)
    {
        String shard = shardKey(base, key, totalElements, shardSize);
        return conn.hset(shard, key, value);
    }

    public String shardHget(
        Jedis conn, String base, String key, int totalElements, int shardSize)
    {
        String shard = shardKey(base, key, totalElements, shardSize);
        return conn.hget(shard, key);
    }

    public Long shardSadd(
        Jedis conn, String base, String member, long totalElements, int shardSize)
    {
        String shard = shardKey(base, "x" + member, totalElements, shardSize);
        return conn.sadd(shard, member);
    }

    private int SHARD_SIZE = 512;
    public void countVisit(Jedis conn, String sessionId) {
        Calendar today = Calendar.getInstance();
        String key = "unique:" + ISO_FORMAT.format(today.getTime());
        long expected = getExpected(conn, key, today);
        long id = Long.parseLong(sessionId.replace("-", "").substring(0, 15), 16);
        if (shardSadd(conn, key, String.valueOf(id), expected, SHARD_SIZE) != 0) {
            conn.incr(key);
        }
    }

    private long DAILY_EXPECTED = 1000000;
    private Map<String,Long> EXPECTED = new HashMap<String,Long>();

    public long getExpected(Jedis conn, String key, Calendar today) {
        if (!EXPECTED.containsKey(key)) {
            String exkey = key + ":expected";
            String expectedStr = conn.get(exkey);

            long expected = 0;
            if (expectedStr == null) {
                Calendar yesterday = (Calendar)today.clone();
                yesterday.add(Calendar.DATE, -1);
                expectedStr = conn.get(
                    "unique:" + ISO_FORMAT.format(yesterday.getTime()));
                expected = expectedStr != null ? Long.parseLong(expectedStr) : DAILY_EXPECTED;

                expected = (long)Math.pow(2, (long)(Math.ceil(Math.log(expected * 1.5) / Math.log(2))));
                if (conn.setnx(exkey, String.valueOf(expected)) == 0) {
                    expectedStr = conn.get(exkey);
                    expected = Integer.parseInt(expectedStr);
                }
            }else{
                expected = Long.parseLong(expectedStr);
            }

            EXPECTED.put(key, expected);
        }

        return EXPECTED.get(key);
    }

    private long USERS_PER_SHARD = (long)Math.pow(2, 20);

    public void setLocation(
        Jedis conn, long userId, String country, String state)
    {
        String code = getCode(country, state);

        long shardId = userId / USERS_PER_SHARD;
        int position = (int)(userId % USERS_PER_SHARD);
        int offset = position * 2;

        Pipeline pipe = conn.pipelined();
        pipe.setrange("location:" + shardId, offset, code);

        String tkey = UUID.randomUUID().toString();
        pipe.zadd(tkey, userId, "max");
        pipe.zunionstore(
            "location:max",
            new ZParams().aggregate(ZParams.Aggregate.MAX),
            tkey,
            "location:max");
        pipe.del(tkey);
        pipe.sync();
    }

    public Pair<Map<String,Long>,Map<String,Map<String,Long>>> aggregateLocation(Jedis conn) {
        Map<String,Long> countries = new HashMap<String,Long>();
        Map<String,Map<String,Long>> states = new HashMap<String,Map<String,Long>>();

        long maxId = conn.zscore("location:max", "max").longValue();
        long maxBlock = maxId;

        byte[] buffer = new byte[(int)Math.pow(2, 17)];
        for (int shardId = 0; shardId <= maxBlock; shardId++) {
            InputStream in = new RedisInputStream(conn, "location:" + shardId);
            try{
                int read = 0;
                while ((read = in.read(buffer, 0, buffer.length)) != -1){
                    for (int offset = 0; offset < read - 1; offset += 2) {
                        String code = new String(buffer, offset, 2);
                        updateAggregates(countries, states, code);
                    }
                }
            }catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }finally{
                try{
                    in.close();
                }catch(Exception e){
                    // ignore
                }
            }
        }

        return new Pair<Map<String,Long>,Map<String,Map<String,Long>>>(countries, states);
    }

    public Pair<Map<String,Long>,Map<String,Map<String,Long>>> aggregateLocationList(
        Jedis conn, long[] userIds)
    {
        Map<String,Long> countries = new HashMap<String,Long>();
        Map<String,Map<String,Long>> states = new HashMap<String,Map<String,Long>>();

        Pipeline pipe = conn.pipelined();
        for (int i = 0; i < userIds.length; i++) {
            long userId = userIds[i];
            long shardId = userId / USERS_PER_SHARD;
            int position = (int)(userId % USERS_PER_SHARD);
            int offset = position * 2;

            pipe.substr("location:" + shardId, offset, offset + 1);

            if ((i + 1) % 1000 == 0) {
                updateAggregates(countries, states, pipe.syncAndReturnAll());
            }
        }

        updateAggregates(countries, states, pipe.syncAndReturnAll());

        return new Pair<Map<String,Long>,Map<String,Map<String,Long>>>(countries, states);
    }

    public void updateAggregates(
        Map<String,Long> countries, Map<String,Map<String,Long>> states, List<Object> codes)
    {
        for (Object code : codes) {
            updateAggregates(countries, states, (String)code);
        }
    }

    public void updateAggregates(
        Map<String,Long> countries, Map<String,Map<String,Long>> states, String code)
    {
        if (code.length() != 2) {
            return;
        }

        int countryIdx = (int)code.charAt(0) - 1;
        int stateIdx = (int)code.charAt(1) - 1;

        if (countryIdx < 0 || countryIdx >= COUNTRIES.length) {
            return;
        }

        String country = COUNTRIES[countryIdx];
        Long countryAgg = countries.get(country);
        if (countryAgg == null){
            countryAgg = Long.valueOf(0);
        }
        countries.put(country, countryAgg + 1);

        if (!STATES.containsKey(country)) {
            return;
        }
        if (stateIdx < 0 || stateIdx >= STATES.get(country).length){
            return;
        }

        String state = STATES.get(country)[stateIdx];
        Map<String,Long> stateAggs = states.get(country);
        if (stateAggs == null){
            stateAggs = new HashMap<String,Long>();
            states.put(country, stateAggs);
        }
        Long stateAgg = stateAggs.get(state);
        if (stateAgg == null){
            stateAgg = Long.valueOf(0);
        }
        stateAggs.put(state, stateAgg + 1);
    }

    public String getCode(String country, String state) {
        int cindex = bisectLeft(COUNTRIES, country);
        if (cindex > COUNTRIES.length || !country.equals(COUNTRIES[cindex])) {
            cindex = -1;
        }
        cindex++;

        int sindex = -1;
        if (state != null && STATES.containsKey(country)) {
            String[] states = STATES.get(country);
            sindex = bisectLeft(states, state);
            if (sindex > states.length || !state.equals(states[sindex])) {
                sindex--;
            }
        }
        sindex++;

        return new String(new char[]{(char)cindex, (char)sindex});
    }

    private int bisectLeft(String[] values, String key) {
        int index = Arrays.binarySearch(values, key);
        return index < 0 ? Math.abs(index) - 1 : index;
    }

    private boolean isDigit(String string) {
        for(char c : string.toCharArray()) {
            if (!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }

    public class RedisInputStream
        extends InputStream
    {
        private Jedis conn;
        private String key;
        private int pos;

        public RedisInputStream(Jedis conn, String key){
            this.conn = conn;
            this.key = key;
        }

        @Override
        public int available()
            throws IOException
        {
            long len = conn.strlen(key);
            return (int)(len - pos);
        }

        @Override
        public int read()
            throws IOException
        {
            byte[] block = conn.substr(key.getBytes(), pos, pos);
            if (block == null || block.length == 0){
                return -1;
            }
            pos++;
            return (int)(block[0] & 0xff);
        }

        @Override
        public int read(byte[] buf, int off, int len)
            throws IOException
        {
            byte[] block = conn.substr(key.getBytes(), pos, pos + (len - off - 1));
            if (block == null || block.length == 0){
                return -1;
            }
            System.arraycopy(block, 0, buf, off, block.length);
            pos += block.length;
            return block.length;
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
