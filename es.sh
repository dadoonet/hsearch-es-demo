#! /bin/sh

docker stop hsearch_es
docker rm hsearch_es
docker run -p 9200:9200 -p 9300:9300 -d --name hsearch_es elasticsearch:2.4
docker logs -f hsearch_es
