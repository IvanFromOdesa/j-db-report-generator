package org.ivank.db.data;

import java.util.List;

public class TableData {
    private String tableName;
    private String pkKey;
    private List<String> columnNames;
    private List<RowData> rows;
    private List<FkKeyData> fkKeyData;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkKey() {
        return pkKey;
    }

    public void setPkKey(String pkKey) {
        this.pkKey = pkKey;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<RowData> getRows() {
        return rows;
    }

    public void setRows(List<RowData> rows) {
        this.rows = rows;
    }

    public List<FkKeyData> getFkKeyData() {
        return fkKeyData;
    }

    public void setFkKeyData(List<FkKeyData> fkKeyData) {
        this.fkKeyData = fkKeyData;
    }
}
