package org.webcurator.domain.model.core;

public class SeedHistoryDTO {
    /**
     * The unique ID of the seed
     **/
    private long oid;
    /**
     * The seed itself
     **/
    private String seed;
    /**
     * The seed's target instance
     **/
    private long targetInstanceOid;
    /**
     * Sets if the seed is primary or secondary.
     */
    private boolean primary;

    public SeedHistoryDTO() {

    }

    public SeedHistoryDTO(long oid, String seed, long targetInstanceOid, boolean primary) {
        this.oid = oid;
        this.seed = seed;
        this.targetInstanceOid = targetInstanceOid;
        this.primary = primary;
    }

    public SeedHistoryDTO(SeedHistory seedHistory) {
        this(seedHistory.getOid(), seedHistory.getSeed(), seedHistory.getTargetInstanceOid(), seedHistory.isPrimary());
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public long getTargetInstanceOid() {
        return targetInstanceOid;
    }

    public void setTargetInstanceOid(long targetInstanceOid) {
        this.targetInstanceOid = targetInstanceOid;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
