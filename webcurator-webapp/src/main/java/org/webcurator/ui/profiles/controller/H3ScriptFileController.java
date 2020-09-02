package org.webcurator.ui.profiles.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.TargetInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class H3ScriptFileController {
    /** The profile manager to load the profile */
    @Autowired
    private TargetInstanceManager targetInstanceManager;
    /** The authority manager for checking permissions */
    @Autowired
    private AuthorityManager authorityManager;
    /**
     * The name of the h3 scripts directory.
     */
    @Value("${h3.scriptsDirectory}")
    private String h3ScriptsDirectory;

    /** Logger for the H3ScriptFileController. **/
    private static Log log = LogFactory.getLog(H3ScriptFileController.class);

    @RequestMapping(value = "/curator/target/h3ScriptFile.html", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // get target instance oid and script file name
        String targetInstanceOid = httpServletRequest.getParameter("targetInstanceOid");
        String scriptFileName = httpServletRequest.getParameter("scriptFileName"); // includes file extension
        if ((targetInstanceOid == null || targetInstanceOid.equals(""))
                || (scriptFileName == null || scriptFileName.equals(""))) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            try {
                // check authority
                TargetInstance ti = targetInstanceManager.getTargetInstance(Long.parseLong(targetInstanceOid), true);
                if (authorityManager.hasAtLeastOnePrivilege(ti.getProfile(), new String[] {Privilege.MANAGE_TARGET_INSTANCES, Privilege.MANAGE_WEB_HARVESTER})) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    httpServletResponse.setContentType("text/plain");
                    PrintWriter pw = httpServletResponse.getWriter();
                    String fileContents = getFileContents(scriptFileName);
                    pw.println(fileContents);
                    pw.flush();
                } else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (Exception e) {
                log.error(e);
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return null;
    }

    private String getFileContents(String scriptFileName) {
        String script = "";
        try {
            File file = new File(h3ScriptsDirectory + File.separator + scriptFileName);
            script = FileUtils.readFileToString(file);
        } catch (IOException e) {
            log.error(e);
        }
        return script;
    }

    /**
     * @param targetInstanceManager The targetInstanceManager to set.
     */
    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }

    /**
     * @param authorityManager The authorityManager to set.
     */
    public void setAuthorityManager(AuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }

    /**
     * @param h3ScriptsDirectory The h3ScriptsDirectory to set.
     */
    public void setH3ScriptsDirectory(String h3ScriptsDirectory) {
        this.h3ScriptsDirectory = h3ScriptsDirectory;
    }
}
