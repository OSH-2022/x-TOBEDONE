import os
import time
while 1:
    os.system('curl -XPOST --data-binary @device_leftrs.txt http://43.142.97.10:9091/metrics/job/device_leftrs')
    os.system('curl -XPOST --data-binary @online_device_count.txt http://43.142.97.10:9091/metrics/job/online_device_count')
    time.sleep(5)
