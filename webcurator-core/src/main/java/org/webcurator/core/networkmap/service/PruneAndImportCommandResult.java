package org.webcurator.core.networkmap.service;

import java.util.List;

public class PruneAndImportCommandResult {
    private int respCode;
    private String respMsg;
    private List<PruneAndImportCommandRowMetadata> metadataDataset;

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public List<PruneAndImportCommandRowMetadata> getMetadataDataset() {
        return metadataDataset;
    }

    public void setMetadataDataset(List<PruneAndImportCommandRowMetadata> metadataDataset) {
        this.metadataDataset = metadataDataset;
    }
}
