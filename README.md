# sforce-jdbc
Salesforce JDBC driver allows Java programs to connect to a Salesforce data services using standard, database independent Java code. Is an open source JDBC driver written in Pure Java, and communicates over SOAP/HTTP(S) protocol.
The main purpose of the driver is to retrieve (only) data from Salesforce services for data analysis. Primary target platform for the driver usage is Eclipse BIRT engine.

## Supported Salesforce and Java versions
The current version of the driver should be compatible with **Salesforce Partner API version 39.0 and higher** and **Java 8**.

## Get the driver
Download the driver [here](sfjdbc.jar)

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
- _user_. Login username.

- _password_. Login password associated with the specified username. 

  **Warning!** A password provided should contains your password and secret key joined in one string.

- _sessionId_. Unique ID associated with this session. 

- _loginDomain_. Top-level domain for a login request. Default value is _login.salesforce.com_. Set _test.salesforce.com_ value to use sandbox.

  


## Supported features
1. Queries support native SOQL;
2. Nested queries are supported;
3. Request caching support on local drive. Canching supports 2 modes: global and session. Global mode means that the cached result will be accessible for all system users for certain JVM session. Session cache mode works for each Salesforce connection session separately. Both modes cache stores request result while JVM still running but no longer than for 1 hour. The cache mode can be enabled with a prefix of SOQL query. How to use:
  * Global cache mode:
  ```SQL
  CACHE GLOBAL SELECT Id, Name FROM Account
  ```
  * Session cache mode
  ```SQL
  CACHE SESSION SELECT Id, Name FROM Account
  ```

## Limitations
1. The driver is only for read-only purposes now. No insert/udate/delete functionality implemented yet.

## Configure BIRT to use Salesforce JDBC driver

1. [How to add a JDBC driver](https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.birt.doc%2Fbirt%2Fcon-HowToAddAJDBCDriver.html)
2. How to set configuration properties for Salesforce JDBC driver.

  Birt provides various ways to set parameters for JDBC driver. For example, it can be done with the property binding feature in the data source editor and a report parameter. 
  
  [image](docs/birt/Data source - property binding.png)
  
  See how it works in [Salesforce JDBC sample](docs/birt/Salesforce JDBC sample.rptdesign)
  


### Sponsors
[Ascendix Technologies Inc.](https://ascendix.com/) <img src="http://ww1.prweb.com/prfiles/2006/12/12/490667/ascendixlogo.jpg" width=100 align="right"/>



