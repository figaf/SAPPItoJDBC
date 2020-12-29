For users using SAP PI/XI prior to 7.5 and wanting to support connection wiht MSSQL or for that matter other Databases this application has been created. It is pretty simple to configure and does not require you to make modification to your SAP XI/PI system. You can just change your communication channels from JDBC to HTTP. Then in your SM59 connection to point to the location of the proxy. Username/Password is sent as basic auth headders.

Supports:
- Select
- Sql Query

This application that acts as proxy, as in input it takes xml documents with sql
SQL statements and is able to connect to the database and execute them. Original issue
that it solves: one of our customers has legacy SAP installation and would like to 
fetch the data from MsSQL 2019 version

Prerequisites:

 - Installed java version 8
 - Installed gradle()
 - Installed git

Caveat: Application doesn’t contain any logic for the schema creation, so db schema shall be created upfront. 

Application is running by default on port 4444.

Customization application variables:
- SERVER_PORT: <you_value>, default 4444
- DB_URL: jdbc url where you MS SQL database is located. Default: jdbc:sqlserver://127.0.0.1:1433;databaseName=foo1;

## !Important! Potential charset issues. ## 

In case if targetOS is Windows, it’s required to add following parameter to the application start: 
`-Dfile.encoding=UTF-8` JVM in Windows has locale specific encodings and adding this parameter allows to handle encoding issues correctly. On macOS or  in the other systems, where UTF-8 is default charset there is no need to specify  this settings. 

#### Log configuration

Application is printing debug outputs to the log files and to the console. 
Log level 'Info' level is set by default, which means that only basic information 
has been captured. Log level might be overwritten by specifying following 
configuration parameter: `-Dlogging.level.root=DEBUG` ; in this case application 
will print detailed information to the files. 
Please note: application won’t print any detailed logs to the console. 

Samples for running application.

#### Case #1. Regular config(target OS windows):

`java -jar -Ddb.url="jdbc:sqlserver://192.168.1.2:1433;databaseName=temp1;" -Dfile.encoding=UTF-8 
saptojdbc-1.0.0.jar`
#### Case #2. Debug config(target OS windows):

`java -jar -Ddb.url="jdbc:sqlserver://192.168.1.2:1433;databaseName=temp1;" -Dfile.encoding=UTF-8 saptojdbc-0.0.1-SNAPSHOT.jar -Dlogging.level.root=DEBUG 
  saptojdbc-1.0.0.jar`
