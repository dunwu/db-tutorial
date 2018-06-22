redis_server=redis-server
redis_sentinel=redis-sentinel

pushd ..
$redis_server server-6381/redis.conf &
$redis_sentinel server-6381/sentinel.conf &
popd
