package io.github.dunwu.javadb.elasticsearch.springboot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import io.github.dunwu.javadb.elasticsearch.springboot.entities.Product;
import io.github.dunwu.javadb.elasticsearch.springboot.entities.User;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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
    public static final String MAPPING_JSON =
        "{\n" + "  \"properties\": {\n" + "    \"_class\": {\n" + "      \"type\": \"keyword\",\n"
            + "      \"index\": false,\n" + "      \"doc_values\": false\n" + "    },\n" + "    \"description\": {\n"
            + "      \"type\": \"text\",\n" + "      \"fielddata\": true\n" + "    },\n" + "    \"enabled\": {\n"
            + "      \"type\": \"boolean\"\n" + "    },\n" + "    \"name\": {\n" + "      \"type\": \"text\",\n"
            + "      \"fielddata\": true\n" + "    }\n" + "  }\n" + "}";

    @Autowired
    private RestHighLevelClient client;

    @BeforeEach
    public void init() throws IOException {

        // 创建索引
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);

        // 设置索引的 settings
        createIndexRequest.settings(
            Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));

        // 设置索引的 mapping
        createIndexRequest.mapping(MAPPING_JSON, XContentType.JSON);

        // 设置索引的别名
        createIndexRequest.alias(new Alias(INDEX_ALIAS));

        AcknowledgedResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
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
    @DisplayName("同步新建文档")
    public void index() throws IOException {
        IndexRequest request = new IndexRequest(INDEX_ALIAS);
        request.id("1");
        Product product = new Product();
        product.setName("机器人");
        product.setDescription("人工智能机器人");
        product.setEnabled(true);
        String jsonString = JSONUtil.toJsonStr(product);
        request.source(jsonString, XContentType.JSON);

        // 同步执行
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    @DisplayName("异步新建文档")
    public void indexAsync() {
        IndexRequest request = new IndexRequest(INDEX_ALIAS);
        Product product = new Product();
        product.setName("机器人");
        product.setDescription("人工智能机器人");
        product.setEnabled(true);
        String jsonString = JSONUtil.toJsonStr(product);
        request.source(jsonString, XContentType.JSON);

        // 异步执行
        client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                System.out.println(indexResponse);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("执行失败");
            }
        });
    }

    @Test
    @DisplayName("删除文档")
    public void delete() throws IOException {

        // 创建文档请求
        IndexRequest request = new IndexRequest(INDEX_ALIAS);
        request.id("1");
        Product product = new Product();
        product.setName("机器人");
        product.setDescription("人工智能机器人");
        product.setEnabled(true);
        String jsonString = JSONUtil.toJsonStr(product);
        request.source(jsonString, XContentType.JSON);

        // 同步执行创建操作
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);

        // 删除文档请求
        DeleteRequest deleteRequest = new DeleteRequest(INDEX_ALIAS, "1");

        // 同步执行删除操作
        // DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        // System.out.println(deleteResponse);

        // 异步执行删除操作
        client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                System.out.println(deleteResponse);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("执行失败");
            }
        });
    }

    @Test
    @DisplayName("更新文档")
    public void update() throws IOException {

        // 创建文档请求
        IndexRequest request = new IndexRequest(INDEX_ALIAS);
        request.id("1");
        Product product = new Product();
        product.setName("机器人");
        product.setDescription("人工智能机器人");
        product.setEnabled(true);
        String jsonString = JSONUtil.toJsonStr(product);
        request.source(jsonString, XContentType.JSON);

        // 同步执行创建操作
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);

        // 查询文档操作
        GetRequest getRequest = new GetRequest(INDEX_ALIAS, "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Product product2 = BeanUtil.mapToBean(getResponse.getSource(), Product.class, true, CopyOptions.create());
        System.out.println("product2: " + product2);
        Assertions.assertEquals(product.getName(), product2.getName());

        // 更新文档请求
        UpdateRequest updateRequest = new UpdateRequest(INDEX_ALIAS, "1");
        Product product3 = new Product();
        product3.setName("扫地机器人");
        product3.setDescription("人工智能扫地机器人");
        product3.setEnabled(true);
        String jsonString2 = JSONUtil.toJsonStr(product3);
        updateRequest.doc(jsonString2, XContentType.JSON);

        // 同步执行更新操作
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse);

        // 异步执行更新操作
        // client.updateAsync(updateRequest, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
        //     @Override
        //     public void onResponse(UpdateResponse updateResponse) {
        //         System.out.println(updateResponse);
        //     }
        //
        //     @Override
        //     public void onFailure(Exception e) {
        //         System.out.println("执行失败");
        //     }
        // });

        // 查询文档操作
        GetResponse getResponse2 = client.get(getRequest, RequestOptions.DEFAULT);
        Product product4 = BeanUtil.mapToBean(getResponse2.getSource(), Product.class, true, CopyOptions.create());
        System.out.println("product4: " + product4);
        Assertions.assertEquals(product3.getName(), product4.getName());
    }

}
