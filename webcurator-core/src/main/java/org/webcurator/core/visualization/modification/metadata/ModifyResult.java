package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.List;

public class ModifyResult {
    private int respCode= VisualizationConstants.RESP_CODE_SUCCESS;
    private String respMsg="Succeed";
    private List<ModifyRowFullData> metadataDataset;
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

    public List<ModifyRowFullData> getMetadataDataset() {
        return metadataDataset;
    }

    public void setMetadataDataset(List<ModifyRowFullData> metadataDataset) {
        this.metadataDataset = metadataDataset;
    }

    public HarvestResultDTO getDerivedHarvestResult() {
        return derivedHarvestResult;
    }

    public void setDerivedHarvestResult(HarvestResultDTO derivedHarvestResult) {
        this.derivedHarvestResult = derivedHarvestResult;
    }
}
