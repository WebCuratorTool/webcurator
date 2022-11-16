package org.webcurator.core.visualization.modification.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webcurator.common.util.Utils;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class ModifyRowFullData extends NetworkMapNodeUrlEntity {
    protected static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private long index;
    private boolean existingFlag;
    private String option;
    private boolean folder;

    private String uploadFileName;
    private String uploadFileContent;
    private long uploadFileLength;
    private String modifiedMode;
    private long lastModifiedDate;
    private String lastModifiedPresentationString = "";

    private String cachedFileName;

    private int respCode;
    private String respMsg;

    @JsonIgnore
    public static void setValue(ModifyRowFullData row, String key, String value) {
        if (Utils.isEmpty(key) || Utils.isEmpty(value)) {
            return;
        }
        if (key.trim().equalsIgnoreCase("option")) {
            row.option = value.toUpperCase();
        } else if (key.trim().equalsIgnoreCase("target")) {
            row.url = value;
        } else if (key.trim().equalsIgnoreCase("modifiedMode")) {
            row.modifiedMode = value.toUpperCase();
        } else if (key.trim().equalsIgnoreCase("lastModifiedDate")) {
            LocalDateTime dt = parseDateTime(value);
            if (dt == null) {
                row.lastModifiedDate = -1;
            } else {
                row.lastModifiedDate = dt.toEpochSecond(ZoneOffset.UTC);
            }
        }
    }

    public static LocalDateTime parseDateTime(String val) {
        DateTimeFormatter[] dateTimeFormatterList = {DateTimeFormatter.BASIC_ISO_DATE, DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_LOCAL_DATE};
        for (DateTimeFormatter formatter : dateTimeFormatterList) {
            try {
                LocalDateTime dt = LocalDateTime.parse(val, formatter);
                return dt;
            } catch (DateTimeParseException e) {
                continue;
            }
        }
        return null;
    }

    public String getLastModifiedPresentationString() {
        if (modifiedMode == null) {
            return null;
        }
        if (modifiedMode.equalsIgnoreCase("FILE") || modifiedMode.equalsIgnoreCase("CUSTOM")) {
            Date warcDate = new Date();
            warcDate.setTime(lastModifiedDate);
            lastModifiedPresentationString = writerDF.format(warcDate);
        } else {
            lastModifiedPresentationString = modifiedMode;
        }
        return lastModifiedPresentationString;
    }

    public void setLastModifiedPresentationString(String lastModifiedPresentationString) {
        this.lastModifiedPresentationString = lastModifiedPresentationString;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public boolean isExistingFlag() {
        return existingFlag;
    }

    public void setExistingFlag(boolean existingFlag) {
        this.existingFlag = existingFlag;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getModifiedMode() {
        return modifiedMode;
    }

    public void setModifiedMode(String modifiedMode) {
        this.modifiedMode = modifiedMode;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

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

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public long getUploadFileLength() {
        return uploadFileLength;
    }

    public void setUploadFileLength(long uploadFileLength) {
        this.uploadFileLength = uploadFileLength;
    }

    public String getUploadFileContent() {
        return uploadFileContent;
    }

    public void setUploadFileContent(String uploadFileContent) {
        this.uploadFileContent = uploadFileContent;
    }

    public String getCachedFileName() {
        return cachedFileName;
    }

    public void setCachedFileName(String cachedFileName) {
        this.cachedFileName = cachedFileName;
    }

    @JsonIgnore
    public static ModifyRowFullData getInstance(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, ModifyRowFullData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
