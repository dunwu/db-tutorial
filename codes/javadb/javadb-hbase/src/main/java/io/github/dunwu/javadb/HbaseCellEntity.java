package io.github.dunwu.javadb;

/**
 * HBase Cell 实体
 *
 * @author Zhang Peng
 * @date 2019-03-04
 */
public class HbaseCellEntity {
    private String table;
    private String row;
    private String colFamily;
    private String col;
    private String val;

    public HbaseCellEntity() {}

    public HbaseCellEntity(String row, String colFamily, String col, String val) {
        this.row = row;
        this.colFamily = colFamily;
        this.col = col;
        this.val = val;
    }

    public HbaseCellEntity(String table, String row, String colFamily, String col, String val) {
        this.table = table;
        this.row = row;
        this.colFamily = colFamily;
        this.col = col;
        this.val = val;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColFamily() {
        return colFamily;
    }

    public void setColFamily(String colFamily) {
        this.colFamily = colFamily;
    }

    public String getCol() {
        return col;
    }

    public void setCol(String col) {
        this.col = col;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "HbaseCellEntity{" + "table='" + table + '\'' + ", row='" + row + '\'' + ", colFamily='" + colFamily
            + '\'' + ", col='" + col + '\'' + ", val='" + val + '\'' + '}';
    }
}
