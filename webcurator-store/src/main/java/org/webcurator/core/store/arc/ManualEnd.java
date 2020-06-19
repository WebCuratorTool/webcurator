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
package org.webcurator.core.store.arc;

import java.time.Duration;
import java.util.Properties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.RestClientResponseHandler;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class ManualEnd {
	
	public static class CommandLine {
		Properties props = new Properties();
		
		public CommandLine(String[] args) { 
			for(int i=0;i < args.length; i+= 2 ) {
				props.put(args[i].substring(1), args[i+1]);
			}
		}
		
		public String getArg(String key) {
			return props.getProperty(key);
		}
	}


    private static void harvestComplete(String host, int port, HarvestResultDTO harvestResultDTO) {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplateBuilder.errorHandler(new RestClientResponseHandler())
                .setConnectTimeout(Duration.ofSeconds(15L));

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(host, port, HarvestCoordinatorPaths.HARVEST_COMPLETE))
                .queryParam("harvest-result", harvestResultDTO);

        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Void.class);
    }

    private static void syntax() {
    	System.out.println("Syntax: ");
    	System.out.println(" -ti tiOid -hrnum 1 -host hostname -port portnumber -compressed [true|false] -baseDir basedir -ext extension");
    	System.exit(1);
	}

    public static String baseUrl(String host, int port) {
        return host + ":" + port;
    }

    public static String getUrl(String host, int port, String appendUrl) {
        return baseUrl(host, port) + appendUrl;
    }
}
