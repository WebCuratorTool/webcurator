<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.webcurator.domain.model.core.HarvestResult" %>

<div id="resultsTable">
    <table width="100%" cellpadding="2" cellspacing="0" border="0">
        <c:if test="${action eq 'prune'}">
        <tr><td><span class="midtitleGrey">To Be Pruned</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBePruned}">
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td class="subBoxText">No To Be Pruned urls are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
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
        </c:if>

        <c:if test="${action eq 'file'}">
        <tr><td><span class="midtitleGrey">To Be Imported by File</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBeImportedByFile}">
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td class="subBoxText" colspan="3">No To Be Imported by Files are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
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
        </c:if>

        <c:if test="${action eq 'url'}">
        <tr><td><span class="midtitleGrey">To Be Imported by URL</span></td></tr>
        <tr>
            <td width="100%">
                    <c:choose>
                        <c:when test="${empty listToBeImportedByURL}">
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td class="subBoxText">No To Be Imported by URLs are available.</td>
                            </tr>
                        </table>
                        </c:when>
                        <c:otherwise>
                        <table cellpadding="3" cellspacing="0" border="0" width="100%">
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
                            <c:if test="${listToBePruned.more}">
                                <tr>
                                  <td class="subBoxText">
                                     <a href="curator/target/modification/view/more?targetInstanceOid=${ti.oid}&harvestResultNumber=${hr.harvestNumber}&action=url" target="_blank">More...</a> |
                                  </td>
                                </tr>
                            </c:if>
                        </table>
                        </c:otherwise>
                    </c:choose>
            </td>
        </tr>
        </c:if>

        <tr>
            <td colspan="2" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
        </tr>
        <tr><td colsapan="2">&nbsp;</td></tr>
        <tr class="tableRowLite">
            <td width="30%"><a href="curator/target/patching-view-hr.html?targetInstanceOid=${ti.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}"><img src="images/generic-btn-done.gif" border="0"></a></td>
        </tr>
	</table>
</div>