# Hibernate Search and Elasticsearch demo

This repository shows how to connect from a pure Hibernate application to elasticsearch.

## Scenarios

You can look at the different branches:

* [pure hibernate](https://github.com/dadoonet/hsearch-es-demo/tree/00-legacy)
* [direct connection to elasticsearch](https://github.com/dadoonet/hsearch-es-demo/tree/01-elasticsearch)
* [connection to elasticsearch using hibernate search](https://github.com/dadoonet/hsearch-es-demo/tree/02-hibernatesearch)

## Run!

To run the project:

```sh
git checkout <00-legacy|01-elasticsearch|02-hibernatesearch>
mvn clean install
```

You should see something like:

```
[INFO] --- maven-failsafe-plugin:2.19.1:integration-test (default) @ hibernate-search-with-elasticsearch-tia ---

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.hibernate.demos.HibernateSearchWithDbIT
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.202 sec - in org.hibernate.demos.HibernateSearchWithDbIT

Results :

Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Important files

Main files you should look at:

* POM file: `pom.xml`
* Configuration: `src/main/resources/META-INF/persistence.xml`
* Top entity: `src/main/java/org/hibernate/demos/hswithes/model/VideoGame.java`
* Tests: `src/test/java/org/hibernate/demos/HibernateSearchWithDbIT.java`

## Running elasticsearch

When using docker, you can run the `es.sh` script. Basically it does:

```sh
#! /bin/sh

docker stop hsearch_es
docker rm hsearch_es
docker run -p 9200:9200 -p 9300:9300 -d --name hsearch_es elasticsearch:2.4
docker logs -f hsearch_es
```

When done, you can stop the docker instance:

```
docker stop hsearch_es
```

## Resources

* [Elasticsearch Java guide](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)
* [Hibernate search documentation](http://docs.jboss.org/hibernate/search/5.6/reference/en-US/html/ch11.html)
