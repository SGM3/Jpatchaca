# Jpatchaca

## Requirement
 - Java 6+
 - Maven

## How to MVN

1. Update dependencies

```
$ cd <path>
$ mvn deploy:deploy-file -Durl=<path>/jpatchaca-model/src/main/resources -Dfile=jpatchaca-model/src/main/resources/jira-soap-4.0.jar -DgroupId=com.dolby.jira.net -DartifactId=jira-soap -Dpackaging=jar -Dversion=4.0
```

2. Package jar file 

```
$ mvn package -Dmaven.test.skip=true
```

3. Build eclipse project

```
$ mvn eclipse:eclipse
```