<%@ page import="org.webcurator.ui.tools.command.PatchCommand"%>
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
			<legend class="groupBoxLabel">Patch with pywb</legend>
			<table width=100%>
			    <tr>
			        <td>
			        Start a recording session with entry point <c:out value="${command.seedUrl}"/>
			        </td>
			    </tr>
				<tr>
					<td>
				    	<a href="<c:out value='${command.recordingUrl}'/>" target="_blank"><img src="images/resume-icon.gif" alt="Start recording session"></a>
				    </td>
				    <td>
					    <form action="curator/tools/patch.html" method="POST">
					        <input type="hidden" name="recordingUrl" value="<c:out value='${command.recordingUrl}'/>"/>
					        <input type="hidden" name="actionCmd" value="<c:out value='${PatchCommand.ACTION_SAVE}'/>"/>
					        <input type="image" src="images/generic-btn-save.gif"/>
				    	</form>
					</td>
					<td>
				        <a href="curator/target/quality-review-toc.html?targetInstanceOid=<c:out value='${command.targetInstanceOid}'/>&harvestResultId=<c:out value='${command.harvestResultId}'/>&harvestNumber=<c:out value='${command.harvestNumber}'/>">
				          <img src="images/generic-btn-cancel.gif" alt="Cancel"/>
				        </a>
					</td>
				</tr>
			</table>
			</fieldset>
		</td>
	</tr>
</table>
</div>
