# 可行性报告

## 目录

- [可行性报告](#可行性报告)
  - [目录](#目录)
  - [项目介绍](#项目介绍)
  - [理论依据](#理论依据)
  - [技术依据](#技术依据)
    - [InfluxDB](#influxdb)
    - [中央索引服务器](#中央索引服务器)
      - [WebSockets](#websockets)
      - [asyncio](#asyncio)
    - [neo4j](#neo4j)
  - [技术路线](#技术路线)
  - [参考内容](#参考内容)

## 项目介绍

## 理论依据

## 技术依据

### InfluxDB

Prometheus Server 本身就是一个时序数据库，将采集到的监控数据按照时间序列的方式存储在本地磁盘当中。Prometheus 的本地存储设计可以减少其自身运维和管理的复杂度，同时能够满足大部分用户监控规模的需求。但是本地存储也意味着 Prometheus 无法持久化数据，无法存储大量历史数据，同时也无法灵活扩展和迁移。

为了保持 Prometheus 的简单性，Prometheus 并没有尝试在自身中解决以上问题，而是通过定义两个标准接口 (remote_write/remote_read)，让用户可以基于这两个接口对接将数据保存到任意第三方的存储服务中，这种方式在 Promthues 中称为Remote Storage 。

为此，我们需要使用时序数据库。与传统数据库通常记录数据的当前值不同，时序型数据库则记录所有的历史数据，在处理当前时序数据时又要不断接收新的时序数据，同时时序数据的查询也总是以时间为基础查询条件。

> 时序数据：按照时间顺序记录系统、设备状态变化的数据被称为时序数据（Time Series Data），如CPU利用率、某一时间的环境温度等。时序数据以时间作为主要的查询维度，通常会将连续的多个时序数据绘制成线，制作基于时间的多维度报表，用于揭示数据背后的趋势、规律、异常，进行实时在线预测和预警。

其中 InfluxDB 时序数据库是较为常见的选择。

InfluxDB 是一个由 InfluxData 开发的开源时序型数据库，着力于高性能地查询与存储时序型数据，在DB-Engines Ranking时序型数据库排行榜上排名第一，广泛应用于DevOps监控、IoT监控、实时分析等场景。

它自带各种特殊函数如求标准差，随机取样数据，统计数据变化比等，使数据统计和实时分析变得十分方便，适合用于包括DevOps监控，应用程序指标，物联网传感器数据和实时分析的后端存储。

**特性**

1. Time Series（时间序列）：你可以使用与时间有关的相关函数（如最大，最小，求和等）
2. Metrics（度量）：你可以实时对大量数据进行计算
3. Events（事件）：它支持任意的事件数据

**特点**

1. 为时间序列数据专门编写的自定义高性能数据存储。 TSM引擎具有**高性能**的写入和数据压缩
2. 无系统环境依赖，部署方便。
3. 无结构化（SchemaLess）的数据模型，灵活强大。
4. 提供简单、高性能的写入、查询 http api，Native HTTP API, 内置http支持，使用http读写
5. 强大的类SQL查询语句的操作接口，学习成本低，上手快。
6. 丰富的权限管理功能，精细到“表”级别。
7. 丰富的时效管理功能，自动删除过期数据，自定义删除指标数据。
8. 低成本存储，采样时序数据，压缩存储。
9. 丰富的聚合函数，支持AVG、SUM、MAX、MIN等聚合函数。

### 中央索引服务器

中央索引服务器使用websockets协议进行网络通信，使用asyncio进行异步通信

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

程序在执行 IO 密集型任务的时候，程序会因为等待 IO 而阻塞。协程遇到 IO 操作而阻塞时，立即切换到别的任务，如果操作完成则进行回调返回执行结果。

实现**并发执行**

基本关键字：

event_loop：事件循环，程序开启一个无限循环，把一些函数注册到事件循环上，当满足事件发生的时候，调用相应的协程函数

coroutine：协程，指一个使用 async 关键字定义的函数，它的调用不会立即执行函数，而是会返回一个协程对象。协程对象需要注册到事件循环，由事件循环调用

task：协程对象不能直接运行，在注册事件循环的时候，其实是 run_until_complete 方法将协程包装成一个 task 对象

future：代表将来执行或者没有执行的任务的结果，他和 task 没有本质上的区别

async/await：关键字，async 定义一个协程，await 用于挂起阻塞的异步调用接口

?await 即等待另一个事件发生，挂起当前协程，所需事件发生后，再恢复当前写成的执行

?注：async 和 await 这两个关键字只能用于 Python3.5 以及后续版本

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

Neo4j是一个高性能的NOSQL图形数据库，它将结构化数据存储在图上而不是表中。它是一个嵌入式的、基于磁盘的、具备完全的事务特性的Java持久化高性能引擎，该引擎具有成熟数据库的所有特性。

- #### 储存结构[12]

  Neo4J属于原生图数据库，其使用的存储后端专门为图结构数据的存储和管理进行定制和优化的，在图上互相关联的节点在数据库中的物理地址也指向彼此，因此更能发挥出图结构形式数据的优势。

  - **Nodes: 15bytes**

    1byte：inUse标记和关系id的高位信息

    4 bytes：第一个relation id

    4 bytes：第一个property id

    5 bytes：4bytes的IsLables信息和1byte的hsbLables信息

    1 byte：保留字段extra，记录该Node是否为一个superNode

  - **Relation: 34bytes**

    1 byte：存该关系记录是否在使用中，以及关系的起点和下一个属性的高位信息

    8 bytes：该关系的起点和终点

    8 bytes：该关系的类型

    16 bytes：该关系起点前后关系、终点前后关系

    4 bytes：第一个property的id

    1 byte：是否为起终点的第一个关系

  - **property：41bytes**

    1 byte：前后property id的高位信息

    8 bytes：前后property

    32 bytes：默认的4个属性块

- #### Cypher查询语言[13]

  Cypher是以重声明式查询语言，可用于表达性和高效的查询更新和图管理。它同时适合开发人员和运营管理人员。Cypher的设计简单同时功能强大，可以轻松表达高度复杂的数据库查询操作。Cypher受到许多不同方法的启发，并以表达查询为基础。 许多关键字（例如`WHERE`和`ORDER BY`）都受到SQL的启发。 模式匹配借鉴了SPARQL的表达方法。 某些列表语义是从Haskell和Python等语言中借用的，使查询变得容易。 

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

  ![simple_example](./src/simple_example.png)

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

- #### **图算法**[14]

  Neo4j基于其特殊的储存结构与Cypher查询语言，设计并优化了多种图上的算法，使得查询、插入、删除等图操作的效率大大提高。

  - **中心性算法（Centrality algorithms）**:主要用来判断一个图中不同节点的重要性
  - **社区发现算法（Community detection algorithms）**:评估一个群体是如何聚集或划分的，以及其增强或分裂的趋势
  - **路径寻找算法（Path Finding algorithms）**:用于找到最短路径，或者评估路径的可用性和质量
  - **相似度算法（Similarity algorithms）**:用于计算节点间的相似度
  - **链接预测算法（Link Prediction algorithms）**:有助于确定一对节点的紧密程度
  - **预处理算法（Preprocessing functions and procedures）**:使用one-hot encoding对数据进行预处理


## 技术路线



## 参考内容