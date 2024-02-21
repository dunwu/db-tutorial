package io.github.dunwu.javadb.elasticsearch;

import io.github.dunwu.javadb.elasticsearch.entity.Page;
import io.github.dunwu.javadb.elasticsearch.entity.User;
import io.github.dunwu.javadb.elasticsearch.util.JsonUtil;
import org.assertj.core.api.Assertions;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ElasticsearchTemplate 测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
public class ElasticsearchTemplateTest {

    public static final String INDEX = "user";
    public static final String TYPE = "_doc";
    public static final String TEST_ID_01 = "1";
    public static final String TEST_ID_02 = "2";

    private static final ElasticsearchTemplate TEMPLATE;

    static {
        TEMPLATE = ElasticsearchFactory.newElasticsearchTemplate();
    }

    @Test
    @DisplayName("根据ID精确查询")
    public void getById() throws IOException {
        GetResponse response = TEMPLATE.getById(INDEX, TYPE, TEST_ID_01);
        System.out.println("记录：" + JsonUtil.toString(response.getSourceAsMap()));
    }

    @Test
    @DisplayName("根据ID精确查询POJO")
    public void pojoById() throws IOException {
        User entity = TEMPLATE.pojoById(INDEX, TYPE, TEST_ID_01, User.class);
        System.out.println("记录：" + JsonUtil.toString(entity));
    }

    @Test
    @DisplayName("根据ID精确批量查询POJO")
    public void pojoListByIds() throws IOException {
        List<String> ids = Arrays.asList(TEST_ID_01, TEST_ID_02);
        List<User> list = TEMPLATE.pojoListByIds(INDEX, TYPE, ids, User.class);
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(2);
        for (User entity : list) {
            System.out.println("记录：" + JsonUtil.toString(entity));
        }
    }

    @Test
    @DisplayName("分页查询")
    public void pojoPage() throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("theme", 3));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);

        Page<User> page = TEMPLATE.pojoPage(INDEX, TYPE, searchSourceBuilder, User.class);
        Assertions.assertThat(page).isNotNull();
        Assertions.assertThat(page.getContent()).isNotEmpty();
        for (User entity : page.getContent()) {
            System.out.println("记录：" + JsonUtil.toString(entity));
        }
    }

    @Test
    @DisplayName("条件数量查询")
    public void count() throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("theme", 3));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        long total = TEMPLATE.count(INDEX, TYPE, searchSourceBuilder);
        Assertions.assertThat(total).isNotZero();
        System.out.println("符合条件的总记录数：" + total);
    }

    @Test
    @DisplayName("条件查询")
    public void query() throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("theme", 3));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        SearchResponse response = TEMPLATE.query(INDEX, TYPE, searchSourceBuilder);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHits()).isNotNull();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println("记录：" + hit.getSourceAsString());
            Map<String, Object> map = hit.getSourceAsMap();
            Assertions.assertThat(map).isNotNull();
            Assertions.assertThat(map.get("theme")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("写操作测试")
    public class WriteTest {

        String json1 =
            "{\"id\":1,\"username\":\"user1\",\"password\":\"xxxxxx\",\"age\":18,\"email\":\"user1@xxx.com\"}";
        String json2 =
            "{\"id\":2,\"username\":\"user2\",\"password\":\"xxxxxx\",\"age\":18,\"email\":\"user2@xxx.com\"}";

        @Test
        @DisplayName("插入、更新")
        public void saveAndUpdate() throws IOException, InterruptedException {

            User origin = JsonUtil.toBean(json1, User.class);
            if (origin == null) {
                System.err.println("反序列化失败！");
                return;
            }

            TEMPLATE.save(INDEX, TYPE, origin);
            TimeUnit.SECONDS.sleep(1);
            User expectEntity = TEMPLATE.pojoById(INDEX, TYPE, origin.getDocId(), User.class);
            Assertions.assertThat(expectEntity).isNotNull();

            expectEntity.setAge(20);
            TEMPLATE.updateById(INDEX, TYPE, expectEntity);
            TimeUnit.SECONDS.sleep(18);
            User expectEntity2 =
                TEMPLATE.pojoById(INDEX, TYPE, origin.getDocId(), User.class);
            Assertions.assertThat(expectEntity2).isNotNull();
            Assertions.assertThat(expectEntity2.getAge()).isEqualTo(20);
        }

        @Test
        @DisplayName("批量插入、更新")
        public void batchSaveAndUpdate() throws IOException, InterruptedException {

            User origin1 = JsonUtil.toBean(json1, User.class);
            if (origin1 == null) {
                System.err.println("反序列化失败！");
                return;
            }

            User origin2 = JsonUtil.toBean(json2, User.class);
            if (origin2 == null) {
                System.err.println("反序列化失败！");
                return;
            }

            List<User> list = Arrays.asList(origin1, origin2);
            List<String> ids = list.stream().map(User::getDocId).collect(Collectors.toList());

            TEMPLATE.batchSave(INDEX, TYPE, list);
            List<User> newList = TEMPLATE.pojoListByIds(INDEX, TYPE, ids, User.class);
            Assertions.assertThat(newList).isNotEmpty();

            newList.forEach(entity -> {
                entity.setAge(20);
            });
            TEMPLATE.batchUpdateById(INDEX, TYPE, newList);
            TimeUnit.SECONDS.sleep(1);

            List<User> expectList =
                TEMPLATE.pojoListByIds(INDEX, TYPE, ids, User.class);
            Assertions.assertThat(expectList).isNotEmpty();
            for (User item : expectList) {
                Assertions.assertThat(item.getAge()).isEqualTo(20);
            }
        }

    }

}
