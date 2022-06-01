# 脚本所在的上层目录
cd `dirname $0`/..
# 项目代码路径
export CODE_HOME=`pwd`
# 设置依赖路径
export CLASSPATH="$CODE_HOME/classes:$CODE_HOME/lib/*"
# java可执行文件位置
export _EXECJAVA="java"
# logs
export LOG_DIR="$CODE_HOME/logs"
mkdir -p "$CODE_HOME/logs"
#JVM 配置
#export JAVA_OPTS="-server -Xms128m -Xmx256m -XX:PermSize=128m -XX:MaxPermSize=256m -Dfile.encoding=UTF-8"
# 启动类
export MAIN_CLASS=com.natodk.ALiYunDDNS

printf "============ Starting $(date) ============== \n"
printf "exec command [tail -fn200 ../logs/console.log] to view starting log\n"
printf "================================================================\n"

$_EXECJAVA $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS > $LOG_DIR/console.log 2>&1 &
