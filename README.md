# sforce-jdbc
Salesforce JDBC driver allows Java programs to connect to a Salesforce data services using standard, database independent Java code. Is an open source JDBC driver written in Pure Java, 
and communicates over SOAP/HTTP(S) protocol.
The main purpose of the driver is to retrieve (only) data from Salesforce services for data analysis. Primary target platform for the driver usage is Eclipse BIRT engine.

The original Git repository for this driver is [here](https://github.com/ascendix/salesforce-jdbc)
However that version is not compatible with IntelliJ because of a lot of unsupported features:
* table names and columns names filtration is not implemented
* table name and column names are case sensitive
* no metadata provided for queries so IntelliJ just ignores the results returned by the driver

These issues were fixed in the current version in this fork.

[Watch the demo video](https://spuliaiev-sfdc.github.io/salesforce-jdbc/docs/SOQL-JDBC-IntelliJ-demo-264.mp4)

[![Watch the demo video](https://spuliaiev-sfdc.github.io/salesforce-jdbc/docs/intelliJ.png)](https://spuliaiev-sfdc.github.io/salesforce-jdbc/docs/SOQL-JDBC-IntelliJ-demo-264.mp4)

## Supported Salesforce and Java versions
The current version of the driver should be compatible with **Salesforce Partner API version 39.0 and higher** and **Java 8**.

## Get the driver
Download the driver JAR file:
1. Read-Only version 1.3.1 : from [here](https://spuliaiev-sfdc.github.io/salesforce-jdbc/deliverables/sf-jdbc-driver-1.3.1-SNAPSHOT-jar-with-dependencies.jar)
2. Write support version 1.4.0 : from [here](https://spuliaiev-sfdc.github.io/salesforce-jdbc/deliverables/sf-jdbc-driver-1.4.0-SNAPSHOT-jar-with-dependencies.jar)

## Supported features
1. Queries support native SOQL;
  ```SQL
   select Id, Account.Name, Owner.id, Owner.Name from Account
```
2. Nested queries are supported;
3. Write is supported as INSERT/UPDATE statements for version >= 1.4.0
   
   The following functions are supported as part of calculation of new values:
   * NOW()
   * GETDATE()
    
   For Example:
  ```SQL
   INSERT INTO Account(Name, Phone) VALUES 
    ('Account01', '555-123-1111'),
    ('Account02', '555-123-2222');
    
    INSERT INTO Contact(FirstName, LastName, AccountId) 
      SELECT Name, Phone, Id 
        FROM Account
        WHERE Name like 'Account0%';

    UPDATE Contact SET LastName = 'Updated_Now_'+NOW()
        WHERE AccountId IN (
                SELECT ID from Account where Phone = '555-123-1111'
        );
```
4. Request caching support on local drive. Caching supports 2 modes: global and session. Global mode means that the cached result will be accessible for all system users for certain JVM session. Session cache mode works for each Salesforce connection session separately. Both modes cache stores request result while JVM still running but no longer than for 1 hour. The cache mode can be enabled with a prefix of SOQL query. 

How to use:
 * Global cache mode:
  ```SQL
  CACHE GLOBAL SELECT Id, Name FROM Account
  ```
 * Session cache mode
  ```SQL
  CACHE SESSION SELECT Id, Name FROM Account
  ```
5. Reconnect to other organization at the same host
```SQL
-- Postgres Notation
CONNECT USER admin@OtherOrg.com IDENTIFIED by "123456"

-- Oracle Notation
CONNECT admin@OtherOrg.com/123456

-- Postgres Notation to a different host using secure connection (by default)
CONNECT 
    TO ap1.stmpa.stm.salesforce.com
    USER admin@OtherOrg.com IDENTIFIED by "123456"

-- Postgres Notation to a different host - local host using insecure connection
CONNECT 
    TO http://localhost:6109
    USER admin@OtherOrg.com IDENTIFIED by "123456"
```
P.S. You need to use the machine host name in the connection url - not MyDomain org host name.

## Limitations
1. ***Version < 1.4.0*** The driver is only for read-only purposes now. Insert/update/delete functionality is not implemented yet.
2. ***Version >= 1.4.0*** Limited support of INSERT/UPDATE operations


## With Maven

### Add repositories
    <repositories>
        <repository>
            <id>com.ascendix.maven</id>
            <name>Ascendix Maven Repo</name>
            <url>https://github.com/ascendix/mvnrepo/raw/master</url>
        </repository>
        <repository>
            <id>mulesoft-releases</id>
            <name>MuleSoft Releases Repository</name>
            <url>http://repository.mulesoft.org/releases/</url>
            <layout>default</layout>
        </repository>
    </repositories>

### Add dependency   
    <dependency>
        <groupId>com.ascendix.salesforce</groupId>
        <artifactId>sf-jdbc-driver</artifactId>
        <version>1.4.0-SNAPSHOT</version>
     </dependency>


## How to connect

### Driver class name
com.ascendix.jdbc.salesforce.ForceDriver 

### JDBC URL format
```
jdbc:ascendix:salesforce://[;propertyName1=propertyValue1[;propertyName2=propertyValue2]...]
```
There are two ways to connect to Salesforce:
1. by using _user_ and _password_;
2. by using _sessionId_.

_User_ and _password_ parameters are ignored if _sessionId_ parameter is set.

An example for a connection URL with _user_ and _password_ parameters: 
```
jdbc:ascendix:salesforce://;user=myname@companyorg.com.xre.ci;password=passwordandsecretkey
```
An example for a connection URL with _sessionId_ parameter: 
```
jdbc:ascendix:salesforce://;sessionId=uniqueIdAssociatedWithTheSession
```
### Configuration Properties
| Property | Description |
| --- | --- |
| _user_ | Login username. |
| _password_ | Login password is associated with the specified username. <br>**Warning!** A password provided should contain your password and secret key joined in one string.|
| _sessionId_ | Unique ID associated with this session. |
| _loginDomain_ | Top-level domain for a login request. <br>Default value is _login.salesforce.com_. <br>Set _test.salesforce.com_ value to use sandbox. |
| _https_ | Switch to use HTTP protocol instead of HTTPS <br>Default value is _true_|
| _api_ | Api version to use. <br>Default value is _50.0_. <br>Set _test.salesforce.com_ value to use sandbox. |
| _client_ | Client Id to use. <br>Default value is empty.  |
| _insecurehttps_ | Allow invalid certificates for SSL.  |

## Configure BIRT Studio to use Salesforce JDBC driver

1. [How to add a JDBC driver](https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.birt.doc%2Fbirt%2Fcon-HowToAddAJDBCDriver.html)
2. How to set configuration properties for Salesforce JDBC driver.

    Birt provides various ways to set parameters for JDBC driver. For example, it can be done with the property binding feature in the data source editor and a report parameter. 
  
    ![image](/docs/birt/Data%20source%20-%20property%20binding.png)
  
     See how it's done in [Salesforce JDBC report sample](docs/birt/Salesforce JDBC sample.rptdesign)
  

## Configure IntelliJ to use Salesforce JDBC driver

1. [How to add a JDBC driver](https://www.jetbrains.com/help/idea/data-sources-and-drivers-dialog.html)
2. How to set configuration properties for Salesforce JDBC driver.

    IntelliJ provides various ways to set parameters for JDBC driver. For example, it can be done with the property binding feature in the data source editor and a report parameter.
    Example JDBC Url:
    
    ```jdbc:ascendix:salesforce://dev@Local.org:123456@localorg.localhost.internal.salesforce.com:6109?https=false&api=48.0``` 
  
    Please check what kind of access do you have to your org - HTTP or HTTPS and the API version to use.
    Here is screenshot about results output and autocomplete support for SOQL queries in IntelliJ:
  
    ![image](/docs/Autocomplete-SOQL.png)
  

## In case of issues with the WSDL

Steps to update the partners.wsdl

1. Get and build https://github.com/forcedotcom/wsc
2. Run command:
   
   `java -jar target/force-wsc-50.0.0-uber.jar blt/app/main/core/shared/submodules/wsdl/src/main/wsdl/partner.wsdl sforce-partner.jar`
3. Copy the com.sforce.soap to the driver

## SOQL Parser

This project uses a bit modified version of MuleSoft SOQL Parser which also supports quotes around field names.
It could be obtained from here:  https://github.com/spuliaiev-sfdc/salesforce-soql-parser


## Version History

### 1.3.1.3
CONNECT command parsing fixes

### 1.3.1.0
Re-connection to a different host using CONNECT command

### 1.3.0.1
   Insecure HTTPS - disabling the SSL Certificate verification    

### 1.2.6.03
   Update force-partner-api to 51.0.0    

### 1.2.6.02
   Fields for SubSelects aliases

   Returning flat resultset for field

### 1.2.6.01
   Update force-partner-api to 50.0.0

   Implement parameters:
* loginDomain
* client
* https
* api 

   Implement missing JDBC methods which are required for JetBrains IntelliJ IDE

### Sponsors
[Ascendix Technologies Inc.](https://ascendix.com/) <img src="http://ww1.prweb.com/prfiles/2006/12/12/490667/ascendixlogo.jpg" width=100 align="right"/>



