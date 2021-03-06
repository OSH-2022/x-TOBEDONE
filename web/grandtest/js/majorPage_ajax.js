/*
function createAndDownloadFile(fileName, fileType, content) {
	var aTag = document.createElement('a');
	var blob = new Blob([content], { type: fileType, name: fileName });
	aTag.download = fileName;
	aTag.href = URL.createObjectURL(blob);
	aTag.click();
	URL.revokeObjectURL(blob);
}*/
/*
function waitForSocketConnection(socket, callback){
	setTimeout(
		function () {
			if (socket.readyState === 1) {
				console.log("Connection is made")
				if (callback != null){
					callback();
				}
			} else {
				console.log("wait for connection...")
				waitForSocketConnection(socket, callback);
			}

		}, 10); // wait 5 milisecond for the connection...
}
function syncSleep(time) {
	const start = new Date().getTime();
	while (new Date().getTime() - start < time) {}
}*/
function WebSocketDownload(ip,port,fragmentName,content,digest,fragmentId)
{
	var ret_bytes;
	var ret_digest;
	if ("WebSocket" in window)
	{
		let ws = new WebSocket("ws://"+ip+":"+port);
		ws.binaryType="arraybuffer";
		ws.onopen = function()
		{
				//alert("Sending Message...");
				ws.send("D");
				ws.send(fragmentName);
				console.log('send filename');
		};

		ws.onmessage = function (evt)
		{
			let received_data = evt.data;
			//alert("received");
			//if(evt.data instanceof Blob ){
			if(evt.data instanceof ArrayBuffer ){
				//alert("Received arraybuffer");
				console.log('Blob');
				ret_bytes= received_data;
				console.log('recv bytes');
			}
			if(typeof(evt.data) =='string') {
				//alert("Received data string");
				console.log('string');
				ret_digest= received_data;
				console.log('recv digest');
			}
		};

		ws.onclose = function()
		{
			// sí websocket
			//alert("Connection Closed...");
			content[fragmentId]=ret_bytes;
			digest[fragmentId]=ret_digest;
			console.log('closed connection');
		};
	}
	else
	{
	}
	/*
	alert("Start...");
	syncSleep(2000);
	//alert("Finish...");
	console.log(ret_bytes);
	*/
	return ret_bytes;
}
/*
function decodeFile(fileName,fileType,nod,noa,content,digest,fileSize)
{
	console.log(fileName);
	console.log(fileType);
	console.log((fileSize));
	console.log(nod);
	console.log(noa);

	for(var i=0;i<noa+nod;i++){
		console.log(content[i]);
		console.log(digest[i]);
	}
}*/
function decodeFile(fileName, fileType, numOfDivision, numOfAppend, content, digest, fileSize) {
	//clean wrong parts
	var errors = 0;
	/*
	for (var i = 0; i < content.length; i++) {
		if (digest[i] != objectHash.MD5(content[i])) {
			errors += 1;
			content[i] = new Uint8Array(content[i].length);
		}
	}*/

	//console.log(content);
	const t5 = Date.now();//Decode timing start

	var contentView=new Array(content.length);
	for(var i=0;i<content.length;i++){
		contentView[i]=new Uint8Array(content[i]);
	}
	//var decoded = erasure.recombine(contentView, fileSize, numOfDivision, numOfAppend);
	var decoded = callDecoder(contentView, numOfDivision, numOfAppend);
	//console.log(decoded);
	if (decoded.length > fileSize)
		decoded = decoded.subarray(0, fileSize);
	const t6 = Date.now();//Decode timing end

	// after decoded, download the file and show info(time, errors)
	createAndDownloadFile(fileName, fileType, decoded);

	if (document.getElementById("decode") != null)
		document.getElementById("decode").innerHTML += "Decode with " + errors + " errors succeeded in " + (t6 - t5) + "mS</br>";
	console.log("Erasure decode took " + (t6 - t5) + " mS");
	return Promise.resolve(true);
}
function WebSocketUpload(ip,port,fragmentName,fragmentContent,digest)
{
	if ("WebSocket" in window)
	{
		var ws = new WebSocket("ws://"+ip+":"+port);

		ws.onopen = function()
		{
			ws.send("U");
			ws.send(fragmentName);
			console.log(fragmentName);
			ws.send(digest);
			console.log(digest);
			ws.send(fragmentContent);
			console.log(fragmentContent);
			//ws.close();
		};

		ws.onmessage = function (evt)
		{
			let respondMsg = evt.data;
			console.log(respondMsg);//success or not
		};

		ws.onclose = function()
		{
			console.log("upload closed");
		};
	}
	else
	{
		alert("");//TODO
	}
}
function encodeFile(selectedFile) {
	/* After file selected, get info(name, type, size) as global,
     * and read filestream As ArrayBuffer
     * use FileReader, seemingly usable for Chrome & Firefox
     * turn to upLoader()
     * handleFileSelect(this) -> upLoader
     * */
	// sendFragments((str)fileName,(str)fileType,(int)numOfDivision,(int)numOfAppend,(byte[][])content(content),(string[])digest,(int)fileSize);

	let numOfDivision = 5;
	let numOfAppend = 2;
	var fileType = [];
	var fileName = [];
	var fileSize;
	// TODO temp fix
	var content = [];
	var digest = [];
	/*
     * user choose a file, and trigger handleFileSelect -> upLoader(this)
     * upLoader get the file in *.result as raw, then create a worker to do encoding
     * evt : from                  ¨L not this evt
     *  function handleFileSelect(evt) {
            ...
            var reader = new FileReader();
            ...                 ¨L Maybe this evt(I'm not sure)
            reader.onload = upLoader;
        }
     * */
	function upLoader(evt) {
		/*
		if (document.getElementById("tips") != null)
			document.getElementById("tips").innerHTML = "<h3>Please wait during erasure code profiling...</h3></br>";
		/*receive file*/

		var fileString = evt.target.result;
		/*
		if (document.getElementById("info") != null)
			document.getElementById("info").innerHTML = "loaded as Uint8Array...</br>";
		if (document.getElementById("encode") != null)
			document.getElementById("encode").innerHTML = "";
		if (document.getElementById("decode") != null)
			document.getElementById("decode").innerHTML = "";*/
		let raw = new Uint8Array(fileString);
		/*if (document.getElementById("info") != null)
			document.getElementById("info").innerHTML +=
				"<h3>file name</h3> " + fileName
				+ "</br><h3>file type</h3> " + fileType
				+ "</br><h3>file size</h3> " + fileSize / 1024 + " KB"
				+ "</br>Division " + numOfDivision
				+ " Append " + numOfAppend
				+ "</br></br>";
*/
		// create a worker to do the erasure coding
		/*
		var blob = new Blob(["onmessage = function(e) { postMessage(e.data); }"]);
		// Obtain a blob URL reference to our worker 'file'.
		var blobURL = window.URL.createObjectURL(blob);
		var worker = new Worker(blobURL);
		worker.onmessage = function (e) {
			alert("waiting for worker");
			console.log(e.data);*/
			/*fileEncoder*/
			const t1 = Date.now();//Encode timing start
			//TODO
			//content = erasure.split(raw, numOfDivision, numOfAppend);
			content = callEncoder(raw,numOfDivision,numOfAppend);
			const t2 = Date.now();//Encode timing end
			console.log("Erasure encode took " + (t2 - t1) + " mS");
			//if (document.getElementById("encode") != null)
			//	document.getElementById("encode").innerHTML += "Encode took " + (t2 - t1) + "mS to generate " + content.length + " fragments</br>";
			//TODO
			//var digest = new Array();
			const t3 = Date.now();//Hash timing start
			for (var i = 0; i < content.length; i++) {
				digest[i] = objectHash.MD5(content[i]);
			}
			const t4 = Date.now();//Hash timing end
			//if (document.getElementById("encode") != null)
			//	document.getElementById("encode").innerHTML += "Hash took " + (t4 - t3) + "mS to generate " + content.length + " digests</br>";
			/* Next we can use sendFragments() to send the results to the backend,
             * hopefully content[][] remain as 2d array
             * */
			// Here we use decodeFile to test if encode and decode both work properlly.
			//decodeFile(fileName, fileType, numOfDivision, numOfAppend, content, digest, fileSize);
			console.log("Success");

			console.log(content);
			encodeCallBack({
				fileName: fileName,
				fileType: fileType,
				numOfDivision: numOfDivision,
				numOfAppend: numOfAppend,
				content: content,
				digest: digest,
				fileSize: fileSize
			},selectedFile)
		//};
		//console.log(raw);
		//worker.postMessage({ input: raw });
	}
	if (selectedFile) {
		//console.log(selectedFile);
		fileType = selectedFile.type;
		fileName = selectedFile.name;
		fileSize = selectedFile.size;
		var reader = new FileReader();
		//reader.readAsBinaryString(files[0]);
		reader.onload = upLoader;
		reader.readAsArrayBuffer(selectedFile);
		//alert("reading");
	}
}

function encodeCallBack(fileInfo, selectedFile){
	var uploadForm = new FormData();
	var deviceArray;
	var fileId;
	var path = "/";
	if(curr_path_array.length>1)
		path="";
	for(var i=1;i<curr_path_array.length;i++)
		path = path + curr_path_array[i] + "/" ;
	uploadForm.append("path", path);
	uploadForm.append("fileName", fileInfo.fileName);
	uploadForm.append("fileType", fileInfo.fileType);
	uploadForm.append("nod", fileInfo.numOfDivision);
	uploadForm.append("noa", fileInfo.numOfAppend);
	uploadForm.append("fileSize", fileInfo.fileSize);
	uploadForm.append("whose", $.cookie("username"));
	$.ajax({
		url: "FileUploader!uploadRegister.action",
		type: "POST",
		data: uploadForm,
		dataType: "text",
		processData: false,
		contentType: false,
		async: false,								//此处采用同步查询进度
		success: function (databack) {
			var retFileInfo = $.parseJSON(databack);
			let result = retFileInfo.result;
			deviceArray = retFileInfo.devices.forms;
			fileId=retFileInfo.fileId;
			console.log(result);
			//alert(result);
		}
	});

	//alert("Before upload");
	/*
	console.log(typeof(deviceArray[0].ip));
	console.log(deviceArray[0].ip);
	console.log(typeof(deviceArray[0].port));
	console.log(deviceArray[0].port);
	console.log(fileInfo.content[0]);
	console.log(fileInfo.digest[0]);
	*/
	for (var i = 0; i < deviceArray.length; i++) {
		WebSocketUpload(deviceArray[i].ip, deviceArray[i].port, (fileId * 100 + i).toString(), fileInfo.content[i], fileInfo.digest[i]);
	    
	}

	console.log("finish upload to storage, begin to upload to ray");

	// 修改 ip 和 port，这里对应的是 ray 的地址
	ip = "124.220.19.232";
	port = "9998";

	var content_x ="";
	var digest_x = "";

	function upLoader(evt) {
		var fileString = evt.target.result;
		content_x = new Uint8Array(fileString);
		digest_x = objectHash.MD5(content_x);

		WebSocketUpload(ip, port, selectedFile.name, content_x, digest_x);
	}

	var reader = new FileReader();
	//reader.readAsBinaryString(files[0]);
	reader.onload = upLoader;
	reader.readAsArrayBuffer(selectedFile);
	
	console.log("finish upload to ray");
}
function fileUpload() {
	console.log("begin to fileUpload");
	let selectedFile = document.getElementById('files').files[0];//TODO multisel file
	console.log("begin to encode");
	encodeFile(selectedFile);
	console.log("***************************************************");
	console.log(selectedFile);
	console.log("***************************************************");
	// console.log("begin to download to ray");
	// fileDownloadtoRay(selectedFile);
	// console.log("finish downloading to ray");
	
} 

function fileDownload() {
	var path;
	var name;
	var item=$("#file_list_body").children();
	item = item.next();
	while(item.length!=0)
	{
		name = "";
		path = "";
		//如果ｉｔｅｍ不为空，则进行处理
		var children=item.children();
		if( (children[1].children[1].className=="glyphicon glyphicon-file") && (children[1].children[0].children[0].checked) )
		{
			//文件路径
			path = path + "/";
			/*********/					if(curr_path_array.length>1)
			path="";
			for(var i=1;i<curr_path_array.length;i++)
				path = path + curr_path_array[i] + "/" ;
			//文件名
			name = name + $.trim(children[1].innerText);
			//alert(path + "  " + name);


			/*
             *
             * 此处应当利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
             *
             * */
			//利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
			var result;
			var	form=new FormData();
			var deviceArray;
			var fileInfo;
			form.append("path",path);
			form.append("name",name);
			$.ajax({
				url:"FileDownloader!downloadRegister.action",
				type:"POST",
				data:form,
				dataType:"text",
				processData:false,
				contentType:false,
				async: false,								//此处采用同步查询进度
				success:function(databack){
					fileInfo = $.parseJSON(databack);
					//alert(result);
				}
			});
			result = fileInfo.result;
			deviceArray = fileInfo.devices.forms;
			console.log(result);

			//错误处理
			if(result=="NotEnoughFragments")
			{
				$("#statusFeedback").text("在线碎片数目不足！");
				return;
			}
			else if(result == "Error")
			{
				$("#statusFeedback").text("服务器响应该请求内部出错！");
				return;
			}
			var content= new Array(fileInfo.noa+fileInfo.nod);
			var digest= new Array(fileInfo.noa+fileInfo.nod);
			for(var i=0;i<deviceArray.length;i++)
			{
				console.log(deviceArray[i]);
				let received_bytes=WebSocketDownload(deviceArray[i].ip,deviceArray[i].port,deviceArray[i].filename,content,digest,deviceArray[i].fragmentId);
				//console.log(received_bytes);
				console.log('Back');
				//console.log(content[deviceArray[i].fragmentId];
				//createAndDownloadFile(deviceArray[i].filename, 'jpg', received_bytes)
			}
			let downloadTimeoutId =setTimeout(function(){
				decodeFile(fileInfo.name,fileInfo.fileType,fileInfo.nod,fileInfo.noa,content,digest,fileInfo.fileSize);
			}, 10000)

            //添加进度条
			/*
            var ratio1 = 0;
            var progress_bar='<div class="progress progress-striped active"><div class="progress-bar progress-bar-success" role=\"progressbar" style="width: '
                +ratio1+'%;">'
                +path+name+'</div></div>';
            $("#download_progress_area").append(progress_bar);

			 */
		}
		//
		item = item.next();
	}
}

function fileDelete() {
	var path;
	var name;
	var item=$("#file_list_body").children();
	item = item.next();
	while(item.length!=0)
	{
		name = "";
		path = "";
		//如果ｉｔｅｍ不为空，则进行处理
		var children=item.children();
		if( children[1].children[0].children[0].checked ){
			//文件路径
			path = path + "/";
			/*********/					if(curr_path_array.length>1)
			path="";
			for(var i=1;i<curr_path_array.length;i++)
				path = path + curr_path_array[i] + "/" ;
			//文件名
			name = name + $.trim(children[1].innerText);
			//alert(path + "  " + name);


			/*
             *
             * 此处应当利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
             *
             * */
			//利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
			var result;
			var	form=new FormData();
			var deviceArray;
			var deleteResult;
			
			var isfolder = 0;
			if(children[1].children[1].className=="glyphicon glyphicon-file")
				isfolder = 0;
			else
				isfolder = 1;
			
			var whose = $.cookie("username");
			console.log(whose + " " + path + " " + name);
			form.append("path", path);
			form.append("name", name);
			form.append("isfolder", isfolder);
			form.append("whose", whose);

			let ws2 = new WebSocket("ws://101.33.236.114:9090"); //创建WebSocket连接
			
			if(isfolder == 0) //网页端的删除文件行为需要同步到图数据库上
				ws2.onopen = function()
				{
					ws2.send(whose + "_web");
					console.log("('delete', {'name': '" + name + "', 'path': '" + path + "', 'owner': '" + whose + "'})");
					ws2.send("('delete', {'name': '" + name + "', 'path': '" + path + "', 'owner': '" + whose + "'})");
				}
			else //删目录
			{
				var dirpath = path + name + "/";
				if(path == "/"){
					dirpath = name + "/";
				}

				ws2.onopen = function()
				{
					ws2.send(whose + "_web");
					console.log("('delfolder', {'path': '" + dirpath + "', 'owner': '" + whose + "'})");
					ws2.send("('delfolder', {'path': '" + dirpath + "', 'owner': '" + whose + "'})");
				}
			}
				
			$.ajax({
				url:"FileDownloader!deleteRegister.action",
				type:"POST",
				data:form,
				dataType:"text",
				processData:false,
				contentType:false,
				async: false,								//此处采用同步查询进度
				success:function(databack){
					deleteResult = $.parseJSON(databack);
					//alert(result);
				}
			});
			
			console.log("Delete " + deleteResult.result);
		}
		//
		item = item.next();
	}
}


function dialog_display(){
	document.getElementById("rename_dialog").style.display = "block";
	document.getElementById("rename_dialog").style.width = "200px";
	document.getElementById("rename_dialog").style.height = "100px";
	document.getElementById("rename_dialog").style.border = "1px solid #00f";
}

function add_dir(){
	var dir_name = $("#dir_name").val();
	console.log("new dir " + dir_name + " added.");
	var form = new FormData();
	var path = "/";
	if(curr_path_array.length > 1)
		path = "";
	for(var i=1;i<curr_path_array.length;i++)
		path = path + curr_path_array[i] + "/" ;
	// 获取文件夹的路径


	var whose = $.cookie("username");
	console.log("Path: " + path);
	console.log("Whose: "+ whose);

	form.append("name", dir_name); //新建的目录名称
	form.append("path", path);  //新建目录的位置
	form.append("whose", whose);  //新建目录所属的用户

	var Result;
	$.ajax({
		url: "FileDownloader!adddirRegister.action",
		type: "POST",
		data: form,
		dataType: "text",
		processData: false,
		contentType: false,
		async: false,
		success: function(databack){
			Result = $.parseJSON(databack);
		}
	});
	console.log("Add dir " + Result.result);
}

function Check_fileRename() {
	var path;
	var name;
	var item=$("#file_list_body").children();
	var flag = false;
	item = item.next();
	while(item.length!=0)
	{
		name = "";
		path = "";
		//如果ｉｔｅｍ不为空，则进行处理
		var children=item.children();
		if(children[1].children[0].children[0].checked)
		{
			//文件路径
			path = path + "/";
			/*********/					
			if(curr_path_array.length>1)
				path="";
			for(var i=1;i<curr_path_array.length;i++)
				path = path + curr_path_array[i] + "/" ;
			//文件名
			name = name + $.trim(children[1].innerText);
			flag = true;
			console.log(path+"-"+name);
			break;
		}
		//
		item = item.next();
	}
	if(flag){  //表明有选中文件
		console.log("Rename action detected.");
		dialog_display();
	}
}

function fileRename() {
	var new_name = $("#new_name").val();
	console.log("the new file name is " + new_name);
	var path;
	var name;
	var item=$("#file_list_body").children();
	item = item.next();
	while(item.length!=0)
	{
		name = "";
		path = "";
		//如果ｉｔｅｍ不为空，则进行处理
		var children=item.children();
		if( children[1].children[0].children[0].checked )
		{
			//文件路径
			path = path + "/";
			/*********/					
			if(curr_path_array.length>1)
				path="";
			for(var i=1;i<curr_path_array.length;i++)
				path = path + curr_path_array[i] + "/" ;
			//文件名
			name = name + $.trim(children[1].innerText);

			var isfolder;
			if(children[1].children[1].className=="glyphicon glyphicon-file")
				isfolder = 0;
			else
				isfolder = 1;
			
			var renameResult;
			var form = new FormData();
			form.append("path", path);
			form.append("name", name);
			form.append("newname", new_name);
			form.append("isfolder", isfolder);
			form.append("whose", $.cookie("username"));

			// 网页端的重命名行为需要同步到图数据库上
			let ws2 = new WebSocket("ws://101.33.236.114:9090"); //创建WebSocket连接
			
			if(isfolder == 0)
			{ //重命名文件
				ws2.onopen = function()
				{
					ws2.send($.cookie("username")+"_web");
					ws2.send("('rename', {'name': '" + name + "', 'path': '" + path + "', 'owner': '" + $.cookie("username") + "', 'newname': '" + new_name + "'})");
					console.log("('rename', {'name': '" + name + "', 'path': '" + path + "', 'owner': '" + $.cookie("username") + "', 'newname': '" + new_name + "'})");
				}
			}
			else
			{   //重命名目录
				var dirpath = path + name + "/";
				if(path == "/"){ //根目录
					dirpath = name + "/";
				}
				
				var newdirpath = path + new_name + "/";
				if(path == "/"){
					newdirpath = new_name + "/";
				}
				
				ws2.onopen = function()
				{
					ws2.send($.cookie("username")+"_web");
					ws2.send("('refolder', {'newpath': '" + newdirpath + "', 'path': '" + dirpath + "', 'owner': '" + $.cookie("username") + "'})");
					console.log("('refolder', {'newpath': '" + newdirpath + "', 'path': '" + dirpath + "', 'owner': '" + $.cookie("username") + "'})");
				}
			}

			console.log(path + " " + name + " " + new_name);

			var dirpath = path + name + "/";
			if(path=="/")
				dirpath = name + "/";
			console.log(dirpath);

			$.ajax({
				url:"FileDownloader!renameRegister.action",
				type:"POST",
				data:form,
				dataType:"text",
				processData:false,
				contentType:false,
				async:false,
				success:function(databack){
					renameResult = $.parseJSON(databack);
				}
			});

			console.log("Rename " + renameResult.result);
			break;
		}
		//
		item = item.next();
	}
}

$(document).ready(function(){
	curr_path_array = [];
	curr_path_array[0] = "/";
	curr_path_html = "<li>当前路径:ROOT</li>";
	//ws2 = new WebSocket("ws://101.33.236.114:9090");
	
	//面包屑式访问路径显示  初始化
	$("#curr_path").html(curr_path_html);
	
	// 文件下载
	// 绑定点击事件
	$("#button_download").click(function(){
		fileDownload();
	});

	$("#button_upload").click(function() {
		$("#files").click();
		//location.reload();
	});

	$("#button_delete").click(function(){
		fileDelete();
		//location.reload();
	})

	$("#button_rename").click(function(){
		console.log("Start renaming");
		Check_fileRename();
	})

	$("#button_confirm").click(function(){
		document.getElementById("rename_dialog").style.display="none";
		fileRename();
		//location.reload();
	});

	$(".close").click(function(){
		document.getElementById("rename_dialog").style.display="none";
	})

	$(".dirclose").click(function(){
		document.getElementById("dirname_dialog").style.display="none";
	})

	$("#button_adddir").click(function(){
		document.getElementById("dirname_dialog").style.display="block";
		document.getElementById("dirname_dialog").style.width = "200px";
		document.getElementById("dirname_dialog").style.height = "100px";
		document.getElementById("dirname_dialog").style.border = "1px solid #00f";
	})

	$("#button_confirm2").click(function(){
		add_dir();
		document.getElementById("dirname_dialog").style.display="none";
		location.reload();
	})
	/*
		<tr id="file_list_first">
		<td> </td>
 		<td> <label><input type="checkbox">&emsp;&emsp;</label><span class="glyphicon glyphicon-folder-open"></span>&emsp;../</td>
 		<td></td>
 		<td></td>
		</tr>

*/
	
	
	//点击文件目录进入其子目录　　刷新文件目录列表
	$("#file_list_body").on("click","i.glyphicon-folder-open",
			function(){
			//如果是文件而不是文件夹，点击不刷新目录，提示信息
			/*if(this.children[1].children[1].className=="glyphicon glyphicon-file")
			{
				$("#statusFeedback").text("您所点击的是文件而不是文件夹，无法进入该目录！");
				return;
			}
			if(this.children[1].children[1].className="glyphicon glyphicon-folder-open" && this.children[1].children[0].children[0].checked)
				return;*/
			//更新路径显示
			console.log("click deteced");
			curr_path_array = curr_path_array.concat( $.trim(this.innerText) ); //此处用$.trim去除空格
			console.log($.trim(this.innerText));
			curr_path_html = "<li>ROOT</li>";
			for(var i=1;i<curr_path_array.length;i++)
			curr_path_html = curr_path_html + "<li>" + curr_path_array[i] + "</li>";
			$("#curr_path").html(curr_path_html);		
			//ajax
			var QueryPath="/";
			if(curr_path_array.length>1)
				QueryPath="";
			for(var i=1;i<curr_path_array.length;i++)
			{
				QueryPath = QueryPath + curr_path_array[i] + "/" ;
			}
			var	form=new FormData();

			var whose = $.cookie("username");
			form.append("Whose", whose);
			form.append("Path",QueryPath);
			//console.log(form.get("whose"));
			//alert(queryPath);
			$.ajax({
					url:"GetFileList.action",
					type:"POST",
					data:form,
					dataType:"text",
					processData:false,
					contentType:false,
					success:function(databack){
						var obj = $.parseJSON(databack);
						var new_file_list = obj.html;
						//alert(new_file_list);
						$("#file_list_body").html(new_file_list);
					}
			});
			$("#statusFeedback").text("成功进入该目录！");
		}
	);
	
	//点击的是返回上一层的文件项
	$("#file_list_body").on("click","tr.file_list_back",
			function()
			{
				//如果是顶层目录，点击上级目录无操作，提示信息
				if(curr_path_array.length==1)
				{
					$("#statusFeedback").text("已经是根目录了，无法返回上一层！");
					return; 
				}
				//更新路径显示
				curr_path_array.pop();
				curr_path_html = "<li>ROOT</li>";
				for(var i=1;i<curr_path_array.length;i++)
				curr_path_html = curr_path_html + "<li>" + curr_path_array[i] + "</li>";
				$("#curr_path").html(curr_path_html);	
				
				//ajax
				var QueryPath="/";
/*********/		if(curr_path_array.length>1)
					QueryPath="";
				for(var i=1;i<curr_path_array.length;i++)
				{
					QueryPath = QueryPath + curr_path_array[i] + "/" ;
				}
				var	form=new FormData();
				form.append("Path",QueryPath);
				var whose = $.cookie("username");
				form.append("Whose", whose);
				
				//alert(queryPath);
				$.ajax({
						url:"GetFileList.action",
						type:"POST",
						data:form,
						dataType:"text",
						processData:false,
						contentType:false,
						success:function(databack){
							var obj = $.parseJSON(databack);
							var new_file_list = obj.html;
							//alert(new_file_list);
							$("#file_list_body").html(new_file_list);
						}
				});
				$("#statusFeedback").text("成功返回上层目录！");
			}
	);

	
	//定时刷新预下载进度
	function refresh_progress(){
		var progressArray = $("#download_progress_area").children();
		var str="";
		var ratio=100;
		for(var i=0;i<progressArray.length;i++)
		if(progressArray[i].className=="progress progress-striped active")
		{
			//alert("here length="+progressArray.length + "i="+i);
			var path="";
			var name="";
			var strArray;
			strArray = progressArray[i].innerText.split('/');
			for(var j=0;j<strArray.length-1;j++)
				path = path + strArray[j] + "/";
			name = strArray[strArray.length-1];
			//str = str + path + name + "    ";
			//alert(name+" "+path)
			/*
			 * 
			 * 此处应远程调用　public static int progressCheck(String path, String name)　　返回进度
			 * 
			 * */
			/*
			var	form=new FormData();
			form.append("path",path);
			form.append("name",name);
			$.ajax({
					url:"FileDownloader!progressCheck.action",
					type:"POST",
					data:form,
					dataType:"text",
					processData:false,
					contentType:false,
					async: false,								//此处采用同步查询进度
					success:function(databack){
						var obj = $.parseJSON(databack);
						var result = obj.result;
						if(result == "Error")
						{
							ratio = 0;
							$("#statusFeedback").text("查询进度出错！");
						}
						else
							ratio = parseInt(result);

					}
			});*/

			var countFrag=0;
			for(var j=0;j<digest.length;++j){
				if(digest[j]!==undefined)
					countFrag++;
			}
			if(countFrag<nod)
				ratio=100*countFrag/nod;
			else
				ratio=100;

			//////////////////////////////////////////////////////////////////
			//进度条的ｈｔｍｌ代码
			var progress_bar='<div class="progress progress-striped active"><div class="progress-bar progress-bar-success" role=\"progressbar" style="width: '
				+ratio+'%;">'
				+path+name+'</div></div>';
			//如果预下载完成
			if(ratio==100)
			{
				/*
				 // 此处应当调用远程函数　　public static int decodeFile(String path, String name)
				var	form=new FormData();
				form.append("path",path);
				form.append("name",name);
				$.ajax({
						url:"FileDownloader!decodeFile.action",
						type:"POST",
						data:form,
						dataType:"text",
						processData:false,
						contentType:false,
						async: false,								//此处采用同步查询进度
						success:function(databack){
							var obj = $.parseJSON(databack);
							var result = obj.result;
							if(result == "Error")
								$("#statusFeedback").text("解码拼接出错！");
							else
								$("#statusFeedback").text("解码拼接文件成功！");

						}


				});*/


				var clickToGetFile = '<a href="#" download="' + name + '">' + progress_bar + '</a>';
				//alert(temp);
				progressArray[i].outerHTML = clickToGetFile;
				
			}
			else
			{
				//修改进度条进度
				//alert(progress_bar);
				progressArray[i].outerHTML = progress_bar;				
			}
			///////////////////////////////////////////////////////
		}
		
	}
	//设置进度刷新间隔
	window.setInterval(function(){refresh_progress();},3000);

	
	
	
	//自动删除下载过的文件链接和进度条
	$("#download_progress_area").on("click","a",
			function()
			{
				this.outerHTML = "";
			}

	);
	
	
	
//总的结束符	
});

/*
   			<tr id="file_list_first">
      			<td> </td>
         		<td> <label><input type="checkbox">&emsp;&emsp;</label><span class="glyphicon glyphicon-folder-open"></span>&emsp;../</td>
         		<td></td>
         		<td></td>
      		</tr>
 
 */
