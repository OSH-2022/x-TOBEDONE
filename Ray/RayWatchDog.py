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

if __name__ == "__main__":

    print("Ray Watchdog")

    mountPointNoSlash = "D:"
    mountPointSlash = "D:/"
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

    def on_created(event):
        message = "Watchdog: "
        if not event.is_directory:
            message = "file "
            message += f"{event.src_path} created"
            print(message)
            #sendTasklist.append(createDatapack("create", event.src_path))
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
        wsClient = await websockets.connect(wsUrl)
        return wsClient

    async def wsSender(wsClient):
        while True:
            while len(sendTasklist) == 0:
                await asyncio.sleep(0.1)
            toSend = sendTasklist.popleft()
            while time.time() < toSend["time"]:
                await asyncio.sleep(0.1)
            print("Message sent: ", toSend["message"])
            await wsClient.send(toSend["message"])

    async def wsReceiver(wsClient):
        while True:
            socketRecv = await wsClient.recv()
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
