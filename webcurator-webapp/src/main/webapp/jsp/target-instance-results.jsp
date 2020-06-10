<%@taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="authority" uri="http://www.webcurator.org/authority"  %>
<%@taglib prefix="wct" uri="http://www.webcurator.org/wct" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@page import="org.webcurator.ui.target.command.TargetInstanceCommand" %>
<%@page import="org.webcurator.domain.model.auth.Privilege"%>

<input type="hidden" name="<%=TargetInstanceCommand.PARAM_OID%>" value="<c:out value="${command.targetInstanceId}"/>"/>
<input type="hidden" name="<%=TargetInstanceCommand.PARAM_CMD%>" value="<c:out value="${command.cmd}"/>"/>
<input type="hidden" name="<%=TargetInstanceCommand.PARAM_HR_ID%>" value=""/>
<input type="hidden" name="<%=TargetInstanceCommand.PARAM_REJREASON_ID%>" value=""/>

<div id='html-select-tag' style='display: none;'>
    <a href="#" onclick="rejectHarvest();">Reject for Reason:</a>&nbsp;
    <select name="<%=TargetInstanceCommand.PARAM_REASON%>">
        <c:forEach items="${reasons}" var="o">
            <option value="<c:out value="${o.oid}"/>"><c:out value="${o.name}"/></option>
        </c:forEach>
    </select>&nbsp;
</div>

<script>
  function clickReIndex(hrOid) {
    if( confirm('<spring:message code="ui.label.targetinstance.results.confirmReIndex" javaScriptEscape="true"/>')) {
  	  document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_REINDEX%>'; 
	  document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid; 
	  document.forms['tabForm'].submit(); 
	}
	return false;
  }
  
  function clickReject(hrOid) {
    if( confirm('<spring:message code="ui.label.targetinstance.results.confirmReject" javaScriptEscape="true"/>')) {
  	  document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_REJECT%>'; 
	  document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid; 
	  document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_REJREASON_ID%>.value=document.getElementsByName('<%=TargetInstanceCommand.PARAM_REASON%>'+hrOid)[0].value; 
	  document.forms['tabForm'].submit(); 
	}
	return false;
  }
  
  function clickEndorse(hrOid) { 
    if( confirm('<spring:message code="ui.label.targetinstance.results.confirmEndorse" javaScriptEscape="true"/>')) {
      document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_ENDORSE%>'; 
      document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid; 
      document.forms['tabForm'].submit(); 
    }
    return false;
  }
  
  function clickUnEndorse(hrOid) { 
    if( confirm('<spring:message code="ui.label.targetinstance.results.confirmUnEndorse" javaScriptEscape="true"/>')) {
      document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_UNENDORSE%>'; 
      document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid; 
      document.forms['tabForm'].submit(); 
    }
    return false;
  }

    function clickPause(hrOid){
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_PAUSE%>';
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid;
        document.forms['tabForm'].submit();
    }

    function clickResume(hrOid){
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_RESUME%>';
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid;
        document.forms['tabForm'].submit();
    }

    function clickAbort(hrOid){
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_ABORT%>';
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid;
        document.forms['tabForm'].submit();
    }

    function clickStop(hrOid){
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_STOP%>';
        document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid;
        document.forms['tabForm'].submit();
    }

    function clickDelete(hrOid){
        if( confirm('<spring:message code="ui.label.targetinstance.results.confirmDelete" javaScriptEscape="true"/>')) {
            document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_CMD%>.value='<%=TargetInstanceCommand.ACTION_DELETE%>';
            document.forms['tabForm'].<%=TargetInstanceCommand.PARAM_HR_ID%>.value=hrOid;
            document.forms['tabForm'].submit();
        }
    }

  //When viewing this target instance from the "Harvest history" screen, clicking on "review" will 
  //cause the "Return to Harvest History Page" to forget which target instance was originally being
  //reviewed.  Warn the user to try and prevent mistakes.
  function checkForHistory() {
	//Loaded from JSP
	var historyInSession = <%= session.getAttribute( "sessionHarvestHistoryTIOid" )!= "" && session.getAttribute( "sessionHarvestHistoryTIOid" )!= null%>;
	if(historyInSession===true) {
	  return confirm("WARNING: As you were previously reviewing another target instance from the Harvest History screen,"
	  	+ " reviewing this target instance will cause WCT to forget the previous target instance being reviewed.  Please"
	  	+ " carefully check the target instance ID when endorsing or rejecting.");
	}
	return true;
  }

</script>

<div id="annotationsBox">
	<table width="100%" cellpadding="3" cellspacing="0" border="0">
		<tr>
			<td class="annotationsHeaderRow">No.</td>
			<td class="annotationsHeaderRow">Date</td>
			<td class="annotationsHeaderRow">Derived From</td>
			<td class="annotationsHeaderRow">User</td>
			<td class="annotationsHeaderRow">Notes</td>
			<td class="annotationsHeaderRow">State</td>
			<td class="annotationsHeaderRow">Action</td>
		</tr>
		<c:choose>
			<c:when test="${empty results}">
			<tr> 
			<td class="annotationsLiteRow" colspan="6"><spring:message code="ui.label.targetinstance.results.noResults"/></td>
			</tr>
			</c:when>
			<c:otherwise>
				<c:forEach var="hr" items="${results}">
				  <tr>
				    <td class="annotationsLiteRow"><c:out value="${hr.harvestNumber}"/></td>
				    <td class="annotationsLiteRow"><wct:date value="${hr.creationDate}" type="fullDateTime"/></td>
   				    <td class="annotationsLiteRow"><c:out value="${hr.derivedFrom}"/></td>
				    <td class="annotationsLiteRow"><c:out value="${hr.createdBy.niceName}"/></td>
				    <td class="annotationsLiteRow"><c:out value="${hr.provenanceNote}"/></td>
				    <td class="annotationsLiteRow">
				    ${hr.state}
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
				    <td class="annotationsLiteRow">
				    <c:if test="${editMode && hr.state != 4}">
				    	<c:choose>
				    		<c:when test="${hr.state eq 3}"> <!-- Indexing -->
				    		<authority:hasPrivilege privilege="<%=Privilege.ENDORSE_HARVEST%>" scope="<%=Privilege.SCOPE_OWNER%>">    		
					    		<a href="#" onclick="javascript: return clickReIndex(<c:out value="${hr.oid}"/>);">Restart Indexing</a>
					    		<c:choose>
						    		<c:when test="${wct:xHoursElapsed(12,hr.creationDate)}">
								    	&nbsp;|&nbsp;<a href="#" onclick="javascript: return clickReject(<c:out value="${hr.oid}"/>); ">Reject&nbsp;(Stuck&nbsp;in&nbsp;Indexing&nbsp;State):</a>&nbsp;
						    			<select name="<%=TargetInstanceCommand.PARAM_REASON%><c:out value="${hr.oid}"/>">				
											<c:forEach items="${reasons}" var="o">
												<option value="<c:out value="${o.oid}"/>"><c:out value="${o.name}"/></option>
											</c:forEach>
										</select>&nbsp;    			
						    		</c:when>
					    		</c:choose>
					    	</authority:hasPrivilege>
					    	&nbsp;
				    		</c:when>
				    		
				    		<c:when test="${(instance.state eq 'Harvested' && hr.state != 3) || (instance.state eq 'Patching' && hr.state != 3 && hr.state < 5)}">
				    		<authority:hasPrivilege privilege="<%=Privilege.ENDORSE_HARVEST%>" scope="<%=Privilege.SCOPE_OWNER%>">
					    	<a href="curator/target/quality-review-toc.html?targetInstanceOid=${hr.targetInstance.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}" onclick="return checkForHistory()">Review</a>
					    	&nbsp;|&nbsp;
					    	<a href="#" onclick="javascript: return clickEndorse(<c:out value="${hr.oid}"/>);">Endorse</a>
					    	&nbsp;|&nbsp;
					    	<c:choose>
					    		<c:when test="${hr.state eq 2}">
					    		Rejection&nbsp;Reason:&nbsp;<c:out value="${hr.rejReason.name}"/>    		
					    		</c:when>
					    		<c:otherwise>
							    	<a href="#" onclick="javascript: return clickReject(<c:out value="${hr.oid}"/>); ">Reject&nbsp;for&nbsp;Reason:</a>&nbsp;
					    			<select name="<%=TargetInstanceCommand.PARAM_REASON%><c:out value="${hr.oid}"/>">				
										<c:forEach items="${reasons}" var="o">
											<option value="<c:out value="${o.oid}"/>"><c:out value="${o.name}"/></option>
										</c:forEach>
									</select>&nbsp;    			
					    		</c:otherwise>
					    	</c:choose>
					    	</authority:hasPrivilege>
					    	&nbsp;
				    		</c:when>
				    		
				    		<c:when test="${(instance.state eq 'Endorsed' || instance.state eq 'Rejected') && hr.state != 3}">    		   		    		
                                <authority:hasPrivilege privilege="<%=Privilege.ARCHIVE_HARVEST%>" scope="<%=Privilege.SCOPE_OWNER%>">
                                <c:if test="${hr.state == 1}">
                                <c:choose>
                                <c:when test="${customDepositFormRequired}">
                                <a href="curator/target/deposit-form-envelope.html?targetInstanceID=<c:out value="${hr.targetInstance.oid}"/>&harvestResultNumber=<c:out value="${hr.harvestNumber}"/>">Next</a>
                                </c:when>
                                <c:otherwise>
                                <a href="curator/archive/submit.html?instanceID=<c:out value="${hr.targetInstance.oid}"/>&harvestNumber=<c:out value="${hr.harvestNumber}"/>" onclick="return confirm('<spring:message code="ui.label.targetinstance.results.confirmSubmit" javaScriptEscape="true"/>');">Submit to Archive</a>
                                </c:otherwise>
                                </c:choose>
                                </c:if>
                                </authority:hasPrivilege>
                                <authority:hasPrivilege privilege="<%=Privilege.UNENDORSE_HARVEST%>" scope="<%=Privilege.SCOPE_OWNER%>">
                                <c:if test="${hr.state == 1}">
                                &nbsp;|&nbsp;
                                <a href="#" onclick="javascript: return clickUnEndorse(<c:out value="${hr.oid}"/>);">UnEndorse</a>
                                </c:if>
                                <c:if test="${hr.state eq 2}">
                                Rejection&nbsp;Reason:&nbsp;<c:out value="${hr.rejReason.name}"/>
                                </c:if>
                                </authority:hasPrivilege>
				    		</c:when>

                            <c:when test="${instance.state eq 'Patching'}">
                                <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                <a href="curator/target/patching-view-hr.html?targetInstanceOid=${hr.targetInstance.oid}&harvestResultId=${hr.oid}&harvestNumber=${hr.harvestNumber}" onclick="return checkForHistory()"><img src="images/action-icon-view.gif" title="View" alt="click here to VIEW this item" width="15" height="19" border="0"></a>

                                <authority:hasPrivilege privilege="<%=Privilege.LAUNCH_TARGET_INSTANCE_IMMEDIATE%>" scope="<%=Privilege.SCOPE_AGENCY%>">
                                    <c:if test="${hr.state == 50 }">
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <a href="curator/target/ti-harvest-now.html?targetInstanceId=${instance.oid}&harvestResultId=${hr.oid}"><img src="images/resume-icon.gif" title="Harvest Now" alt="click here to Harvest this item" width="21" height="20" border="0"></a>
                                    </c:if>
                                </authority:hasPrivilege>

                                <authority:showControl ownedObject="${instance}" privileges='<%=Privilege.MANAGE_TARGET_INSTANCES + ";" + Privilege.MANAGE_WEB_HARVESTER%>' editMode="true">
                                    <authority:show>
                                    <c:if test="${hr.state == 50 || hr.state == 62 || hr.state == 72}">
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <input type="image" src="images/action-icon-delete.gif" title="Delete" alt="click here to DELETE this item" width="18" height="19" border="0" onclick='javascript: clickDelete("${hr.oid}");'/>
                                    </c:if>
                                    </authority:show>
                                </authority:showControl>

                                <authority:hasPrivilege privilege="<%=Privilege.MANAGE_WEB_HARVESTER%>" scope="<%=Privilege.SCOPE_AGENCY%>">
                                    <c:if test="${hr.state == 60 || hr.state == 70 }">
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <input type="image" src="images/pause-icon.gif" title="Pause" alt="click here to Pause this item" width="21" height="20" border="0" onclick='javascript: clickPause("${hr.oid}");'/>
                                    </c:if>
                                    <c:if test="${hr.state == 61 || hr.state == 71 }">
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <input type="image" src="images/resume-icon.gif" title="Resume" alt="click here to Resume this item" width="21" height="20" border="0" onclick='javascript: clickResume("${hr.oid}");'/>
                                    </c:if>
                                    <c:if test="${hr.state == 60 || hr.state == 61 || hr.state == 62}">
                                        <!--
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <input type="image" src="images/abort-icon.gif" title="Abort" alt="click here to Abort this item" width="21" height="20" border="0" onclick='javascript: clickAbort("${hr.oid}");'/>
                                        -->
                                    </c:if>
                                    <c:if test="${hr.state == 60 || hr.state == 70 || hr.state == 61 || hr.state == 71}">
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <input type="image" src="images/stop-icon.gif" title="Stop" alt="click here to Stop this item" width="21" height="20" border="0" onclick='javascript: clickStop("${hr.oid}");'/>
                                    </c:if>
                                    <c:if test="${(hr.state == 60 || hr.state == 61 || hr.state == 62) && instance.profile.isHeritrix3Profile()}">
                                       <!--
                                        <img src="images/action-sep-line.gif" alt="" width="7" height="19" border="0" />
                                        <a href="javascript:viewH3ScriptConsole(${instance.oid});" title="View"><img src="images/h3-script-console.png" title="H3 Script Console" alt="click here to Open H3 Script Console" width="21" height="20" border="0"></a>
                                        -->
                                    </c:if>
                                </authority:hasPrivilege>
                            </c:when>
                            <c:otherwise>
                            &nbsp;
                            </c:otherwise>
				    	</c:choose>
				    </c:if>
				    <c:if test="${!editMode}">
				    &nbsp;
					    		<c:if test="${hr.state eq 2}">
					    		Rejection&nbsp;Reason:&nbsp;<c:out value="${hr.rejReason.name}"/>    		
					    		</c:if>
					    		
				    </c:if>
				    </td>
				  </tr>
			  </c:forEach>
		</c:otherwise>
		</c:choose>
	</table>
</div>

<script src="spa/dist/jquery/jquery-3.4.1.min.js"></script>
<script src="spa/dist/bootstrap/bootstrap.bundle.min.js"></script>
<script src="spa/dist/adminlte/js/adminlte.js"></script>
<script src="spa/dist/jquery/menu/jquery.contextMenu.js"></script>
<script src="spa/dist/jquery/menu/jquery.ui.position.js"></script>
<script src="spa/dist/ag-grid/ag-grid-community.js"></script>
<script src="spa/tools/visualization.js"></script>

<script>

</script>