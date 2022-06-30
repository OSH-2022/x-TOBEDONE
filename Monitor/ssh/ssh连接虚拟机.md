[(13条消息) 如何使用虚拟机利用SSH协议与主机连接_今年夏天.的博客-CSDN博客_虚拟机ssh](https://blog.csdn.net/qq_42828394/article/details/123284099?utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~aggregatepage~first_rank_ecpm_v1~rank_v31_ecpm-1-123284099-null-null.pc_agg_new_rank&utm_term=虚拟机连接ssh&spm=1000.2123.3001.4430)

### NAT 模式

#### 检查 VMware 服务情况

任务管理器-服务，保证 VMware DHCP Service 和 VMware NAT Service 这两个服务启动，且正常运行

#### 本机查看 VMnet8 网段

dos 窗口执行命令 ipcondig /all

我的 VMnet8 的网段是 192.168.10.X

#### 虚拟机的配置

网络适配设置为 NAT 模式

#### 检查虚拟机的网段

VMware-编辑-虚拟网络编辑器，查看 VMware8 的 IP 网段是否和第二步查看的相同

#### 修改虚拟机的 ip 为静态 ip

#### 

