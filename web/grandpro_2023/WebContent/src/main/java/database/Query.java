package database;

import java.sql.*;

public class Query {

    // 如果一个变量被声明为final，它只能被赋值一次。最终变量的值在设置后不能修改。
    
    //驱动程序名
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // URL指向要访问的数据库名mydata
    static final String DB_URL = "jdbc:mysql://localhost:3306/mysql?useSSL=false&serverTimezone=Asia/Shanghai";

    //static final String DB_URL = "jdbc:mysql://mymysql:3306/mysql?useSSL=false"; // docker
    static final String USER = "root";
    static final String PASS = "201314";

    Connection conn = null;

    public Query(){ // 构造函数
        try{
            //Class.forName 传入 com.mysql.jdbc.Driver 之后,就知道我连接的数据库是 mysql
            // 声明Connection对象
            Class.forName(JDBC_DRIVER);
            
            // 远程连接数据库
            // getConnection()方法，连接MySQL数据库
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
            if (conn!=null)
                conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileItem queryFile(String path,String name,String mywhose){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE NAME='%s' AND PATH='%s' AND WHOSE='%s';",name,path,mywhose);
            // 执行查询操作
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int id  = rs.getInt("ID");
                int nod = rs.getInt("NOD");
                int noa = rs.getInt("NOA");
                String attr = rs.getString("ATTRIBUTE");
                String time = rs.getString("TIME");
                boolean isFolder = rs.getBoolean("ISFOLDER");
                String fileType=rs.getString("FILETYPE");
                int fileSize=rs.getInt("FILESIZE");
                String whose=rs.getString("WHOSE");
                int isShare=rs.getInt("ISSHARE");
                int originID=rs.getInt("ORIGINID");

                fileItem=new FileItem(id,name,path,attr,time,nod,noa,isFolder,fileType,fileSize,whose,isShare,originID);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileItem;
    }
	
    public boolean changePath(String path, String name, String newpath, String whose){
        Statement stmt=null;
        try{
            stmt = conn.createStatement();

            String sql;
            sql = String.format("Update DFS.FILE SET PATH='%s' WHERE NAME='%s' AND PATH='%s' AND WHOSE='%s';", newpath, name, path, whose);
            
            stmt.executeUpdate(sql);

            System.out.println("重命名成功");
            conn.close();
            System.out.println("数据库关闭成功");
            return  true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean renameFile(String path, String name, String newname, String whose){
        Statement stmt=null;
        try{
            stmt = conn.createStatement();

            String sql;
            sql = String.format("Update DFS.FILE SET NAME='%s' WHERE NAME='%s' AND PATH='%s' AND WHOSE='%s'", newname, name, path,whose);
            
            stmt.executeUpdate(sql);

            System.out.println("重命名成功");
            conn.close();
            System.out.println("数据库关闭成功");
            return  true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDir(String path, String name, String whose){
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql;
            sql = String.format("DELETE FROM DFS.FILE WHERE NAME='%s' AND PATH='%s' AND ISFOLDER='1' AND WHOSE='%s';", name, path, whose);

            stmt.executeUpdate(sql);

            System.out.println("数据库删除成功");
            conn.close();
            System.out.println("数据库关闭成功");
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public FileItem[] querySharedFile(int id){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;

        try{
            stmt = conn.createStatement();
            String sql;
            
            // 查出该文件相关的共享文件
            sql = String.format("SELECT * FROM DFS.FILE WHERE ORIGINID='%d'", id);

            rs = stmt.executeQuery(sql);
            
            if(!rs.last())
                return null;

            
            int count = rs.getRow();

            fileArray=new FileItem[count];
            int i=0;
            rs.first();

            while(i<count){
                int fileid = rs.getInt("ID");
                int nod = rs.getInt("NOD");
                int noa = rs.getInt("NOA");
                String whose = rs.getString("WHOSE");
                String name = rs.getString("NAME");
                String attr = rs.getString("ATTRIBUTE");
                String time = rs.getString("TIME");
                boolean isFolder = rs.getBoolean("ISFOLDER");
                String fileType=rs.getString("FILETYPE");
                int fileSize=rs.getInt("FILESIZE");
                String path=rs.getString("PATH");
                int isShare=rs.getInt("ISSHARE");
                int originID=rs.getInt("OriginID");
                fileArray[i]=new FileItem(fileid,name,path,attr,time,nod,noa,isFolder,fileType,fileSize,whose,isShare,originID);
                rs.next();
                i++;
            }
            conn.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileArray;
    }

    public boolean deleteFile(String path, String name, String whose){
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql;
            sql = String.format("DELETE FROM DFS.FILE WHERE NAME='%s' AND PATH='%s' AND ISFOLDER='0' AND WHOSE='%s';", name, path, whose);

            stmt.executeUpdate(sql);

            System.out.println("数据库删除成功");
            conn.close();
            System.out.println("数据库关闭成功");
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFragment(int fragmentID){
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql;
            sql = String.format("DELETE FROM DFS.FRAGMENT WHERE ID='%d'", fragmentID);

            stmt.executeUpdate(sql);

            System.out.println("数据库删除成功");
            //conn.close();
            System.out.println("数据库关闭成功");
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /*
	public FileItem queryFile(int id){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
            	int noa = rs.getInt("NOA");
                String name  = rs.getString("NAME");
                String path  = rs.getString("PATH");              
                String attr = rs.getString("ATTRIBUTE");
                String time = rs.getString("TIME");
                boolean isFolder = rs.getBoolean("ISFOLDER");
    
                fileItem=new FileItem(id,name,path,attr,time,noa,isFolder);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return fileItem;
	}

    public FileItem[] queryFile(String whose){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;

        int id, noa;
        String name,attr, time, path;
        boolean isFolder;

        int count,i;

        try{
            stmt = conn.createStatement();
            String sql;
            //sql = String.format("SELECT * FROM DFS.FILE WHERE WHOSE='xixi' ");
            //sql = String.format("SELECT * FROM DFS.FILE WHERE WHOSE='%s' ",whose);
            //sql = "SELECT * FROM DFS.FILE WHERE WHOSE='"+whose+"' AND PATH='/'";
            sql = "SELECT * FROM DFS.FILE WHERE WHOSE='"+whose+"'";
            rs = stmt.executeQuery(sql);
            if (!rs.last())
                return null;
            count = rs.getRow();
            fileArray=new FileItem[count];
            i=0;
            rs.first();

            while (i<count) {
                id = rs.getInt("ID");
                noa = rs.getInt("NOA");
                name = rs.getString("NAME");
                path = rs.getString("PATH");
                attr = rs.getString("ATTRIBUTE");
                time = rs.getString("TIME");
                isFolder = rs.getBoolean("ISFOLDER");

                fileArray[i]=new FileItem(id,name,path,attr,time,noa,isFolder);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileArray;
    }*/

    public int queryUserTime(String whose){
        Statement stmt = null;
        ResultSet rs = null;
        int time = 0;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.USER WHERE NAME='%s'", whose);
            rs = stmt.executeQuery(sql);

            if (rs.next())
                time = rs.getInt("TIME");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        //System.out.println(time);
        return time;
    }

    /*
    public int queryDeviceTime(int id){
        Statement stmt = null;
        ResultSet rs = null;
        int time = 0;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ID='%d'", id);
            rs = stmt.executeQuery(sql);

            if (rs.next())
                time = rs.getInt("TIME");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        //System.out.println(time);
        return time;
    }*/


    public FileItem[] queryDirFile(String whose, String dirpath){
        // dirpath 是目录的绝对路径
        // 查询某个目录下的文件列表
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;

        int id, noa, nod;
        String name, attr, time;
        boolean isFolder;

        int count, i;

        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = String.format("SELECT * FROM DFS.FILE WHERE WHOSE='%s' AND PATH REGEXP '^%s';", whose, dirpath);

            rs = stmt.executeQuery(sql);
            
            if(!rs.last())
                return null;

            count = rs.getRow();

            fileArray=new FileItem[count];
            i=0;
            rs.first();

            while(i<count){
                id = rs.getInt("ID");
                nod = rs.getInt("NOD");
                noa = rs.getInt("NOA");
                name = rs.getString("NAME");
                attr = rs.getString("ATTRIBUTE");
                time = rs.getString("TIME");
                isFolder = rs.getBoolean("ISFOLDER");
                String fileType=rs.getString("FILETYPE");
                int fileSize=rs.getInt("FILESIZE");
                String path=rs.getString("PATH");
                int isShare=rs.getInt("ISSHARE");
                int originID=rs.getInt("OriginID");
                fileArray[i]=new FileItem(id,name,path,attr,time,nod,noa,isFolder,fileType,fileSize,whose,isShare,originID);
                rs.next();
                i++;
            }
            conn.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileArray;
    }

    public FileItem[] queryFileList(String whose, String path){
        // 查询文件列表
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;

        int id, noa,nod;
        String name,attr, time;
        boolean isFolder;

        int count,i;

        try{
            // 创建statement类对象，用来执行SQL语句 
            stmt = conn.createStatement();
            String sql;

            //要执行的SQL语句
            sql = "SELECT * FROM DFS.FILE WHERE WHOSE='"+whose+"' AND PATH='"+path+"'";

            // rs 为 ResultSet 类，用来存放获取的结果集！！
            rs = stmt.executeQuery(sql);
            if (!rs.last())
                return null;
            count = rs.getRow();
            fileArray=new FileItem[count];
            i=0;
            rs.first();

            while (i<count) {
                id = rs.getInt("ID");
                nod = rs.getInt("NOD");
                noa = rs.getInt("NOA");
                name = rs.getString("NAME");
                attr = rs.getString("ATTRIBUTE");
                time = rs.getString("TIME");
                isFolder = rs.getBoolean("ISFOLDER");
                String fileType=rs.getString("FILETYPE");
                int fileSize=rs.getInt("FILESIZE");
                int isShare=rs.getInt("ISSHARE");
                int originID=rs.getInt("ORIGINID");

                fileArray[i]=new FileItem(id,name,path,attr,time,nod,noa,isFolder,fileType,fileSize,whose,isShare,originID);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileArray;
    }
/*
	public FileItem[] queryFile(String path){
		Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;
        
        int id, noa;
        String name,attr, time;
        boolean isFolder;
        
        int count,i;
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE PATH='%s'",path);
            rs = stmt.executeQuery(sql);
            
            if (!rs.last())
            	return null;           	
            count = rs.getRow();
            fileArray=new FileItem[count];
            i=0;
            rs.first();
            
            while (i<count) {
                id = rs.getInt("ID");                
                noa = rs.getInt("NOA");
                name = rs.getString("NAME");
                attr = rs.getString("ATTRIBUTE");
                time = rs.getString("TIME");
                isFolder = rs.getBoolean("ISFOLDER");
    
                fileArray[i]=new FileItem(id,name,path,attr,time,noa,isFolder);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return fileArray;
	}*/

    public boolean queryDir(String whose, String dirName, String path){
    // 用于查询某个用户的特定路径下是否有某个名字的目录
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE WHOSE='%s' AND NAME='%s' AND PATH='%s' AND ISFOLDER='1'; ", whose, dirName, path);
            rs = stmt.executeQuery(sql);
            conn.close();
            if(rs.next()) //说明已经存在这个文件夹
                return true;
            else{
                return false; //说明这个文件夹不存在
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean addDir(String whose, String dirName, String path){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("INSERT INTO DFS.FILE (NAME,PATH,ATTRIBUTE,TIME,NOD,NOA,ISFOLDER,WHOSE,FILETYPE,FILESIZE)"
            +"VALUES ('%s','%s','rwxrwxrwx','', 0, 0,true,'%s','', 0);", dirName, path, whose);

            stmt.executeUpdate(sql);

            conn.close();

            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String queryFragment(int id){
        Statement stmt = null;
        ResultSet rs = null;
        String path = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FRAGMENT WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);


            if (rs.next())
                path = rs.getString("PATH");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return path;
    }

    public int queryFragmentNumbers(int fileId){
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.FRAGMENT WHERE ID>='%d' AND ID<'%d'",
                    fileId*100, (fileId+1)*100);
            rs = stmt.executeQuery(sql);

            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
    }

    public String queryFragDevice(int fragid){
        // 查询某个文件碎片存在哪个节点上
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FRAGMENT WHERE ID='%d';", fragid);

            rs = stmt.executeQuery(sql);

            // 返回文件碎片所在的存储节点
            String ret = rs.getString("PATH");
            
            conn.close();

            return ret;

        }catch(Exception e)
        {
            e.printStackTrace();
            return "1000";
        }
    }

    public int queryDeviceLftRS(int deviceid){
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ID='%d';", deviceid);

            rs = stmt.executeQuery(sql);
            //conn.close();

            if(!rs.last())
                return -1;

            // 返回设备的剩余容量
            return rs.getInt("LEFTRS");
            
        }catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateDeviceLftRS(int deviceid, int fragsize, int lftRS){
        // 更新设备的剩余容量

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            String sql;

            int newRS = lftRS + fragsize;

            sql = String.format("UPDATE DFS.DEVICE SET LEFTRS='%d' WHERE ID='%d';", newRS, deviceid);

            stmt.executeUpdate(sql);
            //conn.close();

            return true;

            // 返回文件碎片所在的存储节点
            //return rs.getInt("PATH");
            
        }catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    public DeviceItem[] queryOnlineDevice(){
        Statement stmt = null;
        ResultSet rs = null;
        DeviceItem deviceArray[] = null;

        String ip;
        int port,rst,id,time,leftrs;

        int count,i;

        try{
            stmt = conn.createStatement();
            String sql;

            // 查询在线的设备，按使用空间的降序排列
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ISONLINE=true ORDER BY RS DESC");
            rs = stmt.executeQuery(sql);

            if (!rs.last())
                return null;
            count = rs.getRow(); // 在线的设备总数
            deviceArray=new DeviceItem[count];
            i=0;
            rs.first();

            while (i<count){

                id = rs.getInt("ID");
                ip  = rs.getString("IP");
                port = rs.getInt("PORT");
                rst = rs.getInt("RS");
                time = rs.getInt("TIME");
                leftrs = rs.getInt("LEFTRS");

                deviceArray[i]=new DeviceItem(id,ip,port,true,rst,time,leftrs);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            // 出现异常
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return deviceArray;
    }

    public DeviceItem queryDevice(int id){
        Statement stmt = null;
        ResultSet rs = null;
        DeviceItem deviceItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String ip  = rs.getString("IP");
                int port = rs.getInt("PORT");
                boolean isOnline = rs.getBoolean("ISONLINE");
                int rst = rs.getInt("RS");
                int time = rs.getInt("TIME");
                int leftrs = rs.getInt("LEFTRS");

                deviceItem=new DeviceItem(id,ip,port,isOnline,rst,time,leftrs);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return deviceItem;
    }

    public RequestItem queryRequestById(int id){
        Statement stmt = null;
        ResultSet rs = null;
        RequestItem requestItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int type = rs.getInt("TYPE");
                int fid = rs.getInt("FRAGMENTID");
                int did = rs.getInt("DEVICEID");

                requestItem=new RequestItem(id,type,fid,did);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return requestItem;
    }

    public RequestItem queryFirstRequest(int id){
        Statement stmt = null;
        ResultSet rs = null;
        RequestItem requestItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE DEVICEID='%d' LIMIT 1",id);
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int rid = rs.getInt("ID");
                int type = rs.getInt("TYPE");
                int fid = rs.getInt("FRAGMENTID");

                requestItem=new RequestItem(rid,type,fid,id);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return requestItem;
    }

    public RequestItem[] queryRequest(int deviceId){
        Statement stmt = null;
        ResultSet rs = null;
        RequestItem requsetArray[] = null;

        int id, type, fid, did;

        int count,i;

        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE DEVICEID='%d'",deviceId);
            rs = stmt.executeQuery(sql);

            if (!rs.last())
                return null;
            count = rs.getRow();
            requsetArray=new RequestItem[count];
            i=0;
            rs.first();

            while (i<count) {
                id = rs.getInt("ID");
                type = rs.getInt("TYPE");
                did = rs.getInt("DEVICEID");
                fid = rs.getInt("FRAGMENTID");

                requsetArray[i]=new RequestItem(id,type,fid,did);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return requsetArray;
    }

    public int queryRequestNumbers(int deviceId){
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.REQUEST WHERE DEVICEID='%d'",deviceId);
            rs = stmt.executeQuery(sql);

            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
    }

    public int queryRequestNumbers(int fileId, int type){
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.REQUEST WHERE FRAGMENTID>='%d' "
                    + "AND FRAGMENTID<'%d' AND TYPE='%d'", fileId*100, (fileId+1)*100, type);
            rs = stmt.executeQuery(sql);

            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
    }

    public String queryUserPasswd(String name){
        Statement stmt = null;
        ResultSet rs = null;
        String passwd = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.USER WHERE NAME='%s'",name);
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                passwd = rs.getString("PASSWD");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return passwd;
    }

    public int queryUserID(String name){
        Statement stmt = null;
        ResultSet rs = null;
        int id = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.USER WHERE NAME='%s'",name);
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                id = rs.getInt("ID");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return id;
    }

    public int addFile(FileItem file){
        Statement stmt = null;
        ResultSet rs = null;
        int suc = -1;
        int fileId=-1;
        try{
            stmt = conn.createStatement();
            String sql;
            if (file.isFolder())
                sql = String.format("INSERT INTO DFS.FILE (NAME,PATH,ATTRIBUTE,TIME,NOD,NOA,ISFOLDER,WHOSE,FILETYPE,FILESIZE) "
                                + "VALUES ('%s','%s','%s','%s',%d,%d,true,'%s','%s',%d);",file.getFileName(),file.getPath(),
                        file.getAttribute(),file.getTime(),1,0,file.getWhose(),"",0);
            else // 添加的是文件
                sql = String.format("INSERT INTO DFS.FILE (NAME,PATH,ATTRIBUTE,TIME,NOD,NOA,ISFOLDER,WHOSE,FILETYPE,FILESIZE,ISSHARE,ORIGINID) "
                                + "VALUES ('%s','%s','%s','%s',%d,%d,false,'%s','%s',%d,%d,%d);",file.getFileName(),file.getPath(),
                        file.getAttribute(),file.getTime(),file.getNod(),file.getNoa(),file.getWhose(),file.getFileType(),file.getFileSize(),
                        file.getIsShare(),file.getOriginID());
            System.out.println(sql);
            suc = stmt.executeUpdate(sql);
            if (suc>0){
                rs = stmt.executeQuery("select LAST_INSERT_ID()");
                rs.next();
                fileId=rs.getInt(1);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return fileId;
    }

    /*public int deleteFile(int id){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.FILE WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                return 1;
            else
                return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }*/

    public int alterFile(FileItem file){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            if (file.isFolder())
                sql = String.format("UPDATE DFS.FILE SET NAME='%s',PATH='%s',ATTRIBUTE='%s',"
                                + "TIME='%s',NOA=%d,ISFOLDER=true WHERE id=%d;",file.getFileName(),file.getPath(),
                        file.getAttribute(),file.getTime(),file.getNoa(),file.getId());
            else
                sql = String.format("UPDATE DFS.FILE SET NAME='%s',PATH='%s',ATTRIBUTE='%s',"
                                + "TIME='%s',NOA=%d,ISFOLDER=false WHERE id=%d;",file.getFileName(),file.getPath(),
                        file.getAttribute(),file.getTime(),file.getNoa(),file.getId());
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                return 1;
            else
                return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int alterDevice(DeviceItem device){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            //sql = "SELECT * FROM DFS.FILE WHERE WHOSE='"+whose+"' AND PATH='"+path+"'";
            if (device.isOnline())
                sql = "UPDATE DFS.DEVICE SET IP='"+device.getIp()+"',PORT='"+device.getPort()+
                        "',ISONLINE=true,RS='"+device.getRs()+"',LEFTRS='"+device.getLeftrs()+
                        "'WHERE id='"+device.getId()+"';";
            else
                sql = "UPDATE DFS.DEVICE SET IP='"+device.getIp()+"',PORT='"+device.getPort()+
                        "',ISONLINE=false,RS='"+device.getRs()+"',LEFTRS='"+device.getLeftrs()+
                        "'WHERE id='"+device.getId()+"';";
            /*
            if (device.isOnline())
                sql = String.format("UPDATE DFS.DEVICE SET IP='%s',PORT=%d,ISONLINE=true"
                                + "RS=%d WHERE id=%d;",device.getIp(),device.getPort(),device.getRs(),
                        device.getId());
            else
                sql = String.format("UPDATE DFS.DEVICE SET IP='%s',PORT=%d,ISONLINE=false"
                                + "RS=%d WHERE id=%d;",device.getIp(),device.getPort(),device.getRs(),
                        device.getId());*/
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                return 1;
            else
                return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int addFragment(int id,String path){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("INSERT INTO DFS.FRAGMENT VALUES (%d,'%s');",
                    id,path);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                suc=1;
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    /*public int deleteFragment(int id){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.FRAGMENT WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                suc=1;
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int alterFragment(int id, String path){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("UPDATE DFS.FRAGMENT SET PATH='%s' WHERE id=%d;",
                    path, id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                return 1;
            else
                return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }*/

    public int addRequest(RequestItem request){
        Statement stmt = null;
        ResultSet rs = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("INSERT INTO DFS.REQUEST (TYPE,FRAGMENTID,DEVICEID) "
                            + "VALUES ('%d','%d','%d');",
                    request.getType(), request.getFragmentId(), request.getDeviceId());
            suc = stmt.executeUpdate(sql);
            if (suc>0){
                rs = stmt.executeQuery("select LAST_INSERT_ID()");
                rs.next();
                suc=rs.getInt(1);
            }
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int deleteRequest(int id){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.REQUEST WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                suc=1;
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int addUser(String name, String passwd){
        Statement stmt = null;
        ResultSet rs = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("INSERT INTO DFS.USER (NAME,PASSWD) "
                    + "VALUES ('%s','%s');", name, passwd);
            suc = stmt.executeUpdate(sql);
            if (suc>0){
                rs = stmt.executeQuery("select LAST_INSERT_ID()");
                rs.next();
                suc=rs.getInt(1);
            }
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(rs!=null && !rs.isClosed())
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int alterUser(int id, String name, String passwd){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("UPDATE DFS.USER SET NAME='%s',PASSWD=%s WHERE id=%d;",
                    name, passwd, id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                return 1;
            else
                return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }

    public int deleteUser(int id){
        Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.USER WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
                suc=1;
            else
                suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(stmt!=null && !stmt.isClosed())
                    stmt.close();
            }
            catch(Exception e){
            }
        }
        return suc;
    }
}
