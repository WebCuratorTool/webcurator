package org.webcurator.core.networkmap.service;

public class PruneAndImportCommandResult {
    private int respCode;
    private String respMsg;
    private PruneAndImportCommandTargetMetadata metadata;

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

    public PruneAndImportCommandTargetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PruneAndImportCommandTargetMetadata metadata) {
        this.metadata = metadata;
    }
}
