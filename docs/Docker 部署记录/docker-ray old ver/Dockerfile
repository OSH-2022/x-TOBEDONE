FROM ubuntu

ENV DEBIAN_FRONTEND noninteractive

WORKDIR /

COPY . /

RUN sed -i s/archive.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    sed -i s/security.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    apt-get update
    # 换用国内的源

RUN apt-get install -y software-properties-common && \
    add-apt-repository -y ppa:deadsnakes/ppa && \
    apt-get update && \
    apt install -y python3.8
    # 获取 python3.8

RUN apt-get install -y gcc && apt-get install -y wget && apt-get install -y git
    # 安装相关的编译环境

RUN wget https://bootstrap.pypa.io/get-pip.py && \
    apt-get install -y python3.8-distutils && \
    python3.8 get-pip.py
    # 获得 python3.8 对应的 pip

RUN python3.8 -m pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple &&\
    python3.8 -m pip install -r requirements.txt && \
    python3.8 -m pip install -U ray
    # 下载依赖的包

RUN python3.8 -m nltk.downloader stopwords

RUN python3.8 -m spacy download en
    
RUN python3.8 -m nltk.downloader universal_tagset

RUN cd pke && python3.8 setup.py install && cd ..

RUN cp -r ./pke /usr/local/lib/python3.8/dist-packages/pke/

# RUN ray start --head --port=6379

# RUN cd ray_tagging

# ENTRYPOINT [ "python3.8" ]

# CMD ["tag_server.py" ]
