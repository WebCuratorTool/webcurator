<%@ page language="java" pageEncoding="UTF-8"%><%@ page import="org.webcurator.core.common.EnvironmentFactory" %><%@ page import="org.webcurator.core.common.Environment" %><%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

String failed = request.getParameter("failed");

Environment env = EnvironmentFactory.getEnv();
String wctAppVersion = env.getApplicationVersion();
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <base href="<%=basePath%>">
    <link rel="stylesheet" media="screen" href="styles/styles.css" type="text/css" title="WCT" />
    <link rel="stylesheet" media="screen" href="styles/basic.css" type="text/css" title="WCT" />
    <title>Login</title>    
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <script src="scripts/jquery-3.4.1.min.js"></script>
    <script>
        var gParent=parent;

        function isEmbedFrame(){
            var embed = false;
            if(gParent && gParent.getEmbedFlag){
                embed = gParent.getEmbedFlag();
            }
            return embed;
        }
        function auth() {
            var username=document.getElementsByName('username')[0].value;
            var password=document.getElementsByName('password')[0].value;
            var reqBodyPayload=encodeURI('username='+username+'&password='+password);
            var reqMode=isEmbedFrame()?'embed':'independent';
            fetch('login', {
                method: 'POST',
                redirect: 'follow',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Request-Mode': reqMode
                },
                body: reqBodyPayload
            }).then((response) => {
               console.log(response.headers);
               if(isEmbedFrame()){//For embed page authentication
                   console.log('Auth-Result: ' + response.headers.get('Auth-Result'));
                   if(response.headers.get('Auth-Result')==='pass'){
                       console.log('Auth success and callback');
                       gParent.authCallback();
                   }else if(response.headers.get('Auth-Result')==='failed'){
                       console.log('Auth failed and show failed message');
                       return response.text();
                   }
               }else{                    //For un-embed page authentication
                   console.log('Content-Type: ' + response.headers.get('Content-Type'));
                   if(response.headers.get('Content-Type').startsWith('text/html')){
                      return response.text();
                   }else{
                      auth(); //Request again to get the home page
                   }
               }
            }).then((response)=>{
               if(response){
                   document.querySelector('html').innerHTML=response;
               }
            });
        }

        $(function(){
            if(isEmbedFrame()){
                $('#form-login-submit').attr('action', 'javascript: auth();');
            }else{
                $('#form-login-submit').attr('action', 'login');
            }
        });

    </script>
  </head>

<body onLoad="document.forms[0].elements[0].focus();">
<a name="top"></a>
<div id="topBar"><img src="images/web-curator-tool-logo.gif" alt="Web Curator Tool" width="320" height="68" border="0" /></div>
<br class="clear" />
		<form name="login" action="javascript: auth();" method="POST" id="form-login-submit">
			<div id="loginBox">
                <%
                    if (failed != null && failed.equals("true")) {
                %>
                    <SPAN>The user name or password entered were incorrect.</SPAN>
                    <br/>
                    <br/>
                <%
                    }
                %>

				<div id="homeRightBoxTop">
				<img src="images/home-box-top-right.gif" alt="" width="20" height="13" border="0" align="right" /><img src="images/home-box-top-left.gif" alt="" width="20" height="13" border="0" /></div>
				<div id="homeRightBoxContent">
					<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td width="8" background="images/home-box-left.gif"><img src="images/x.gif" width="8" height="1" border="0" /></td>
						<td valign="top" width="73"><img src="images/login-icon.jpg" alt="" width="73" height="108" border="0" /></td>
						<td valign="top" width="100%">
							<div id="homeBoxTitle">Login <%= wctAppVersion %></div>
							<div id="homeBoxLine"><img src="images/x.gif" width="1" height="5" border="0" /></div>
						  	<div id="homeBoxText">
								username<br />
								<input type="text" name="username" width="20" style="width:200px"><br />
								password<br />
								<input type="password" name="password" width="20" style="width:200px"><br />
								<input type="image" src="images/home-btn-login.gif" alt="login" width="67" height="18" border="0" vspace="5"/>
							</div>
						</td>
						<td width="10" background="images/home-box-right.gif"><img src="images/x.gif" width="10" height="1" border="0" /></td>
					</tr>
					</table>
				</div>
				<div id="homeRightBoxBottom"><img src="images/home-box-btm-right.gif" alt="" width="20" height="14" border="0" align="right" /><img src="images/home-box-btm-left.gif" alt="" width="20" height="14" border="0" /></div>
			</div>
		</form>
  </body>
</html>
