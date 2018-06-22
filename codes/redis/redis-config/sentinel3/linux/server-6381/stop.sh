redis_cli=redis-cli

pushd ..
$redis_cli -p 26381 SHUTDOWN NOSAVE
$redis_cli -p 6381 SHUTDOWN NOSAVE
popd
