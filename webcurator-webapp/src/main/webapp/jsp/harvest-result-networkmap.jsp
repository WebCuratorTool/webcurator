<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.webcurator.domain.model.core.HarvestResult" %>

<div id="resultsTable">
<a href="curator/target/quality-review-toc.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}" style="display: none;"><img src="images/generic-btn-done.gif" border="0" id="btn-link-done"></a>
<iframe id="iframe-networkmap-content" class="content-window-normal-screen" src='/spa/tools/visualization.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}'></iframe>
	<!--table width="100%" cellpadding="2" cellspacing="0" border="0"  class="sub-win-footer">
        <tr>
            <td class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr class="tableRowLite">
            <td><a href="javascript: applyContentWindow();"><img src="images/generic-btn-apply.gif" border="0" id="btn-link-done"></a></td>
            <td><a href="curator/target/quality-review-toc.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}"><img src="images/generic-btn-done.gif" border="0" id="btn-link-done"></a></td>
        </tr>
	</table-->
</div>

<script src="scripts/jquery-3.4.1.min.js"></script>
<script src="scripts/wct-coordinator.js"></script>
