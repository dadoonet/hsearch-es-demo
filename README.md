# Hibernate Search and Elasticsearch demo

This repository shows how to connect from a pure Hibernate application to elasticsearch.

## Scenarios

You can look at the different branches:

* [tree/00-legacy](pure hibernate)
* [tree/01-elasticsearch](direct connection to elasticsearch)
* [tree/02-hibernatesearch](connection to elasticsearch using hibernate search)

## Run!

To run the project:

```sh
git checkout 00-legacy
mvn clean install
```

You should see something like:

```
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ hibernate-search-with-elasticsearch-tia ---
[INFO] Building jar: /Users/dpilato/Documents/Elasticsearch/Talks/hbsearch-es/code/hsearch-es-demo/target/hibernate-search-with-elasticsearch-tia-1.0-SNAPSHOT.jar
[INFO] 
[INFO] --- maven-failsafe-plugin:2.19.1:integration-test (default) @ hibernate-search-with-elasticsearch-tia ---

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.hibernate.demos.HibernateSearchWithDbIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.105 sec - in org.hibernate.demos.HibernateSearchWithDbIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

```

## Important files

Main files you should look at:

* POM file: [pom.xml](pom.xml)
* Configuration: [src/main/resources/META-INF/persistence.xml](persistence.xml)
* Top entity: [src/main/java/org/hibernate/demos/hswithes/model/VideoGame.java](VideoGame.java)
* Tests: [src/test/java/org/hibernate/demos/HibernateSearchWithDbIT.java](HibernateSearchWithDbIT.java)

## Running elasticsearch

TODO: add the Docker recipe

## Resources

* [Elasticsearch Java guide](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)
* [Hibernate search documentation](http://docs.jboss.org/hibernate/search/5.6/reference/en-US/html/ch11.html)
