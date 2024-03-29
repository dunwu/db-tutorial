/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dunwu.javadb.mongodb.springboot.textsearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import java.util.List;

import static io.github.dunwu.javadb.mongodb.springboot.textsearch.util.ConsoleResultPrinter.printResult;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Christoph Strobl
 * @author Thomas Darimont
 */
@SpringBootTest(classes = {MongoTestConfiguration.class})
public class TextSearchTemplateTests {

    @Autowired
    MongoOperations operations;


    /**
     * Show how to do simple matching. Note that text search is case insensitive and will also find entries like
     * {@literal releases}.
     */
    @Test
    public void findAllBlogPostsWithRelease() {

        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny("release");
        List<BlogPost> blogPosts = operations.find(query(criteria), BlogPost.class);

        printResult(blogPosts, criteria);
    }

    /**
     * Sort by relevance relying on the value marked with
     * {@link org.springframework.data.mongodb.core.mapping.TextScore}.
     */
    @Test
    public void findAllBlogPostsByPhraseSortByScore() {

        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase("release");

        TextQuery query = new TextQuery(criteria);
        query.setScoreFieldName("score");
        query.sortByScore();

        List<BlogPost> blogPosts = operations.find(query, BlogPost.class);

        printResult(blogPosts, criteria);
    }
}
