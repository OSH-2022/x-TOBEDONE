Docker 参考教程 [Docker 入门指南：如何在 Ubuntu 上安装和使用 Docker - 卡拉云 (kalacloud.com)](https://kalacloud.com/blog/how-to-install-and-use-docker-on-ubuntu/#第-1-步-安装-docker)

### 1. 安装 Docker

官方 Ubuntu 存储库中提供的 Docker 安装软件包可能不是最新版本。

Ubuntu 官方的版本库中并不一定是 Docker 最新的安装包，为了保证是最新版，我们从 Docker 官方库来安装。

首先，更新现有的软件包列表：

```bash
sudo apt update
```

注意：如果无法更新，可能是你的软件源指向是国外的服务器，很可能已经被墙。所有首次更新请打开 VPN。

接下来，安装一些必备软件包，让 apt 通过 HTTPS 使用软件包。

```bash
sudo apt install apt-transport-https ca-certificates curl software-properties-common
```

然后将官方 Docker 版本库的 GPG 密钥添加到系统中：

```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
```

执行后显示

```bash
OK
```

将 Docker 版本库添加到APT源：

```bash
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"
```

执行后显示：

```bash
Get:1 https://download.docker.com/linux/ubuntu focal InRelease [36.2 kB]                                
Hit:2 http://cn.archive.ubuntu.com/ubuntu focal InRelease                                               
Get:3 https://download.docker.com/linux/ubuntu focal/stable amd64 Packages [3056 B]
Hit:4 http://cn.archive.ubuntu.com/ubuntu focal-updates InRelease
Hit:5 http://cn.archive.ubuntu.com/ubuntu focal-backports InRelease
Hit:6 http://cn.archive.ubuntu.com/ubuntu focal-security InRelease
Fetched 39.2 kB in 2s (16.1 kB/s)
Reading package lists... Done
```

接下来，我们用新添加的 Docker 软件包来进行升级更新。

```bash
sudo apt update
```

确保要从 Docker 版本库，而不是默认的 Ubuntu 版本库进行安装：

```bash
apt-cache policy docker-ce
```

执行后会看到这样的结果（ Docker 的版本号可能略有不同）

```bash
docker-ce:
  Installed: (none)
  Candidate: 5:19.03.12~3-0~ubuntu-focal
  Version table:
     5:19.03.12~3-0~ubuntu-focal 500
        500 https://download.docker.com/linux/ubuntu focal/stable amd64 Packages
     5:19.03.11~3-0~ubuntu-focal 500
        500 https://download.docker.com/linux/ubuntu focal/stable amd64 Packages
     5:19.03.10~3-0~ubuntu-focal 500
        500 https://download.docker.com/linux/ubuntu focal/stable amd64 Packages
     5:19.03.9~3-0~ubuntu-focal 500
        500 https://download.docker.com/linux/ubuntu focal/stable amd64 Packages
```

请注意，到目前这一步`docker-ce`还未安装，但根据上一步中的列表，可以看到 docker-ce 来自 Docker 官方版本库。

最后，安装 Docker ：

```bash
sudo apt install docker-ce
```

现在 Docker 已经安装完毕。我们启动守护程序。检查 Docker 是否正在运行：

```bash
sudo systemctl status docker
```

执行结果类似以下内容，说明该服务处于活动状态并且正在运行：

```bash
● docker.service - Docker Application Container Engine
     Loaded: loaded (/lib/systemd/system/docker.service; enabled; vendor preset: enabled)
     Active: active (running) since Sat 2020-07-08 02:55:20 UTC; 5min ago
TriggeredBy: ● docker.socket
       Docs: https://docs.docker.com
   Main PID: 4287 (dockerd)
      Tasks: 8
     Memory: 36.4M
     CGroup: /system.slice/docker.service
             └─4287 /usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock
```

现在，安装 Docker 不仅可以提供 Docker 服务（守护程序），还可以为您提供`docker`命令行实用程序或 Docker 客户端。

### 2. 在不使用 sudo 的情况下执行 Docker 命令

（该步骤不影响 Docker 使用）

默认情况下，`docker`命令只能由 **root** 用户或由 **docker** 组中的用户运行，docker 组用户是在Docker安装过程中自动创建的。

如果你在执行`docker`命令时，没有用 sudo ，并且使用的用户也不是 docker 组成员，那么结果会显示：

```bash
Output
docker: Cannot connect to the Docker daemon. Is the docker daemon running on this host?.
See 'docker run --help'.
```

如果要避免`sudo`在运行`docker`命令时键入任何内容，请将用户名添加到`docker`组中：

如果不想用 sudo 来执行 docker 命令，那么我们只需要把对应的用户添加到 docker 组中即可。（指令的 ${USER} 换成自己的用户名，下同）

```bash
sudo usermod -aG docker ${USER}
```

使用新组成员身份执行命令，需要注销后重新登录，或使用su来切换身份。

```bash
su - ${USER}
```

系统将提示你输入此用户密码以继续。

我们可以通过`id`这个命令来确认刚刚添加的用户是否已经在 docker 组中：

```bash
id -nG
Output
kalacloud sudo docker
```

如果你需要添加一个用户到`docker`组，而你又不是以该用户的身份登录的，可使用此命令来添加：

```bash
sudo usermod -aG docker ${USER}
```

### 3. 部署 Dontpanic 存储服务端节点

先安装 docker-compose:

```bash
sudo apt install docker-compose
```

切到 `x-dontpanic\demo\docker\dirserver` 目录（docker-compose.yml 所在的目录）下，运行 ：

```bash
docker-compose build
```

注意，如果进行第二步操作，需要在指令前面加上 `sudo`，否则出现如下报错信息。

<img src="image/docker-compose-err.png" style="zoom: 80%;" />

成功后会显示如下信息：

![](image/dontpanic-successful.png)

然后运行：

```
docker-compose up
```

服务端节点就成功运行起来了（同样，当前用户如果不在 docker 用户组中，则需要在命令前加上 `sudo`）。

### 4. 目前存在的问题

容器化没有正常启动 DFS 数据库，导致出现 pipe broken

