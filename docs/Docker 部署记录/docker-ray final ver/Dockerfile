FROM ubuntu

ENV DEBIAN_FRONTEND noninteractive

WORKDIR /

COPY . /

RUN sed -i s/archive.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    sed -i s/security.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list &&\
    apt-get update
    # 换用国内的源

RUN apt install -y python3 && apt install -y python3-pip

RUN apt-get install -y gcc && apt-get install -y wget && apt-get install -y git
    # 安装相关的编译环境

RUN pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple &&\
    pip3 install -r requirements.txt && \
    pip3 install -U ray
    # 下载依赖的包

RUN python3 -m nltk.downloader stopwords

RUN python3 -m spacy download en
    
RUN python3 -m nltk.downloader universal_tagset

RUN cd pke && python3 setup.py install && cd ..

RUN cp -r ./pke /usr/local/lib/python3.10/dist-packages/pke/

# RUN ray start --head --port=6379

# RUN cd ray_tagging

# ENTRYPOINT [ "python3.8" ]

# CMD ["tag_server.py" ]
