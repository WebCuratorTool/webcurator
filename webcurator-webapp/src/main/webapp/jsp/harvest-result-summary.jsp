<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.webcurator.domain.model.core.HarvestResult" %>

<div id="resultsTable">
    <iframe class="content-window-normal-screen" src='spa/tools/patching-view-hr.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}'></iframe>

	<table width="100%" cellpadding="2" cellspacing="0" border="0">
        <tr>
            <td colspan="2" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
        </tr>
        <tr><td colsapan="2">&nbsp;</td></tr>
        <tr class="tableRowLite">
            <td width="100%"><a href="curator/target/target-instance.html?targetInstanceId=${ti.oid}&cmd=edit&init_tab=RESULTS"><img src="images/generic-btn-done.gif" border="0"></a></td>
        </tr>
	</table>
</div>