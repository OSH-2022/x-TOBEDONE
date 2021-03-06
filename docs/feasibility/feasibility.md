# 可行性报告

## 目录

- [可行性报告](#可行性报告)
  - [目录](#目录)
  - [项目介绍](#项目介绍)
  - [理论依据](#理论依据)
    - [监控系统的含义与作用](#监控系统的含义与作用)
    - [DisGraFS 是什么](#disgrafs-是什么)
    - [DisGraFS 中存在的问题](#disgrafs-中存在的问题)
    - [Reed-Solomon 编码](#reed-solomon-编码)
      - [编解码原理](#编解码原理)
        - [编码](#编码)
        - [解码](#解码)
      - [编码矩阵](#编码矩阵)
        - [基于范德蒙德（Vandermonde）矩阵](#基于范德蒙德vandermonde矩阵)
        - [基于柯西（Cauchy）矩阵](#基于柯西cauchy矩阵)
        - [柯西编解码过程优化](#柯西编解码过程优化)
    - [监控的意义——工业界中的监控系统](#监控的意义工业界中的监控系统)
      - [分布式监控 CAT](#分布式监控-cat)
      - [伴鱼数据库监控系统](#伴鱼数据库监控系统)
      - [Open-falcon](#open-falcon)
      - [Zabbix](#zabbix)
  - [技术依据](#技术依据)
    - [DisGraFS 的系统架构](#disgrafs-的系统架构)
    - [中央索引服务器](#中央索引服务器)
      - [WebSockets](#websockets)
      - [asyncio](#asyncio)
    - [neo4j](#neo4j)
      - [储存结构](#储存结构)
      - [Cypher查询语言](#cypher查询语言)
      - [图算法](#图算法)
    - [Ray](#ray)
    - [Docker 容器化服务端](#docker-容器化服务端)
    - [文件编解码](#文件编解码)
    - [Prometheus](#prometheus)
    - [InfluxDB](#influxdb)
    - [Grafana](#grafana)
    - [Lua](#lua)
      - [协同程序](#协同程序)
  - [技术路线](#技术路线)
  - [参考内容](#参考内容)

## 项目介绍

我们这个项目是基于 2021 年的 x-DisGraFS 以及 2020 年的 x-dontpanic 展开。DisGraFS 是一个分布式图文件系统，该项目学习总结了当今主流几个分布式文件系统的优点，并且将文件标签与图结构联系起来描述文件之间的关系，统一了单机文件系统和分布式系统的优点。该项目将图结构与思想应用于分布式文件系统上面，使得分布式图文件系统兼具图文件系统方便用户快速搜索、模糊搜索、查找相关文件的特点以及分布式文件系统的海量文件存储、云存储的特点。dontpanic 实现了容器化、高可用性的分布式文件系统。

DisGraFS 并未完全实现其最初的设想，它的远程存储集群并没有真正搭建起来，DisGraFS 实际的实现中要求用户要将存储节点挂载到本地，形成一个所谓的客户端，这一项增加了用户的使用难度。本项目将以 2020 年的 dontpanic 为参考，搭建远程分布式存储集群，实现远程通信和远程文件传输。

此外，DisGraFS 缺少必要的监控组件，而监控在整个系统的开发运维中具有重要的作用，它可以对服务、系统、平台运行状态实时监控，收集运行信息，分析结果并预知存在的故障风险，一旦发生故障第一时间发出告警信息，最终保证系统持续、稳定、安全运行。本项目将致力于在 DisGraFS 上安装分布式监控系统，了解系统的运行状态，保证服务的正确且稳定运行。



## 理论依据

### 监控系统的含义与作用

分布式监控是部署在分布式系统内的监控组件，它可以监视和显示集群中各节点的状态信息，它有运行在各个节点的进程，可以采集不同节点之间的通信消息，采集各个节点的资源利用率，最后将采集到的数据汇总到一个数据库，进行分析处理后以直观的图形化界面进行呈现。

监控系统的功能主要包括：

* 对服务、系统、平台运行状态实时监控，保证系统能安全稳定地运行
* 收集运行信息，分析结果并预知存在的故障风险，一旦发生故障需要第一时间发出告警信息
* 通过监控数据确定故障发生位置，协助生成解决方案
* 监控数据可视化，便于数据统计、导出和分析

### DisGraFS 是什么

DisGraFS 是由 [OSH-2021 x-DisGraFS 小组](https://github.com/OSH-2021/x-DisGraFS) 构建一个分布式图文件系统，将图结构的思想应用于分布式文件系统上面，使其兼顾图文件系统方便用户快速搜索、模糊搜索、查找相关文件的特点，以及分布式文件系统的海量文件存储、云存储的特点。

### DisGraFS 中存在的问题

1. DisGraFS 的远程存储集群实际没有实现。按照 DisGraFS 本来的设想，当整个分布式文件系统搭建运行起来以后，用户只需要进入到 DisGraFS 提供的网页，登陆验证通过后，即可看到整个图文件系统，并在网页上实现文件的上传、移动和删除。而 DisGraFS 在具体实现中，为了简化工作量（工作量确实非常大），DisGraFS 小组将远程存储节点进行了简化，远程存储集群的 Redis 数据库仅有一台服务器（OSS 采用阿里云提供的服务，或许存在分布式集群），而本地用户需要将这个存储集群挂载到本地，才能实现文件的传输。我们计划完成他们最初的设想，重新搭建存储集群，这将是一个真正具有多个存储节点的、远程的集群，用户的所有操作仅需要在网页端执行。
2. DisGraFS 并不具备可运维性，也没有进行可用性的保障，它的日志文件分散，开发人员很难同时获得整个系统的运行状态，也很难看到各个节点的资源使用情况，甚至无法及时了解某个物理节点是否还处于连接状态，这使得它的可用性不能得到保证。这也是我们要搭建监控组件的原因。

### Reed-Solomon 编码

纠删码（Erasure Code）是一种编码技术。它通过计算将 n 份原始数据增加至 n+m 份数据，并能由其中的任意 n 份数据还原出原始数据，即可以容忍不多于 m 份的数据失效。纠删码主要应用在网络传输中，用以提高存储系统的可靠性。相比多副本复制而言，它能以更小的数据冗余度获得更高数据可靠性， 但编码方式较复杂，需要大量计算。

里德-所罗门码（Reed-Solomon codes，RS codes）是纠删码的一类，常被应用在分布式文件系统中，我们希望使用它来提升文件系统的可靠性。下面介绍它的编解码原理。

#### 编解码原理

##### 编码

RS 编码以 word 为编码和解码单位，大的数据块拆分到字长为 w（取值一般为 8 或者 16 位）的 word，然后对 word 进行编解码。数据块的编码原理与 word 编码原理相同。把输入数据视为向量 D = (D1, D2, .., Dn), 编码后数据视为向量 (D1, D2, .., Dn, C1, C2, .., Cm)，RS 编码可视为如下图所示矩阵运算。

<img src="image\research-RS-1" alt="img"  />

上图最左边是编码矩阵（或称为生成矩阵、分布矩阵，Distribution Matrix），编码矩阵需要满足任意 n\*n 子矩阵可逆。为方便数据存储，编码矩阵上部是单位阵，下部是 m\*n 矩阵。下部矩阵可以选择范德蒙德矩阵或柯西矩阵。

##### 解码

RS 最多能容忍 m 个数据块被删除，数据恢复的过程如下：

- 假设 D1、D4、C2 丢失，从编码矩阵中删掉丢失的数据块/编码块对应的行。根据 RS 编码运算等式，可以得到 B' 以及等式：

<img src="image\research-RS-2-new" alt="img"  />

- 由于 B' 是可逆的，记 B' 的逆矩阵为 B'^(-1)，则 B'\*B'^(-1) = I 单位矩阵。两边左乘 B' 逆矩阵：

<img src="image\research-RS-4" alt="img"  />

- 得到如下原始数据 D 的计算公式，从而恢复原始数据 D：

<img src="image\research-RS-5-new" alt="img"  />

#### 编码矩阵

##### 基于范德蒙德（Vandermonde）矩阵

在线性代数中有一种矩阵称为范德蒙德矩阵，它的任意的子方阵均为可逆方阵。

一个 m 行 n 列的范德蒙德矩阵定义如下图左边，其中 Ai 均不相同，且不为 0。令 A1, A2, .., An 分别为 1, 2, 3, .., n，则得到范德蒙德矩阵为下图右边：

<img src="image\feasibility-RS-Vandermonde-1" alt="img"  />

编码矩阵就是单位矩阵和范德蒙德矩阵的组合。输入数据 D 和编码矩阵的乘积就是编码后的数据。

采用这种方法的算法复杂度还是比较高的，编码复杂度为 O(mn)，其中 m 为校验数据个数，n 为输入数据个数。解码复杂度为 O(n^3)。

##### 基于柯西（Cauchy）矩阵

柯西矩阵的任意一个子方阵都是奇异矩阵，存在逆矩阵。而且柯西矩阵在迦罗华域上的求逆运算，可以在 O(n^2) 的运算复杂度内完成。使用柯西矩阵，比范德蒙德矩阵的优化主要有两点：

- 降低了矩阵求逆的运算复杂度。范德蒙矩阵求逆运算的复杂度为 O(n^3)，而柯西矩阵求逆运算的复杂度仅为 O(n^2)。
- 通过有限域转换，将 GF(2^w) 域中的元素转换成二进制矩阵，将乘法转换为逻辑与，降低了乘法运算复杂度。（二进制的加法即 XOR，乘法即 AND）

柯西矩阵的描述如下图左边，Xi 和 Yi 都是迦罗华域 GF(2^w) 中的元素。右边是基于柯西矩阵的编码矩阵：

<img src="image\feasibility-RS-Cauchy-1-new.png" alt="img" style="zoom:80%;" />

##### 柯西编解码过程优化

在范德蒙编码的时候，我们可以采用对数/反对数表的方法，将乘法运算转换成了加法运算，并且在迦罗华域中，加法运算转换成了 XOR 运算。

柯西编解码为了降低乘法复杂度，采用了有限域上的元素都可以使用二进制矩阵表示的原理，将乘法运算转换成了迦罗华域“AND 运算”和“XOR 逻辑运算”，提高了编解码效率。

从数学的角度，在迦罗华有限域中，任何一个 GF(2^w) 域上的元素都可以映射到 GF(2) 二进制域，并且采用一个二进制矩阵的方式表示 GF(2^w) 中的元素。例如 GF(2^3) 域中的元素可以表示成 GF(2) 域中的二进制矩阵：

<img src="image\feasibility-RS-GF-1" alt="img"  />

上图中，黑色方块表示逻辑 1，白色方块表示逻辑 0。通过这种转换，GF(2^w) 域中的阵列就可以转换成 GF(2) 域中的二进制阵列。生成矩阵的阵列转换表示如下：

<img src="image\feasibility-RS-GF-2" alt="img"  />

在 GF(2^w) 域中的编码矩阵为 K\*(K+m)，转换到 GF(2) 域中，使用二进制矩阵表示，编码矩阵变成了 wk\*w(k+m) 二进制矩阵。采用域转换的目的是简化 GF(2^w) 域中的乘法运算。在 GF(2) 域中，乘法运算变成了逻辑与运算，加法运算变成了 XOR 运算，可以大大降低运算复杂度。

和范德蒙编解码中可能使用的对数/反对数方法相比，这种方法不需要构建对数或反对数表，可以支持 w 为很大的 GF 域空间。采用这种有限域转换的方法之后，柯西编码运算可以表示如下：

<img src="image\feasibility-RS-GF-3" alt="img"  />

使用柯西矩阵要优于范德蒙德矩阵的方法，柯西矩阵的运算复杂度为 O(n*(n-m))，解码复杂度为 O(n^2)。

### 监控的意义——工业界中的监控系统

#### 分布式监控 CAT

<img src="https://p0.meituan.net/travelcube/83345ea439339ec421cc4727212a875840027.png" alt="CAT" style="zoom: 33%;" />

CAT（Central Application Tracking），是美团点评基于 Java 开发的一套开源的分布式实时监控系统。美团点评基础架构部希望在基础存储、高性能通信、大规模在线访问、服务治理、实时监控、容器化及集群智能调度等领域提供业界领先的、统一的解决方案，CAT 目前在美团点评的产品定位是应用层的统一监控组件，在中间件（RPC、数据库、缓存、MQ 等）框架中得到广泛应用，为各业务线提供系统的性能指标、健康状况、实时告警等服务。

监控整体要求就是快速发现故障、快速定位故障以及辅助进行程序性能优化。为了做到这些，美团对监控系统的一些非功能做了如下的要求：

- 实时处理：信息的价值会随时间锐减，尤其是事故处理过程中。
- 全量数据：最开始的设计目标就是全量采集，全量的好处有很多。
- 高可用：所有应用都倒下了，需要监控还站着，并告诉工程师发生了什么，做到故障还原和问题定位。
- 故障容忍：CAT 本身故障不应该影响业务正常运转，CAT 挂了，应用不该受影响，只是监控能力暂时减弱。
- 高吞吐：要想还原真相，需要全方位地监控和度量，必须要有超强的处理吞吐能力。
- 可扩展：支持分布式、跨IDC部署，横向扩展的监控系统。
- 不保证可靠：允许消息丢失，这是一个很重要的 trade-off，目前 CAT 服务端可以做到4个9的可靠性，可靠系统和不可靠性系统的设计差别非常大。

CAT 从开发至今，一直秉承着**简单的架构就是最好的架构**原则，主要分为三个模块：CAT-client、CAT-consumer、CAT-home。

- Cat-client 提供给业务以及中间层埋点的底层 SDK。
- Cat-consumer 用于实时分析从客户端提供的数据。
- Cat-home 作为用户给用户提供展示的控制端。

在实际开发和部署中，Cat-consumer 和 Cat-home 是部署在一个 JVM 内部，每个 CAT 服务端都可以作为 consumer 也可以作为 home，这样既能减少整个层级结构，也可以增加系统稳定性。

#### 伴鱼数据库监控系统

数据库监控作为数据库配套建设不可或缺的一环，可以及时发现机器和数据库性能问题，并帮助止损。伴鱼团队对数据库监控系统主要有如下需求：

- 数据库以集群为单位，集群成员的变动需要修改prometheus对应的监控配置文件，无法自动修改
- 机器指标和数据库指标采集分属不同的exporter，难以集群或机器维度同时展示两类指标
- 集群和机器告警配置差异化以及告警时间段抑制，配置不太灵活
- 日常巡检和监控大盘难以定制

基于以上监控告警需求，并结合在对 prometheus、阿里云数据库监控等一些优秀的监控系统架构调研的基础上，伴鱼团队设计了伴鱼数据库监控系统，包含以下核心功能：

- 基于集群维度的机器和数据库指标采集，集群成员变动，无需修改配置
- 支持集群和机器维度的机器指标和数据库性能指标数据的同时展示
- 通过报警模版，支持报警的差异化配置
- 支持报警时间段抑制和灵活的报警策略
- 灵活定制监控大盘，方便巡检需求

伴鱼数据库监控整体架构，如下图所示。

<img src="https://tech.ipalfish.com/blog/images/tidb_monitor/m1.jpeg" alt="伴鱼" style="zoom: 45%;" />

各组件的功能，说明如下：

- agent 模块，功能类似 prometheus exporter 组件，暴露 metric 接口，接收请求采集数据
- schedule 模块，获取监控任务，并根据集群名从 db config service 服务获取具体的集群信息，然后按照监控任务配置的采集时间间隔，定时到对应的 agent 拉取 metrics
- monitor 模块，负责监控数据存储/查询、数据分析和规则报警
- alarm 模块，公司内部报警服务，支持钉钉和电话报警
- http server 模块，负责监控任务、报警模版和报警规则的配置以及监控数据的查询展示

#### Open-falcon

Open-falcon 是小米运维团队从互联网公司的需求出发，根据多年的运维经验，结合 SRE、SA、DEVS 的使用经验和反馈，开发的一套面向互联网的企业级开源监控产品。

<img src="https://pic2.zhimg.com/80/v2-38b65a6c1d62fe300e05a1089f92e81d_1440w.jpg?source=1940ef5c" alt="open-falcon" style="zoom:67%;" />

1. 自动发现，支持 falcon-agent、snmp，支持用户主动 push、用户自定义插件支持
2. 支持每个周期上亿次的数据采集、告警判定、历史数据存储和查询
3. 高效的 portal、支持策略模板、模板继承和覆盖、多种告警方式、支持 callback 调用
4. 单机支撑200万 metric 的上报、归档、存储
5. 采用 rrdtool 的数据归档策略，秒级返回上百个 metric 一年的历史数据
6. 多维度的数据展示，用户自定义 Screen
7. 通过各种插件目前支持 Linux、Windows、Mysql、Redis、Memache、RabbitMQ 和交换机监控。

#### Zabbix

Zabbix 是一个基于WEB界面的提供分布式系统监控以及网络监控功能的企业级开源运维平台，也是目前国内互联网用户中使用最广的监控软件，遇到的 85% 以上用户在使用 Zabbix 做监控解决方案。

Zabbix 易于管理和配置，能生成比较漂亮的数据图，其自动发现功能大大减轻日常管理的工作量，丰富的数据采集方式和 API 接口可以让用户灵活进行数据采集，而分布式系统架构可以支持监控更多的设备。理论上，通过 Zabbix 提供的插件式架构，可以满足企业的任何需求。



## 技术依据

### DisGraFS 的系统架构

去年 DisGraFS 的最终实现版本，共分为5个组成部分：索引服务器、分布式存储集群、分布式计算集群、网页端和客户端。

- 索引服务器：进行分布式存储集群与分布式计算集群的通信、网页端部署的位置，目前也负责构建与维护图数据库（但若有需要，也可将图数据库的部分分离出去）；
- 分布式存储集群：基于 Juicefs 的分布式储存系统，管理、存储和调度分布式存储系统中的所有文件；
- 分布式计算集群：基于 Ray 的分布式计算系统，将文本语义识别、图像识别、语音识别以及元数据提取等任务分散给计算集群中的多个计算机；
- 网页端：直观显示文件所构成的图，并将用户在图上的操作以友好方式展示。
- 客户端：客户端负责直接接收用户对文件系统的操作，并针对不同的平台对其进行实现。

<img src="image\structure.png" alt="structure" style="zoom: 50%;" />

### 中央索引服务器

中央索引服务器使用 websockets 协议进行网络通信，使用 asyncio 进行异步通信

#### WebSockets

websockets 是一种网络通信协议

HTTP 协议是一种无状态的、无连接的、单向的应用层协议。它采用了请求/响应模型。通信请求只能由客户端发起，服务端对请求做出应答处理。这种通信模型有一个弊端：HTTP 协议无法实现服务器主动向客户端发起消息。这种单向请求的特点，注定了如果服务器有连续的状态变化，客户端要获知就非常麻烦。大多数 Web 应用程序将通过频繁的异步 JavaScript 和 XML（AJAX）请求实现长轮询。轮询的效率低，非常浪费资源（因为必须不停连接，或者 HTTP 连接始终打开）

WebSocket 连接允许客户端和服务器之间进行全双工通信，以便任一方都可以通过建立的连接将数据推送到另一端。WebSocket 只需要建立一次连接，就可以一直保持连接状态。这相比于轮询方式的不停建立连接显然效率要大大提高。

使用到的函数：

```python
wsClient = websockets.connect('ws://47.119.121.73:9090')
wsClient.send("str")
recv_text = wsClient.recv()
websocket.close_connection()
```

注：ws 表示不加密

注：传输的都是字符串，所以需要进行处理

#### asyncio

程序在执行 IO 密集型任务的时候，程序会因为等待 IO 而阻塞。协程遇到 IO 操作而阻塞时，立即切换到别的任务，如果操作完成则进行回调返回执行结果。实现**并发执行**。

基本关键字：

+ **event_loop**：事件循环，程序开启一个无限循环，把一些函数注册到事件循环上，当满足事件发生的时候，调用相应的协程函数

+ **coroutine**：协程，指一个使用 async 关键字定义的函数，它的调用不会立即执行函数，而是会返回一个协程对象。协程对象需要注册到事件循环，由事件循环调用

+ **task**：协程对象不能直接运行，在注册事件循环的时候，其实是 run_until_complete 方法将协程包装成一个 task 对象

+ **future**：代表将来执行或者没有执行的任务的结果，他和 task 没有本质上的区别

+ **async/await**：关键字，async 定义一个协程，await 用于挂起阻塞的异步调用接口

  await 即等待另一个事件发生，挂起当前协程，所需事件发生后，再恢复当前写成的执行

  注：async 和 await 这两个关键字只能用于 Python3.5 以及后续版本

基本使用：

定义一个协程并创建

```python
import asyncio

# 我们通过async关键字定义一个协程,当然协程不能直接运行，需要将协程加入到事件循环loop中
async def do_some_work(x):
    print("waiting:", x)

coroutine = do_some_work(2)
loop = asyncio.get_event_loop()        # asyncio.get_event_loop：创建一个事件循环
# 通过loop.create_task(coroutine)创建task,同样的可以通过 asyncio.ensure_future(coroutine)创建task
task = loop.create_task(coroutine)     # 创建任务, 不立即执行
loop.run_until_complete(task)         # 使用run_until_complete将协程注册到事件循环，并启动事件循环
```

`asyncio.wait` ：把所有 task 任务结果收集起来。

### neo4j

Neo4j是一个高性能的 NOSQL 图形数据库，它将结构化数据存储在图上而不是表中。它是一个嵌入式的、基于磁盘的、具备完全的事务特性的 Java 持久化高性能引擎，该引擎具有成熟数据库的所有特性。

#### 储存结构

Neo4J 属于原生图数据库，其使用的存储后端专门为图结构数据的存储和管理进行定制和优化的，在图上互相关联的节点在数据库中的物理地址也指向彼此，因此更能发挥出图结构形式数据的优势。

#### Cypher查询语言

Cypher 是以重声明式查询语言，可用于表达性和高效的查询更新和图管理。它同时适合开发人员和运营管理人员。Cypher 的设计简单同时功能强大，可以轻松表达高度复杂的数据库查询操作。Cypher 受到许多不同方法的启发，并以表达查询为基础。 许多关键字（例如 `WHERE` 和 `ORDER BY`）都受到 SQL 的启发。 模式匹配借鉴了 SPARQL 的表达方法。 某些列表语义是从 Haskell 和 Python 等语言中借用的，使查询变得容易。 

例如，下面是一个创建图的简单实例：

```cypher
CREATE (john:Person {name: 'John'})
CREATE (joe:Person {name: 'Joe'})
CREATE (steve:Person {name: 'Steve'})
CREATE (sara:Person {name: 'Sara'})
CREATE (maria:Person {name: 'Maria'})
CREATE (john)-[:FRIEND]->(joe)-[:FRIEND]->(steve)
CREATE (john)-[:FRIEND]->(sara)-[:FRIEND]->(maria)
```

使用上述代码，即可创造出如下图所示的简单图:

<img src="image\simple_example.png" alt="simple_example" style="zoom:50%;" />

在这个图中，如果我们需要查询John的朋友，只需要使用下面的语句：

```cypher
MATCH (john {name: 'John'})-[:FRIEND]->()-[:FRIEND]->(fof)
RETURN john.name, fof.name
```

就可以得到结果：

```
+----------------------+
| john.name | fof.name |
+----------------------+
| "John"    | "Maria"  |
| "John"    | "Steve"  |
+----------------------+
2 rows
```

#### 图算法

Neo4j 基于其特殊的储存结构与 Cypher 查询语言，设计并优化了多种图上的算法，使得查询、插入、删除等图操作的效率大大提高。

- 中心性算法（Centrality algorithms）:主要用来判断一个图中不同节点的重要性
- 社区发现算法（Community detection algorithms）:评估一个群体是如何聚集或划分的，以及其增强或分裂的趋势
- 路径寻找算法（Path Finding algorithms）：用于找到最短路径，或者评估路径的可用性和质量
- 相似度算法（Similarity algorithms）：用于计算节点间的相似度
- 链接预测算法（Link Prediction algorithms）：有助于确定一对节点的紧密程度
- 预处理算法（Preprocessing functions and procedures）：使用 one-hot encoding 对数据进行预处理

### Ray

**概述**

Ray 是一个分布式高性能计算框架，遵循了典型的 Master-Slave 的设计，Master 负责全局协调和状态维护；Slave 执行分布式计算任务。不过和传统的分布式计算系统不同的是 Ray 使用了混合任务调度的思路。Ray 集群由一个 head 和多个 worker 节点构成，架构如这个图所示。

<img src="image\ray.png" alt="image-20220416235305038" style="zoom: 50%;" />

Ray 集群包括一组同类的 worker 节点和一个集中的全局控制存储（GCS）实例。GCS 是集中的服务端，是 worker 之间传递消息的纽带。每个 Server 都有一个共用的 Object Store，也就是用 Apache Arrow/Plasma 构建的内存数据。 Local Scheduler 是 Server 内部的调度，同时通过 GCS 来和其他 Server 上的 worker 通信。Object Store 之间也有通信，作用是传递 worker 之间的数据。

在 DisGraFS 中，利用 Ray 这样一个分布式计算框架，搭建分布式计算集群，对存储集群中的文件进行打标签。

**Ray cluster 搭建**

前提：准备作为计算节点的各台服务器在同一个局域网中，安装有相同版本的 python 和 ray

head 节点通过以下命令进行创建：

```shell
sudo pip3 install -U ray
ray start --head --port=6379
```

worker 节点执行以下命令进行创建：

```shell
sudo pip3 install -U ray
ray start --address='192.168.10.132:6379' --redis-password='5241590000000000' #视实际情况修改address
```

Ray 的搭建需要所有节点在同一局域网内，而 Ray 的 head 节点需要保持运行，才能保证其他 worker 节点的连接，所以我们把 head 节点部署在一台服务器上，worker 节点目前位于我们本地电脑的虚拟机。局域网的实现是通过搭建 vpn，将本地接到服务器的内网。

<img src="image\vpn.png" alt="vpn"  />



### Docker 容器化服务端

在生产环境上，传统的手工部署方法可能会出现下列事件：

- 你的 Linux 发行版很老，而你需要运行一个给新版本的 Linux 或者完全不同的 Linux 发行版设计的程序。
- 你和朋友一起设计一个大型程序，由于你们的运行环境不同，有时候在某台机器上正常运行的程序，在另一台机器上没法正常运行。
- 你希望在多台服务器上部署同一个项目，但是项目需要非常复杂的配置，一个一个配置服务器的成本非常大。

而容器化技术可以方便解决上述问题，容器可以把应用及其依赖项都将打包成一个可以复用的镜像并与其他进程环境隔离。

在运行环境、网络拓扑、安全策略和存储方案变化时，软件可能会显现一些出乎意料的问题；而容器使开发环境和运行环境统一。同时容器并不像虚拟机那样模拟全部硬件（这对于很多轻量型应用是小题大做），它只虚拟化了文件系统、网络、运行环境等，在核心本地运行指令，不需要任何专门的接口翻译和系统调用替换机制，减少了很多虚拟化开销。

使用容器技术很好地简化了目录节点的配置，同时还可以减少开发环境和部署环境不同带来的问题。

很多时候人们可能因为配置文档过于复杂或者因为环境问题配置失败，就放弃了一个项目。提供一键部署的方案，降低了部署的成本和学习门槛，非常有利于项目的推广。

### 文件编解码

dontpanic 采用纠删码技术中的里德-所罗门算法（Reed-Solomon Code）对文件进行冗余，并使用性能相比于范德蒙矩阵更好的柯西矩阵作为编码矩阵。

### Prometheus

Prometheus 是一个开源系统监控和警报工具包，它将实时的指标数据（metrics）记录并存储在通过 Http 拉取模型构建的时间序列数据库中，有着较灵活的询问功能和实时告警功能。

**优势**

1. 基于 Prometheus 丰富的 Client 库，用户可以轻松地在应用程序中添加对 Prometheus 的支持，从而让用户可以获取服务和应用内部真正的运行状态

2. 强大的数据模型。所有采集的监控数据均以指标（metric）的形式保存在内置的时间序列数据库（TSDB）当中，所有的样本除了基本的指标名称外，还包含一组用于描述该样本特征的标签

3. 高效：对于单一 Prometheus Server 示例而言它可以处理数以百万的监控指标，每秒处理数十万的数据点。它拥有强大的查询语言 PromQL。不依赖分布式存储；单个服务节点具有自治能力。时间序列数据是服务端通过 HTTP 协议主动拉取获得的。支持多种类型的图表和仪表盘。

**组件**

+ Prometheus Server 作为服务端，用来存储时间序列数据。
+ 客户端库用来检测应用程序代码。
+ 用于支持临时任务的推送网关。
+ Jobs/Exporter 用来监控 HAProxy，StatsD，Graphite 等特殊的监控目标，并向 Prometheus 提供标准格式的监控样本数据。
+ alertmanager 用来处理告警。
+ Pushgateway 用于暂时存放 Prometheus 来不及处理的 Job 中的数据，防止监控数据丢失


   其中大多数组件都是用 Go 编写的，因此很容易构建和部署为静态二进制文件。

**Prometheus 的架构**

Prometheus 的整体架构以及生态系统组件如下图所示：

<img src="image\Prometheus_framework.png" alt="Prometheus_framework" style="zoom:80%;" />

Prometheus Server 直接从监控目标中或者间接通过推送网关来拉取监控指标，它在本地存储所有抓取到的样本数据，并对此数据执行一系列规则，以汇总和记录现有数据的新时间序列或生成告警。可以通过 Grafana 或者其他工具来实现监控数据的可视化。

**工作原理**

Prometheus 所有采集的监控数据均以指标（metric）的形式保存在内置的时间序列数据库当中（TSDB）：属于同一指标名称，同一标签集合的、有时间戳标记的数据流。除了存储的时间序列，Prometheus 还可以根据查询请求产生临时的、衍生的时间序列作为返回结果。

在 Prometheus 的架构设计中，Prometheus Server 主要负责数据的收集，存储并且对外提供数据查询支持，而实际的监控样本数据的收集则是由 Exporter 完成。因此为了能够监控到某些东西，如主机的 CPU 使用率，我们需要使用到 Exporter。Prometheus 周期性的从 Exporter 暴露的 HTTP 服务地址（通常是 /metrics）拉取监控样本数据。

Exporter 可以是一个相对开放的概念，其可以是一个独立运行的程序独立于监控目标以外，也可以是直接内置在监控目标中。只要能够向 Prometheus 提供标准格式的监控样本数据即可。

为了能够采集到主机的运行指标如 CPU, 内存，磁盘等信息。我们可以使用 Node Exporter。

### InfluxDB

Prometheus Server 本身就是一个时序数据库，将采集到的监控数据按照时间序列的方式存储在本地磁盘当中。Prometheus 的本地存储设计可以减少其自身运维和管理的复杂度，同时能够满足大部分用户监控规模的需求。但是本地存储也意味着 Prometheus 无法持久化数据，无法存储大量历史数据，同时也无法灵活扩展和迁移。

为了保持 Prometheus 的简单性，Prometheus 并没有尝试在自身中解决以上问题，而是通过定义两个标准接口 (remote_write/remote_read)，让用户可以基于这两个接口对接将数据保存到任意第三方的存储服务中，这种方式在 Promthues 中称为Remote Storage 。

为此，我们需要使用时序数据库。与传统数据库通常记录数据的当前值不同，时序型数据库则记录所有的历史数据，在处理当前时序数据时又要不断接收新的时序数据，同时时序数据的查询也总是以时间为基础查询条件。

> 时序数据：按照时间顺序记录系统、设备状态变化的数据被称为时序数据（Time Series Data），如 CPU 利用率、某一时间的环境温度等。时序数据以时间作为主要的查询维度，通常会将连续的多个时序数据绘制成线，制作基于时间的多维度报表，用于揭示数据背后的趋势、规律、异常，进行实时在线预测和预警。

其中 InfluxDB 时序数据库是较为常见的选择。

InfluxDB 是一个由 InfluxData 开发的开源时序型数据库，着力于高性能地查询与存储时序型数据，在DB-Engines Ranking时序型数据库排行榜上排名第一，广泛应用于 DevOps 监控、IoT 监控、实时分析等场景。

它自带各种特殊函数如求标准差，随机取样数据，统计数据变化比等，使数据统计和实时分析变得十分方便，适合用于包括 DevOps 监控，应用程序指标，物联网传感器数据和实时分析的后端存储。

**特性**

1. Time Series（时间序列）：你可以使用与时间有关的相关函数（如最大，最小，求和等）
2. Metrics（度量）：你可以实时对大量数据进行计算
3. Events（事件）：它支持任意的事件数据

**特点**

1. 为时间序列数据专门编写的自定义高性能数据存储。 TSM 引擎具有高性能的写入和数据压缩
2. 无系统环境依赖，部署方便。
3. 无结构化（SchemaLess）的数据模型，灵活强大。
4. 提供简单、高性能的写入、查询 http api，Native HTTP API, 内置 http 支持，使用 http 读写
5. 强大的类 SQL 查询语句的操作接口，学习成本低，上手快。
6. 丰富的权限管理功能，精细到“表”级别。
7. 丰富的时效管理功能，自动删除过期数据，自定义删除指标数据。
8. 低成本存储，采样时序数据，压缩存储。
9. 丰富的聚合函数，支持 AVG、SUM、MAX、MIN 等聚合函数。

### Grafana

Prometheus 中的 Graph 面板可查询数据形成图表。但是缺点也很明显，这些查询结果都是临时的，无法持久化的，更别说我们想实时关注某些特定监控指标的变化趋势。

为了简化这些问题 Prometheus 内置了一个简单的解决方案 `Console Template` ,它允许用户通过 Go 模板语言创建任意的控制台界面，并且通过 Prometheus Server 对外提供访问路径。

Console Teamplet 虽然能满足一定的可视化需求，但是也仅仅是对 Prometheus 的基本能力的补充。同时使用也会有许多问题，首先用户需要学习和了解 Go Template 模板语言，其它其支持的可视化图表类型也非常有限，最后其管理也有一定的成本。而 Grafana 则可以让我们以更简单的方式创建更加精美的可视化报表。

Grafana 是一个跨平台的开源的度量分析和可视化工具，可以通过将采集的数据查询然后可视化的展示，并及时通知。它主要有以下六大特点：

- 展示方式：快速灵活的客户端图表，面板插件有许多不同方式的可视化指标和日志，官方库中具有丰富的仪表盘插件，比如热图、折线图、图表等多种展示方式；
- 数据源：Graphite，InfluxDB，OpenTSDB，Prometheus，Elasticsearch，CloudWatch和KairosDB等；
- 通知提醒：以可视方式定义最重要指标的警报规则，Grafana 将不断计算并发送通知，在数据达到阈值时通过 Slack、PagerDuty 等获得通知；
- 混合展示：在同一图表中混合使用不同的数据源，可以基于每个查询指定数据源，甚至自定义数据源；
- 注释：使用来自不同数据源的丰富事件注释图表，将鼠标悬停在事件上会显示完整的事件元数据和标记；
- 过滤器：Ad-hoc 过滤器允许动态创建新的键/值过滤器，这些过滤器会自动应用于使用该数据源的所有查询。

### Lua

Lua 在葡萄牙语里代表美丽的月亮。事实证明她没有糟蹋这个优美的单词，Lua 语言正如它名字所预示 的那样成长为一门简洁、优雅且富有乐趣的语言。 Lua 从一开始就是作为一门方便嵌入(其它应用程序)并可扩展的轻量级脚本语言来设计的，因此她 一直遵从着简单、小巧、可移植、快速的原则，官方实现完全采用 ANSI C 编写，能以 C 程序库的 形式嵌入到宿主程序中。LuaJIT 2 和标准 Lua 5.1 解释器采用的是著名的 MIT 许可协议。

Lua 脚本可以很容易的被 C/C++ 代码调用，也可以反过来调用 C/C++ 的函 数，这使得 Lua 在应用程序中可以被广泛应用。不仅仅作为扩展脚本，也可 以作为普通的配 置文件，代替 XML , Ini 等文件格式，并且更容易理解和维护。 在目前所有脚本引擎中，Lua 的速度是最快的。这一切都决定了 Lua 是作为嵌入式脚本的最佳选择。

#### 协同程序

Lua 协同程序（ Coroutine ），与线程类似，有独立的堆栈、局部变量、指令指针，但也与其 他协同程序共享全局变量和其他大部分东西。 在任一指定时刻只有一个协同程序在运行，并且它只有在明确被 要求挂起 时才会被挂起。 协同程序类似于同步的多线程。

coroutine方法：

- create-创建（可以基于函数创建）
- resume-重启，yield-挂起（遇到yield的时候就代表挂起当前线程，等到再次 resume） 
- status-查看状态（dead，suspended，running） wrap-返回函数，调用该函数就相当于创建 coroutine
- running-返回正在跑的 coroutine，实际上是返回一个 coroutine 的线程号

下面的代码演示了协同程序的相关用法。

```lua
co2 = coroutine.create(
    function()
        for i=1,10 do
            print(i)
            if i == 3 then
                print(coroutine.status(co2))  --running
                print(coroutine.running()) --thread:XXXXXX
            end
            coroutine.yield()
        end
    end
)
 
coroutine.resume(co2) --1
coroutine.resume(co2) --2
coroutine.resume(co2) --3
 
print(coroutine.status(co2))   -- suspended
print(coroutine.running())
 
print("----------")
```

其运行结果如下。

```lua
----------
1
2
3
running
thread: 0x7fb801c05868    false
suspended
thread: 0x7fb801c04c88    true
----------
```



## 技术路线

我们的项目分为四个步骤：

第一步，复现 DisGraFS，学习其代码逻辑，找到源码中可以部署监控的位置。

第二步，利用 Prometheus 部署监控，对监控到的信息进行处理，最后用 Grafana 进行图形化呈现。

第三步，参考 dontpanic，搭建容器化远程分布式存储集群，实现远程通信和远程文件传输

第四步，根据监控到的信息，对 DisGraFS 进行性能上的优化

目前已完成第一步骤，正同步启动第二步和第三步。

## 参考内容

https://www.zhihu.com/question/53936892

https://tech.ipalfish.com/blog/2020/07/21/tidb_monitor/

https://tech.meituan.com/2018/11/01/cat-in-depth-java-application-monitoring.html

https://tech.meituan.com/2018/11/01/cat-pr.html

[OSH-2021/x-DisGraFS: Distributed Graph Filesystem (github.com)](https://github.com/OSH-2021/x-DisGraFS)

[OSH-2020/x-dontpanic: team dontpanic in ustc-osh-2020 (github.com)](https://github.com/OSH-2020/x-dontpanic)

