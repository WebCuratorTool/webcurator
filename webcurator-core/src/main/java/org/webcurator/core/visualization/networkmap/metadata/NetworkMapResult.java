package org.webcurator.core.visualization.networkmap.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkMapResult {
    private static final Logger log = LoggerFactory.getLogger(NetworkMapResult.class);

    public static final int RSP_CODE_SUCCESS = 0;

    public static final int RSP_CODE_WARN = 1;

    public static final int RSP_ERROR_DB_NOT_FOUND = -1;
    public static final int RSP_ERROR_BAD_REQUEST = -2;
    public static final int RSP_ERROR_INITIAL_EXTRACTOR_FAILED = -3;

    public static final int RSP_ERROR_DATA_NOT_EXIST = -4;

    public static final int RSP_ERROR_SYSTEM = -9;

    private int rspCode = RSP_CODE_SUCCESS;
    private String rspMsg = "Success";
    private String payload;

    public int getRspCode() {
        return rspCode;
    }

    public void setRspCode(int rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public static NetworkMapResult getSuccessResult() {
        return new NetworkMapResult();
    }

    public static NetworkMapResult getDBMissingErrorResult() {
        NetworkMapResult result = new NetworkMapResult();
        result.setRspCode(RSP_ERROR_DB_NOT_FOUND);
        result.setRspMsg("Could not find index BDB");
        log.warn("Could not find index BDB");
        return result;
    }

    public static NetworkMapResult getDataNotExistResult() {
        return getDataNotExistResult("Node does not exist");
    }

    public static NetworkMapResult getDataNotExistResult(String msg) {
        NetworkMapResult result = new NetworkMapResult();
        result.setRspCode(RSP_ERROR_DATA_NOT_EXIST);
        result.setRspMsg(msg);
        log.warn(msg);
        return result;
    }

    public static NetworkMapResult getBadRequestResult() {
        return getBadRequestResult("Bad request");
    }

    public static NetworkMapResult getBadRequestResult(String msg) {
        NetworkMapResult result = new NetworkMapResult();
        result.setRspCode(RSP_ERROR_BAD_REQUEST);
        result.setRspMsg(msg);
        log.warn(msg);
        return result;
    }

    public static NetworkMapResult getInitialExtractorFailedResult() {
        return getBadRequestResult("Failed to initial index extracting processor");
    }

    public static NetworkMapResult getInitialExtractorFailedResult(String msg) {
        NetworkMapResult result = new NetworkMapResult();
        result.setRspCode(RSP_ERROR_INITIAL_EXTRACTOR_FAILED);
        result.setRspMsg(msg);
        log.warn(msg);
        return result;
    }

    public static NetworkMapResult getSystemError() {
        return getBadRequestResult("System error");
    }

    public static NetworkMapResult getSystemError(String msg) {
        NetworkMapResult result = new NetworkMapResult();
        result.setRspCode(RSP_ERROR_SYSTEM);
        result.setRspMsg(msg);
        log.warn(msg);
        return result;
    }
}
