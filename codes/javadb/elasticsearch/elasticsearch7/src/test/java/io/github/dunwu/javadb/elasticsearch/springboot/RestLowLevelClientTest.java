package io.github.dunwu.javadb.elasticsearch.springboot;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.github.dunwu.javadb.elasticsearch.springboot.entities.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2022-02-25
 */
public class RestLowLevelClientTest {

    @Test
    public void method() throws IOException {
        // Create the low-level client
        RestClient restClient = RestClient.builder(
            new HttpHost("localhost", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);
        SearchResponse<Product> search = client.search(s -> s
                .index("products")
                .query(q -> q
                    .term(t -> t
                        .field("name")
                        .value(v -> v.stringValue("bicycle"))
                    )),
            Product.class);

        for (Hit<Product> hit : search.hits().hits()) {
            System.out.println(hit.score());
        }
    }

}
