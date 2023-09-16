package org.ivank.db.data;

import java.util.List;
import java.util.Optional;

public class DbData {

    private List<TableData> tableData;

    public List<TableData> getTableData() {
        return tableData;
    }

    public void setTableData(List<TableData> tableData) {
        this.tableData = tableData;
    }

    // Alternatively looks for the data in loaded dbdata.
    // TODO: compare efficiency with db call
    public static Optional<RowData> byFkKey(DbData dbData, String fkKeyValue, FkKeyData fkKeyData) {
        return dbData.getTableData().stream()
                .filter(t -> t.getTableName().equals(fkKeyData.fkKeyTable()))
                .flatMap(t -> t.getRows().stream().filter(r -> r.row().get(fkKeyData.fkKey()).equals(fkKeyValue)))
                .findAny();
    }
}
