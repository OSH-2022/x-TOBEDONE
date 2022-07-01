import os
import time
while 1:
    os.system('curl -XPOST --data-binary @device_leftrs.txt http://124.223.225.122:9100/metrics/job/leftrs')
    os.system('curl -XPOST --data-binary @online_device_count.txt http://124.223.225.122:9100/metrics/job/online_device_count')
    time.sleep(5)
