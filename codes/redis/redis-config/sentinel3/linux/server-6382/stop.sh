redis_cli=redis-cli

pushd ..
$redis_cli -p 26382 SHUTDOWN NOSAVE
$redis_cli -p 6382 SHUTDOWN NOSAVE
popd
