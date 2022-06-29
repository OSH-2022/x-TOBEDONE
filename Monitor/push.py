import os
import time
while 1:
    os.system('curl -XPOST --data-binary @leftrs.txt http://192.168.116.132:9091/metrics/job/leftrs')
    os.system('curl -XPOST --data-binary @online_device_count.txt http://192.168.116.132:9091/metrics/job/online_device_count')
    time.sleep(5)
