package io.github.dunwu.javadb.elasticsearch.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.BaseApplicationTests;
import io.github.dunwu.javadb.elasticsearch.entity.User;
import io.github.dunwu.javadb.elasticsearch.entity.common.PageData;
import io.github.dunwu.javadb.elasticsearch.entity.common.ScrollData;
import io.github.dunwu.javadb.elasticsearch.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ElasticsearchTemplate 测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
@Slf4j
public class UserEsMapperTest extends BaseApplicationTests {

    static final int FROM = 0;
    static final int SIZE = 10;
    private static final String day = "2024-04-07";

    @Autowired
    private UserEsMapper mapper;

    @Nested
    @DisplayName("删除索引测试")
    class DeleteIndexTest {

        @Test
        @DisplayName("删除当天索引")
        public void deleteIndex() throws IOException {
            String index = mapper.getIndex();
            boolean indexExists = mapper.isIndexExists();
            if (!indexExists) {
                log.info("【ES】{} 不存在！", index);
                return;
            }
            mapper.deleteIndex();
            indexExists = mapper.isIndexExists();
            Assertions.assertThat(indexExists).isFalse();
        }

        @Test
        @DisplayName("根据日期删除索引")
        public void deleteIndexInDay() throws IOException {
            String index = mapper.getIndex(day);
            boolean indexExists = mapper.isIndexExistsInDay(day);
            if (!indexExists) {
                log.info("【ES】{} 不存在！", index);
                return;
            }
            mapper.deleteIndexInDay(day);
            indexExists = mapper.isIndexExistsInDay(day);
            Assertions.assertThat(indexExists).isFalse();
        }

    }

    @Nested
    @DisplayName("创建索引测试")
    class CreateIndexTest {

        @Test
        @DisplayName("创建当天索引")
        public void createIndex() throws IOException {

            String index = mapper.getIndex();
            boolean indexExists = mapper.isIndexExists();
            if (indexExists) {
                log.info("【ES】{} 已存在！", index);
                return;
            }

            mapper.createIndexIfNotExists();
            indexExists = mapper.isIndexExists();
            Assertions.assertThat(indexExists).isTrue();
        }

        @Test
        @DisplayName("根据日期创建索引")
        public void createIndexInDay() throws IOException {

            String index = mapper.getIndex(day);
            boolean indexExists = mapper.isIndexExistsInDay(day);
            if (indexExists) {
                log.info("【ES】{} 已存在！", index);
                return;
            }

            mapper.createIndexInDay(day);
            indexExists = mapper.isIndexExistsInDay(day);
            Assertions.assertThat(indexExists).isTrue();
        }

    }

    @Nested
    @DisplayName("写操作测试")
    class WriteTest {

        @Test
        @DisplayName("保存当天数据")
        public void save() throws IOException {
            String id = "1";
            User entity = getOneMockData(id);
            mapper.save(entity);
            User newEntity = mapper.pojoById(id);
            log.info("entity: {}", JsonUtil.toString(newEntity));
            Assertions.assertThat(newEntity).isNotNull();
        }

        @Test
        @DisplayName("保存指定日期数据")
        public void saveInDay() throws IOException {
            String id = "1";
            User entity = getOneMockData(id);
            mapper.saveInDay(day, entity);
            User newEntity = mapper.pojoByIdInDay(day, id);
            log.info("entity: {}", JsonUtil.toString(newEntity));
            Assertions.assertThat(newEntity).isNotNull();
        }

        @Test
        @DisplayName("批量保存当天数据")
        public void batchSave() throws IOException, InterruptedException {
            int total = 10000;
            List<List<User>> listGroup = CollectionUtil.split(getMockList(total), 1000);
            for (List<User> list : listGroup) {
                mapper.asyncSaveBatch(list);
            }
            TimeUnit.SECONDS.sleep(20);
            long count = mapper.count(new SearchSourceBuilder());
            log.info("count: {}", count);
            Assertions.assertThat(count).isEqualTo(10 * 1000);
        }

        @Test
        @DisplayName("批量保存指定日期数据")
        public void batchSaveInDay() throws IOException, InterruptedException {
            int total = 10000;
            List<List<User>> listGroup = CollectionUtil.split(getMockList(total), 1000);
            for (List<User> list : listGroup) {
                mapper.asyncSaveBatchInDay(day, list);
            }
            TimeUnit.SECONDS.sleep(20);
            long count = mapper.countInDay(day, new SearchSourceBuilder());
            log.info("count: {}", count);
            Assertions.assertThat(count).isEqualTo(10 * 1000);
        }

    }

    @Nested
    @DisplayName("读操作测试")
    class ReadTest {

        @Test
        @DisplayName("根据ID查找当日数据")
        public void pojoById() throws IOException {
            String id = "1";
            User newEntity = mapper.pojoById(id);
            log.info("entity: {}", JsonUtil.toString(newEntity));
            Assertions.assertThat(newEntity).isNotNull();
        }

        @Test
        @DisplayName("根据ID查找指定日期数据")
        public void pojoByIdInDay() throws IOException {
            String id = "1";
            User newEntity = mapper.pojoByIdInDay(day, id);
            log.info("entity: {}", JsonUtil.toString(newEntity));
            Assertions.assertThat(newEntity).isNotNull();
        }

        @Test
        @DisplayName("获取匹配条件的记录数")
        public void count() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            long total = mapper.count(searchSourceBuilder);
            Assertions.assertThat(total).isNotZero();
            log.info("符合条件的记录数：{}", total);
        }

        @Test
        @DisplayName("获取匹配条件的指定日期记录数")
        public void countInDay() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            long total = mapper.countInDay(day, searchSourceBuilder);
            Assertions.assertThat(total).isNotZero();
            log.info("符合条件的记录数：{}", total);
        }

        @Test
        @DisplayName("获取匹配条件的记录")
        public void query() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(FROM);
            searchSourceBuilder.size(SIZE);
            SearchResponse response = mapper.query(searchSourceBuilder);
            Assertions.assertThat(response).isNotNull();
            Assertions.assertThat(response.getHits()).isNotNull();
            for (SearchHit hit : response.getHits().getHits()) {
                log.info("记录：{}", hit.getSourceAsString());
                Map<String, Object> map = hit.getSourceAsMap();
                Assertions.assertThat(map).isNotNull();
                Assertions.assertThat(Integer.valueOf((String) map.get("docId"))).isLessThan(100);
            }
        }

        @Test
        @DisplayName("获取匹配条件的指定日期记录")
        public void queryInDay() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(FROM);
            searchSourceBuilder.size(SIZE);
            SearchResponse response = mapper.queryInDay(day, searchSourceBuilder);
            Assertions.assertThat(response).isNotNull();
            Assertions.assertThat(response.getHits()).isNotNull();
            for (SearchHit hit : response.getHits().getHits()) {
                log.info("记录：{}", hit.getSourceAsString());
                Map<String, Object> map = hit.getSourceAsMap();
                Assertions.assertThat(map).isNotNull();
                Assertions.assertThat(Integer.valueOf((String) map.get("docId"))).isLessThan(100);
            }
        }

        @Test
        @DisplayName("from + size 分页查询当日数据")
        public void pojoPage() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(FROM);
            searchSourceBuilder.size(SIZE);
            PageData<User> page = mapper.pojoPage(searchSourceBuilder);
            Assertions.assertThat(page).isNotNull();
            Assertions.assertThat(page.getContent()).isNotEmpty();
            for (User entity : page.getContent()) {
                log.info("记录：{}", JsonUtil.toString(entity));
            }
        }

        @Test
        @DisplayName("from + size 分页查询指定日期数据")
        public void pojoPageInDay() throws IOException {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(FROM);
            searchSourceBuilder.size(SIZE);
            PageData<User> page = mapper.pojoPageInDay(day, searchSourceBuilder);
            Assertions.assertThat(page).isNotNull();
            Assertions.assertThat(page.getContent()).isNotEmpty();
            for (User entity : page.getContent()) {
                log.info("记录：{}", JsonUtil.toString(entity));
            }
        }

        @Test
        @DisplayName("search after 分页查询当日数据")
        protected void pojoPageByLastId() throws IOException {

            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            long total = mapper.count(searchSourceBuilder);
            ScrollData<User> scrollData = mapper.pojoPageByLastId(null, SIZE, queryBuilder);
            if (scrollData == null || scrollData.getScrollId() == null) {
                return;
            }
            Assertions.assertThat(scrollData.getTotal()).isEqualTo(total);

            long count = 0L;
            scrollData.getContent().forEach(data -> {
                log.info("docId: {}", data.getDocId());
            });
            count += scrollData.getContent().size();

            String scrollId = scrollData.getScrollId();
            while (CollectionUtil.isNotEmpty(scrollData.getContent())) {
                scrollData = mapper.pojoPageByLastId(scrollId, SIZE, queryBuilder);
                if (scrollData == null || CollectionUtil.isEmpty(scrollData.getContent())) {
                    break;
                }
                if (StrUtil.isNotBlank(scrollData.getScrollId())) {
                    scrollId = scrollData.getScrollId();
                }
                scrollData.getContent().forEach(data -> {
                    log.info("docId: {}", data.getDocId());
                });
                count += scrollData.getContent().size();
            }
            log.info("total: {}", total);
            Assertions.assertThat(count).isEqualTo(total);
        }

        @Test
        @DisplayName("search after 分页查询指定日期数据")
        protected void pojoPageByLastIdInDay() throws IOException {

            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            long total = mapper.count(searchSourceBuilder);
            ScrollData<User> scrollData = mapper.pojoPageByLastIdInDay(day, null, SIZE, queryBuilder);
            if (scrollData == null || scrollData.getScrollId() == null) {
                return;
            }
            Assertions.assertThat(scrollData.getTotal()).isEqualTo(total);

            long count = 0L;
            scrollData.getContent().forEach(data -> {
                log.info("docId: {}", data.getDocId());
            });
            count += scrollData.getContent().size();

            String scrollId = scrollData.getScrollId();
            while (CollectionUtil.isNotEmpty(scrollData.getContent())) {
                scrollData = mapper.pojoPageByLastIdInDay(day, scrollId, SIZE, queryBuilder);
                if (scrollData == null || CollectionUtil.isEmpty(scrollData.getContent())) {
                    break;
                }
                if (StrUtil.isNotBlank(scrollData.getScrollId())) {
                    scrollId = scrollData.getScrollId();
                }
                scrollData.getContent().forEach(data -> {
                    log.info("docId: {}", data.getDocId());
                });
                count += scrollData.getContent().size();
            }
            log.info("total: {}", total);
            Assertions.assertThat(count).isEqualTo(total);
        }

        @Test
        @DisplayName("滚动翻页当日数据")
        public void pojoScroll() throws IOException {

            final int size = 100;

            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size).sort("docId", SortOrder.ASC).query(queryBuilder).trackScores(false);

            long total = mapper.count(searchSourceBuilder);
            log.info("total: {}", total);

            ScrollData<User> scrollData = mapper.pojoScrollBegin(searchSourceBuilder);
            if (scrollData == null || scrollData.getScrollId() == null) {
                return;
            }

            long count = 0L;
            scrollData.getContent().forEach(data -> {
                log.info("docId: {}", data.getDocId());
            });
            Assertions.assertThat(scrollData.getTotal()).isEqualTo(total);
            count += scrollData.getContent().size();

            String scrollId = scrollData.getScrollId();
            while (CollectionUtil.isNotEmpty(scrollData.getContent())) {
                scrollData = mapper.pojoScroll(scrollId, searchSourceBuilder);
                if (scrollData != null && StrUtil.isNotBlank(scrollData.getScrollId())) {
                    scrollId = scrollData.getScrollId();
                }
                scrollData.getContent().forEach(data -> {
                    log.info("docId: {}", data.getDocId());
                });
                count += scrollData.getContent().size();
            }
            mapper.pojoScrollEnd(scrollId);
            Assertions.assertThat(count).isEqualTo(total);
        }

        @Test
        @DisplayName("滚动翻页指定日期数据")
        public void pojoScrollInDay() throws IOException {

            final int size = 100;

            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(size).sort("docId", SortOrder.ASC).query(queryBuilder).trackScores(false);

            long total = mapper.countInDay(day, searchSourceBuilder);
            log.info("total: {}", total);

            ScrollData<User> scrollData = mapper.pojoScrollBeginInDay(day, searchSourceBuilder);
            if (scrollData == null || scrollData.getScrollId() == null) {
                return;
            }

            long count = 0L;
            scrollData.getContent().forEach(data -> {
                log.info("docId: {}", data.getDocId());
            });
            Assertions.assertThat(scrollData.getTotal()).isEqualTo(total);
            count += scrollData.getContent().size();

            String scrollId = scrollData.getScrollId();
            while (CollectionUtil.isNotEmpty(scrollData.getContent())) {
                scrollData = mapper.pojoScroll(scrollId, searchSourceBuilder);
                if (scrollData != null && StrUtil.isNotBlank(scrollData.getScrollId())) {
                    scrollId = scrollData.getScrollId();
                }
                scrollData.getContent().forEach(data -> {
                    log.info("docId: {}", data.getDocId());
                });
                count += scrollData.getContent().size();
            }
            mapper.pojoScrollEnd(scrollId);
            Assertions.assertThat(count).isEqualTo(total);
        }

    }

    public User getOneMockData(String id) {
        return User.builder()
                   .id(id)
                   .name("测试数据" + id)
                   .age(RandomUtil.randomInt(1, 100))
                   .build();
    }

    public List<User> getMockList(int num) {
        List<User> list = new LinkedList<>();
        for (int i = 1; i <= num; i++) {
            User entity = getOneMockData(String.valueOf(i));
            list.add(entity);
        }
        return list;
    }

}
