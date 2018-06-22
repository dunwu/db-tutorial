redis_server=../../bin/osx/redis-server
redis_sentinel=../../bin/osx/redis-sentinel

pushd ..
$redis_server server-6380/redis.conf &
$redis_sentinel server-6380/sentinel.conf &
popd
