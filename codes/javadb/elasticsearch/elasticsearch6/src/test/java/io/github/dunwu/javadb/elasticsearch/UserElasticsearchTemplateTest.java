package io.github.dunwu.javadb.elasticsearch;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import io.github.dunwu.javadb.elasticsearch.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 使用 ElasticsearchTemplate 对 user 索引进行测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-04-09
 */
@Slf4j
public class UserElasticsearchTemplateTest extends BaseElasticsearchTemplateTest<User> {

    @Override
    protected String getAlias() {
        return "user";
    }

    @Override
    protected String getIndex() {
        String date = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
        return getAlias() + "_" + date;
    }

    @Override
    protected String getType() {
        return "_doc";
    }

    @Override
    protected int getShard() {
        return 5;
    }

    @Override
    protected int getReplica() {
        return 1;
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected Map<String, String> getPropertiesMap() {
        return User.getPropertiesMap();
    }

    @Override
    protected User getOneMockData(String id) {
        return User.builder()
                   .id(id)
                   .name("测试数据" + id)
                   .age(RandomUtil.randomInt(1, 100))
                   .build();
    }

    @Override
    protected List<User> getMockList(int num) {
        List<User> list = new LinkedList<>();
        for (int i = 1; i <= num; i++) {
            User entity = getOneMockData(String.valueOf(i));
            list.add(entity);
        }
        return list;
    }

    @Test
    @DisplayName("索引管理测试")
    public void indexTest() throws IOException {
        super.deleteIndex();
        super.createIndex();
        super.getIndexList();
    }

    @Test
    @DisplayName("写数据测试")
    protected void writeTest() throws IOException {
        super.save();
        super.saveBatch();
    }

    @Test
    @DisplayName("异步写数据测试")
    public void asyncWriteTest() throws IOException, InterruptedException {
        super.asyncSave();
        super.asyncSaveBatch();
    }

    @Test
    @DisplayName("读数据测试")
    public void readTest() throws IOException {
        super.getById();
        super.pojoById();
        super.pojoListByIds();
        super.count();
        super.query();
        super.pojoPage();
        super.pojoPageByLastId();
        super.pojoScroll();
    }

}
