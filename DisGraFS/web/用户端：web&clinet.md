# 用户端:web&server

[用户端:web&server](#用户端webserver)

- [Web](#web)
  - [Web 文件夹](#web-文件夹)
  - [网页端文件修改](#网页端文件修改)
    - [具体操作流程](#具体操作流程)
- [客户端](#客户端)
  - [安装客户端](#安装客户端)

## Web

### Web 文件夹

> fonts，js，css，sass，index.html 组成第一层网页，用于登陆和检测服务器是否已连接的目的
>
> GraphGui 为一个废弃的图数据库交互页面，以免后来人用的上，故暂且放置于文件夹中
>
> GraphGui2 为当前的图数据库交互页面，有搜索，打开，删除文件的功能，从第一层登陆网页登陆后将进入此页面。其中 action.js，ui.js，login.js 是用于服务于交互页面的 js 文件，login.js 用于刚刚进入网页时连接上上面所述的服务器主程序 serverWeb.py，action.js 用于进行一些事件的反馈相应，如点击“打开文件”后向服务器发生消息等，ui.js 处理一些诸如鼠标点击等事件的相应。其他文件为网页框架文件，值得一提的是 node_modules 文件夹里面是一个较好的使用 js 和 neo4j 进行交互的一个js框架，名字叫做 pototo，如果需要修改底层交互的方式，请修改pototo 文件。d3 是一个较好的使用 js 显示图形的框架，详细的使用说明可以查阅其官网的文档。如需修改此页面较上层的一些交互逻辑，可修改 GraphGui2/js/main.js。如需修改搜索的部分，可修改 GraphGui2/js/auto-complete.js，这两个文件都是已经被本组修改过的，若需要原始文件可在 pototo 的 github 上获得。
>
> Download 为下载客户端的页面，DownloadFile 文件夹内用来存放客户端，如需更改存放的客户端的名字请同步更改 Download 下 index.html 中 a 标签的 href 值。其他文件夹中的文件均为网页框架，对网页主逻辑不构成影响。

### 网页端文件修改

> 直接将网页文件直接部署到服务器上即可，其中有形如 **47.119.121.73** 的部分，均改为你自己的服务器的公网IP即可。

#### 具体操作流程

找到`x-DisGraFS\web&server\web\index.html`

找到代码：
```html
var ws = new WebSocket("ws://192.168.14.98:9090"); //创建WebSocket连接
```
将上述`192.168.14.98`更改为服务器的公网ip
- 同理 如果想在 windows 上连接到 ubuntu 虚拟机的 serveWeb 也只需要输入为虚拟机的 ip

  虚拟机 ip 可以在 ubuntu-桌面-右键-网络-有线-设置-IpV4 地址
  如果连接失败请尝试 windows 能否 ping 到虚拟机 ip，虚拟机能否 ping 到 windowsip
  如果不行，请检查 windows 防火墙-高级设置-入站规则-虚拟机监控回显请求ipv4 打开应该就可以了

- 如果是在同一台电脑上运行请使用 `127.0.0.1` 即环路 ip

在打开 `serverWeb.py` 的前提下 打开 `index.html` 即可成功连接到服务端
连接成功标志为 serveWeb.py 运行的终端看到如下提示

```shell
Neo4j服务器连接成功...
主服务器初始化成功，等待连接...
Sat Apr  9 13:07:41 2022 :mainWeb
websocket:  9090
```
并且打开index.html未出现错误提示 `file:// 连接服务器失败`

## 客户端

> 客户端提供了两个平台，Windows 和 Ubuntu。启动客户端是依靠 `DisGraFS-Client.py`，Windows 和 Ubuntu 下，这个 python 文件也会有细微的差别。

### 安装客户端

双击 setup.dat 即可弹出什么就安装什么