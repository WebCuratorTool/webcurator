<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<script language="javascript" src="scripts/jquery-3.4.1.min.js"></script>
<script>
	document.addEventListener('mouseup', function(e) {
        var container = document.getElementById('thumbnailModal');
        if (!container.contains(e.target)) {
            container.style.display = 'none';
        }
    });

    function recreate(hrOid){
        var recreateLive=$('#is-recreate-live').is(':checked');
        var recreateIndex=$('#is-recreate-index').is(':checked');
        var recreateHarvested=$('#is-recreate-harvested').is(':checked');
        if(!recreateLive && !recreateIndex && !recreateHarvested){
            alert("No operation is checked, the request will be ignored.");
            return;
        }

        if(confirm('Reloading will abort the current result and create a new one. The Harvest Result will be reloaded and any changes since the last save will be lost. The harvested screenshot will be forced to regenerated if reindexing is applied. - continue?')) {
            var reqUrl='curator/index/recreate?hrOid='+hrOid+'&recreateLive='+recreateLive+'&recreateIndex='+recreateIndex+'&recreateHarvested='+recreateHarvested;

            fetch(reqUrl, {
                method: 'GET',
                redirect: 'follow',
                headers: {'Content-Type': 'application/json'}
            }).then((response) => {
                return response.json();
            }).then((response) => {
                console.log(response);
                alert(response["msg"]);
            }).catch((error) => {
              console.log(error);
              alert(error);
            });
        }
    }
</script>

<div id="resultsTable">
	<table width="100%" cellpadding="2" cellspacing="0" border="0">
		<tr>
			<td><span class="midtitleGrey">Quality Review Tools</span></td>
		</tr>
		<tr>
			<td width="30%" class="tableHead">Seed</td>
			<td width="50%" class="tableHead">Actions</td>
			<td width="10%" class="tableHead" style="text-align: center;">Live</td>
            <td width="10%" class="tableHead" style="text-align: center;">Harvested</td>
		</tr>
		<tr>
            <c:forEach items="${seeds}" var="seed">
            <tr>
                <td width="30%">
                    <c:choose>
                    <c:when test="${seed.primary == 'true'}" >
                        <b>${seed.seedUrl}</b>
                    </c:when>
                    <c:otherwise>
                        ${seed.seedUrl}
                    </c:otherwise>
                    </c:choose>
                </td>
                <td width="50%">
                  <c:if test="${seed.browseUrl != ''}">
                  <a href="<c:out value="${seed.browseUrl}"/>" target="_blank">Review this Harvest</a>
                  |
                  </c:if>
                  <c:if test="${seed.accessUrl != ''}">
                  <a href="<c:out value="${seed.accessUrl}"/>" target="_blank">Review in Access Tool</a>
                  |
                  </c:if>
                  <a href="<c:out value="${seed.seedUrl}"/>" target="_blank">Live Site</a>
                  <c:choose>
                    <c:when test="${archiveUrl == ''}"></c:when>
                    <c:otherwise>
                    | <a href="<c:out value="${archiveUrl}" escapeXml="false"/><c:out value="${seed.seedUrl}"/>" target="_blank">
                          <c:choose>
                            <c:when test="${archiveName == ''}">Archives Harvested</c:when>
                            <c:otherwise><c:out value="${archiveName}"/></c:otherwise>
                          </c:choose>
                      </a>
                    </c:otherwise>
                  </c:choose>
                  <c:choose>
                    <c:when test="${archiveAlternative == ''}"></c:when>
                    <c:otherwise>
                    | <a href="<c:out value="${archiveAlternative}" escapeXml="false"/><c:out value="${seed.seedUrl}"/>" target="_blank"><c:out value="${archiveAlternativeName}"/></a>
                    </c:otherwise>
                  </c:choose>
                  <c:choose>
                    <c:when test="${webArchiveTarget == ''}">
                    | Web Archive not configured
                    </c:when>
                    <c:otherwise>
                    | <a href="<c:out value="${webArchiveTarget}" escapeXml="false"/><c:out value="${targetOid}"/>" target="_blank">Web Archive</a></td>
                    </c:otherwise>
                  </c:choose>

                </td>

                <c:if test="${enableScreenshots && thumbnailRenderer eq 'screenshotTool'}">
                    <c:set var = "seedId" value = "${seed.id}" />
                    <c:set var = "fileUrl" value = "${screenshotUrl}" />
                    <c:set var = "primarySeedOid" value = "${primarySeedId}" />
                    <c:set var = "liveUrl" value = "${fn:replace(fileUrl, 'seedId', seedId)}" />
                    <c:set var = "harvestedUrl" value = "${fn:replace(liveUrl, 'live', 'harvested')}" />


                    <td style='width:10%; text-align:center;'><img src="${liveUrl}" alt="Image unavailable" width="100px" style="padding: 5px; cursor: pointer;" onclick="document.getElementById('thumbnailModal').style.display='block';" /></td>
                    <td style='width:10%; text-align:center;'><img src="${harvestedUrl}" alt="Image unavailable" width="100px" style="padding: 5px; cursor: pointer;" onclick="document.getElementById('thumbnailModal').style.display='block';" /></td>
                </c:if>
            </tr>
            <tr>
                <td colspan="4" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
            </tr>
            </c:forEach>

		</tr>
		<tr>
		  <td>&nbsp;</td>
		</tr>
		<tr>
			<td width="30%" class="tableHead">Tool</td>
			<td width="70%" colspan="3" class="tableHead">Description</td>
		</tr>

<%--		<tr>--%>
<%--            <td width="30%">--%>
<%--                <a href="javascript: recreate(${command.harvestResultId});">Recreate screenshot or reindex</a>--%>
<%--                <!--input type="submit" value="Recreate screenshot or reindex"-->--%>
<%--            </td>--%>
<%--            <td width="70%" colspan="3">--%>
<%--                <ul class="reindexOptions">--%>
<%--                    <li>--%>
<%--                        <c:choose>--%>
<%--                            <c:when test="${enableScreenshots}">--%>
<%--                                <c:choose>--%>
<%--                                    <c:when test="${screenshotState.liveScreenshot}">--%>
<%--                                        <input type="checkbox" id="is-recreate-live" name="is-recreate-live">--%>
<%--                                        <label for="is-recreate-live">Live screenshot (&radic;)</label>--%>
<%--                                    </c:when>--%>
<%--                                    <c:otherwise>--%>
<%--                                        <input type="checkbox" id="is-recreate-live" name="is-recreate-live" checked>--%>
<%--                                        <label for="is-recreate-live">Live screenshot(&empty;)</label>--%>
<%--                                    </c:otherwise>--%>
<%--                                </c:choose>--%>
<%--                            </c:when>--%>
<%--                            <c:otherwise>--%>
<%--                                <input type="checkbox" id="is-recreate-live" name="is-recreate-live" disabled>--%>
<%--                                <label for="is-recreate-live">Live screenshot</label>--%>
<%--                            </c:otherwise>--%>
<%--                        </c:choose>--%>
<%--                    </li>--%>
<%--                    <li>--%>
<%--                        <c:choose>--%>
<%--                            <c:when test="${screenshotState.indexAvailable}">--%>
<%--                                <input type="checkbox" id="is-recreate-index" name="is-recreate-index">--%>
<%--                                <label for="is-recreate-index">Playback indexes (&radic;)</label>--%>
<%--                            </c:when>--%>
<%--                            <c:otherwise>--%>
<%--                                <input type="checkbox" id="is-recreate-index" name="is-recreate-index" checked>--%>
<%--                                <label for="is-recreate-index">Playback indexes(&empty;)</label>--%>
<%--                            </c:otherwise>--%>
<%--                        </c:choose>--%>
<%--                    </li>--%>
<%--                    <li>--%>
<%--                        <c:choose>--%>
<%--                            <c:when test="${enableScreenshots}">--%>
<%--                                <c:choose>--%>
<%--                                    <c:when test="${screenshotState.harvestedScreenshot}">--%>
<%--                                        <input type="checkbox" id="is-recreate-harvested" name="is-recreate-harvested">--%>
<%--                                        <label for="is-recreate-harvested">Harvested screenshot (&radic;)</label>--%>
<%--                                    </c:when>--%>
<%--                                    <c:otherwise>--%>
<%--                                        <input type="checkbox" id="is-recreate-harvested" name="is-recreate-harvested" checked>--%>
<%--                                        <label for="is-recreate-harvested">Harvested screenshot(&empty;)</label>--%>
<%--                                    </c:otherwise>--%>
<%--                                </c:choose>--%>
<%--                            </c:when>--%>
<%--                            <c:otherwise>--%>
<%--                                <input type="checkbox" id="is-recreate-harvested" name="is-recreate-harvested" disabled>--%>
<%--                                <label for="is-recreate-harvested">Harvested screenshot</label>--%>
<%--                            </c:otherwise>--%>
<%--                        </c:choose>--%>
<%--                    </li>--%>
<%--                </ul>--%>
<%--            </td>--%>
<%--        </tr>--%>
<%--        <tr>--%>
<%--            <td colspan="4" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>--%>
<%--        </tr>--%>

		<tr>
			<td width="30%"><a href="<%=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+ request.getContextPath()%>/curator/tools/harvest-history.html?targetInstanceOid=<c:out value="${command.targetInstanceOid}"/>&harvestResultId=<c:out value="${command.harvestResultId}"/>">Harvest History</a></td>
			<td width="70%" colspan="3">Compare current harvest result with previous harvests.</td>
		</tr>
		<tr>
			<td colspan="4" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
		</tr>	
		<!--tr>
			<td width="30%"><a href="curator/tools/treetool.html?loadTree=<c:out value="${command.harvestResultId}"/>&targetInstanceOid=<c:out value="${targetInstanceOid}"/>&logFileName=aqa-report(<c:out value="${command.harvestNumber}"/>).xml">Tree View</a></td>			
			<td width="70%" colspan="3">Graphical view of harvested data.</td>
		</tr-->
		<tr>
            <td width="30%"><a href="curator/target/harvest-result-networkmap.html?targetInstanceOid=${targetInstanceOid}&harvestResultId=${command.harvestResultId}&harvestNumber=${command.harvestNumber}">Harvest Analysis and Patching</a></td>
            <td width="70%" colspan="3">Analyse harvested data through visualization and tree views, while importing and pruning missing or unwanted content.</td>
        </tr>
		<tr>
			<td colspan="4" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
		</tr>
		<tr>
			<td colsapan="4">&nbsp;</td>
		</tr>	
		<tr class="   ">
			<td width="30%"><a id="quality-review-toc-done" href="curator/target/target-instance.html?targetInstanceId=<c:out value="${targetInstanceOid}&cmd=edit&init_tab=RESULTS"/>"><img src="images/generic-btn-done.gif" border="0"></a></td>
		    <td width="70%">&nbsp;</td>
		</tr>
	</table>
</div>

<div id="thumbnailModal" style="display: none;">
	<c:if test="${thumbnailRenderer eq 'screenshotTool'}">
		<div id="thumbnailModalHeader">
            <span>Screenshot</span>
            <span id="close" onclick="document.getElementById('thumbnailModal').style.display='none';"> &times; </span>
        </div>

        <div id="thumbnailTableContainer">
            <table id="thumbnailTable" style="border: 0px none; width: 100%;">
                <tbody>
                    <c:forEach var = "seed" items = "${seeds}" >
                        <tr><td colspan="2">
                            <table class="panel_dotted_row">
                                <tr>
                                    <td style='width:15%; text-align:left;'><span style='font-weight: bold;'>Live</span></td>
                                    <td colspan='2' style='width:70%; height:18px; text-align:center;'>${seed["seedUrl"]}</td>
                                    <td style='width:15%; text-align:right;'><span style='font-weight: bold;'>Harvested</span></td>
                                </tr>
                            </table>
                        </td></tr>

                        <tr>
                            <c:set var = "fileUrl" value = "${fn:replace(screenshotUrl,'-thumbnail', '')}" />
                            <c:set var = "liveUrl" value = "${fn:replace(fileUrl, 'seedId', seed['id'])}" />
                            <c:set var = "harvestedUrl" value = "${fn:replace(liveUrl, 'live', 'harvested')}" />
                            <td>
                                <img src="${liveUrl}" alt="Image unavailable" style="width: 95%; padding: 5px;">
                            </td>
                            <td>
                                <img src="${harvestedUrl}" alt="Image unavailable" style="width: 95%; padding: 5px;">
                            </td>
                        </tr>

                        <tr><td colspan="2"><table class="panel_header_row"><tr><td style="height:8px;"></td></tr></table></td></tr>
                    </c:forEach>
                </tbody>
            </table>
		</div>
	</c:if>
</div>
