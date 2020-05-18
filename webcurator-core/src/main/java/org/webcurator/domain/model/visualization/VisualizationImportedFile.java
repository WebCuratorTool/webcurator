package org.webcurator.domain.model.visualization;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "VISUALIZATION_IMPORTED_FILE")
public class VisualizationImportedFile {
    @Id
    @Column(name="VIF_OID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long oid = null;

    @NotNull
    @Size(max=1024)
    @Column(name = "VIF_FILE_NAME")
    private String fileName;

    @Column(name = "VIF_CONTENT_LENGTH")
    private Long contentLength;

    @NotNull
    @Size(max=256)
    @Column(name = "VIF_CONTENT_TYPE")
    private String contentType;

    @Column(name = "VIF_LAST_MODIFIED_DATE")
    private Long lastModifiedDate;

    @Column(name = "VIF_UPLOAD_DATE")
    private Long uploadDate;

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

    public Long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Long uploadDate) {
        this.uploadDate = uploadDate;
    }
}
