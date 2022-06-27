# 计算端:ray&tagging

[计算端:ray&tagging](#计算端raytagging)

- [Ray](#ray)
  - [Ray cluster 搭建](#ray-cluster-搭建)
    - [header 节点](#header-节点)
    - [worker节点](#worker节点)
- [tagging](#tagging)
  - [tagging程序依赖包安装](#tagging程序依赖包安装)

## Ray

+ 前提：虚拟机中安装有 python 和 pip

+ 安装 ray 最新发行版：

  ```shell
  sudo apt-get update
  sudo pip3 install -U ray
  sudo pip3 install 'ray[default]' 
  ```

  注：可能会遇到如下报错：

  > The directory 'xxx' or its patent directory is not owned by the current...

  这个 warning 的内容大概是，当前用户不拥有目录或其父目录，并且缓存已被禁用。可以忽略这个 warning，如果想要解决，则可修改为如下命令：

  ```shell
  sudo -H pip3 install ...
  ```

  安装结果如下：

  <img src="image\image-20220408161425411.png" alt="image-20220408161425411" style="zoom:80%;" />

+ 另外，我的 python 版本是 3.6.9，之后可能会需要统一分布式集群的 python 版本

### Ray cluster 搭建

前提要求：各台服务器在**同一个局域网**中，安装有**相同版本的python和ray**。

#### header 节点

```shell
ray start --head --port=6379
```

<img src="image\image-20220408162006133.png" alt="image-20220408162006133" style="zoom:67%;" />

#### worker节点

```shell
ray start --address='192.168.10.132:6379' --redis-password='5241590000000000' #视实际情况修改address
```

预期看到以下界面

<img src="image\image-20220408162751753.png" alt="image-20220408162751753" style="zoom:80%;" />

如果要退出集群，只需

```shell
ray stop
```

## tagging

### tagging程序依赖包安装

默认配置为清华源

```shell
pip install pdfplumber
pip install sphinx
pip install ffmpeg	#这一句出了问题,会有 warning 或许应该修改为：sudo apt install ffmpeg

pip install SpeechRecognition
pip install tinytag
pip install pydub
pip install nltk
pip install spacy
python -m nltk.downloader stopwords
python -m nltk.downloader universal_tagset
python3 -m spacy download en
pip install git+https://github.com/boudinfl/pke.git
```



可能出现的问题以及解决方案：

1. 可能会出现如下 warning：

   ![image-20220408165842811](image\image-20220408165842811.png)

   解决方案：将提到的路径添加到环境变量

   ```shell
   vim ~/.bashrc
   export PATH=/home/xxx/.local/bin/:$PATH #这一行放在 .bashrc 文件的最后，xxx 替换为你的用户名
   source ~/.bashrc
   ```

2. 只能 python3 安装，不能 python 安装

   解答：这是因为 /usr/bin 下面只有 python3 命令，没有 python 命令。解决方案是做一个软链接：

   `sudo ln -s /usr/bin/python3 /usr/bin/python`

3. pip3 安装报错

   使用 pip 安装
