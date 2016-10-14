#!/bin/bash

set -e

trap 'cleanup' EXIT

function cleanup
{
    rm -f $POM_NAME $POM_NAME.versionsBackup
}

POM_NAME=jpatchaca-pom.xml
cat <<POM_DOC > $POM_NAME
 <project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>objective</groupId>
    <artifactId>jpatchaca-launcher</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>org.jpatchaca</groupId>
            <artifactId>jpatchaca-system</artifactId>
            <version>LATEST</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
                <plugin>
                 <groupId>org.codehaus.mojo</groupId>
                 <artifactId>exec-maven-plugin</artifactId>
                 <configuration>
                 <executable>java</executable>
				 <arguments>
					 <argument>-classpath</argument>
					 <classpath />
					 <argument>main.Main</argument>
				 </arguments>
                </configuration>
            </plugin>
          </plugins>
    </build>
    <repositories>
		<repository>
			<id>objective-internal-releases</id>
			<name>Objective Internal Releases</name>
			<url>http://repo:8081/nexus/content/groups/internal/</url>
			<releases>
				<enabled>true</enabled>
				<checksumPolicy>fail</checksumPolicy>
			</releases>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>objective-internal-snapshots</id>
			<name>Objective Internal Snapshots</name>
			<url>http://repo:8081/nexus/content/groups/all-snapshots/</url>
			<releases>
				<enabled>false</enabled>
			</releases>
			<snapshots>
				<enabled>true</enabled>
				<checksumPolicy>fail</checksumPolicy>
			</snapshots>
		</repository>
     </repositories>
 </project>
POM_DOC

echo Baixando JPatchaca. Isso pode levar um tempo, aguarde por favor.

mvn -q -f $POM_NAME exec:exec
