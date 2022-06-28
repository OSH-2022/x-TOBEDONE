# 中央索引服务器:neo4j&server

[中央索引服务器:neo4j&server](#中央索引服务器neo4jserver)

- [neo4j](#neo4j)
  - [安装 jdk11](#安装-jdk11)
  - [安装 neo4j](#安装-neo4j)
  - [部署服务器端](#部署服务器端)

## neo4j

### 安装 jdk11

+ 首先卸载服务器上原本可能存在的 openjdk

  `sudo apt-get remove openjdk*`

+ 在[华为镜像站](https://repo.huaweicloud.com/java/jdk/11.0.1+13/)下载压缩包

  `jdk-11.0.1_linux-x64_bin.tar.gz `

+ 找到一个合适的路径，建议在 `/usr/local`，新建文件夹，在其中解压缩

  `sudo tar zxvf jdk-11.0.1_linux-x64_bin.tar.gz`

+ 从根目录进入 etc/profile 文件

  ```java
  export JAVA_HOME=/usr/local/jdk11/jdk-11.0.1
  export JRE_HOME=${JAVA_HOME}/jre
  export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
  export PATH=${JAVA_HOME}/bin:$PATH
  ```

  注意要将上述代码第一行，路径名称修改为自己设置的位置

+ 使该配置文件生效

  `source /etc/profile`

+ 查看是否成功安装：

  `java -version`

  ![image-20220405145021107](image\image-20220405145021107.png)

  注：可能会出现，使用命令 `source /etc/profile` 后，使用 `java -version`可以正确显示上述命令，而关掉当前命令行终端再打开后，再次输入 `java -version` 却显示没有 java 命令，如已经严格按照上述步骤配置，那么解决方案是重启。

### 安装 neo4j

+ 下载（可以修改版本）

  ` curl -O http://dist.neo4j.org/neo4j-community-4.4.0-unix.tar.gz`

+ 解压缩

  `tar -axvf neo4j-community-4.4.0-unix.tar.gz`

+ 找到解压缩后的文件夹修改配置文件，该配置文件是在 /neo4j-community-4.4.0/conf 中的 neo4j.conf

  `sudo vim neo4j.conf`

+ 可以参考[这个链接](https://blog.csdn.net/u013946356/article/details/81736232)查看更详细的参考，这里只列举几个较为关键的配置

  ①修改 load csv 时路径，找到下面这一行，并在前面加个 #，可从任意路径读取文件
  dbms.directories.import=import

  ②可以远程通过 ip 访问 neo4j 数据库，找到并删除以下这一行开头的 #

  dbms.default_listen_address=0.0.0.0

  ③允许从远程 url 来 load csv
  dbms.security.allow_csv_import_from_file_urls=true

  ④设置 neo4j 可读可写
  dbms.read_only=false

  ⑤默认 bolt 端口是 7687，http 端口是 7474，https 关口是 7473；修改如下：		

  <img src="image\image-20220405151833045.png" alt="image-20220405151833045" style="zoom:67%;" />	

+ 启动服务（同样道理./neo4j stop停止服务）

  `cd neo4j-community-4.4.0`

  `cd bin`

  `./neo4j start`	

+ 浏览器查看
  http://0.0.0.0:7474/
  登录用户名密码默认都是 neo4j
  会让修改一下密码，~~建议修改为 11，因为简单~~

<img src="image\image-20220405153110974.png" alt="image-20220405153110974" style="zoom: 67%;" />

+ 注：可能会出现按照上述步骤配置，能够在命令行显示 neo4j 已经启动，但是浏览器打开对应网址却无法加载，这时考虑是否是因为虚拟机的防火墙导致，关闭防火墙指令：

  `sudo ufw disable`



### 部署服务器端

+ `pip3 install websockets`

  `pip3 install neo4j`

  注：如果没有 python 和 pip3，需先安装好 python，然后运行以下命令安装pip

   `sudo apt-get install python3-pip`
+ 找到位于./web&server/main_server目录下的serverWeb的文件
  
    找到其中的如下所示代码
    ```python
    if __name__ == "__main__":
    #端口名、用户名、密码根据需要改动
    #create_newnode(node)用于创建结点（包括检测标签、创建标签节点、添加相应的边等功能）
    #delete_node(node.name)用于删去名为node.name的结点
    
    #连接数据库 
    scheme = "neo4j"  # Connecting to Aura, use the "neo4j+s" URI scheme
    host_name = "localhost"
    port = 7474
    url = "bolt://47.119.121.73:7687".format(scheme=scheme, host_name=host_name, port=port)
    user = "neo4j"
    password = "disgrafs"
    
    Neo4jServer = pytoneo.App(url, user, password)
    print("Neo4j服务器连接成功...")
    
    #启动webserver服务器
    start_server = websockets.serve(main_logic, '0.0.0.0', 9090)
    print("主服务器初始化成功，等待连接...")
    
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
    ```
    - 将url修改为服务器的公网ip `bolt`字段意义暂时不清楚。若为本机部署请修改为`neo4j://0.0.0.0:7687` 7687为默认端口 如果使用的不是默认端口请自行修改
    - `user`和`password`修改为前文登录`neo4j://0.0.0.0:7474`使用的账号与密码
    - 目前print功能仅表示程序运行至此 **不代表成功连接**
+ 最后到 DisGraFS: /web&serer/main_server 下，运行服务器端：

  `python3 serverWeb.py`

  <img src="image\image-20220405155150240.png" alt="image-20220405155150240"  />

  至此，**服务器启动成功**

  web&server/main_server 这个文件夹也就完成了它的使命

> main_server 中所存放的为服务器端所需的两个文件：pytoneo.py 和 serverWeb.py。pytoneo.py 为工具型程序，用来和 neo4j 数据库进行交互，serverWeb.py 将调用 pytoneo.py 的函数以实现创建结点，删除结点等功能。

