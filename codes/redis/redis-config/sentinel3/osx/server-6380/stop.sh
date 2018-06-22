redis_cli=../../bin/osx/redis-cli

pushd ..
$redis_cli -p 26380 SHUTDOWN NOSAVE
$redis_cli -p 6380 SHUTDOWN NOSAVE
popd
