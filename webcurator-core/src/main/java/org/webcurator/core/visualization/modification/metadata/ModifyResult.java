package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.List;

public class ModifyResult {
    private int respCode;
    private String respMsg;
    private List<ModifyRowMetadata> metadataDataset;
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

    public List<ModifyRowMetadata> getMetadataDataset() {
        return metadataDataset;
    }

    public void setMetadataDataset(List<ModifyRowMetadata> metadataDataset) {
        this.metadataDataset = metadataDataset;
    }

    public HarvestResultDTO getDerivedHarvestResult() {
        return derivedHarvestResult;
    }

    public void setDerivedHarvestResult(HarvestResultDTO derivedHarvestResult) {
        this.derivedHarvestResult = derivedHarvestResult;
    }
}
