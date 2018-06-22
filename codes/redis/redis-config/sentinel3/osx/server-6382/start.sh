redis_server=../../bin/osx/redis-server
redis_sentinel=../../bin/osx/redis-sentinel

pushd ..
$redis_server server-6382/redis.conf &
$redis_sentinel server-6382/sentinel.conf &
popd
