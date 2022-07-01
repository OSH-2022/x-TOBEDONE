from time import sleep
import mysql.connector
sql = "SELECT * FROM DFS.DEVICE WHERE ISONLINE=true ORDER BY RS DESC"

if __name__ == "__main__":
    while 1:        
        mydb = mysql.connector.connect(
            host="43.142.97.10",
            user="root",
            passwd="201314",
            database="DFS"
        )
        mycursor = mydb.cursor()
        mycursor.execute(sql)
        myresult = mycursor.fetchall()
        print(myresult)
        fo = open("leftrs.txt","w+")
        s="dontpanic_online_device_count "
        s+=str(len(myresult))
        s+='\n'
        fo.write(s)
        fo.close()
        sleep(5)
