redis_server=redis-server
redis_sentinel=redis-sentinel

pushd ..
$redis_server server-6380/redis.conf &
$redis_sentinel server-6380/sentinel.conf &
popd
