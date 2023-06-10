package database;

public class FileItem {
		
	private int id;
	private String fileName;
	private String path;
	private String attribute;
	private String time;
	private int nod;
	private int noa;
	private boolean isFolder;
	private String fileType;
	private int fileSize;
	private String whose;
	private int isShare;
	private int originID;
	
	FileItem(int id, String fileName, String path, String attribute, String time, int nod, int noa, 
	boolean isFolder, String fileType, int fileSize,String whose, int isShare, int originID) {
		this.id=id;
		this.fileName = fileName;
		this.path=path;
		this.attribute=attribute;
		this.time=time;
		this.nod=nod;
		this.noa=noa;
		this.isFolder=isFolder;
		this.fileType=fileType;
		this.fileSize=fileSize;
		this.whose=whose;
		this.isShare=isShare;
		this.originID=originID;
	}

	public FileItem(String fileName, String path, String attribute, String time, int nod, int noa, 
	boolean isFolder, String fileType, int fileSize,String whose,int isShare, int originID ){
		this.fileName = fileName;
		this.path=path;
		this.attribute=attribute;
		this.time=time;
		this.nod=nod;
		this.noa=noa;
		this.isFolder=isFolder;
		this.fileType=fileType;
		this.fileSize=fileSize;
		this.whose=whose;
		this.isShare=isShare;
		this.originID=originID;
	}

	public int getIsShare(){
		return this.isShare;
	}

	public void setIsShare(int isShare){
		this.isShare = isShare;
	}

	public int getOriginID(){
		return this.originID;
	}

	public void setOriginID(int originID){
		this.originID = originID;
	}


	public int getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getNod() {
		return nod;
	}

	public int getNoa() {
		return noa;
	}

	public void setNoa(int noa) {
		this.noa = noa;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getFileType() {
		return fileType;
	}
	public int getFileSize() {
		return fileSize;
	}

	public void setWhose(String whose){this.whose=whose;}
	public String getWhose(){return  whose;}
}
