<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.webcurator.domain.model.core.HarvestResult" %>

<div id="resultsTable">
    <iframe id="iframe-networkmap-content" class="content-window-normal-screen" src='spa/tools/visualization.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}'></iframe>
</div>


<div style="display: none;">
    <a href="curator/target/quality-review-toc.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}"><img src="images/generic-btn-done.gif" border="0" id="btn-link-done"></a>
</div>

<!-- Login Page -->
<div id="popup-window-login" style="display: none;"></div>

<script src="scripts/jquery-3.4.1.min.js"></script>
<script src="scripts/wct-coordinator.js"></script>
