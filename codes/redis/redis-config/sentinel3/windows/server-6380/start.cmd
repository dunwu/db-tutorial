@echo off
SET redis_server=..\..\bin\windows\redis-server
SET redis_sentinel=..\..\bin\windows\redis-server

pushd ..
start %redis_server% server-6380/redis.conf
start %redis_sentinel% server-6380/sentinel.conf --sentinel
popd
