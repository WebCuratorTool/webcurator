package org.webcurator.core.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.rest.AbstractRestClient;

import java.util.List;

public class PruneAndImportRemoteClient extends AbstractRestClient implements PruneAndImportService {
    public PruneAndImportRemoteClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public PruneAndImportCommandResult uploadFile(String fileName, boolean replaceFlag, byte[] doc) {
        return null;
    }

    @Override
    public PruneAndImportCommandTarget downloadFile(String fileName) {
        return null;
    }

    @Override
    public List<PruneAndImportCommandTargetMetadata> checkFiles(List<PruneAndImportCommandTargetMetadata> items) {
        return null;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(long job, int harvestResultNumber, int newHarvestResultNumber, List<PruneAndImportCommandTargetMetadata> dataset) {
        return null;
    }
}
