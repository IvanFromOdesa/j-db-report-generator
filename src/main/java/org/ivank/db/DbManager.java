package org.ivank.db;

import org.ivank.Logger;
import org.ivank.Properties;
import org.ivank.db.data.DbData;
import org.ivank.db.data.FkKeyData;
import org.ivank.db.data.RowData;
import org.ivank.db.data.TableData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DbManager {
    private static Connection connection;
    private static String tableLimit;
    private static String rowLimit;
    private static final String SELECT_ALL = "Select * from ";

    public static void connect(Properties properties) {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:" + properties.getDbName() + "://" + properties.getDbPath(), properties.getUser(), properties.getPassword());
                if (properties.isTableLimitPresent()) {
                    tableLimit = properties.getTableLimit();
                }
                if (properties.isRowLimitPresent()) {
                    rowLimit = properties.getRowLimit();
                }
            } catch (SQLException e) {
                errorResponse(e);
            }
        }
    }

    private static void errorResponse(SQLException e) {
        Logger.error(e);
        throw new RuntimeException(e);
    }

    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    public static DbData loadDbData() {
        // Since JDBC 4.0, Class.forName is not needed
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            DbData dbData = new DbData();
            List<String> tableNames = loadTableNames(connection.getCatalog(), metaData);
            Logger.info("Loading tables data started.");
            dbData.setTableData(loadTableData(tableNames, metaData));
            Logger.info("Loading tables data finished.");
            return dbData;
        } catch (SQLException e) {
            errorResponse(e);
        }
        return null;
    }

    public static Optional<RowData> loadByFkKey(String fkKeyValue, List<String> columnNames, FkKeyData fkKeyData) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SELECT_ALL + fkKeyData.fkKeyTable() + " where " + fkKeyData.fkKey() + "=" + fkKeyValue);
            List<RowData> rowList = getRowList(columnNames, resultSet);
            return Optional.ofNullable(rowList.get(0));
        } catch (SQLException e) {
            errorResponse(e);
        }
        return Optional.empty();
    }

    private static List<String> loadTableNames(String catalog, DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = metaData.getTables(catalog, null, "%", new String[] {"TABLE"});
        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            tableNames.add(tableName);
            Logger.info("Found table: " + tableName);
        }
        return tableNames;
    }

    private static List<TableData> loadTableData(List<String> tableNames, DatabaseMetaData metaData) throws SQLException {
        List<TableData> tableDataList = new ArrayList<>();
        for (int count = 0; count < tableNames.size(); count ++) {
            String tableName = tableNames.get(count);
            TableData tableData = new TableData();
            List<String> columnNames = new ArrayList<>();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("COLUMN_NAME"));
            }
            tableData.setTableName(tableName);
            tableData.setColumnNames(columnNames);
            tableData.setRows(loadRowData(tableName, columnNames, metaData.getConnection()));
            tableData.setFkKeyData(getFkKeyData(metaData, tableName));
            tableData.setPkKey(getPkKey(metaData, tableName));
            tableDataList.add(tableData);
            if (tableLimit != null && count >= Integer.parseInt(tableLimit)) {
                Logger.warning(String.format("Limit %s tables.", tableLimit));
                break;
            }
        }
        return tableDataList;
    }

    private static List<FkKeyData> getFkKeyData(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet resultSet = metaData.getExportedKeys(metaData.getConnection().getCatalog(), null, tableName);
        List<FkKeyData> fkKeyData = new ArrayList<>();
        while (resultSet.next()) {
            fkKeyData.add(new FkKeyData(resultSet.getString("FKTABLE_NAME"), resultSet.getString("FKCOLUMN_NAME")));
        }
        return fkKeyData;
    }

    private static String getPkKey(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet resultSet = metaData.getPrimaryKeys(null, null, tableName);
        String res = "";
        while (resultSet.next()) {
            res = resultSet.getString("COLUMN_NAME");
        }
        return res;
    }

    private static List<RowData> loadRowData(String tableName, List<String> columnNames, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL + tableName + (rowLimit != null ? " limit " + rowLimit : "") + ";");
        List<RowData> rowData = getRowList(columnNames, resultSet);
        statement.close();
        return rowData;
    }

    private static List<RowData> getRowList(List<String> columnNames, ResultSet resultSet) throws SQLException {
        List<RowData> rowData = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, String> row = new HashMap<>();
            for (String columnName : columnNames) {
                String data = resultSet.getString(columnName);
                if (data != null) {
                    row.put(columnName, data);
                }
            }
            rowData.add(new RowData(row));
        }
        return rowData;
    }
}
