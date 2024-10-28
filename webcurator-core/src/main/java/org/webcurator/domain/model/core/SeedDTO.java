package org.webcurator.domain.model.core;

public class SeedDTO {
    private Long oid;
    private String seed;
    private boolean primary;
    private Long targetOid;

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public Long getTargetOid() {
        return targetOid;
    }

    public void setTargetOid(Long targetOid) {
        this.targetOid = targetOid;
    }
}
