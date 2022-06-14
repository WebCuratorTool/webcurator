<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<script>
	document.addEventListener('mouseup', function(e) {
    var container = document.getElementById('thumbnailModal');
    if (!container.contains(e.target)) {
        container.style.display = 'none';
    }
});
</script>

<div id="resultsTable">
	<table width="100%" cellpadding="2" cellspacing="0" border="0">
		<tr>
			<td><span class="midtitleGrey">Quality Review Tools</span></td>
		</tr>
		<tr>
			<td colspan="3" class="tableHead">Browse</td>			
		</tr>
		<tr>
			<td colspan="3">
				<table width="100%">
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
						  <a href="<%=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+ request.getContextPath()%>/<c:out value="${seed.browseUrl}"/>" target="_blank">Review this Harvest</a> 
						  | 
						  </c:if>
						  <c:if test="${seed.accessUrl != ''}">
						  <a href="<c:out value="${seed.accessUrl}"/>" target="_blank">Review in Access Tool</a> 
						  | 
						  </c:if>
						  <a href="<c:out value="${seed.seed}"/>" target="_blank">Live Site</a>
						  <c:choose>
						    <c:when test="${archiveUrl == ''}"></c:when>
						    <c:otherwise>
						    | <a href="<c:out value="${archiveUrl}" escapeXml="false"/><c:out value="${seed.seed}"/>" target="_blank">
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
						    | <a href="<c:out value="${archiveAlternative}" escapeXml="false"/><c:out value="${seed.seed}"/>" target="_blank"><c:out value="${archiveAlternativeName}"/></a>
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
						<td width="20%">
							<c:if test="${thumbnailRenderer eq 'screenshotTool'}">
							    <c:set var = "seedId" value = "${seed.id}" />
								<c:set var = "fileUrl" value = "${screenshotUrl}" />
								<c:set var = "primarySeedOid" value = "${primarySeedId}" />
								<c:set var = "liveUrl" value = "${fn:replace(fileUrl, 'seedId', seedId)}" />
								<c:set var = "harvestedUrl" value = "${fn:replace(liveUrl, 'live', 'harvested')}" />
								<img src="${liveUrl}" alt="Image unavailable" width="100px" style="padding: 5px; cursor: pointer;" onclick="document.getElementById('thumbnailModal').style.display='block';" />
								<img src="${harvestedUrl}" alt="Image unavailable" width="100px" style="padding: 5px; cursor: pointer;" onclick="document.getElementById('thumbnailModal').style.display='block';" />
							</c:if>
						</td>
					</tr>
					<tr>			
						<td colspan="3" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
				    </tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		<tr>
		  <td>&nbsp;</td>
		</tr>
		<tr>
			<td width="30%" class="tableHead">Tool</td>
			<td width="70%" class="tableHead">Description</td>			
		</tr>
		
		<tr>
			<td width="30%"><a href="<%=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+ request.getContextPath()%>/curator/tools/harvest-history.html?targetInstanceOid=<c:out value="${command.targetInstanceOid}"/>&harvestResultId=<c:out value="${command.harvestResultId}"/>">Harvest History</a></td>
			<td width="70%">Compare current harvest result with previous harvests.</td>			
		</tr>
		<tr>
			<td colspan="2" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
		</tr>	
		<!--tr>
			<td width="30%"><a href="curator/tools/treetool.html?loadTree=<c:out value="${command.harvestResultId}"/>&targetInstanceOid=<c:out value="${targetInstanceOid}"/>&logFileName=aqa-report(<c:out value="${command.harvestNumber}"/>).xml">Tree View</a></td>			
			<td width="70%">Graphical view of harvested data.</td>			
		</tr-->
		<tr>
            <td width="30%"><a href="curator/target/harvest-result-networkmap.html?targetInstanceOid=${targetInstanceOid}&harvestResultId=${command.harvestResultId}&harvestNumber=${command.harvestNumber}">Harvest Analysis and Patching</a></td>
            <td width="70%">Analyse harvested data through visualization and tree views, while importing and pruning missing or unwanted content.</td>
        </tr>
		<tr>
			<td colspan="2" class="tableRowSep"><img src="images/x.gif" alt="" width="1" height="1" border="0" /></td>
		</tr>
		<tr>
			<td colsapan="2">&nbsp;</td>
		</tr>	
		<tr class="   ">
			<td width="30%"><a href="curator/target/target-instance.html?targetInstanceId=<c:out value="${targetInstanceOid}&cmd=edit&init_tab=RESULTS"/>"><img src="images/generic-btn-done.gif" border="0"></a></td>			
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
                        <tr>
                            <td colspan='2' style='margin-bottom: -10px; height:18px; font-size:14px; vertical-align:bottom; text-align:center;'>${seed["seedUrl"]}</td>
                        </tr>

                        <tr>
                            <td style='width:50%; text-align:center;'><span style='border-style: inset;'>Live</span></td>
                            <td style='width:50%; text-align:center;'><span style='border-style: inset;'>Harvested</span></td>
                        </tr>

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

                        <tr>
                            <td colspan='2' style='height:8px; background:white;'></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
		</div>
	</c:if>
</div>
