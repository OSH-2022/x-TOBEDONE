import asyncio
import websockets
import time
import paramiko


# 服务器端主逻辑
# websocket和path是该函数被回调时自动传过来的，不需要自己传

client_path = ["", "/home/ubuntu/Documents/OSH_2022/Project/client_test/t1/", "/home/ubuntu/Documents/OSH_2022/Project/client_test/t2/"] # path[i] 表示 ID 为 1 的 client 存碎片的路径

async def main_logic(websocket, path):
    print("server begin")
    #引用全局变量
    try:
        #记录日志
        localtime = time.asctime( time.localtime(time.time()))
        print(localtime)
        
        recv_text = await websocket.recv()
        print(recv_text)
        
        lis = eval(recv_text)   # 获取接收到的数据
        
        paramiko.util.log_to_file("paramiko.log")

        s = paramiko.SSHClient()
        s.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        hostname = '43.142.97.10'
        port = 22
        username = 'ubuntu'
        password = 'nuwhyx-kYgqiq-7qifza'
        
        
        s.connect(hostname=hostname, port=port, username=username, password=password)
        
        for pair in lis:  # 遍历要删除的文件碎片的 id
            frag_id = pair[0]
            device_id = pair[1]
            execmd = "rm " + client_path[device_id] + str(frag_id)
            stdin, stdout, stderr = s.exec_command(execmd)
            stdin.write("Y")
            
            execmd = "rm " + client_path[device_id] + str(frag_id) + ".digest"
            stdin, stdout, stderr = s.exec_command(execmd)
            stdin.write("Y")
            
            '''
            for frag_id in range(100*id , 100*id+7):
                execmd = "rm " + '/home/ubuntu/Documents/OSH_2022/Project/client_test/t1/'+ str(frag_id)
                print(execmd)
                stdin, stdout, stderr = s.exec_command(execmd)
                stdin.write("Y")
                
                execmd = "rm " + '/home/ubuntu/Documents/OSH_2022/Project/client_test/t1/' + str(frag_id) + ".digest"
                print(execmd)
                stdin, stdout, stderr = s.exec_command(execmd)
                stdin.write("Y")
            '''
            
        
        
        print("websocket: ",websocket.port)
        

    #当前的websocket连接断开        
    except websockets.ConnectionClosed:

        print("websocket closed")
        return
    
    # #其他的异常情况
    # except:
    #     print("错误的连接信息")

if __name__ == "__main__":
    #端口名、用户名、密码根据需要改动
    #create_newnode(node)用于创建结点（包括检测标签、创建标签节点、添加相应的边等功能）
    #delete_node(node.name)用于删去名为node.name的结点



    #连接数据库 
    start_server = websockets.serve(main_logic, '0.0.0.0', 9092, ping_interval = None)
    print("主服务器初始化成功，等待连接...")
    
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
