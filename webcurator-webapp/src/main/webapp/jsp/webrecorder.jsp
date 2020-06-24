<%@ page import="org.webcurator.ui.tools.command.WebrecorderCommand"%>
<%@ page import="org.webcurator.common.ui.Constants"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri='http://www.webcurator.org/wct' prefix='wct'%>

<style>
td {
	font-size: 8pt;
	font-family: arial;
	margin: 0;
	padding: 0;
}

a {
	color: black;
	text-decoration: none;
}

</style>


<div id="resultsTable">
<table width="800px" cellpadding="5" cellspacing="0" border="0">
	<tr>
		<td colspan="3" class="tableRowLite"><img src="images/x.gif" alt="" width="1"
			height="5" border="0" /></td>
	</tr>
	<tr valign="top">
		<td valign="top" width="45%">
			<fieldset style="width:370px;">
			<legend class="groupBoxLabel">Patch with webrecorder</legend>
			<table width=100%>
			    <tr>
			        <td>
			            Webrecorder status: [not implemented yet]
			        </td>
			    </tr>
				<tr>
					<td>
					    <form action="curator/tools/webrecorder.html" method="POST" target="_blank">
					        <input type="hidden" name="recordingUrl" value="<c:out value='${command.recordingUrl}'/>"/>
					        <input type="hidden" name="userName" value="<c:out value='${command.userName}'/>"/>
					        <input type="hidden" name="recordingName" value="<c:out value='${command.recordingName}'/>"/>
					        <input type="hidden" name="seedUrl" value="<c:out value='${command.seedUrl}'/>"/>
					        <input type="hidden" name="sessionCookie" value="<c:out value='${command.sessionCookie}'/>"/>
					        <input type="hidden" name="actionCmd" value="<c:out value='${WebrecorderCommand.ACTION_RECORD}'/>"/>
					        <input type="image" src="images/home-btn-open.gif"/>
				    	</form>
					</td>
				</tr>
			</table>
			</fieldset>
		</td>
	</tr>
</table>
</div>
