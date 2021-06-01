# java-zookeeper-demo
Application: Maven Apache ZooKepper demo 


#### Features
- ZooKeeper server
- Auto-healing with new leader election

## 

## Application Stack

Stack  | version |
--- | --- |  
*Java* | 1.8
*Frontend* | n/a
*Build Tool* | Maven
*Build env* | java jar

## ZooKeeper Set-up 
`mkdir <ZOOKEEPER_HOME>/logs`

`vim <ZOOKEEPER_HOME>/cong/zoo.cfg && dataDir=<ZOOKEEPER_HOME>/logs`

`cd <ZOOKEEPER_HOME>/bin && ./zkServer.sh start`

`cd <ZOOKEEPER_HOME>/bin/zkCli.sh && create /election ""`

## Application Run

`mvn clean package`

`jenv shell 11.0.2`

`java -jar <PROJECT_ROOT>target/leader.election-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Application GIT branches
- main

## Further enhancements 