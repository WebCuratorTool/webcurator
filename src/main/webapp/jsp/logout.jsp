<%@ page import="org.webcurator.common.ui.Constants" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

session.invalidate();
response.sendRedirect(basePath+Constants.CNTRL_HOME);
%>