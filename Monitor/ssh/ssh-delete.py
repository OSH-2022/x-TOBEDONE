import asyncio
import websockets
import time


# 服务器端主逻辑
# websocket和path是该函数被回调时自动传过来的，不需要自己传
async def main_logic(websocket, path):
    #引用全局变量
    try:
        #记录日志
        localtime = time.asctime( time.localtime(time.time()))
        print(localtime)
        
        recv_text = await websocket.recv()



        print(recv_text)
        
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
    start_server = websockets.serve(main_logic, '0.0.0.0', 9091, ping_interval = None)
    print("主服务器初始化成功，等待连接...")
    
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
