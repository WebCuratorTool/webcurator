package org.webcurator.ui.target.controller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/target/annotation-ajax.html")
public class AnnotationAjaxController {

    /** The manager to use to access the target instance. */
    @Autowired
    private TargetInstanceManager targetInstanceManager;
    /** The manager to use to access the target. */
    @Autowired
    private TargetManager targetManager;
    /** the logger. */
    private Log log;

    /** Default constructor. */
    public AnnotationAjaxController() {
        super();
        log = LogFactory.getLog(getClass());
    }

	@PostMapping
	protected ModelAndView processFormSubmission(@RequestParam("targetOid") Long targetOid,
												 @RequestParam("targetInstanceOid") Long targetInstanceOid,
												 @RequestParam("ajax_request_type") String ajaxRequest,
												 HttpServletRequest request) throws Exception {
//		String ajaxRequest = request.getParameter(Constants.AJAX_REQUEST_TYPE);
		if (ajaxRequest.equals(Constants.AJAX_REQUEST_FOR_TI_ANNOTATIONS)) {
			return processTargetInstanceRequest(request, targetOid, targetInstanceOid);
		}
		if (ajaxRequest.equals(Constants.AJAX_REQUEST_FOR_TARGET_ANNOTATIONS)) {
			return processTargetRequest(targetOid);
		}
		return null;
	}

	private ModelAndView processTargetInstanceRequest(HttpServletRequest request, Long targetOid, Long targetInstanceOid)
			throws Exception {
		TargetInstanceCommand searchCommand = (TargetInstanceCommand) request.getSession().getAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);

		// ensure that the seachCommand is valid
		if (searchCommand == null) return null;

        TargetInstanceCriteria criteria = new TargetInstanceCriteria();
        criteria.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATE_DESC_BY_TARGET_OID);
        criteria.setTargetSearchOid(targetOid);
        criteria.setSearchOid(targetInstanceOid);
		User user = AuthUtil.getRemoteUserObject();
    	criteria.setAgency(user.getAgency().getName());
    	searchCommand.setAgency(user.getAgency().getName());

        Set<String> states = new HashSet<String>();
		states.add(TargetInstance.STATE_HARVESTED);
		states.add(TargetInstance.STATE_ENDORSED);
		states.add(TargetInstance.STATE_ARCHIVED);
		states.add(TargetInstance.STATE_REJECTED);
		criteria.setStates(states);

		Pagination instances = targetInstanceManager.search(criteria, 0, 3);

		ModelAndView mav = new ModelAndView(Constants.VIEW_TI_ANNOTATION_HISTORY);

		// the annotations are not recovered by hibernate during the ti fetch so we need to add them
		if ( instances != null ) {
			for (Iterator<TargetInstance> i = ((List<TargetInstance>) instances.getList()).iterator( ); i.hasNext(); ) {
				TargetInstance ti = i.next();
				ti.setAnnotations(targetInstanceManager.getAnnotations(ti));
			}
			mav.addObject(TargetInstanceCommand.MDL_INSTANCES, instances);
	        mav.addObject(Constants.GBL_CMD_DATA, searchCommand);
	        request.getSession().setAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA, searchCommand);
	        mav.addObject("instances", instances.getList().size());
		}

		instances = null;

		return mav;

	}

	private ModelAndView processTargetRequest(Long targetOid) throws Exception {
		Target target = targetManager.load(targetOid, true);
		// the annotations are not recovered by hibernate during the target fetch so we need to add them
		target.setAnnotations(targetManager.getAnnotations(target));
		ModelAndView mav = new ModelAndView(Constants.VIEW_TARGET_ANNOTATION_HISTORY);
		mav.addObject(TargetInstanceCommand.TYPE_TARGET, target);
		return mav;
	}

	@GetMapping
	protected ModelAndView showForm(@RequestParam("targetOid") Long targetOid,
									@RequestParam("targetInstanceOid") Long targetInstanceOid,
									@RequestParam("ajax_request_type") String ajaxRequest,
									HttpServletRequest request) throws Exception {

//		String ajaxRequest = request.getParameter(Constants.AJAX_REQUEST_TYPE);
		if (ajaxRequest.equals(Constants.AJAX_REQUEST_FOR_TI_ANNOTATIONS)) {
			return processTargetInstanceRequest(request, targetOid, targetInstanceOid);
		}
		if (ajaxRequest.equals(Constants.AJAX_REQUEST_FOR_TARGET_ANNOTATIONS)) {
			return processTargetRequest(targetOid);
		}
		return null;

	}

    /**
     * @param aTargetInstanceManager The targetInstanceManager to set.
     */
    public void setTargetInstanceManager(TargetInstanceManager aTargetInstanceManager) {
        targetInstanceManager = aTargetInstanceManager;
    }

	/**
	 * @param targetManager The targetManager to set.
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}
}
