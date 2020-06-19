package org.webcurator.domain.model.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The Object for transfering Harvest Resources between the web curator components.
 *
 * @author bbeaumont
 */
public class HarvestResourceDTO {
    /**
     * The name of the harvest resource.
     */
    protected String name;
    /**
     * the length of the resource.
     */
    protected long length;
    /**
     * The status code of the resource.
     */
    protected int statusCode;
    /**
     * the id of the resource.
     */
    protected Long oid;
    /**
     * the id of the target instance the resource belongs to.
     */
    protected Long targetInstanceOid;
    /**
     * the harvest result number the resource belongs to.
     */
    protected int harvestResultNumber;
    /**
     * The temporary file name of a harvest resource that is being imported.
     */
    protected String tempFileName;
    /**
     * The content-type of a harvest resource that is being imported.
     */
    protected String contentType = "Unknown";
    /**
     * The url of a harvest resource via that the current current url is fetched
     */
    protected String viaName;

    /**
     * The offset of the resource within the ARC file.
     */
    private long resourceOffset;
    /**
     * The length of the resource.
     */
    private long resourceLength;
    /**
     * the name of the arc file the resource is in.
     */
    private String arcFileName;
    /**
     * flag to indicate that the arc file is compressed.
     */
    private boolean compressed;

    /**
     * seed flag
     */
    private boolean seedFlag;

    /**
     * All links which the current page refers to.
     */
    private List<String> outlinks = new ArrayList<>();

    public HarvestResourceDTO() {
    }

    /**
     * Constuct a new DTO passing in all the initial values.
     *
     * @param targetInstanceOid   the id of the resouces target instance
     * @param harvestResultNumber the number of the resorces harvest result
     * @param oid                 the unique id
     * @param name                the resource name
     * @param length              the resource length
     * @param resOffset           the offset of the resource in the ARC file
     * @param resLength           the length of the resource in the ARC file
     * @param arcFileName         the name of the arc file
     * @param compressed          lag to indicate that the arc file is compressed
     */
    public HarvestResourceDTO(long targetInstanceOid, int harvestResultNumber, long oid, String name, long length, long resOffset, long resLength, String arcFileName, int statusCode, boolean compressed) {
        this.targetInstanceOid = targetInstanceOid;
        this.harvestResultNumber = harvestResultNumber;
        this.oid = oid;
        this.name = name;
        this.length = length;
        this.resourceOffset = resOffset;
        this.resourceLength = resLength;
        this.arcFileName = arcFileName;
        this.statusCode = statusCode;
        this.compressed = compressed;
    }

    /**
     * @return the harvestResultNumber
     */
    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    /**
     * @param harvestResultNumber the harvestResultNumber to set
     */
    public void setHarvestResultNumber(int harvestResultNumber) {
        this.harvestResultNumber = harvestResultNumber;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the oid
     */
    public Long getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }

    /**
     * @return the targetInstanceOid
     */
    public Long getTargetInstanceOid() {
        return targetInstanceOid;
    }

    /**
     * @param targetInstanceOid the targetInstanceOid to set
     */
    public void setTargetInstanceOid(Long targetInstanceOid) {
        this.targetInstanceOid = targetInstanceOid;
    }

    /**
     * @return the name of the job.
     */
    public String buildJobName() {
        return targetInstanceOid.toString();
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the temporary file name
     */
    public String getTempFileName() {
        return tempFileName;
    }

    /**
     * @param tempFileName the temporary file name to set
     */
    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    /**
     * @return the imported content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the imported content type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getViaName() {
        return viaName;
    }

    public void setViaName(String viaName) {
        this.viaName = viaName;
    }

    /**
     * @return the name of the arc file the resource resides in.
     */
    public String getArcFileName() {
        return arcFileName;
    }

    /**
     * @param arcFileName the name of the arc file the resource resides in.
     */
    public void setArcFileName(String arcFileName) {
        this.arcFileName = arcFileName;
    }

    /**
     * @return the flag to indicate that the arc file is compressed.
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * @param compressed the flag to indicate that the arc file is compressed.
     */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    /**
     * @return the length of the resource.
     */
    public long getResourceLength() {
        return resourceLength;
    }

    /**
     * @param resourceLength the length of the resource.
     */
    public void setResourceLength(long resourceLength) {
        this.resourceLength = resourceLength;
    }

    /**
     * @return the offset of the resource in the arc file.
     */
    public long getResourceOffset() {
        return resourceOffset;
    }

    /**
     * @param resourceOffset the offset of the resource in the arc file.
     */
    public void setResourceOffset(long resourceOffset) {
        this.resourceOffset = resourceOffset;
    }

    public List<String> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<String> outlinks) {
        this.outlinks = outlinks;
    }

    public boolean isSeedFlag() {
        return seedFlag;
    }

    public void setSeedFlag(boolean seedFlag) {
        this.seedFlag = seedFlag;
    }

    public void clear() {
        this.outlinks.clear();
    }
}
