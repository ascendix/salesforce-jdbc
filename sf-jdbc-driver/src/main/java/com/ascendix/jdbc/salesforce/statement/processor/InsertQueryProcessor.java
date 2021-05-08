package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.delegates.PartnerService;
import com.ascendix.jdbc.salesforce.resultset.CommandLogCachedResultSet;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InsertQueryProcessor {

    public static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    public static boolean isInsertQuery(String soqlQuery, InsertQueryAnalyzer queryAnalyzer) {
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            return false;
        }
        soqlQuery = soqlQuery.trim();

        return queryAnalyzer.analyse(soqlQuery);
    }

    public static ResultSet processQuery(ForcePreparedStatement statement, String soqlQuery, PartnerService partnerService, InsertQueryAnalyzer insertQueryAnalyzer) {
        CommandLogCachedResultSet resultSet = new CommandLogCachedResultSet();
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            resultSet.log("No INSERT query found");
            return resultSet;
        }

        try {
            int updateCount = 0;
            ISaveResult[] records = partnerService.createRecords(insertQueryAnalyzer.getFromObjectName(), insertQueryAnalyzer.getRecords());

            for(ISaveResult result: records) {
                if (result.isSuccess()) {
                    resultSet.log(insertQueryAnalyzer.getFromObjectName()+" created with Id="+result.getId());
                    updateCount++;
                } else {
                    resultSet.addWarning(insertQueryAnalyzer.getFromObjectName()+" failed to create with error="+ Arrays.stream(result.getErrors()).map(IError::getMessage).collect(Collectors.joining(",")));
                }
            }
            statement.setUpdateCount(updateCount);
            statement.setResultSet(resultSet);
        } catch (ConnectionException e) {
            resultSet.addWarning("Failed request to create entities with error: "+e.getMessage());
            logger.log(Level.SEVERE,"Failed request to create entities with error: "+e.getMessage(), e);
        }
        return resultSet;
    }

}
