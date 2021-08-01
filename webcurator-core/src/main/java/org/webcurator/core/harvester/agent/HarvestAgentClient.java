package org.webcurator.core.harvester.agent;


import com.google.common.collect.ImmutableMap;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the HarvestAgent Interface using SOAP
 * to communicate with the HarvestAgent
 *
 * @author nwaight
 */
@SuppressWarnings("all")
public class HarvestAgentClient extends AbstractRestClient implements HarvestAgent {
    /**
     * Constructor to initialise the base url and rest template
     *
     * @param baseUrl:             the base url of server
     * @param restTemplateBuilder: the rest template
     */
    public HarvestAgentClient(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }


    /**
     * @see HarvestAgent#initiateHarvest(String, String, String)
     */
    public void initiateHarvest(String job, Map<String, String> params) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.INITIATE_HARVEST));
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uri, params, Void.class);
    }

    public void recoverHarvests(List<String> activeJobs) {
        HttpEntity<String> request = this.createHttpRequestEntity(activeJobs);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(getUrl(HarvestAgentPaths.RECOVER_HARVESTS), request, Void.class);
    }

    /**
     * @see HarvestAgent#restrictBandwidth(String, int)
     */
    public void restrictBandwidth(String job, int bandwidthLimit) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESTRICT_BANDWIDTH))
                .queryParam("bandwidth-limit", bandwidthLimit);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#pause(String)
     */
    public void pause(String job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE));
        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#resume(String)
     */
    public void resume(String job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME));
        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#abort(String)
     */
    public void abort(String job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.ABORT));
        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#stop(String)
     */
    public void stop(String job) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STOP));
        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
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
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.LOAD_SETTINGS));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#pauseAll()
     */
    public void pauseAll() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PAUSE_ALL));

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#resumeAll()
     */
    public void resumeAll() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.RESUME_ALL));

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    /**
     * @see HarvestAgent#getStatus()
     */
    public HarvestAgentStatusDTO getStatus() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.STATUS));

        RestTemplate restTemplate = restTemplateBuilder.build();
        HarvestAgentStatusDTO harvestAgentStatusDTO = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                HarvestAgentStatusDTO.class);
        return harvestAgentStatusDTO;
    }

    /**
     * @see HarvestAgent#getName()
     */
    public String getName() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.NAME));

        RestTemplate restTemplate = restTemplateBuilder.build();
        String name = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(), String.class);
        return name;
    }

    /**
     * @see HarvestAgent#getMemoryWarning()
     */
    public boolean getMemoryWarning() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.MEMORY_WARNING));

        RestTemplate restTemplate = restTemplateBuilder.build();
        Boolean memoryWarning = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(), Boolean.class);
        return memoryWarning;
    }

    /**
     * @see HarvestAgent#setMemoryWarning(boolean memoryWarning)
     */
    public void setMemoryWarning(boolean memoryWarning) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.MEMORY_WARNING))
                .queryParam("memory-warning", memoryWarning);

        // The service itself should throw the error if the call is unsupported (whereas it originally would throw the
        // exception on the client.
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    /**
     * @see HarvestAgent#updateProfileOverrides(String, String)
     */
    public void updateProfileOverrides(String job, String profile) {
        HttpEntity<String> request = new HttpEntity<String>(profile);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.UPDATE_PROFILE_OVERRIDES));

        Map<String, String> pathVariables = ImmutableMap.of("job", job);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                request, Void.class);
    }

    /**
     * @see HarvestAgent#purgeAbortedTargetInstances(List<String>)
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) {
        HttpEntity<String> request = this.createHttpRequestEntity(targetInstanceNames);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.PURGE_ABORTED_TARGET_INSTANCES));

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Void.class);
    }

    public boolean isValidProfile(String profile) {
        HttpEntity<String> request = new HttpEntity<String>(profile);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.IS_VALID_PROFILE));

        RestTemplate restTemplate = restTemplateBuilder.build();
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request,
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
        HttpEntity<String> request = new HttpEntity<String>(shellScript);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestAgentPaths.EXECUTE_SHELL_SCRIPT))
                .queryParam("engine", engine);

        Map<String, String> pathVariables = ImmutableMap.of("job-name", jobName);

        RestTemplate restTemplate = restTemplateBuilder.build();
        HarvestAgentScriptResult harvestAgentScriptResult = restTemplate.postForObject(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                request, HarvestAgentScriptResult.class);
        return harvestAgentScriptResult;
    }
}
