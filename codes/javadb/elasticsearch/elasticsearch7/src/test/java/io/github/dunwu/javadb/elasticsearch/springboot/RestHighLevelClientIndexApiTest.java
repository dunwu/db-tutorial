package io.github.dunwu.javadb.elasticsearch.springboot;

import io.github.dunwu.javadb.elasticsearch.springboot.entities.User;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-02-23
 */
@SpringBootTest
public class RestHighLevelClientIndexApiTest {

    public static final String INDEX = "mytest";
    public static final String INDEX_ALIAS = "mytest_alias";
    /**
     * {@link User} 的 mapping 结构（json形式）
     */
    public static final String MAPPING_JSON =
        "{\n" + "  \"properties\": {\n" + "    \"_class\": {\n" + "      \"type\": \"keyword\",\n"
            + "      \"index\": false,\n" + "      \"doc_values\": false\n" + "    },\n" + "    \"description\": {\n"
            + "      \"type\": \"text\",\n" + "      \"fielddata\": true\n" + "    },\n" + "    \"enabled\": {\n"
            + "      \"type\": \"boolean\"\n" + "    },\n" + "    \"name\": {\n" + "      \"type\": \"text\",\n"
            + "      \"fielddata\": true\n" + "    }\n" + "  }\n" + "}";

    @Autowired
    private RestHighLevelClient client;

    @Test
    @DisplayName("创建、删除索引测试")
    public void createAndDeleteIndex() throws IOException {

        // 创建索引
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);

        // 设置索引的 settings
        createIndexRequest.settings(
            Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));

        // 设置索引的 mapping
        createIndexRequest.mapping(MAPPING_JSON, XContentType.JSON);

        // 设置索引的别名
        createIndexRequest.alias(new Alias(INDEX_ALIAS));

        AcknowledgedResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        Assertions.assertTrue(createIndexResponse.isAcknowledged());

        // 判断索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
        Assertions.assertTrue(client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
        GetIndexRequest getIndexAliasRequest = new GetIndexRequest(INDEX_ALIAS);
        Assertions.assertTrue(client.indices().exists(getIndexAliasRequest, RequestOptions.DEFAULT));

        // 删除索引
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(INDEX);
        AcknowledgedResponse deleteResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        Assertions.assertTrue(deleteResponse.isAcknowledged());

        // 判断索引是否存在
        Assertions.assertFalse(client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
        Assertions.assertFalse(client.indices().exists(getIndexAliasRequest, RequestOptions.DEFAULT));
    }


    @Test
    @DisplayName("列出所有索引")
    public void listAllIndex() throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        GetAliasesResponse getAliasesResponse = client.indices().getAlias(request, RequestOptions.DEFAULT);
        Map<String, Set<AliasMetadata>> map = getAliasesResponse.getAliases();
        Set<String> indices = map.keySet();
        indices.forEach(System.out::println);
    }

}
