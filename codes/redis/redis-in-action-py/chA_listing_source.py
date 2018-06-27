
'''
# <start id="linux-redis-install"/>
~:$ wget -q http://redis.googlecode.com/files/redis-2.6.2.tar.gz    # 从http://redis.io/download下载最新版的Redis。本书写作时Redis的最新版为2.6版本。
~:$ tar -xzf redis-2.6.2.tar.gz                                     # 解压源码。
~:$ cd redis-2.6.2/
~/redis-2.6.2:$ make                                                # 编译Redis。
cd src && make all                                                  # 注意观察编译消息，
[trimmed]                                                           # 这里不应该看到错误。
make[1]: Leaving directory `~/redis-2.6.2/src'                      #
~/redis-2.6.2:$ sudo make install                                   # 安装Redis。
cd src && make install                                              # 注意观察安装消息，
[trimmed]                                                           # 这里不应该看到错误。
make[1]: Leaving directory `~/redis-2.6.2/src'                      #
~/redis-2.6.2:$ redis-server redis.conf                             # 启动Redis服务器。
[13792] 26 Aug 17:53:16.523 * Max number of open files set to 10032 # 通过日志确认Redis已经顺利启动。
[trimmed]                                                           #
[13792] 26 Aug 17:53:16.529 * The server is now ready to accept     #
connections on port 6379                                            #
# <end id="linux-redis-install"/>
'''

'''
# <start id="linux-python-install"/>
~:$ wget -q http://peak.telecommunity.com/dist/ez_setup.py          # 下载ez_setup模块
~:$ sudo python ez_setup.py                                         # 通过运行ez_setup模块来下载并安装 setuptools。
Downloading http://pypi.python.org/packages/2.7/s/setuptools/...    #
[trimmed]                                                           #
Finished processing dependencies for setuptools==0.6c11             #
~:$ sudo python -m easy_install redis hiredis                       # 通过运行setuptools的easy_install包来安装redis包以及hiredis包。
Searching for redis                                                 # redis包为Python提供了一个连接至Redis的接口。
[trimmed]                                                           #
Finished processing dependencies for redis                          #
Searching for hiredis                                               # hiredis包是一个C库，它可以提高Python的Redis客户端的速度。
[trimmed]                                                           #
Finished processing dependencies for hiredis                        #
~:$
# <end id="linux-python-install"/>
'''

'''
# <start id="mac-redis-install"/>
~:$ curl -O http://rudix.googlecode.com/hg/Ports/rudix/rudix.py     # 下载用于安装Rudix的引导脚本。
[trimmed]
~:$ sudo python rudix.py install rudix                              # 命令Rudix安装自身。
Downloading rudix.googlecode.com/files/rudix-12.6-0.pkg             # Rudix下载并安装它自身。
[trimmed]                                                           #
installer: The install was successful.                              #
All done                                                            #
~:$ sudo rudix install redis                                        # 命令Rudix安装Redis。
Downloading rudix.googlecode.com/files/redis-2.4.15-0.pkg           # Rudix下载并安装它自身。
[trimmed]                                                           #
installer: The install was successful.                              #
All done                                                            #
~:$ redis-server                                                    # 启动Redis服务器。
[699] 13 Jul 21:18:09 # Warning: no config file specified, using the# Redis使用默认配置启动并运行。
default config. In order to specify a config file use 'redis-server #
/path/to/redis.conf'                                                #
[699] 13 Jul 21:18:09 * Server started, Redis version 2.4.15        #
[699] 13 Jul 21:18:09 * The server is now ready to accept connections#
on port 6379                                                        #
[699] 13 Jul 21:18:09 - 0 clients connected (0 slaves), 922304 bytes#
in use                                                              #
# <end id="mac-redis-install"/>
'''

'''
# <start id="mac-python-install"/>
~:$ sudo rudix install pip                              # 通过Rudix安装名为pip的Python包管理器。
Downloading rudix.googlecode.com/files/pip-1.1-1.pkg    # Rudix正在安装pip。
[trimmed]                                               #
installer: The install was successful.                  #
All done                                                #
~:$ sudo pip install redis                              # 现在可以使用pip来为Python安装Redis客户端库了。
Downloading/unpacking redis                             # Pip正在为Python安装Redis客户端库。
[trimmed]                                               #
Cleaning up...                                          #
~:$
# <end id="mac-python-install"/>
'''

'''
# <start id="windows-python-install"/>
C:\Users\josiah>c:\python27\python                                      # 以交互模式启动Python。
Python 2.7.3 (default, Apr 10 2012, 23:31:26) [MSC v.1500 32 bit...
Type "help", "copyright", "credits" or "license" for more information.
>>> from urllib import urlopen                                          # 从urllib模块里面载入urlopen工厂函数。
>>> data = urlopen('http://peak.telecommunity.com/dist/ez_setup.py')    # 获取一个能够帮助你安装其他包的模块。
>>> open('ez_setup.py', 'wb').write(data.read())                        # 将下载后的模块写入磁盘文件里。
>>> exit()                                                              # 通过执行内置的exit()函数来退出Python解释器。

C:\Users\josiah>c:\python27\python ez_setup.py                          # 运行ez_setup辅助模块。
Downloading http://pypi.python.org/packages/2.7/s/setuptools/...        # ez_setup辅助模块会下载并安装setuptools，
[trimmed]                                                               # 而setuptools可以方便地下载并安装Redis客户端库。
Finished processing dependencies for setuptools==0.6c11                 #

C:\Users\josiah>c:\python27\python -m easy_install redis                # 使用setuptools的easy_install模块来下载并安装Redis。
Searching for redis                                                     #
[trimmed]                                                               #
Finished processing dependencies for redis                              #
C:\Users\josiah>
# <end id="windows-python-install"/>
'''


'''
# <start id="hello-redis-appendix"/>
~:$ python                                          # 启动Python，并使用它来验证Redis的各项功能是否正常。
Python 2.6.5 (r265:79063, Apr 16 2010, 13:09:56) 
[GCC 4.4.3] on linux2
Type "help", "copyright", "credits" or "license" for more information.
>>> import redis                                    # 导入Redis客户端库，如果系统已经安装了hiredis这个C加速库的话，那么Redis客户端库会自动使用hiredis。
>>> conn = redis.Redis()                            # 创建一个指向Redis的连接。
>>> conn.set('hello', 'world')                      # 设置一个值，
True                                                # 并通过返回值确认设置操作是否执行成功。
>>> conn.get('hello')                               # 获取刚刚设置的值。
'world'                                             #
# <end id="hello-redis-appendix"/>
'''
