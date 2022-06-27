import asyncio
import websockets
import time


#标签服务器数组
tag_array = []
tag_num = 0


# 服务器端主逻辑
# websocket和path是该函数被回调时自动传过来的，不需要自己传
async def main_logic(websocket, path):
    #引用全局变量
    global tag_num
    
    try:
        #记录日志
        localtime = time.asctime( time.localtime(time.time()))
        print(localtime,":",end='')
        
        recv_text = await websocket.recv()
        
        print(recv_text)
        
        print("websocket: ",websocket.port)

        #分解接收到的信息
        tag_split = recv_text.split('_')
        print(tag_split)

        #标签端连接逻辑
        tag_array.append(websocket)
        tag_num = tag_num + 1
        print("^^^^")

        # debug
        tag_index = 0

        while True:
            print("&&&&")
            recv_text = await websocket.recv()
            print(tag_split, "：", recv_text)

            recv_list = eval(recv_text)

            # 尝试解码从tag服务器中读出的数据，然后进行增删改的操作，所有跟数据库交互的操作在这边完成
            if recv_list[0] == "create":
                print("创建指令")
                print(recv_list[1])

            elif recv_list[0] == "error":
                print("错误指令")

            else:
                print("标签服务器传输有误")

    
    #当前的websocket连接断开        
    except websockets.ConnectionClosed:
            
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
        print("client didn't login")
        
        return
    
    # #其他的异常情况
    # except:
    #     print("错误的连接信息")

if __name__ == "__main__":
    #端口名、用户名、密码根据需要改动
    #create_newnode(node)用于创建结点（包括检测标签、创建标签节点、添加相应的边等功能）
    #delete_node(node.name)用于删去名为node.name的结点

    
    #启动webserver服务器
    start_server = websockets.serve(main_logic, '0.0.0.0', 9090)
    print("主服务器初始化成功，等待连接...")
    
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
