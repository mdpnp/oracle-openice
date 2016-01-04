# oracle-openice


A project to rapidly store some [OpenICE](https://www.openice.info) data in an Oracle database.

Prerequisites and Tools
=======================

In the lab we are using [Oracle Database Express Edition 11g Release 2](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html).

We converted the RedHat package for use on an Ubuntu server following [these](http://meandmyubuntulinux.blogspot.de/2012/05/installing-oracle-11g-r2-express.html) instructions.

For an Oracle client we are using the free [Oracle SQL Developer](http://www.oracle.com/technetwork/developer-tools/sql-developer/overview/index.html).


Getting Started
===============

1. Clone or download this repository. To download, click the "Download ZIP" button above. If you download the ZIP, the directory sturctures listed below will be `/oracle-openice-master/` instead of `/oracle-openice/`.

1. You must have the Oracle JDBC "thin" driver available. Download this yourself from [here](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) to ensure you agree to the terms of Oracle's licensing.  Unfortunately we are [not allowed](http://stackoverflow.com/questions/1074869/find-oracle-jdbc-driver-in-maven-repository/1074971#1074971) to re-distribute this driver ourselves; you must acquire it from Oracle directly.

1. You must have Apache Maven installed. Use your package manager of choice or download the .bin.zip directly from [Apache](https://maven.apache.org/download.cgi).

1. To add the oracle driver to a local Maven repo:
    ```bash
    mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3 -Dpackaging=jar -Dfile=<Path where the jar is, example downloads>/ojdbc6.jar -DgeneratePom=true
    ```
    
    On Windows, be sure to not include spaces in your directory path. E.g. `C:\Progra~1\...` instead of `C:\Program Files\...`

1. Create a configuration file `.config` containing configuration information.
    ```INI
    # Example Configuration
    # DDS Domain Identifier
    domain=15
    # oracle JDBC connection string
    url=jdbc:oracle:thin:@127.0.0.1:1521/XE
    # oracle username
    user=openice
    # oracle password
    pass=openice
    # DDS Partition information
    partition=MRN=14cd923b336
    ```

1. Generate an SQL Schema
    ```bash
    ./gradlew schemaGen
    ```
    
    On Windows:
    ```bash
    gradlew schemaGen
    ```
    
    This will create a file called `schema.sql` which you must apply to our oracle database.

    For convenience, we have also been committing the schema to the repo at [schema.sql](https://github.com/mdpnp/oracle-openice/blob/master/src/main/sql/schema.sql).  

1. Create a local installation of the software
    ```bash
    ./gradlew installDist
    ```
    
    On Windows:
    ```bash
    gradlew installDist
    ```

1. Run the software from your local installation
    ```bash
    build/install/oracle-openice/bin/oracle-openice
    ````
    On Windows:
    ```bash
    build\install\oracle-openice-master\bin\oracle-openice
    ```

1. Send `SIGINT` (Ctrl-C) when you would like to exit


