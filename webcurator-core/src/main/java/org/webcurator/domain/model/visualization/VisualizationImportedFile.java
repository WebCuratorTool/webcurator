package org.webcurator.domain.model.visualization;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "VISUALIZATION_IMPORTED_FILE")
@NamedQuery(name = "org.webcurator.domain.model.visualization.VisualizationImportedFile.byFileName",
        query = "SELECT vif FROM VisualizationImportedFile vif WHERE vif.fileName = ?1")
public class VisualizationImportedFile {
    public final static String QRY_VISUALIZATION_IMPORTED_FILE_BY_NAME = "org.webcurator.domain.model.visualization.VisualizationImportedFile.byFileName";
    @Id
    @Column(name = "VIF_OID")
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "ImportedFile",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long oid = null;

    @NotNull
    @Size(max = 1024)
    @Column(name = "VIF_FILE_NAME")
    private String fileName;

    @Column(name = "VIF_CONTENT_LENGTH")
    private Long contentLength;

    @NotNull
    @Size(max = 256)
    @Column(name = "VIF_CONTENT_TYPE")
    private String contentType;

    @Column(name = "VIF_LAST_MODIFIED_DATE")
    private Long lastModifiedDate;

    @Column(name = "VIF_UPLOADED_DATE")
    private String uploadedDate;

    @Column(name = "VIF_UPLOADED_TIME")
    private String uploadedTime;

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public String getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(String uploadedTime) {
        this.uploadedTime = uploadedTime;
    }
}
