package userManagement;

import com.opensymphony.xwork2.ActionSupport;

import database.*;

public class GetFileList extends ActionSupport{

	private	static final long serialVersionUID = 1L;
	private  String Path;
	private String Whose;
	//用来返回结果给前端
    private	String	html;
    private String	status;
    
    public	void	setStatus(String status)
    {
    	this.status = status;
    }
    
    public	String	getStatus()
    {
    	return this.status;
    }

    public	void	setPath(String Path)
    {
    	this.Path = Path;
    }
    
    public	String	getPath()
    {
    	return this.Path;
    }

    public  void    setWhose(String Whose) { this.Whose = Whose;}

	public  String  getWhose()  { return this.Whose; }
	
	public String getHtml()
	{
		return this.html;
	}
	
	public void setHtml(String html)
	{
		this.html = html;
	}
	
	@Override  
	public String execute() throws Exception
	{
		
		Query query = new Query();
		System.out.println(Path);
		System.out.println(Whose);
		FileItem[] fileArray= query.queryFileList(Whose, Path);
		//System.out.print(fileArray);
		query.closeConnection();
		
		//更新文件目录  首行始终存在
		html = html +
		"<tr class=\"file_list_back\">"+
			"<td> </td>"+
			"<td> <label><input style=\"height: 20px; width:20px; margin: 0 auto; display: inline; float:left;\" type=\"checkbox\">&emsp;&emsp;</label><span class=\"ion-android-folder\"></span>&emsp;../</td>"+
			"<td></td>"+
			"<td></td>"+
		"</tr>";
		
		//设置查询状态
		if(fileArray==null)
		{
			status = "false";	
			return "success";
		}
		else
			status = "true";
		
		//新增的行
		for(int i=0;i<fileArray.length;i++)
		{
			html = html + 
			"<tr class=\"file_list_go\">"+
				"<td> </td>"+
				(fileArray[i].isFolder()?"<td> <label><input style=\"height: 20px; width:20px; margin: 0 auto; display: inline; float:left;\" type=\"checkbox\"> &emsp;&emsp;</label><i class=\"ion-android-folder\">&emsp;" + fileArray[i].getFileName()+"</i></td>":"<td> <label><input style=\"height: 20px; width:20px; margin: 0 auto; display: inline; float:left;\" type=\"checkbox\">&emsp;&emsp;</label> <i input class=\"glyphicon glyphicon-file\">&emsp;" + fileArray[i].getFileName()+"</i></td>") +
				"<td>"+fileArray[i].getAttribute()+"</td>"+
				"<td>"+fileArray[i].getTime()+"</td>"+
				"<td>"+fileArray[i].getIsShare()+"</td>"+
			"</tr>";			
		}
		return "success";
	}
	

}
