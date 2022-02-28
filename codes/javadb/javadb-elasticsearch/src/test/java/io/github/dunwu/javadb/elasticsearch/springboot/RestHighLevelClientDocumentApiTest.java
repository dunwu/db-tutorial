package io.github.dunwu.javadb.elasticsearch.springboot;

import io.github.dunwu.javadb.elasticsearch.springboot.entities.User;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2022-02-25
 */
@SpringBootTest
public class RestHighLevelClientDocumentApiTest {

    public static final String INDEX = "mytest";
    public static final String INDEX_ALIAS = "mytest_alias";
    /**
     * {@link User} 的 mapping 结构（json形式）
     */
    public static final String MAPPING_JSON = "{\n"
        + "  \"properties\": {\n"
        + "    \"age\": {\n"
        + "      \"type\": \"long\"\n"
        + "    },\n"
        + "    \"desc\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"email\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fielddata\": true\n"
        + "    },\n"
        + "    \"id\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"password\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"title\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"user\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"username\": {\n"
        + "      \"type\": \"text\",\n"
        + "      \"fields\": {\n"
        + "        \"keyword\": {\n"
        + "          \"type\": \"keyword\",\n"
        + "          \"ignore_above\": 256\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}";

    @Autowired
    private RestHighLevelClient client;

    @BeforeEach
    public void init() throws IOException {

        // 创建索引
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);

        // 设置索引的 settings
        createIndexRequest.settings(Settings.builder()
                                            .put("index.number_of_shards", 3)
                                            .put("index.number_of_replicas", 2)
        );

        // 设置索引的 mapping
        createIndexRequest.mapping(MAPPING_JSON, XContentType.JSON);

        // 设置索引的别名
        createIndexRequest.alias(new Alias(INDEX_ALIAS));

        AcknowledgedResponse response =
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        Assertions.assertTrue(response.isAcknowledged());

        // 判断索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
        Assertions.assertTrue(client.indices().exists(getIndexRequest, RequestOptions.DEFAULT));
        GetIndexRequest getIndexAliasRequest = new GetIndexRequest(INDEX_ALIAS);
        Assertions.assertTrue(client.indices().exists(getIndexAliasRequest, RequestOptions.DEFAULT));
    }

    @AfterEach
    public void destroy() throws IOException {
        // 删除索引
        DeleteIndexRequest request = new DeleteIndexRequest(INDEX);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        Assertions.assertTrue(response.isAcknowledged());
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

    @Test
    public void method() throws IOException {
        IndexRequest request = new IndexRequest(INDEX);
        request.id("1");
        String jsonString = "{\n"
            + "  \"id\": \"1\",\n"
            + "  \"userName\": \"Jack\",\n"
            + "  \"age\": 12,\n"
            + "  \"password\": \"123456\",\n"
            + "  \"email\": \"jack@xxx.com\"\n"
            + "}";
        request.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println("indexResponse: " + indexResponse.getResult());
    }

}
