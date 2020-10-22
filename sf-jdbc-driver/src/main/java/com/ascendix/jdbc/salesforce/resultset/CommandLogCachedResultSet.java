package com.ascendix.jdbc.salesforce.resultset;

import com.ascendix.jdbc.salesforce.metadata.ColumnMap;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;

import java.util.*;
import java.util.stream.Collectors;

public class CommandLogCachedResultSet extends CachedResultSet {

    public static String LOG_COLUMN = "Log";

    private static ColumnMap<String, Object> DEFAULT_COLUMN_MAP = new ColumnMap<String, Object>().add(LOG_COLUMN,"Value");

    public CommandLogCachedResultSet() {
        super(new ColumnMap<>());
    }

    public CommandLogCachedResultSet(List<String> commandLog) {
        super(commandLog.stream().map( logLine -> new ColumnMap<String, Object>().add(LOG_COLUMN, logLine)).collect(Collectors.toList()), ForcePreparedStatement.dummyMetaData(DEFAULT_COLUMN_MAP));
    }

    public void log(String logLine) {
        addRow(new ColumnMap<String, Object>().add(LOG_COLUMN, logLine));
    }

}
