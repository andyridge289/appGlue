package com.appglue.library;

public class AppGlueLibrary {
    public static String createTableString(String tableName, String[][] cols) {
        StringBuilder createTable = new StringBuilder(String.format("CREATE TABLE %s (", tableName));

        int length = cols.length - 1;
        for (int i = 0; i < length; i++) {
            createTable.append(String.format("%s %s,", cols[i][0], cols[i][1]));
        }
        createTable.append(String.format("%s %s)", cols[length][0], cols[length][1]));

        return createTable.toString();
    }

    public static String createIndexString(String tableName, String indexName, String[] cols) {
        StringBuilder createIndex = new StringBuilder(String.format("CREATE INDEX IF NOT EXISTS %s ON %s (",
                indexName,
                tableName));

        for (int i = 0; i < cols.length; i++) {
            if (i > 0)
                createIndex.append(",");
            createIndex.append(cols[i]);
        }

        return createIndex.append(")").toString();
    }

    /**
     * Builds a string to get all of the given columns in the given table.
     *
     * @param table   The table to get everything out of
     * @param columns The columns of said table
     * @return A string of SQL
     */
    public static String buildGetAllString(String table, String[][] columns) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            out.append(String.format("%s.%s AS %s_%s", table, columns[i][0], table, columns[i][0])).append(i < columns.length - 1 ? ", " : " ");
        }

        return out.toString();
    }
}
