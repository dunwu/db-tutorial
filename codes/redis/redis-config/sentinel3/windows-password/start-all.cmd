@echo off
SET redis_server=..\..\bin\windows\redis-server
SET redis_sentinel=..\..\bin\windows\redis-server
SET redis_cli=..\..\bin\windows\redis-cli

start %redis_server% server-6380\redis.conf
start %redis_sentinel% server-6380\sentinel.conf --sentinel

start %redis_server% server-6381\redis.conf
start %redis_sentinel% server-6381\sentinel.conf --sentinel

start %redis_server% server-6382\redis.conf
start %redis_sentinel% server-6382\sentinel.conf --sentinel

echo Press enter to see sentinel info on masters and slaves...
pause

%redis_cli% -p 26380 sentinel master mymaster
%redis_cli% -p 26381 sentinel slaves mymaster

echo Press enter again to close this window
pause
