package userManagement;

import com.opensymphony.xwork2.ActionSupport;

import database.*; //数据库


public class UserReg extends ActionSupport{

	private	static final long serialVersionUID = 1L; //UID
	private String userName;	//用户名
	private String userPasswd; 	//密码
	//用来返回结果给前端
    private	String	result;
    
    public	void	setResult(String result)
    {
    	this.result = result;
    }
    
    public	String	getResult()
    {
    	return this.result;
    }
    
	public void setUserName(String name)
	{
		this.userName = name;
	}
	
	public void setUserPasswd(String Passwd)
	{
		this.userPasswd = Passwd;
	}
	
	public String getUserName()
	{
		return this.userName;
	}
	
	public String getUserPasswd()
	{
		return this.userPasswd;
	}
	

	
	@Override  
	public String execute() throws Exception
	{
		
		Query query = new Query();
		int ID = query.addUser(userName, userPasswd); /**新建用户 */
		query.closeConnection();
		if(ID==-1)
			result = "注册失败";
		else
			result = "注册成功";
		return "success";
	}
	

}
