package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.delegates.PartnerService;
import com.ascendix.jdbc.salesforce.resultset.CommandLogCachedResultSet;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;
import com.sforce.soap.partner.IError;
import com.sforce.soap.partner.ISaveResult;
import com.sforce.ws.ConnectionException;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpdateQueryProcessor {

    public static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    public static boolean isUpdateQuery(String soqlQuery, UpdateQueryAnalyzer queryAnalyzer) {
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            return false;
        }
        soqlQuery = soqlQuery.trim();

        return queryAnalyzer.analyse(soqlQuery);
    }

    public static ResultSet processQuery(ForcePreparedStatement statement, String soqlQuery, PartnerService partnerService, UpdateQueryAnalyzer updateQueryAnalyzer) {
        CommandLogCachedResultSet resultSet = new CommandLogCachedResultSet();
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            resultSet.log("No UPDATE query found");
            return resultSet;
        }

        try {
            List<Map<String, Object>> recordsToUpdate = updateQueryAnalyzer.getRecords();
            ISaveResult[] records = partnerService.saveRecords(updateQueryAnalyzer.getFromObjectName(), recordsToUpdate);
            for(ISaveResult result: records) {
                if (result.isSuccess()) {
                    resultSet.log(updateQueryAnalyzer.getFromObjectName()+" updated with Id="+result.getId());
                } else {
                    resultSet.addWarning(updateQueryAnalyzer.getFromObjectName()+" failed to update with error="+ Arrays.stream(result.getErrors()).map(IError::getMessage).collect(Collectors.joining(",")));
                }
            }
        } catch (ConnectionException e) {
            resultSet.addWarning("Failed request to update entities with error: "+e.getMessage());
            logger.log(Level.SEVERE,"Failed request to update entities with error: "+e.getMessage(), e);
        }
        return resultSet;
    }

}
