<!-- 这是拼凑的测试html -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!-- 导入database 支持的包 -->
<%@ page import="database.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">

<head>
    <!-- meta message -->
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
    <meta charset="utf-8" />
    <meta http-equiv="Content-Type" content="text/html" />
    <!-- Load Fonts -->
    <link
        href="https://fonts.googleapis.com/css?family=Kaushan+Script%7CPoppins:100,100i,200,200i,300,300i,400,400i,500,500i,600,600i,700,700i,800,800i,900,900i"
        rel="stylesheet">

    <!--bootstrap和JQerry相关库-->
    <!-- jQuery Scripts -->    
    <script src="../js/jquery/jquery.js"></script>
    <script src="../js/jquery/jquery.cookie.js"></script>
    <script src="../js/bootstrap-3.3.7/js/bootstrap.min.js"></script>


    <!--AJAX相关js动作-->
    <script src="../js/ec/object_hash.js" type="text/javascript"></script>
    <script src="../js/ec/erasure.js"></script>
    <script src="../js/ec/funcs.js"></script>
    <script src="../js/majorPage_ajax.js"></script>
    <script src="https://www.layuicdn.com/layui/layui.js"></script>


    <!-- CSS -->
    <link rel="stylesheet" href="css/basic.css" />
    <link rel="stylesheet" href="css/layout.css" />
    <link rel="stylesheet" href="css/blogs.css" />
    <link rel="stylesheet" href="css/ionicons.css" />
    <link rel="stylesheet" href="css/magnific-popup.css" />
    <link rel="stylesheet" href="css/animate.css" />
    <!-- x-dontpanic 按钮样式 -->
    <link rel="stylesheet" type="text/css" href="https://www.layuicdn.com/layui/css/layui.css" />
    <link rel="stylesheet" type="text/css" href="../js/bootstrap-3.3.7/css/bootstrap.min.css">

    <!-- <style type="text/css">
        .img-width {
            MARGIN: 0;
            WIDTH: 100%;
        }

        .img-width img {
            MAX-WIDTH: 100%;
            HEIGHT: 100%;
            width: 100%;
        }
    </style> -->

    <!-- Favicon -->
    <link rel="shortcut icon" href="images/favicons/favicon.ico">
    <title>GraND Pro</title>
</head>

<body>
    <!-- Page -->
    <div class="page">

        <!-- Preloader -->
        <div class="preloader">
            <div class="centrize full-width">
                <div class="vertical-center">
                    <div class="spinner">
                        <div class="double-bounce1"></div>
                        <div class="double-bounce2"></div>
                    </div>
                </div>
            </div>
        </div>

        <header class="header">
            <div class="fw">
                <div class="logo">
                    <a href="https://github.com/OSH-2022/x-TOBEDONE">x-TOBEDONE</a>
                </div>
                <a href="#" class="menu-btn"><span></span></a>
                <div class="top-menu">
                    <ul>
                        <li><a href="#homeimage">Home</a></li>
                        <li><a href="#fileCatalogTable">GraND Pro</a></li>
                        <li><a href="#wrapper">neo4j</a></li>
                    </ul>
                    <a href="#" class="close"></a>
                </div>
            </div>
        </header>

        <!-- Container -->
        <div class="container">
            <!-- Section Started -->
            <div class="section started" id="homeimage">
                <div class="slide" style="background-image: url(images/background.jpeg);"></div>
                <div class="centrize full-width">
                    <div class="vertical-center">
                        <div class="st-title align-center">
                            <div class="typing-title">
                                <p>Prometheus</p>
                                <p>Ray</p>
                                <p>Neo4j + MySQL</p>
                            </div>
                            <span class="typed-title"></span>
                        </div>
                        <div class="socials">
                            <a target="blank" href="https://www.facebook.com"><i
                                    class="icon ion ion-social-facebook"></i></a>
                            <a target="blank" href="https://github.com"><i class="icon ion ion-social-github"></i></a>
                            <a target="blank" href="https://twitter.com"><i class="icon ion ion-social-twitter"></i></a>
                            <a target="blank" href="https://www.youtube.com"><i
                                    class="icon ion ion-social-youtube"></i></a>
                            <a target="blank" href="https://plus.google.com"><i
                                    class="icon ion ion-social-googleplus"></i></a>
                        </div>
                    </div>
                </div>
                <a href="#" class="mouse-btn"><i class="icon ion ion-chevron-down"></i></a>
            </div>
        </div>

    </div>
    <div>
        <div style="margin:100px auto 10px; text-align:center; width:80%;">
            <h1 class="regular-bold" font-family="Kaushan Script', helvetica;"
                style="font-size: 6rem; background-image: linear-gradient(to right, rgb(74, 180, 237), rgb(19, 236, 193)); -webkit-background-clip: text; color: transparent;">
                GraND Pro
            </h1>
            <h4 class="text-italic" font-family="Kaushan Script', helvetica;"
                style="font-size: 4rem; background-image: linear-gradient(to right, rgb(74, 180, 237), rgb(19, 236, 193)); -webkit-background-clip: text; color: transparent;">
                Graph Network Disk with Prometheus
            </h4>

        </div>
    </div>

    <div class="row clearfix" style="margin:100px;  width:80%;">
        <!--<div class="text-italic">当前访问位置：</div>-->
        <ul class="breadcrumb" id="curr_path"></ul>
        <!--</div>

    <div style="margin: 20px; width:90%;">-->
        <table class="table" id="fileCatalogTable">
            <!--<table> 标签定义 HTML 表格。-->
            <!--简单的 HTML 表格由 table 元素以及一个或多个 tr、th 或 td 元素组成。
                tr 元素定义表格行，th 元素定义表头，td 元素定义表格单元。-->
            <!--thead 为行标题-->
            <!--tbody 为具体的文件信息-->
            <thead>
                <tr>
                    <th></th>
                    <th>&emsp;&emsp;&emsp;&emsp;文件名</th>
                    <th>读写权限</th>
                    <th>修改时间</th>
                    <th>是否为共享文件</th>
                </tr>
            </thead>
            <tbody id="file_list_body">
                <tr class="file_list_back">
                    <td></td>
                    <td>
                        <label>
                            <input style="height: 20px; width:20px; margin: 0 auto; display: inline; float:left;" type="checkbox">&emsp;&emsp;
                        </label>
                        <i class="ion-android-folder"></i>&emsp;../
                        <!--<i>标签显示斜体文本效果-->
                    </td>
                    <td></td>
                    <td></td>
                </tr>
                <%
                    Cookie cookie = null;

                    // 创建cookie对象
                    Cookie[] cookies = null;
                    String username = null;

                    // 获取 cookies 的数据,是一个数组,从浏览器传过来
                    cookies = request.getCookies();
                    if( cookies != null ){
                        //out.println("<h2> 查找 Cookie 名与值</h2>");
                        for (int i = 0; i < cookies.length; i++){
                            cookie = cookies[i];
                            
                            // 比较
                            if (("username").equals(cookie.getName())) {
                                username = cookie.getValue();
                                // 获取用户名
                                //out.print(username);
                            }
                        }
                    } else{
                        out.println("<h2>没有发现 Cookie</h2>");
                    }
                %>
                <%
                    // 重定向到新地址
                        if(username == null){
                    String site = new String("../index.html");
                    response.setStatus(response.SC_MOVED_TEMPORARILY);
                    response.setHeader("Location", site); }
                %>
                <%
                    int i;
                    Query query = new Query();
                    
                    // 获取该用户上传的文件列表
                    FileItem[] files = query.queryFileList(username, "/");
                    query.closeConnection();
                    if(files!=null)
                    {
                        for(i=0;i<files.length;i++)
                        {
                            out.println("<tr class='file_list_go'>");
                            out.println("<td></td>");
                            
                            // 不是文件夹
                            if(files[i].isFolder()==false)
                            {
                                out.println("<td>");
                                out.println("<label><input style=\"height: 20px; width:20px; margin: 0 auto; display: inline; float:left;\" type=\"checkbox\">&emsp;&emsp;</label>");
                                out.println("<i class=\"glyphicon glyphicon-file\">&emsp;"+ files[i].getFileName() + "</i>&emsp;</td>");
                            }
                            else
                            {
                                out.println("<td>");
                                out.println("<label><input style=\"height: 20px; width:20px; margin: 0 auto; display: inline; float:left;\" type=\"checkbox\">&emsp;&emsp;</label>");
                                out.println("<i class=\"ion-android-folder\">&emsp;"+ files[i].getFileName() + "</i>&emsp;</td>");
                            }
                            out.println("<td>"+files[i].getAttribute()+"</td>");
                            out.println("<td>"+files[i].getTime()+"</td>");
                            out.println("<td>"+files[i].getIsShare()+"</td>");
                            out.println("</tr>");
                        }
                    }
                %>
            </tbody>
        </table>
    </div>

    <div id="rename_dialog" style="width:270px; height:30px; margin:0 auto; border:none; display:none;" title="重命名">
        <form>
            <span class="close" style="display:inline; float: right;">&times;</span>
            <input type="text" id="new_name" placeholder="请输入新文件名" style="width:200px; display:inline; float:left;">
            <button class="btn" type="button" style="width:50px; display:inline; float:right;" id="button_confirm">确认</button>
        </form>
    </div>

    <div id="dirname_dialog" style="width:270px; height:30px; margin:0 auto; display: none; border:none;" title="新建目录">
        <form>
            <span class="dirclose" style="display:inline; float: right;">&times;</span>
            <input type="text" id="dir_name" placeholder="请输入目录的名字" style="width:200px; display:inline; float:left;">
            <button class="btn" type="button" style="width:50px; display:inline; float:right;" id="button_confirm2">确认</button>
        </form>
    </div>

    <div class="layui-btn-container" style="margin: 50px auto 30px; text-align:center;">
        <button class="btn" type="button" id="button_download">下载</button>
        <input type="file" id="files" style="display: none" onchange="fileUpload();">
        <!--隐藏类型为 file 的 input 标签-->
        <button class="btn" type="button" id="button_upload">上传</button>
        <button class="btn" type="button" id="button_delete">删除</button>
        <button class="btn" type="button" id="button_rename">重命名</button>
        <button class="btn" type="button" id="button_adddir">新建目录</button>
    </div>

    <!-- ghx -->
    <div id="wrapper">
        <%-- <div id="bg"></div>
	    <div id="overlay"></div> --%>
        <div id="main" ,style="text-align:center">
            <!-- Header -->
            <nav>
                <form method="GET" action="../GraphGui2/index.html" id="myform">
                    <input type="hidden" name="pos" id="pos" value="Z:"><br>
                    <input type="hidden" name="username" id="username" value=""><br>
                    <input class="btn" type="button" value="Graph"
                        style="width: 100px;height: 50px; display:block;margin:0 auto;" onclick="mySubmitForm()">
                </form>
            </nav>
        </div>
    </div>
    <div
        style="margin:10px auto 100px; text-align:center; width:60%; font-family:Microsoft YaHei,微软雅黑,Microsoft JhengHei,华文细黑,STHeiti,MingLiu; font-size: 2.5rem;">
        <p id="statusFeedback">欢迎使用</p>
    </div>

    <div
        style="margin:0 auto; width:80%; text-align:center; font-family:Microsoft YaHei,微软雅黑,Microsoft JhengHei,华文细黑,STHeiti,MingLiu; font-size: 1.2rem;">
        <p>
            <!--  2020 Copyright: -->
            本项目由关浩祥、牛午甲、谭骏飞、徐奥、赵子毅基于
            <a class="text-dark" href="https://github.com/OSH-2021/x-DisGraFS">DisGraFS</a>
            与
            <a class="text-dark" href="https://github.com/OSH-2020/x-dontpanic">dontpanic</a>
            开发，有疑问欢迎
            <a class="text-dark" href="https://github.com/OSH-2022/x-TOBEDONE">提 issue</a>
        </p>
    </div>

    <script src="../js/wasm/wasm_exec.js"></script>
    <script>
        const go = new Go();
        WebAssembly.instantiateStreaming(fetch("../js/wasm/mycoder.wasm"), go.importObject)
            .then((result) => go.run(result.instance));
    </script>

    
    <!-- other scripts -->
    <script>
        function mySubmitForm() {
            var uname = $.cookie("username");
            document.getElementById("username").value = uname;
            document.getElementById("myform").submit();
        }
    </script>
        <!-- jQuery Scripts -->

    <script src="js/jquery.min.js"></script>
    <script src="js/jquery.validate.js"></script>
    <script src="js/jquery.mb.YTPlayer.js"></script>
    <script src="../js/jquery/jquery.js"></script>
    <script src="js/masonry.pkgd.js"></script>
    <script src="js/imagesloaded.pkgd.js"></script>
    <script src="js/masonry-filter.js"></script>
    <script src="js/magnific-popup.js"></script>

    <script src="js/particles.js"></script>
    <script src="js/typed.js"></script>

    <!-- Main Scripts -->
    <script src="js/main.js"></script>

    <script src="../js/jquery/jquery.cookie.js"></script>
</body>

</html>