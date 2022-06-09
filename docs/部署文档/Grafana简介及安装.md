# Grafana

## 安装试运行

**Step1：** 从官网上下载并解压：

```shell
wget https://dl.grafana.com/enterprise/release/grafana-enterprise-8.5.3.linux-amd64.tar.gz
tar -zxvf grafana-enterprise-8.5.3.linux-amd64.tar.gz
```

**Remark：** wget下载默认路径为当前路径

**Step2：** 试运行

```shell
cd grafana-8.5.3/
cd bin/
./grafana-server
```

保持终端，浏览器访问`192.168.116.129:3000` 出现Grafana界面，默认用户名和密码都是admin，出现如下界面说明正常：

![](images/image9.png)

**Step3：** 添加数据源

左侧菜单栏：Configuration -> Data Sources -> Prometheus到如下界面，将URL改为自己的IP加默认端口9090：

![](images/image10.png)

其余默认，点击Save&Test至显示如下：

<img title="" src="images/image11.png" alt="" width="468">
