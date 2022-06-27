import os
import sys
import websockets
import asyncio
import time
import webbrowser
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from watchdog.events import PatternMatchingEventHandler
from collections import deque
import _thread
import queue
import _thread
import tagging

taskQueue = queue.Queue()
sendQueue = queue.Queue()

if __name__ == "__main__":

    print("Ray")

    mountPointNoSlash = "/tmp/raytest"
    mountPointSlash = "/tmp/raytest"
    wsUrl = "ws://localhost:9090"

    ############
    # watchdog #
    ############

    def createDatapack(type, path1 : str, path2 = ""):
        timeStamp = int(round(time.time() * 1000))
        purePath1 = path1[len(mountPointSlash):]
        purePath1 = purePath1.replace('\\', '/')
        purePath2 = ""
        if path2 != "":
            purePath2 = path2[len(mountPointSlash):]
            purePath2 = purePath2.replace('\\', '/')
        dictObj = { "type": type, "path1": purePath1, "path2": purePath2, "time":timeStamp }
        return repr(dictObj)

    sendTasklist = deque()
    modifyCounter = dict()

    print("Establishing watchdog observer...")


    # 处理新创建的文件
    def cmd_handler():
        cmd_text = str()
        if not taskQueue.empty():
            cmd_text = taskQueue.get()
        else:
            return

        result = ()
        # 接收标签返回值
        tag_recv = ""
        # try:
        cmd_dict = eval(cmd_text)
        if cmd_dict["type"] == "create":
            tag_recv = tagging.tagging(cmd_dict["path1"])
            result = ("create", tag_recv)
        else:
            result = ("invalid",)

        # return str(result)
        print("-----------            ",result)
        sendQueue.put(str(result))


    def on_created(event):
        if event.src_path.find("digest") != -1:
            return

        message = "Watchdog: "
        if not event.is_directory:
            message = "file "
            message += f"{event.src_path} created"
            cmd_text = str()
            cmd_text = str(createDatapack("create", event.src_path))
            taskQueue.put(cmd_text)

            # 进行打标
            _thread.start_new_thread(cmd_handler, ())

            print(message)
            modifyCounter[event.src_path] = 0


    event_handler = FileSystemEventHandler()
    event_handler.on_created = on_created
    my_observer = Observer()
    while not os.path.exists(mountPointSlash):
        time.sleep(0.1)
    my_observer.schedule(event_handler, mountPointSlash, recursive=True)
    my_observer.start()
    print("Watchdog observer established")

    async def login():
        wsClient = await websockets.connect('ws://localhost:9090')
        # 发送服务器姓名
        await wsClient.send("PYT_tag")
        print("Connected")
        return wsClient

    async def wsSender(wsClient):
        while True:
            while sendQueue.empty():
                await asyncio.sleep(0.1)
            result = sendQueue.get()
            # result = """('create', {'labels': ['xxa'], 'property': "{name: '1.txt', ext: 'txt', size: '12', owner: 'xxa'}"})"""
            print("sending: ", result)

            await wsClient.send(result)

    async def wsReceiver(wsClient):
        while True:
            socketRecv = await wsClient.recv()
            """
            try:
                command = eval(socketRecv)
                if command["command"] == "exit":
                    asyncio.get_event_loop().stop()
                    return
                elif command["command"] == "open":
                    webbrowser.open("file://" + mountPointSlash + command["parameter"][0])
                elif command["command"] == "delete":
                    #print("deleting: ", mountPointSlash + command["parameter"][0])
                    os.remove(mountPointSlash + command["parameter"][0])
                else:
                    print("Error: Failed to resolve command from server:", socketRecv)
            except Exception:
                print("Error: Failed to execute command from server:", socketRecv)
            """

    try:
        loop = asyncio.get_event_loop()
        wsClient = loop.run_until_complete(login())

        loop.run_until_complete(asyncio.wait([wsSender(wsClient), wsReceiver(wsClient)]))


    except KeyboardInterrupt:
        pass

    finally:
        my_observer.stop()
        loop.call_soon(wsClient.close())
        input("Press Enter to quit")
        sys.exit(0)
