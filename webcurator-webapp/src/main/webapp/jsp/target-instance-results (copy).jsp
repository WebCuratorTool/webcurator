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

<div id="annotationsBox" style="width: 100%; height: 60vh;">
	  <iframe src='/spa/tools/visualization.html?targetInstanceOid="${instance.oid}"' style='width: 100%; height: 100%; margin:0; padding:0; border:0;'></iframe>
</div>