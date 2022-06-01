@echo off
::移动到当前脚本所在目录
::盘符
%~d0
::盘符加路径
cd %~dp0

::返回上级
cd ../

::项目代码路径
set CODE_HOME=%cd%

::设置依赖路径
set CLASSPATH="%CODE_HOME%/classes;%CODE_HOME%/lib/*"

::java可执行文件位置
set _EXECJAVA="java"

::logs
set LOG_DIR="%CODE_HOME%/logs"
::创建目录
md "%CODE_HOME%/logs"

::JVM 配置
::set JAVA_OPTS="-server -Xms128m -Xmx256m -XX:PermSize=128m -XX:MaxPermSize=256m -Dfile.encoding=UTF-8"

::启动类
set MAIN_CLASS=com.natodk.ALiYunDDNS
echo "============ Starting ..============== \n"
%_EXECJAVA% %JAVA_OPTS% -classpath %CLASSPATH% %MAIN_CLASS% > %LOG_DIR%/console.log
