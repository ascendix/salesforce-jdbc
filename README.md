# sforce-jdbc
Salesforce JDBC driver allows Java programs to connect to a Salesforce data services using standard, database independent Java code. Is an open source JDBC driver written in Pure Java, and communicates over SOAP/HTTP(S) protocol.
The main purpose of the driver is to retrieve (only) data from Salesforce services for data analysis. Primary target platform for the driver usage is Eclipse BIRT engine.

The original Git repository for this driver is [here](https://github.com/ascendix/salesforce-jdbc)
However that version is not compatible with IntelliJ because of a lot of unsupported features:
* table names and columns names filtration is not implemented
* table name and column names are case sensitive
* no metadata provided for queries so IntelliJ just ignores the results returned by the driver

These issues were fixed in the current version in this fork.

## Supported Salesforce and Java versions
The current version of the driver should be compatible with **Salesforce Partner API version 39.0 and higher** and **Java 8**.

## Get the driver
Download the driver [here](https://spuliaiev-sfdc.github.io/salesforce-jdbc/deliverables/sf-jdbc-driver-1.2-SNAPSHOT-jar-with-dependencies.jar)


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
        <artifactId>salesforce-jdbc</artifactId>
        <version>1.1-20180403.104727-1</version>
     </dependency>


## How to connect

### Driver class name
com.ascendix.jdbc.salesforce.ForceDriver 

### JDBC URL format
```
jdbc:ascendix:salesforce://[;propertyName1=propertyValue1[;propertyName2=propertyValue2]...]
```
There are two ways to connect to salesforce:
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
| _password_ |Login password associated with the specified username. <br>**Warning!** A password provided should contains your password and secret key joined in one string.|
| _sessionId_ | Unique ID associated with this session. |
| _loginDomain_ | Top-level domain for a login request. <br>Default value is _login.salesforce.com_. <br>Set _test.salesforce.com_ value to use sandbox. |
| _https_ | Switch to use HTTP protocol instead of HTTPS <br>Default value is _true_|
| _api_ | Api version to use. <br>Default value is _43.0_. <br>Set _test.salesforce.com_ value to use sandbox. |


## Supported features
1. Queries support native SOQL;
2. Nested queries are supported;
3. Request caching support on local drive. Caching supports 2 modes: global and session. Global mode means that the cached result will be accessible for all system users for certain JVM session. Session cache mode works for each Salesforce connection session separately. Both modes cache stores request result while JVM still running but no longer than for 1 hour. The cache mode can be enabled with a prefix of SOQL query. How to use:
  * Global cache mode:
  ```SQL
  CACHE GLOBAL SELECT Id, Name FROM Account
  ```
  * Session cache mode
  ```SQL
  CACHE SESSION SELECT Id, Name FROM Account
  ```

## Limitations
1. The driver is only for read-only purposes now. Insert/udate/delete functionality is not implemented yet.

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
  

### Sponsors
[Ascendix Technologies Inc.](https://ascendix.com/) <img src="http://ww1.prweb.com/prfiles/2006/12/12/490667/ascendixlogo.jpg" width=100 align="right"/>



