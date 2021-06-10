/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.reader;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * Log Reader client for communicating with a remote log reader.
 */
public class LogReaderClient extends AbstractRestClient implements LogReader {
    /**
     * Constructor to initialise the base url and rest template
     *
     * @param baseUrl:             the base url of server
     * @param restTemplateBuilder: the rest template
     */
    public LogReaderClient(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
       super(baseUrl, restTemplateBuilder);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFiles(java.lang.String)
     */
    public List<String> listLogFiles(String job) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<List<String>> listResponse = restTemplate.exchange(getUrl(LogReaderPaths.LOG_FILE),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() { });
        List<String> logFiles = listResponse.getBody();

        return logFiles;
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFileAttributes(java.lang.String)
     */
    public List<LogFilePropertiesDTO> listLogFileAttributes(String job) {
        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_PROPERTIES));

        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<List<LogFilePropertiesDTO>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUriString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<LogFilePropertiesDTO>>() { },
                pathVariables);

        return listResponse.getBody();
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#tail(java.lang.String, java.lang.String, int)
     */
    public List<String> tail(String job, String filename, int numberOfLines) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_TAIL))
                .queryParam("filename", filename)
                .queryParam("number-of-lines", numberOfLines);

        return getStringList(job, uriComponentsBuilder);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#countLines(java.lang.String, java.lang.String)
     */
    public Integer countLines(String job, String filename) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_LINE_COUNT))
                .queryParam("filename", filename);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Integer lineCount = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                Integer.class);

        return lineCount;
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#get(java.lang.String, java.lang.String, int, int)
     */
    public List<String> get(String job, String filename, int startLine, int numberOfLines) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_JOB))
                .queryParam("filename", filename)
                .queryParam("start-line", startLine)
                .queryParam("number-of-lines", numberOfLines);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        ResponseEntity<List<String>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() { });
        List<String> getLines = listResponse.getBody();

        return getLines;
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getHopPath(java.lang.String, java.lang.String, java.lang.String)
     */
    public List<String> getHopPath(String job, String resultOid, String filename, String url) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_HOP_PATH))
                .queryParam("filename", filename)
                .queryParam("result-oid", resultOid)
                .queryParam("url", url);

        return getStringList(job, uriComponentsBuilder);
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineBeginning(String, String, String). 
     */
    public Integer findFirstLineBeginning(String job, String filename, String match) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_FIRST_LINE_BEGINNING))
                .queryParam("filename", filename)
                .queryParam("match", match);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Integer firstLine = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                Integer.class);

        return firstLine;
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineContaining(String, String, String). 
     */
    public Integer findFirstLineContaining(String job, String filename, String match) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_FIRST_LINE_CONTAINING_MATCH))
                .queryParam("filename", filename)
                .queryParam("match", match);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Integer firstLine = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                Integer.class);

        return firstLine;
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineAfterTimeStamp(String, String, Long). 
     */
    public Integer findFirstLineAfterTimeStamp(String job, String filename, Long timestamp) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_FIRST_LINE_AFTER_TIMESTAMP))
                .queryParam("filename", filename)
                .queryParam("timestamp", timestamp);

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        Integer firstLine = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                Integer.class);

        return firstLine;
    }
    
    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getByRegularExpression(String, String, String, String, boolean, int, int)
     */
    public List<String> getByRegularExpression(String job, String filename, String regularExpression, String addLines,
                                               boolean prependLineNumbers, int skipFirstMatches, int numberOfMatches) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_BY_REGULAR_EXPRESSION))
                .queryParam("filename", filename)
                .queryParam("regular-expression", regularExpression)
                .queryParam("add-lines", addLines)
                .queryParam("prepend-line-numbers", prependLineNumbers)
                .queryParam("skip-first-matches", skipFirstMatches)
                .queryParam("number-of-matches", numberOfMatches);

        return getStringList(job, uriComponentsBuilder);
    }

    private List<String> getStringList(String job, UriComponentsBuilder uriComponentsBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        Map<String, String> pathVariables = ImmutableMap.of("job", job);
        ResponseEntity<List<String>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });

        return listResponse.getBody();
    }

    public File retrieveLogfile(String job, String filename) {
         try {
            Map<String, String> pathVariables = ImmutableMap.of("job", job);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(LogReaderPaths.LOG_FILE_RETRIEVE_LOG_FILE)).queryParam("filename", filename);
            URL url = uriComponentsBuilder.buildAndExpand(pathVariables).toUri().toURL();

            File file = File.createTempFile("wct", "tmp");
            long bytes = WctUtils.copy(url.openStream(),new FileOutputStream(file));
            if(bytes > 0){
                return file;
            }
            return null;
        } catch (IOException ex) {
            throw new WCTRuntimeException("Failed to retrieve logfile " + filename + " for " + job + ": " + ex.getMessage(), ex);
        }
    }

    public File retrieveAQAFile(String job, String filename) {
        // TODO This does not seem to be implemented anywhere as a service.
        throw new WCTRuntimeException("Failed to retrieve logfile " + filename + " for " + job + ": NOT IMPLEMENTED");
    }
}
