package org.webcurator.core.visualization.browser;

import org.apache.commons.httpclient.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStorePaths;

import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.List;

@RestController
public class VisWayBackController implements VisWayBackService {
    private static final Logger log = LoggerFactory.getLogger(VisWayBackController.class);

    @Autowired
    private VisWayBackClient visWayBackClient;

    @Override
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) {
        try {
            return visWayBackClient.getResource(targetInstanceId, harvestResultNumber, resourceUrl);
        } catch (DigitalAssetStoreException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    @PostMapping(path = DigitalAssetStorePaths.SMALL_RESOURCE)
    public byte[] getSmallResourceExternal(@PathVariable(value = "target-instance-id") long targetInstanceId,
                                           @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                           @RequestParam(value = "resource-url") String resourceUrl) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-id: {}, harvest-result-number: {}, resource-url: {}", targetInstanceId, harvestResultNumber, resourceUrl);
        return getSmallResource(targetInstanceId, harvestResultNumber, URLDecoder.decode(resourceUrl));
    }

    @Override
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        return visWayBackClient.getSmallResource(targetInstanceId, harvestResultNumber, resourceUrl);
    }

    @PostMapping(path = DigitalAssetStorePaths.HEADERS)
    public List<Header> getHeadersExternal(@PathVariable(value = "target-instance-id") long targetInstanceId,
                                           @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                           @RequestParam(value = "resource-url") String resourceUrl) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-id: {}, harvest-result-number: {}, resource-url: {}", targetInstanceId, harvestResultNumber, resourceUrl);
        return getHeaders(targetInstanceId, harvestResultNumber, URLDecoder.decode(resourceUrl));
    }

    @Override
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        return visWayBackClient.getHeaders(targetInstanceId, harvestResultNumber, resourceUrl);
    }
}
