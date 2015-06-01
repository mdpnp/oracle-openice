# HiberDDS


A project to store [OpenICE](https://www.openice.info) DDS data in an Oracle database (a small experiment of the MDPnP team)

Getting Started
===============

1. You must have the Oracle JDBC "thin" driver available. Download this yourself from [here](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) to ensure you agree to the terms of Oracle's licensing.

1. To add the oracle driver to a local Maven repo:
    ```bash
    mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3 -Dpackaging=jar -Dfile=<Path where the jar is, example downloads>/ojdbc6.jar -DgeneratePom=true
    ```

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
    ````
    This will create a file called `schema.sql` which you must apply to our oracle database.

1. Create a local installation of the software
    ```bash
    ./gradlew installDist
    ```

1. Run the software from your local installation
    ```bash
    build/install/HiberDDS/bin/HiberDDS
    ````

1. Send `SIGINT` (Ctrl-C) when you would like to exit


