FROM ubuntu

WORKDIR /

COPY . /

RUN sed -i s/archive.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    sed -i s/security.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    apt-get update

RUN apt install -y python3 && apt install -y python3-pip

RUN pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple

RUN pip3 install -U ray

# CMD ["ray start --head --port=6379"]
