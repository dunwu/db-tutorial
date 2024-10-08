package io.github.dunwu.javadb.elasticsearch;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.elasticsearch.entity.BaseEsEntity;
import io.github.dunwu.javadb.elasticsearch.entity.common.PageData;
import io.github.dunwu.javadb.elasticsearch.entity.common.ScrollData;
import io.github.dunwu.javadb.elasticsearch.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * ElasticsearchTemplate 测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
@Slf4j
public abstract class BaseElasticsearchTemplateTest<T extends BaseEsEntity> {

    static final int FROM = 0;
    static final int SIZE = 10;
    static final String TEST_ID_01 = "1";
    static final String TEST_ID_02 = "2";

    protected ElasticsearchTemplate TEMPLATE = ElasticsearchFactory.newElasticsearchTemplate();

    protected abstract String getAlias();

    protected abstract String getIndex();

    protected abstract String getType();

    protected abstract int getShard();

    protected abstract int getReplica();

    protected abstract Class<T> getEntityClass();

    protected abstract Map<String, String> getPropertiesMap();

    protected abstract T getOneMockData(String id);

    protected abstract List<T> getMockList(int num);

    protected void deleteIndex() throws IOException {
        try {
            Set<String> set = TEMPLATE.getIndexSet(getAlias());
            if (CollectionUtil.isNotEmpty(set)) {
                for (String index : set) {
                    log.info("删除 alias: {}, index: {}", getAlias(), index);
                    TEMPLATE.deleteIndex(index);
                }
            }
        } catch (IOException | ElasticsearchException e) {
            log.error("删除索引失败！", e);
        }
        boolean exists = TEMPLATE.isIndexExists(getIndex());
        Assertions.assertThat(exists).isFalse();
    }

    protected void createIndex() throws IOException {
        boolean exists = TEMPLATE.isIndexExists(getIndex());
        if (exists) {
            return;
        }
        TEMPLATE.createIndex(getIndex(), getType(), getAlias(), getShard(), getReplica());
        TEMPLATE.setMapping(getIndex(), getType(), getPropertiesMap());
        exists = TEMPLATE.isIndexExists(getIndex());
        Assertions.assertThat(exists).isTrue();
    }

    public void getIndexList() throws IOException {
        Set<String> set = TEMPLATE.getIndexSet(getAlias());
        log.info("alias: {}, indexList: {}", getAlias(), set);
        Assertions.assertThat(set).isNotEmpty();
    }

    protected void save() throws IOException {
        String id = "1";
        T oldEntity = getOneMockData(id);
        TEMPLATE.save(getIndex(), getType(), oldEntity);
        T newEntity = TEMPLATE.pojoById(getIndex(), getType(), id, getEntityClass());
        log.info("记录：{}", JsonUtil.toString(newEntity));
        Assertions.assertThat(newEntity).isNotNull();
    }

    protected void saveBatch() throws IOException {
        int total = 5000;
        List<List<T>> listGroup = CollectionUtil.split(getMockList(total), 1000);
        for (List<T> list : listGroup) {
            Assertions.assertThat(TEMPLATE.saveBatch(getIndex(), getType(), list)).isTrue();
        }
        long count = TEMPLATE.count(getIndex(), getType(), new SearchSourceBuilder());
        log.info("批量更新记录数: {}", count);
        Assertions.assertThat(count).isEqualTo(total);
    }

    protected void asyncSave() throws IOException {
        String id = "10000";
        T entity = getOneMockData(id);
        TEMPLATE.save(getIndex(), getType(), entity);
        T newEntity = TEMPLATE.pojoById(getIndex(), getType(), id, getEntityClass());
        log.info("记录：{}", JsonUtil.toString(newEntity));
        Assertions.assertThat(newEntity).isNotNull();
    }

    protected void asyncSaveBatch() throws IOException, InterruptedException {
        int total = 10000;
        List<List<T>> listGroup = CollectionUtil.split(getMockList(total), 1000);
        for (List<T> list : listGroup) {
            TEMPLATE.asyncSaveBatch(getIndex(), getType(), list, DEFAULT_BULK_LISTENER);
        }
        TimeUnit.SECONDS.sleep(20);
        long count = TEMPLATE.count(getIndex(), getType(), new SearchSourceBuilder());
        log.info("批量更新记录数: {}", count);
        Assertions.assertThat(count).isEqualTo(total);
    }

    protected void getById() throws IOException {
        GetResponse response = TEMPLATE.getById(getIndex(), getType(), TEST_ID_01);
        Assertions.assertThat(response).isNotNull();
        log.info("记录：{}", JsonUtil.toString(response.getSourceAsMap()));
    }

    protected void pojoById() throws IOException {
        T entity = TEMPLATE.pojoById(getIndex(), getType(), TEST_ID_01, getEntityClass());
        Assertions.assertThat(entity).isNotNull();
        log.info("记录：{}", JsonUtil.toString(entity));
    }

    protected void pojoListByIds() throws IOException {
        List<String> ids = Arrays.asList(TEST_ID_01, TEST_ID_02);
        List<T> list = TEMPLATE.pojoListByIds(getIndex(), getType(), ids, getEntityClass());
        Assertions.assertThat(list).isNotEmpty();
        Assertions.assertThat(list.size()).isEqualTo(2);
        for (T entity : list) {
            log.info("记录：{}", JsonUtil.toString(entity));
        }
    }

    protected void count() throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        long total = TEMPLATE.count(getIndex(), getType(), searchSourceBuilder);
        Assertions.assertThat(total).isNotZero();
        log.info("符合条件的记录数：{}", total);
    }

    protected void query() throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(FROM);
        searchSourceBuilder.size(SIZE);
        SearchResponse response = TEMPLATE.query(getIndex(), getType(), searchSourceBuilder);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHits()).isNotNull();
        for (SearchHit hit : response.getHits().getHits()) {
            log.info("记录：{}", hit.getSourceAsString());
            Map<String, Object> map = hit.getSourceAsMap();
            Assertions.assertThat(map).isNotNull();
            Assertions.assertThat(Integer.valueOf((String) map.get("docId"))).isLessThan(100);
        }
    }

    protected void pojoPage() throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(FROM);
        searchSourceBuilder.size(SIZE);
        PageData<T> page = TEMPLATE.pojoPage(getIndex(), getType(), searchSourceBuilder, getEntityClass());
        Assertions.assertThat(page).isNotNull();
        Assertions.assertThat(page.getContent()).isNotEmpty();
        for (T entity : page.getContent()) {
            log.info("记录：{}", JsonUtil.toString(entity));
        }
    }

    protected void pojoPageByLastId() throws IOException {

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));

        long total = TEMPLATE.count(getIndex(), getType(), queryBuilder);
        ScrollData<T> scrollData =
            TEMPLATE.pojoPageByScrollId(getIndex(), getType(), null, SIZE, queryBuilder, getEntityClass());
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
            scrollData = TEMPLATE.pojoPageByScrollId(getIndex(), getType(), scrollId, SIZE,
                queryBuilder, getEntityClass());
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

    protected void pojoScroll() throws IOException {

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.rangeQuery("docId").lt("100"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(SIZE).query(queryBuilder).trackScores(false);

        long total = TEMPLATE.count(getIndex(), getType(), queryBuilder);
        ScrollData<T> scrollData =
            TEMPLATE.pojoScrollBegin(getIndex(), getType(), searchSourceBuilder, getEntityClass());
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
            scrollData = TEMPLATE.pojoScroll(scrollId, searchSourceBuilder, getEntityClass());
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
        TEMPLATE.pojoScrollEnd(scrollId);
        log.info("total: {}", total);
        Assertions.assertThat(count).isEqualTo(total);
    }

    final ActionListener<BulkResponse> DEFAULT_BULK_LISTENER = new ActionListener<BulkResponse>() {
        @Override
        public void onResponse(BulkResponse response) {
            if (response != null && !response.hasFailures()) {
                log.info("【ES】异步批量写数据成功！index: {}, type: {}", getIndex(), getType());
            } else {
                log.warn("【ES】异步批量写数据失败！index: {}, type: {}", getIndex(), getType());
            }
        }

        @Override
        public void onFailure(Exception e) {
            log.error("【ES】异步批量写数据异常！index: {}, type: {}", getIndex(), getType());
        }
    };

}
