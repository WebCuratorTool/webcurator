package org.webcurator.core.harvester.agent;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.List;
import java.util.Map;

/**
 * An implementation of the HarvestAgent Interface using SOAP
 * to communicate with the HarvestAgent
 *
 * @author nwaight
 */
public class HarvestAgentClient implements HarvestAgent, HarvestAgentConfig {

    /**
     * the logger.
     */
    private static Log log = LogFactory.getLog(HarvestAgentClient.class);
    /**
     * the host name or ip-address for the harvest agent.
     */
    private String host = "localhost";
    /**
     * the port number for the harvest agent.
     */
    private int port = 8080;

    /**
     * Constructor to initialise the host, port and service.
     *
     * @param host the name of the host
     * @param port the port number
     */
    public HarvestAgentClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String baseUrl() {
        return host + ":" + port;
    }

    public String getUrl(String appendUrl) {
        return baseUrl() + appendUrl;
    }

    /**
     * @see HarvestAgent#initiateHarvest(String, String, String)
     */
    public void initiateHarvest(String job, String profile, String seeds) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.INITIATE_HARVEST))
                .queryParam("profile", profile)
                .queryParam("seeds", seeds);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public void recoverHarvests(List<String> activeJobs) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RECOVER_HARVESTS))
                .queryParam("active-jobs", activeJobs);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#restrictBandwidth(String, int)
     */
    public void restrictBandwidth(String job, int bandwidthLimit) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESTRICT_BANDWIDTH))
                .queryParam("bandwidth-limit", bandwidthLimit);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#pause(String)
     */
    public void pause(String job) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#resume(String)
     */
    public void resume(String job) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#abort(String)
     */
    public void abort(String job) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.ABORT));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#stop(String)
     */
    public void stop(String job) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STOP));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#completeHarvest(String, int)
     */
    public int completeHarvest(String job, int failureStep) {
        throw new WCTRuntimeException("completeHarvest is not supported from the client");
    }

    /**
     * @see HarvestAgent#loadSettings(String)
     */
    public void loadSettings(String job) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.LOAD_SETTINGS));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#pauseAll()
     */
    public void pauseAll() {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE_ALL));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#resumeAll()
     */
    public void resumeAll() {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME_ALL));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#getStatus()
     */
    public HarvestAgentStatusDTO getStatus() {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STATUS));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        HarvestAgentStatusDTO harvestAgentStatusDTO = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                HarvestAgentStatusDTO.class);

        return harvestAgentStatusDTO;
    }

    /**
     * @see HarvestAgent#getName()
     */
    public String getName() {
        return null;
    }

    /**
     * @see HarvestAgent#getMemoryWarning()
     */
    public boolean getMemoryWarning() {
        return getStatus().getMemoryWarning();
    }

    /**
     * @see HarvestAgent#setMemoryWarning(boolean memoryWarning)
     */
    public void setMemoryWarning(boolean memoryWarning) {
        if (log.isErrorEnabled()) {
            log.error("Attempt to call unsupported method setMemoryWarning()");
        }
    }

    /**
     * @see HarvestAgent#updateProfileOverrides(String, String)
     */
    public void updateProfileOverrides(String job, String profile) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.UPDATE_PROFILE_OVERRIDES))
                .queryParam("profile", profile);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#purgeAbortedTargetInstances(List<String>)
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PURGE_ABORTED_TARGET_INSTANCES))
                .queryParam("target-instance-names", targetInstanceNames);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    public boolean isValidProfile(String profile) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.IS_VALID_PROFILE));

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("profile", profile);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);

        return result;
    }

    /**
     * Execute the shell script in the Heritrix3 server for the job.
     *
     * @param jobName     the job
     * @param engine      the script engine: beanshell, groovy, or nashorn (ECMAScript)
     * @param shellScript the script to execute
     * @return the script result
     */
    public HarvestAgentScriptResult executeShellScript(String jobName, String engine, String shellScript) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STATUS))
                .queryParam("engine", engine)
                .queryParam("shell-script", shellScript);

        // TODO Process any exceptions or 404s, etc. as WCTRuntimeException
        Map<String, String> pathVariables = ImmutableMap.of("job-name", jobName);
        HarvestAgentScriptResult harvestAgentScriptResult = restTemplate.getForObject(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HarvestAgentScriptResult.class);

        return harvestAgentScriptResult;
    }

    public String getHost() {
        return host;
    }

    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
}
