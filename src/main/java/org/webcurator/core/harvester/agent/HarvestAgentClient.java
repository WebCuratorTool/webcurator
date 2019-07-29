package org.webcurator.core.harvester.agent;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.rest.RestClientResponseHandler;
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

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor to initialise the host, port and service.
     *
     * @param host the name of the host
     * @param port the port number
     */
    public HarvestAgentClient(String host, int port) {
        this.host = host;
        this.port = port;
        restTemplateBuilder.errorHandler(new RestClientResponseHandler());
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
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.INITIATE_HARVEST))
                .queryParam("profile", profile)
                .queryParam("seeds", seeds);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    public void recoverHarvests(List<String> activeJobs) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RECOVER_HARVESTS))
                .queryParam("active-jobs", activeJobs);

        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#restrictBandwidth(String, int)
     */
    public void restrictBandwidth(String job, int bandwidthLimit) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESTRICT_BANDWIDTH))
                .queryParam("bandwidth-limit", bandwidthLimit);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#pause(String)
     */
    public void pause(String job) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#resume(String)
     */
    public void resume(String job) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#abort(String)
     */
    public void abort(String job) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.ABORT));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#stop(String)
     */
    public void stop(String job) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STOP));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
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
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.LOAD_SETTINGS));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#pauseAll()
     */
    public void pauseAll() {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE_ALL));

        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#resumeAll()
     */
    public void resumeAll() {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME_ALL));

        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#getStatus()
     */
    public HarvestAgentStatusDTO getStatus() {
        RestTemplate restTemplate = restTemplateBuilder.build();;

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STATUS));

        HarvestAgentStatusDTO harvestAgentStatusDTO = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                HarvestAgentStatusDTO.class);

        return harvestAgentStatusDTO;
    }

    /**
     * @see HarvestAgent#getName()
     */
    public String getName() {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.NAME));

        String name = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(), String.class);

        return name;
    }

    /**
     * @see HarvestAgent#getMemoryWarning()
     */
    public boolean getMemoryWarning() {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.MEMORY_WARNING));

        Boolean memoryWarning = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(), Boolean.class);

        return memoryWarning;
    }

    /**
     * @see HarvestAgent#setMemoryWarning(boolean memoryWarning)
     */
    public void setMemoryWarning(boolean memoryWarning) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.MEMORY_WARNING))
                .queryParam("memory-warning", memoryWarning);

        // The service itself should throw the error if the call is unsupported (whereas it originally would throw the
        // exception on the client.
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#updateProfileOverrides(String, String)
     */
    public void updateProfileOverrides(String job, String profile) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.UPDATE_PROFILE_OVERRIDES))
                .queryParam("profile", profile);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#purgeAbortedTargetInstances(List<String>)
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PURGE_ABORTED_TARGET_INSTANCES))
                .queryParam("target-instance-names", targetInstanceNames);

        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    public boolean isValidProfile(String profile) {
        RestTemplate restTemplate = restTemplateBuilder.build();;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.IS_VALID_PROFILE));

        Map<String, String> pathVariables = ImmutableMap.of("profile", profile);
        Boolean result = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                Boolean.class);

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
        RestTemplate restTemplate = restTemplateBuilder.build();;

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.EXECUTE_SHELL_SCRIPT))
                .queryParam("engine", engine)
                .queryParam("shell-script", shellScript);

        Map<String, String> pathVariables = ImmutableMap.of("job-name", jobName);
        HarvestAgentScriptResult harvestAgentScriptResult = restTemplate.postForObject(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, HarvestAgentScriptResult.class);

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
