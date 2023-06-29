package io.github.dunwu.javadb.elasticsearch;

import io.github.dunwu.javadb.elasticsearch.entity.User;
import io.github.dunwu.javadb.elasticsearch.mapper.UserEsMapper;
import io.github.dunwu.javadb.elasticsearch.util.ElasticsearchUtil;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Demo {

    private static final String HOSTS = "127.0.0.1:9200";
    private static final RestHighLevelClient restHighLevelClient = ElasticsearchUtil.newRestHighLevelClient(HOSTS);

    public static void main(String[] args) throws IOException, InterruptedException {

        UserEsMapper mapper = new UserEsMapper(restHighLevelClient);

        System.out.println("索引是否存在：" + mapper.isIndexExists());

        User jack = User.builder().id(1L).username("jack").age(18).build();
        User tom = User.builder().id(2L).username("tom").age(20).build();
        List<User> users = Arrays.asList(jack, tom);

        System.out.println("批量插入：" + mapper.batchInsert(users));
        System.out.println("根据ID查询：" + mapper.getById("1").toString());
        System.out.println("根据ID查询：" + mapper.pojoById("2").toString());
        System.out.println("根据ID批量查询：" + mapper.pojoListByIds(Arrays.asList("1", "2")).toString());

        Thread.sleep(1000);
        System.out.println("根据ID批量删除：" + mapper.batchDeleteById(Arrays.asList("1", "2")));
    }

}
