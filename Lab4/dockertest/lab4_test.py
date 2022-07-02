import time
import ray


@ray.remote
class Worker(object):
    def __init__(self):
        self.r = 0.0

    def calculatePiFor(self, start, elements):
        print("begin to run")
        timenow = time.time()
        for i in range(start, start + elements, 2):
            self.r += (2.0/((2*i+1)*(2*i+3)))
        print(time.time() - timenow)
        print("finish run")
        return self.r

    def result(self):
        return self.r

if __name__ == '__main__':
    # 启动Ray，如果想使用已存在的Ray集群，请使用
    ray.init(address = 'auto')
    time1 = time.time()
    futures = []
    # 将任务切分为10个子任务并行计算
    for i in range(8):
        # 初始化子任务（Actor）
        worker = Worker.remote()
        # 每个子任务计算10000个值，这是异步的，not block
        future = worker.calculatePiFor.remote(i * 125000000, 125000000)
        futures.append(future)

    # 等待所有子任务结果
    results = ray.get(futures)
    # 计算最终结果
    print(4.0*sum(results))

    print("total time is: ",time.time()-time1)