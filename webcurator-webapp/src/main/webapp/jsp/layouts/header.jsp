<%@page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.webcurator.common.ui.Constants" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<div id="topBar"><div id="secondaryNav"><a href="index.html">New UI</a> | <a href="<%= Constants.CNTRL_HOME%>">Home</a> | <a href="curator/target/queue.html?type=queue">Queue</a> | <a href="curator/target/queue.html?type=harvested">Harvested</a> | <a href="<tiles:getAsString name="page-help"/>" target="_blank">Help</a> | <a href="logout">Logout</a>
<br/>User <%= org.webcurator.core.util.AuthUtil.getRemoteUser() %> is logged in.</div><a href="<%= Constants.CNTRL_HOME%>" accesskey="1"><img src="images/web-curator-tool-logo.gif" alt="Web Curator Tool" width="320" height="68" border="0" /></a></div>

