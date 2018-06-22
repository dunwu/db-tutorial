@echo off
SET redis_cli=..\..\bin\windows\redis-cli

pushd ..
%redis_cli% -p 26380 SHUTDOWN NOSAVE
%redis_cli% -p 6380 SHUTDOWN NOSAVE
popd
