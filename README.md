## Spark Streaming 流式计算Demo
主流程：消费 kafka 数据->流式计算两小时的统计指标->回写到结果库

本案例提供了 socket 消费的 debug demo

### streaming-base-properties
base模块demo配置文件
### streaming-base
Spark Streaming 上下文初始化、消费端实现以及相关工具类；提供了消费 socket 消息的 debug 模块
lib 文件夹下的文件是为了解决 spark、es 版本不兼容引起的文件缺失问题而重新打包的类库（POM 中有详细说明）
### streaming-service
Spark Streaming 业务模块，实现具体计算逻辑和回写
### socketsrv
提供了基于 socket 的随机消息生成的服务端

maven 打包命令
在 parent 目录下 输入 mvn clean package -Pprod -DskipTests
