import asyncio
import websockets
import pytoneo
import time

#标签服务器数组
tag_array = []
tag_num = 0

class user_struct:
    username = ""
    web_websocket = 0
#用户数组和用户人数
user_array = []
user_num = 0

Neo4jServer = 0

# 服务器端主逻辑
# websocket和path是该函数被回调时自动传过来的，不需要自己传
async def main_logic(websocket, path):
    #引用全局变量
    global tag_num
    global user_num
    global Neo4jServer
    
    try:
        #记录日志
        localtime = time.asctime( time.localtime(time.time()))
        print(localtime,":",end='')
        
        recv_text = await websocket.recv()
        
        print(recv_text)
        
        print("websocket: ",websocket.port)
        
        #主页面链接上时，返回确认信息
        # if recv_text == "mainWeb":
        #     await websocket.send("Close:A Good Connection, please close it")
        #     await websocket.close_connection()
        #     return
        
        #分解接收到的信息
        tag_split = recv_text.split('_')
        print(tag_split)
        
        #网页端连接逻辑
        if tag_split[1] == "web":
        # 现在网页端只保留重命名和删除功能
            print(tag_split,"：",recv_text)
            #记录用户信息
            user_item = user_struct()
            user_item.username = tag_split[0]
            user_item.web_websocket = websocket
            
            user_array.append(tag_split[1])
            user_num = user_num + 1
            
            while True:
                recv_text = await websocket.recv()
                print(tag_split,"：",recv_text)
                
                recv_list = eval(recv_text)
                
                if recv_list[0] == "rename":
                    print("重命名")
                    print(recv_list[1])
                    Neo4jServer.rename_node(recv_list[1])
                    
                elif recv_list[0] == "delete":
                    print("正在删除")
                    print(recv_list[1])
                    Neo4jServer.delete_node(recv_list[1])
                    
                else:
                    print("网页端传输有误:"+recv_list)
                
        #标签端连接逻辑
        elif tag_split[1] == "tag":
            
            tag_array.append(websocket)
            tag_num = tag_num + 1
            
            #debug
            tag_index = 0
            
            while True:
                recv_text = await websocket.recv()
                print(tag_split,"：",recv_text)
                
                recv_list = eval(recv_text)
                
                #现在只剩下create功能
                if recv_list[0] == "create":
                    print("创建指令")
                    print(recv_list[1])
                    Neo4jServer.create_newnode(recv_list[1])
                                        
                else:
                    print("标签服务器传输有误:"+recv_list)
        
        #错误的连接信息        
        else:
            await websocket.send("A wrong user, please check your message.")
            await websocket.close_connection()
            return
    
    #当前的websocket连接断开        
    except websockets.ConnectionClosed:
        
        user_index = -1
        for user_item in user_array:
            user_index = user_index + 1
            #网页连接断开则弹出整个连接
            if user_item.web_websocket == websocket:
                
                #弹出整个连接
                user_array.pop(user_index)
                user_num = user_num - 1
                
                print(user_item.username," web exit")
                return
            
        tag_index = -1
        for tag_item in tag_array:
            tag_index = tag_index + 1
            #弹出当前打标连接
            if tag_item == websocket:
                tag_array.pop(tag_index)
                tag_num = tag_num - 1
                
            print("打标服务器[",tag_index,"]已退出")
            return
        
        #未找到这个websocket    
        print("没见过的神奇websocket")
        
        return
    

if __name__ == "__main__":
    #端口名、用户名、密码根据需要改动
    #create_newnode(node)用于创建结点（包括检测标签、创建标签节点、添加相应的边等功能）
    #delete_node(node) 需要提供文件名 所有者 路径
    
    #连接数据库 
    scheme = "neo4j"  # Connecting to Aura, use the "neo4j+s" URI scheme
    host_name = "localhost"
    port = 7474
    url = "bolt://0.0.0.0:7687".format(scheme=scheme, host_name=host_name, port=port)
    user = "neo4j"
    password = "11"
    Neo4jServer = pytoneo.App(url, user, password)
    print("Neo4j服务器连接成功...")
    
    #启动webserver服务器
    start_server = websockets.serve(main_logic, '0.0.0.0', 9090)
    print("主服务器初始化成功，等待连接...")
    
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
