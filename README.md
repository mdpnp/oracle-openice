# HiberDDS
A project to test the suitability of the Hibernate framework to be used manage DDS topics (a small experiment of the MDPnP team)
--------

To add the oracle driver to a local Maven repo:

mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3 -Dpackaging=jar -Dfile=<Path where the jar is, example downloads>/ojdbc6.jar -DgeneratePom=true

  <dependency>
        <groupId>com.oracle</groupId>
        <artifactId>ojdbc6</artifactId>
        <version>11.2.0.3</version>
    </dependency>

    