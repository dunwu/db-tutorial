package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import io.github.dunwu.javadb.elasticsearch.springboot.constant.OrderType;
import io.github.dunwu.javadb.elasticsearch.springboot.constant.QueryJudgeType;
import io.github.dunwu.javadb.elasticsearch.springboot.elasticsearch.QueryDocument;
import io.github.dunwu.javadb.elasticsearch.springboot.elasticsearch.QueryField;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-12-17
 */
@Data
@ToString
@QueryDocument(orders = {@QueryDocument.Order(value = "age", type = OrderType.ASC),
    @QueryDocument.Order(value = "email", type = OrderType.DESC)})
public class UserQuery {

    @Id
    private String id;

    @QueryField(judgeType = QueryJudgeType.Like)
    private String userName;

    @QueryField(judgeType = QueryJudgeType.Equals)
    private Integer age;

    @QueryField(judgeType = QueryJudgeType.Equals)
    private String email;

}
