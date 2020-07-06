package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.List;

public class PruneAndImportCommandResult {
    private int respCode;
    private String respMsg;
    private List<PruneAndImportCommandRowMetadata> metadataDataset;
    private HarvestResultDTO derivedHarvestResult;

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

    public HarvestResultDTO getDerivedHarvestResult() {
        return derivedHarvestResult;
    }

    public void setDerivedHarvestResult(HarvestResultDTO derivedHarvestResult) {
        this.derivedHarvestResult = derivedHarvestResult;
    }
}
