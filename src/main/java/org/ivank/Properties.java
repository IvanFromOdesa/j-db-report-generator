package org.ivank;

import org.ivank.ex.NullInstanceException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Properties {
    private static final String DB_NAME = "dbName";
    private static final String DB_PATH = "dbPath";
    private static final String FORMAT = "format";
    private static final String PASSWORD = "password";
    private static final String USER = "user";
    private static final String OUTPUT =  "output";
    private static final String TABLE_LIMIT = "tables.limit";
    private static final String ROW_LIMIT = "rows.limit";
    private static final String SEPARATOR = "=";

    private final String dbName;
    private final String dbPath;
    private final String password;
    private final String user;
    private final String format;
    private final String output;
    private final String tableLimit;
    private final String rowLimit;

    private static Properties properties;

    public static Properties getInstance(String... path) {
        if (properties == null) {
            if (path.length == 0) {
                throw new NullInstanceException("No path specified.");
            }
            properties = init(path[0]);
        }
        return properties;
    }

    private Properties(String dbName, String dbPath, String password, String user, String format, String output, String tableLimit, String rowLimit) {
        this.dbName = dbName;
        this.dbPath = dbPath;
        this.password = password;
        this.user = user;
        this.format = format;
        this.output = output;
        this.tableLimit = tableLimit;
        this.rowLimit = rowLimit;
    }

    private static Properties init(String propertiesPath) {
        String dbName = "", dbPath = "",
                password = "", user = "",
                format = "",
                output = "",
                tableLimit = "", rowLimit = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(propertiesPath)), 4 * 1024)) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith(DB_NAME)) {
                    dbName = readProperty(s);
                } else if (s.startsWith(DB_PATH)) {
                    dbPath = readProperty(s);
                } else if (s.startsWith(PASSWORD)) {
                    password = readProperty(s);
                } else if (s.startsWith(USER)) {
                    user = readProperty(s);
                } else if (s.startsWith(FORMAT)) {
                    format = readProperty(s);
                } else if (s.startsWith(OUTPUT)) {
                    output = readProperty(s);
                } else if (s.startsWith(TABLE_LIMIT)) {
                    tableLimit = readProperty(s);
                } else if (s.startsWith(ROW_LIMIT)) {
                    rowLimit = readProperty(s);
                }
            }
            validateBlank(dbName, dbPath, password, user, format, output);
            validateLimits(tableLimit, rowLimit);
        } catch (FileNotFoundException e) {
            Logger.error("File not found: " + propertiesPath, e);
        } catch (IOException e) {
            Logger.error(e);
        }
        return new Properties(dbName, dbPath, password, user, format, output, tableLimit, rowLimit);
    }

    private static void validateLimits(String tableLimit, String rowLimit) {
        String [] toCheck = new String[2];
        if (!Objects.equals(tableLimit, "")) {
            toCheck[0] = tableLimit;
        }
        if (!Objects.equals(rowLimit, "")) {
            toCheck[1] = rowLimit;
        }
        validateNumber(Arrays.stream(toCheck).filter(Objects::nonNull).toArray(String[]::new));
    }

    private static void validateBlank(String... properties) {
        List<String> errors = new ArrayList<>();
        for (String property : properties) {
            if (property.isEmpty()) {
                errors.add(property);
            }
        }
        if (!errors.isEmpty()) {
            RuntimeException e = new RuntimeException();
            Logger.error("Properties empty: " + errors, e);
            throw e;
        }
    }

    private static void validateNumber(String... properties) {
        List<String> errors = new ArrayList<>();
        for (String property : properties) {
            if (!property.matches("^-?\\d+$")) {
                errors.add(property);
            }
        }
        if (!errors.isEmpty()) {
            RuntimeException e = new RuntimeException();
            Logger.error("Not numbers: " + errors, e);
            throw e;
        }
    }

    private static String readProperty(String line) {
        return line.substring(line.indexOf(SEPARATOR) + 1).trim();
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getFormat() {
        return format;
    }

    public String getOutput() {
        return output;
    }

    public String getTableLimit() {
        return tableLimit;
    }

    public String getRowLimit() {
        return rowLimit;
    }

    public boolean isTableLimitPresent() {
        return tableLimit != null && !"".equals(tableLimit);
    }

    public boolean isRowLimitPresent() {
        return rowLimit != null && !"".equals(rowLimit);
    }

    public String getCatalog() {
        return dbPath.substring(dbPath.indexOf("/") + 1);
    }
}
