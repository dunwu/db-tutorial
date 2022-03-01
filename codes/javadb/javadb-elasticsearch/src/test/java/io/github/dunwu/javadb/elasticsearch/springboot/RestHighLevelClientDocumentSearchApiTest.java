package io.github.dunwu.javadb.elasticsearch.springboot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.github.dunwu.javadb.elasticsearch.springboot.entity.ecommerce.KibanaSampleDataEcommerceBean;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-03-01
 */
@SpringBootTest
public class RestHighLevelClientDocumentSearchApiTest {

    public static final String INDEX = "kibana_sample_data_ecommerce";
    @Autowired
    private RestHighLevelClient client;

@Test
@DisplayName("获取匹配条件的记录总数")
public void count() throws IOException {
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.matchPhraseQuery("customer_gender", "MALE"));
    sourceBuilder.trackTotalHits(true);

    CountRequest countRequest = new CountRequest(INDEX);
    countRequest.source(sourceBuilder);

    CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
    long count = countResponse.getCount();
    System.out.println("命中记录数：" + count);
}

@ParameterizedTest
@ValueSource(ints = {0, 1, 2, 3})
@DisplayName("分页查询测试")
public void pageTest(int page) throws IOException {

    int size = 10;
    int offset = page * size;
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.matchPhraseQuery("customer_gender", "MALE"));
    sourceBuilder.from(offset);
    sourceBuilder.size(size);
    sourceBuilder.trackTotalHits(true);

    SearchRequest searchRequest = new SearchRequest(INDEX);
    searchRequest.source(sourceBuilder);
    SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
    SearchHit[] hits = response.getHits().getHits();
    for (SearchHit hit : hits) {
        KibanaSampleDataEcommerceBean bean =
            BeanUtil.mapToBean(hit.getSourceAsMap(), KibanaSampleDataEcommerceBean.class, true,
                               CopyOptions.create());
        System.out.println(bean);
    }
}

@Test
@DisplayName("条件查询")
public void matchPhraseQuery() throws IOException {
    SearchRequest searchRequest = new SearchRequest(INDEX);
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("customer_last_name", "Jensen"));
    sourceBuilder.query(boolQueryBuilder);
    sourceBuilder.trackTotalHits(true);
    searchRequest.source(sourceBuilder);
    SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
    SearchHit[] hits = response.getHits().getHits();
    for (SearchHit hit : hits) {
        KibanaSampleDataEcommerceBean bean =
            BeanUtil.mapToBean(hit.getSourceAsMap(), KibanaSampleDataEcommerceBean.class, true,
                               CopyOptions.create());
        System.out.println(bean);
    }
}

}
