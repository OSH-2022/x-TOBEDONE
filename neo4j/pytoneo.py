import logging
import queue
import string
from unittest import result
from neo4j import GraphDatabase
from neo4j.exceptions import ServiceUnavailable

class App:

    def __init__(self, uri, user, password):
        self.driver = GraphDatabase.driver(uri, auth=(user, password))

    def close(self):
        
        self.driver.close()

    def create_newnode(self, newnode):
        with self.driver.session() as session:
            # Write transactions allow the driver to handle retries and transient errors
            i=0
            # 将含有空格的标签转换成下划线连接
            for labelname in newnode['labels']:
                labelnamelist=labelname.split()
                labelname_='_'.join(labelnamelist)
                newnode['labels'][i]=labelname_
                i=i+1
            # 获取owner给label
            owner = newnode['property']
            owner = owner[owner.find('owner'):]
            owner = owner[owner.find('\'')+1:]#去掉第一个引号
            owner = owner[:owner.find('\'')]
            
            result=session.write_transaction(
                self._create_and_return_newnode, self,newnode)
            session.write_transaction(
                    self._createnamenode, result[0])
            i=1
            # 创建标签（可选） 新建关系
            for labelname in newnode['labels']:
                self.create_labelnode(labelname,owner)
                session.write_transaction(
                    self._create_and_return_relationship, labelname, result[0],owner)
                i=i+1


    def create_labelnode(self, labelname,owner):
        with self.driver.session() as session:
            # Write transactions allow the driver to handle retries and transient errors
            session.write_transaction(
                self._create_and_return_labelnode, labelname,owner)

    def find_label(self, labelname):
        with self.driver.session() as session:
            result = session.read_transaction(self._find_and_return_label, labelname)
            return len(result)

    def delete_node(self,deletenode):
        with self.driver.session() as session:
            session.write_transaction(self._delete_node, deletenode)

    def delete_floder(self,node):
        with self.driver.session() as session:
            session.write_transaction(self._delete_floder, node)
            
    def delete_all(self):
        with self.driver.session() as session:
            session.write_transaction(self._delete_all)
            
    def rename_node(self,node):
        with self.driver.session() as session:
            session.write_transaction(self._rename_node,node)
            
    def share_node(self,node):
        with self.driver.session() as session:
            session.write_transaction(self._share_node,node)
            
    def rename_floder(self,node):
        with self.driver.session() as session:
            session.write_transaction(self._rename_floder,node)
            

    @staticmethod
    def _createnamenode(tx,id):
        # 创建一个与文件名字相同的标签
        # 修改：如果已存在同名文件标签 直接新增一个tag
        # 与节点同名的节点标记owner属性
        query=("MATCH (n) WHERE n.id = "+str(id)+
                " MERGE (m:Label { name: n.name, owner: n.owner}) "+ # create->merge
                " CREATE (n)-[r1:tag]->(m) RETURN m"
        )
        tx.run(query)

    @staticmethod      
    def _create_and_return_relationship(tx, labelname, id,owner):
        query = (
            "MATCH (n:FILE) WHERE n.id="+str(id)+
            " MATCH (m:Label) WHERE m.name=\'"+ labelname+"\' and m.owner = \'"+owner+"\'" 
            "CREATE (n)-[r1:tag]->(m) "
            # "CREATE (m)-[r2:tag"+"]->(n) "
            "RETURN n,m"
        )
        tx.run(query)

    @staticmethod
    def _create_and_return_newnode(tx, graph, newnode):
        labellist=newnode['labels']
        query = "CREATE (p:FILE"
        for labelname in labellist:
            query=query+':'+labelname
        query=query+newnode['property']+") "
        # 增加一个属性id 大小是内置id
        query=query+"SET p.id=id(p) RETURN p.id AS id"
        result = tx.run(query)
        return [record["id"] for record in result]

    @staticmethod
    def _create_and_return_labelnode(tx, labelname,owner):
        query = (
            "MERGE (p1:Label{ name: $labelname , owner: $owner}) "
            "RETURN p1"
        )
        tx.run(query, labelname=labelname,owner=owner)


    @staticmethod
    def _find_and_return_label(tx, labelname):
        query = (
            "MATCH (p:Label) "
            "WHERE p.name=\'"+labelname+"\' RETURN p"
        )
        result = tx.run(query)
        # print(result)
        # print([record["p"] for record in result])
        return [record["p"] for record in result]
        

    @staticmethod
    def _delete_node(tx, deletenode):
        # 从deletenode中分割出 name owner path三个信息
        nodename_split = deletenode['name'].split('.')
        nodename = nodename_split[0]
        nodeext = nodename_split[1]
        path = deletenode['path']
        owner = deletenode['owner']
        query = (
            "MATCH (p:FILE{name:\""+nodename+"\", ext: \""+nodeext+"\", path: \""+path+"\",owner: \""+owner+"\"})" 
            " DETACH DELETE p"
            
        )
        tx.run(query)
        # 记得删除孤立节点
        query = (
            "MATCH (n) where not exists((n)--()) detach delete n"
        )
        tx.run(query)

    @staticmethod
    def _delete_floder(tx,node):
        path = node['path']# 与newpath一起对应重命名的文件夹
        owner = node['owner']
        query = (
            "match (p{owner: \""+owner+"\"}) where p.path starts with \""+path+"\" "
            "return p"
        )
        data = tx.run(query).data()
        for node in data:
            property = node['p']
            #print(property)
            name = property['name']
            ext = property['ext']
            # 下面对应节点的path
            nodepath = property['path']
            query=(
                "match (n{name: \""+name+"\", owner: \""+owner+"\", path:\""+nodepath+"\", ext:\"" +ext +"\"}) "
                "detach delete n"
            ) 
            tx.run(query)
        # 删除孤立标签
        query = (
            "MATCH (n) where not exists((n)--()) detach delete n"
        )
        tx.run(query)
        return
        
    @staticmethod
    def _delete_all(tx):
        tx.run("match (n) detach delete n")
        
    @staticmethod
    def _rename_node(tx,node):
        nodename_split = node['name'].split('.')
        nodename = nodename_split[0]
        nodeext = nodename_split[1]
        path = node['path']
        owner = node['owner']
        newname_split = node['newname'].split('.')
        newname = newname_split[0]
        newext = newname_split[1]
        # 修改标签 有一个与源文件同名的标签得修改
        # 第一步 删除tag
        query = (
            "MATCH (p:FILE{name:\""+nodename+"\", ext: \""+nodeext+"\", path: \""+path+"\",owner: \""+owner+"\"})"
            "-[r]-(n:Label{name:\""+nodename+"\"})"
            " delete r"
        )
        tx.run(query)
        # 记得删除孤立节点
        query = (
            "MATCH (n) where not exists((n)--()) detach delete n"
        )
        tx.run(query)
        # 第三步 修改节点属性
        query = (
            "MATCH (p:FILE{name:\""+nodename+"\", ext: \""+nodeext+"\", path: \""+path+"\",owner: \""+owner+"\"})"
            "set p.name = \""+newname+"\", p.ext = \""+newext+"\""
        )
        tx.run(query)
        # 第四步 新建该文件名的标签 加关系
        query = (
            "MERGE (p1:Label{ name: $labelname , owner: $owner}) "
            "RETURN p1"
        )
        tx.run(query, labelname=newname,owner=owner)
        # 第五步 加关系
        query = (
            "MATCH (p:FILE{name:\""+newname+"\", ext: \""+newext+"\", path: \""+path+"\",owner: \""+owner+"\"})"
            " MATCH (m:Label) WHERE m.name=\'"+ newname +"\' and m.owner = \'"+owner+"\'" 
            " CREATE (p)-[r1:tag]->(m) "
            "RETURN p,m"
        )
        tx.run(query)
        return
        
    @staticmethod
    def _share_node(tx,node):
        nodename_split = node['name'].split('.')
        nodename = nodename_split[0]
        nodeext = nodename_split[1]
        path = node['path']
        owner = node['owner']
        newname_split = node['newname'].split('.')
        newname = newname_split[0]
        newext = newname_split[1]
        new_owner = node["newowner"]
        new_path = node["newpath"]
        
        # TODO:拷贝节点 拷贝label 拷贝关系
        
        
    @staticmethod
    def _rename_floder(tx,node):
        path = node['path']# 与newpath一起对应重命名的文件夹
        owner = node['owner']
        newpath = node['newpath']
        query = (
            "match (p:FILE{owner: \""+owner+"\"}) where p.path starts with \""+path+"\" "
            "return p"
        )
        data = tx.run(query).data()
        # print(data)
        for node in data:
            property = node['p']
            name = property['name']
            ext = property['ext']
            # 下面两个对应节点的前后path
            originpath = property['path']
            finalpath = newpath + originpath[len(path):]
            query=(
                "match (n:FILE{name: \""+name+"\", owner: \""+owner+"\", path:\""+originpath+"\", ext:\"" +ext +"\"}) "
                "set n.path = \""+finalpath+"\""
            )
            tx.run(query)
        return
        
        

if __name__ == "__main__":
    #端口名、用户名、密码根据需要改动
    #create_newnode(node)用于创建结点（包括检测标签、创建标签节点、添加相应的边等功能）
    #delete_node(node.name)用于删去名为node.name的结点
    scheme = "bolt"  # Connecting to Aura, use the "neo4j+s" URI scheme
    host_name = "101.33.236.114"
    port = 7687
    url = "{scheme}://{host_name}:7687".format(scheme=scheme, host_name=host_name, port=port)
    user = "neo4j"
    password = "11"
    #node = {"nodename":"ipadpro.pdf","path":"home/","owner":"zzy","newname":"hi.txt"}
    node = {"newpath":"home/","path":"TIME/","owner":"tanjf"}
    app = App(url, user, password)
    app.rename_floder(node)
    app.close()
    