# JuiceFS 搭建与挂载

## Reference

[官方 github 文档 快速上手](https://github.com/juicedata/juicefs/blob/main/docs/zh_cn/getting-started/_quick_start_guide.md)

[官方 github 文档 分布式](https://github.com/juicedata/juicefs/blob/main/docs/zh_cn/getting-started/for_distributed.md#1-安装客户端)

## Platform

vlab 网络环境复杂，端口开启还得申请，建议不要用

以下用[阿里云开发者成长计划下的学生专享云服务器 ECS](https://developer.aliyun.com/plan/grow-up)，Ubuntu 20.04 x86_64

## JuiceFS 简介

通过阅读[JuiceFS 的技术架构](https://github.com/juicedata/juicefs/blob/main/docs/zh_cn/introduction/architecture.md)可以了解到，JuiceFS 是一个数据与元数据分离的分布式文件系统，以对象存储作为主要的数据存储，以 Redis、PostgreSQL、MySQL 等数据库作为元数据存储。

目前，JuiceFS有两个版本：一个是2021年刚刚开源的版本，也就是[GitHub上的版本](https://github.com/juicedata/juicefs)，需要自己在服务器上搭建；另一个是[软件即服务版本](https://juicefs.com/)，由其开发公司直接提供服务。两种版本有不同的客户端，不能混用。在DisGraFS中，我们采用开源版本，在一个阿里云服务器上自己搭建。

## Steps

### 安装 FUSE

vlab 默认不开启，叫助教开启即可

阿里云服务器自带

要自己手动安装 fuse 的话，参考教程：

https://github.com/libfuse/libfuse

https://www.jianshu.com/p/040bb60aa468

### 安装 JuiceFS 客户端

DisGraFS 已经给了压缩包，无需再在官网上下载（除非版本太旧）

在 DisGraFS 目录下：

```bash
$ cd JuiceFS/JuiceFS-Mounting/
$ tar -xzvf juicefs-0.15.2-linux-amd64.tar.gz
$ sudo install juicefs /usr/local/bin # 这里 install 的用法应该类似于 cp 命令
$ rm -rf LICENSE README_CN.md juicefs README.md

# 查看是否安装成功
$ which juicefs
/usr/local/bin/juicefs
```

### 准备 Redis 数据库

不用阿里云的 Redis，自己搭 (毕竟要钱)

先安装 Redis：

```bash
$ sudo apt update      # 确保软件包索引是最新的
$ sudo apt-get upgrade # 更新已安装的包
$ sudo apt install build-essential tcl
$ sudo apt-get install redis-server
$ which redis-server
/usr/bin/redis-server
$ which redis-cli
/usr/bin/redis-cli	
```

Redis 相关基本操作：

```shell
redis-server             # 启动 redis-server，关闭命令行窗口好像也还是会运行着
# redis-server &         # 以后台程序方式运行
ps -ef | grep redis      # 检测后台进程是否存在, redis-server *:6379 的 * 表示任意 ip 地址都可连接
netstat -ntpl |grep 6379 # 检测 6379 端口是否在监听
redis-cli                # 使用客户端检测连接是否正常
127.0.0.1:6379> ping
PONG
127.0.0.1:6379> SHUTDOWN # 终止 redis-server
not connected> quit
```

还可以直接通过下面的命令停止/启动/重启 redis:

```shell
/etc/init.d/redis-server stop
/etc/init.d/redis-server start
/etc/init.d/redis-server restart

redis-cli -h 127.0.0.1 -p 6379 shutdown

# 如果都不行：
kill -9
```

Redis 默认只能 localhost 访问，要外部访问需要配置

首先修改配置文件 /etc/redis/redis.conf，有三步：

* 把所有 bind 注释掉
* requirepass 可以设置个密码（非必须）
* protected-mode 设置为 no

然后配置防火墙，开放 6379 端口，如果是内网访问，只需要配置 Ubuntu 本身的防火墙，而如果是公网访问，除了配置 Ubuntu 本身的防火墙外，还要配置阿里云的防火墙，即走公网的话有两层墙，一层 Ubuntu 的，一层是第三方阿里云的

Ubuntu 的配置：阿里云默认防火墙关闭：

```bash
$ sudo ufw status
Status: inactive
```

要是要手动开放的话自己去找文档吧

阿里云的配置：进入云服务器管理控台–>网络与安全–>安全组–>点击配置规则–>放行 6379 端口，重启一下服务器即可

然后 /etc/init.d/redis-server stop 停止原来的 Redis，再 redis-server /etc/redis/redis.conf 重启（要指定配置文件路径）

此时便可通过 localhost、内网或公网登陆

查询内网 ip：

```bash
$ ip addr
```

查询外网 ip:

```bash
$ curl ifconfig.me
```

登陆 redis：

```bash
# localhost
$ redis-cli
127.0.0.1:6379> auth 123
OK
127.0.0.1:6379> ping
PONG
127.0.0.1:6379> quit

# 内网
$ redis-cli -h <内网 ip>

# 公网
$ redis-cli -h <公网 ip>
```

### 准备对象存储 OSS

可参考文档：[JuiceFS 如何设置对象存储](https://github.com/juicedata/juicefs/blob/main/docs/zh_cn/reference/how_to_setup_object_storage.md#juicefs-如何设置对象存储)

使用[阿里云 OSS 服务](https://www.aliyun.com/product/oss)

购买 OSS 个人新用户免费试用 3 个月的标准存储包，再开通 OSS 服务，再创建 Bucket（详见官网教程）

创建 Bucket 中有一个日志功能，不清楚是否对监控有帮助，暂未开启，有需要可开启

一般而言，对象存储通过 `Access Key ID` 和 `Access Key Secret` 验证用户身份，对应到 JuiceFS 文件系统就是 `--access-key` 和 `--secret-key` 这两个选项，故之后需要创建 `Access key` 和 `Access Key Secret`, [参考教程](https://help.aliyun.com/document_detail/38738.html)

### 创建文件系统

```bash
$ juicefs format --storage oss --bucket https://disgrafs-tobedone-1.oss-cn-hangzhou.aliyuncs.com --access-key <access key id> --secret-key <access key secret> redis://:<redis 数据库密码>@<redis 服务器公网 ip>:6379/1 disgrafs-tobedone-jfs-1
```

### 挂载文件系统

* Linux:

大部分 Linux 自带 FUSE，但如果没有，需要先手动安装

```bash
$ juicefs mount redis://:<redis 数据库密码>@<redis 服务器公网 ip>/1 mnt -v --writeback # mnt 是挂载目录，自定
```

-v 表示显示调试信息，--writeback 开启针对小文件上传的写回优化

`Ctrl + C` 结束程序即可自动卸载, 原挂载文件夹可以删除

* Windows:

没试过，下面是上一届的说法

由于 Windows 不自带 FUSE，需要先手动安装 `winfsp`

```bash
$ juicefs mount redis://:<redis 数据库密码>@<redis 服务器公网 ip>/1 Z: -v --writeback
```

Z: 表示挂载到 Z 盘，Windows 下必须挂载到某个空闲的盘符，不能指定任意文件夹

在 cmd 中按下 `Ctrl + C` 结束程序，即可自动卸载

* MacOS

没装好，抱歉

目前已知需要先安装[macFUSE](https://osxfuse.github.io/)