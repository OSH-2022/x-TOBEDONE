import paramiko


def sshclient_execmd(hostname, port, username, password, command):
    paramiko.util.log_to_file("paramiko.log")

    s = paramiko.SSHClient()
    s.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    s.connect(hostname=hostname, port=port, username=username, password=password)
    # ssh = s.invoke_shell()

    if command == 'start':
        # execmd = "cd /home/ubuntu/Documents/OSH_2022/Project/x-dontpanic/demo/normal/ && java -jar client.jar"
        # execmd = "cd /home/nwj1804/github/x-dontpanic/demo/normal/ && java -jar client.jar"
        s.exec_command("cd /home/nwj1804/github/x-dontpanic/demo/normal/ && java -jar client.jar")
    elif command == 'stop':
        stdin, stdout, stderr = s.exec_command("ps -aux | grep java")
        # print(stdout.read().decode())
        pidset = stdout.read().decode().split('\n')
        for item in pidset:
            if item.find('client.jar')!=-1:
                pid = item.split()
                s.exec_command("kill "+str(pid[1]))
    else:
        stdin, stdout, stderr = s.exec_command(command)
        # stdin.write("Y")  # Generally speaking, the first connection, need a simple interaction.
        print(stdout.read())
        # print(stderr.read())
    s.close()


def main():
    hostname = '43.142.97.10'
    port = 22
    username = 'ubuntu'
    password = 'nuwhyx-kYgqiq-7qifza'
    while True:
        command = input()
        # if execmd == 'start':
        #     # execmd = "java -jar /home/ubuntu/Documents/OSH_2022/Project/x-dontpanic/demo/normal/client.jar"
        #     execmd = "cd /home/nwj1804/github/x-dontpanic/demo/normal/ && java -jar client.jar"
        # elif execmd == 'stop':
        #     sshclient_execmd(hostname, port, username, password, "pidof java")

        sshclient_execmd(hostname, port, username, password, command)


if __name__ == "__main__":
    main()