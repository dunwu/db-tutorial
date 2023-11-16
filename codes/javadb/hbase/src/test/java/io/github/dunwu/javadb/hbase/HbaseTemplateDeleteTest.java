package io.github.dunwu.javadb.hbase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hbase 删除测试
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-13
 */
public class HbaseTemplateDeleteTest {

    public static final String TABLE_NAME = "test:test";

    private static final HbaseTemplate HBASE_TEMPLATE;

    static {
        try {
            HBASE_TEMPLATE = HbaseFactory.newHbaseTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("删除单条记录")
    public void testDelete() throws IOException {
        String rowkey = "test-key-1";
        HBASE_TEMPLATE.delete(TABLE_NAME, rowkey);
    }

    @Test
    @DisplayName("批量删除记录")
    public void testBatchDelete() throws IOException, InterruptedException {
        List<String> rowkeys = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            rowkeys.add("test-key-" + i);
        }
        HBASE_TEMPLATE.batchDelete(TABLE_NAME, rowkeys.toArray(new String[0]));
    }

}
