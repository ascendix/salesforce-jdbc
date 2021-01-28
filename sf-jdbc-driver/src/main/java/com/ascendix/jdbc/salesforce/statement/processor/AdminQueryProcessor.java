package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.ForceDriver;
import com.ascendix.jdbc.salesforce.delegates.PartnerService;
import com.ascendix.jdbc.salesforce.resultset.CommandLogCachedResultSet;
import com.ascendix.jdbc.salesforce.statement.ForcePreparedStatement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminQueryProcessor {

    /* ADMIN Queries
        Postgres LOGIN command  format
          https://www.postgresql.org/docs/9.1/ecpg-sql-connect.html
          https://docs.oracle.com/cd/E18283_01/server.112/e16604/ch_twelve015.htm

      Example: CONNECT TO tcp:postgresql://localhost/connectdb USER connectuser IDENTIFIED BY connectpw;
      Local Syntax: CONNECT [ TO <smthg>] USER <user-name> IDENTIFIED BY <password>;
     */
    private static final Pattern LOGIN_COMMAND_PG = Pattern.compile("CONNECT\\s+(TO\\s+(?<url>\\S+))?(\\s+USER\\s+(?<username>\\S+)\\s+IDENTIFIED BY\\s+(?<userpass>\\S+))?\\s*;?", Pattern.CASE_INSENSITIVE);

    private static final Pattern LOGIN_COMMAND_ORA = Pattern.compile("CONN(?:ECT)?\\s+(?<username>\\S+)/(?<userpass>\\S+)\\s*;?", Pattern.CASE_INSENSITIVE);


    public static boolean isAdminQuery(String soqlQuery) {
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            return false;
        }
        soqlQuery = soqlQuery.trim();

        Matcher matcherLogin = LOGIN_COMMAND_PG.matcher(soqlQuery);
        if (matcherLogin.matches()) {
            return true;
        }
        matcherLogin = LOGIN_COMMAND_ORA.matcher(soqlQuery);
        if (matcherLogin.matches()) {
            return true;
        }
        return false;
    }

    public static ResultSet processQuery(ForcePreparedStatement statement, String soqlQuery, PartnerService partnerService) throws SQLException {
        CommandLogCachedResultSet resultSet = new CommandLogCachedResultSet();
        if (soqlQuery == null || soqlQuery.trim().length() == 0) {
            resultSet.log("No SOQL or ADMIN query found");
            return resultSet;
        }
        soqlQuery = soqlQuery.trim();

        Matcher matcherLogin = LOGIN_COMMAND_PG.matcher(soqlQuery);
        if (!matcherLogin.matches()) {
            // if no PG command - also check for ORA
            matcherLogin = LOGIN_COMMAND_ORA.matcher(soqlQuery);
        }
        if (matcherLogin.matches()) {
            resultSet.log("Admin query: CONNECT ATTEMPT");
            String userName = matcherLogin.group("username");
            String userPass = matcherLogin.group("userpass");
            String url = matcherLogin.group("url");
            if (userName != null) {
                userName = userName.trim().replaceAll("^['\"]", "").replaceAll("['\"]$", "");
            }
            if (userPass != null) {
                userPass = userPass.trim().replaceAll("^['\"]", "").replaceAll("['\"]$", "");
            }
            if (url != null) {
                url = url.trim().replaceAll("^['\"]", "").replaceAll("['\"]$", "");
            }
            // evaluate credentials and url properties - just for logging
            String logUserName = userName;
            String logHost = "existing host";

            if (url != null) {
                try {
                    Properties props = ForceDriver.getConnStringProperties(url);
                    if (logUserName == null) {
                        logUserName = props.getProperty("user");
                    }
                    if (props.getProperty("loginDomain") != null) {
                        logHost = props.getProperty("loginDomain");
                    }
                } catch (IOException e) {
                    // No log as it's only for logging - will be triggered inside the reconnect operation
                }
            }

            try {
                boolean reconnected = statement.reconnect(url, userName, userPass);
                if (reconnected) {
                    resultSet.log("Admin query: CONNECTION SUCCESSFUL as " + logUserName + " to " + logHost);
                } else {
                    resultSet.log("Admin query: CONNECTION FAILED as " + logUserName + " to " + logHost);
                }
            } catch (Exception e) {
                resultSet.log("Admin query: CONNECTION ERROR as " + logUserName + " to " + logHost + " : " +e.getMessage());
                throw new SQLException("CONNECTION ERROR as " + logUserName + " to " + logHost + " : " +e.getMessage(), "08000", e);
            }
        }
        return resultSet;
    }
}
