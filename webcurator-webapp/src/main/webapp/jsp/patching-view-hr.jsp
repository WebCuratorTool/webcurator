<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.webcurator.domain.model.core.ArcHarvestResult" %>

<div id="resultsTable">
	<table width="100%" cellpadding="2" cellspacing="0" border="0">
		<tr><td><span class="midtitleGrey">Overall</span></td></tr>
		<tr width="100%" >
		    <td width="30%"><b>Harvest Number: </b> ${hr.harvestNumber}</td>
            <td width="30%"><b>Derived From: </b> ${hr.derivedFrom}</td>
            <td width="30%">
                <b>State: </b>
                <c:choose>
                    <c:when test="${hr.state == 1}">
                        Endorsed
                    </c:when>
                    <c:when test="${hr.state == 2}">
                        Rejected
                    </c:when>
                    <c:when test="${hr.state == 3}">
                        Indexing
                    </c:when>
                    <c:when test="${hr.state == 4}">
                        Aborted
                    </c:when>
                    <c:when test="${hr.state == 50}">
                        Patch Scheduled
                    </c:when>
                    <c:when test="${hr.state == 60}">
                        Patch Harvesting
                    </c:when>
                    <c:when test="${hr.state == 61}">
                        Patch Harvesting Paused
                    </c:when>
                    <c:when test="${hr.state == 62}">
                        Patch Harvesting Aborted
                    </c:when>
                    <c:when test="${hr.state == 70}">
                        Patch Modifying
                    </c:when>
                    <c:when test="${hr.state == 71}">
                        Patch Modifying Paused
                    </c:when>
                    <c:when test="${hr.state == 72}">
                        Patch Modifying Aborted
                    </c:when>
                    <c:when test="${hr.state == 80}">
                        Patch Indexing
                    </c:when>
                    <c:when test="${hr.state == 81}">
                        Patch Indexing Paused
                    </c:when>
                    <c:when test="${hr.state == 82}">
                        Patch Indexing Aborted
                    </c:when>
                </c:choose>
            </td>
        </tr>
        <tr><td colsapan="3">&nbsp;</td></tr>
        <tr><td class="tableRowSep" colspan="3"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td></tr>
        <tr><td><span class="midtitleGrey">Logs</span></td></tr>
    </table>

    <div>
        <div style="display: inline-block; width: 30%; vertical-align:top;">
                <table cellpadding="3" cellspacing="0" border="0" width="100%">
                    <tr><th class="tableHead" colspan="3">Patching Crawling Logs</th></tr>
                    <c:choose>
                        <c:when test="${empty logsCrawling}">
                            <tr>
                                <td class="subBoxText">No log files are available.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <th width="50%">Filename</th>
                                <th width="20%">Action</th>
                                <th width="20%">Size</th>
                            </tr>
                            <c:forEach items="${logsCrawling}" var="logFile">
                            <tr>
                                <td class="subBoxText">
                                    <c:out value="${logFile.name}"/>
                                </td>
                                <td class="subBoxText">
                                    <a href="curator/target/${logFile.viewer}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_NORMAL}" target="_blank">View</a> |
                                    <a href="curator/target/${logFile.retriever}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_NORMAL}">Download</a>
                                </td>
                                <td class="subBoxText">
                                    <c:out value="${logFile.lengthString}"/>
                                </td>
                            </tr>
                            </c:forEach>
                        </c:otherwise>
                </c:choose>
          </table>
        </div>
        <div style="display: inline-block; width: 30%; vertical-align:top;">
            <table cellpadding="3" cellspacing="0" border="0" width="100%">
                <tr><th class="tableHead" colspan="3">Patching Modifying Logs</th></tr>
                <c:choose>
                    <c:when test="${empty logsModifying}">
                        <tr>
                            <td class="subBoxText">No log files are available.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <th width="50%">Filename</th>
                            <th width="20%">Action</th>
                            <th width="20%">Size</th>
                        </tr>
                        <c:forEach items="${logsModifying}" var="logFile">
                        <tr>
                            <td class="subBoxText">
                                <c:out value="${logFile.name}"/>
                            </td>
                            <td class="subBoxText">
                                <a href="curator/target/${logFile.viewer}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_MODIFYING}" target="_blank">View</a> |
                                <a href="curator/target/${logFile.retriever}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_MODIFYING}">Download</a>
                            </td>
                            <td class="subBoxText">
                                <c:out value="${logFile.lengthString}"/>
                            </td>
                        </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </table>
        </div>
        <div style="display: inline-block; width: 30%; vertical-align:top;">
            <table cellpadding="3" cellspacing="0" border="0" width="100%">
                <tr><th class="tableHead" colspan="3">Patching Indexing Logs</th></tr>
                <c:choose>
                    <c:when test="${empty logsIndexing}">
                        <tr>
                            <td class="subBoxText">No log files are available.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <th width="50%">Filename</th>
                            <th width="20%">Action</th>
                            <th width="20%">Size</th>
                        </tr>
                        <c:forEach items="${logsIndexing}" var="logFile">
                        <tr>
                            <td class="subBoxText">
                                <c:out value="${logFile.name}"/>
                            </td>
                            <td class="subBoxText">
                                <a href="curator/target/${logFile.viewer}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_INDEXING}" target="_blank">View</a> |
                                <a href="curator/target/${logFile.retriever}?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&logFileName=${logFile.name}&prefix=${ArcHarvestResult.PATCH_STAGE_TYPE_INDEXING}">Download</a>
                            </td>
                            <td class="subBoxText">
                                <c:out value="${logFile.lengthString}"/>
                            </td>
                        </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </table>
        </div>
    </div>

    <table width="100%" cellpadding="2" cellspacing="0" border="0">
        <tr><td>&nbsp;</td></tr>
        <tr><td class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td></tr>
        <tr><td><span class="midtitleGrey">To Be Pruned</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBePruned}">
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <td class="subBoxText">No To Be Pruned urls are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <th width="100%">Target URL</th>
                            </tr>
                            <c:forEach items="${listToBePruned}" var="e">
                            <tr>
                                <td class="subBoxText">
                                    <c:out value="${e.url}"/>
                                </td>
                            </tr>
                            </c:forEach>
                        </table>
                        </c:otherwise>
                    </c:choose>
            </td>
        </tr>

        <tr><td>&nbsp;</td></tr>
        <tr><td class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td></tr>
        <tr><td><span class="midtitleGrey">To Be Imported by File</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBeImportedByFile}">
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <td class="subBoxText" colspan="3">No To Be Imported by Files are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <th width="70%">Target URL</th>
                                <th width="15%">File Name</th>
                                <th width="15%">Modified Date</th>
                            </tr>
                            <c:forEach items="${listToBeImportedByFile}" var="e">
                            <tr>
                                <td class="subBoxText">
                                    <c:out value="${e.url}"/>
                                </td>
                                <td class="subBoxText">
                                    <c:out value="${e.name}"/>
                                </td>
                                <td class="subBoxText">
                                    <c:out value="${e.lastModifiedPresentationString}"/>
                                </td>
                            </tr>
                            </c:forEach>
                        </table>
                        </c:otherwise>
                    </c:choose>
            </td>
        </tr>

        <tr><td>&nbsp;</td></tr>
        <tr><td class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td></tr>
        <tr><td><span class="midtitleGrey">To Be Imported by URL</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBeImportedByURL}">
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <td class="subBoxText">No To Be Imported by URLs are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0">
                            <tr>
                                <th width="100%">Target URL</th>
                            </tr>
                            <c:forEach items="${listToBeImportedByURL}" var="e">
                            <tr>
                                <td class="subBoxText">
                                    <c:out value="${e.url}"/>
                                </td>
                            </tr>
                            </c:forEach>
                        </table>
                        </c:otherwise>
                    </c:choose>
            </td>
        </tr>

        <tr>
            <td colspan="2" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
        </tr>
        <tr><td colsapan="2">&nbsp;</td></tr>
        <tr class="tableRowLite">
            <td width="30%"><a href="curator/target/target-instance.html?targetInstanceId=${ti.oid}&cmd=edit&init_tab=RESULTS"><img src="images/generic-btn-done.gif" border="0"></a></td>
        </tr>
	</table>
</div>