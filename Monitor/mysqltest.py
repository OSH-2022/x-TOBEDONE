from time import sleep
import mysql.connector
sql = "SELECT * FROM DFS.DEVICE WHERE ISONLINE=true ORDER BY RS DESC"

if __name__ == "__main__":
    while 1:        
        mydb = mysql.connector.connect(
            host="localhost",
            user="root",
            passwd="201314",
            database="DFS"
        )
        mycursor = mydb.cursor()
        mycursor.execute(sql)
        myresult = mycursor.fetchall()
        print(myresult)
        fo = open("leftrs.txt","w+")
        for item in myresult:
            s="dontpanic_device_leftrs{id=\""
            s+=str(item[0])
            s+="\"} "
            s+=str(item[6])
            s+='\n'
            fo.write(s)
        fo.close()
        sleep(5)
