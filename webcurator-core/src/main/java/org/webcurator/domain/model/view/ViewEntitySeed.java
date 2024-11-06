package org.webcurator.domain.model.view;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "SEED")
public class ViewEntitySeed {
    @Id
    @NotNull
    @Column(name = "S_OID")
    private Long oid;
    /**
     * The seed itself
     **/
    @Size(max = 1024)
    @Column(name = "S_SEED")
    private String seed;
    /**
     * The seed's target
     **/
    @ManyToOne
    @JoinColumn(name = "S_TARGET_ID")
    private ViewEntityTargetSummary target;

    /**
     * Sets if the seed is primary or secondary.
     */
    @Column(name = "S_PRIMARY")
    private boolean primary;

    public @NotNull Long getOid() {
        return oid;
    }

    public void setOid(@NotNull Long oid) {
        this.oid = oid;
    }

    public @Size(max = 1024) String getSeed() {
        return seed;
    }

    public void setSeed(@Size(max = 1024) String seed) {
        this.seed = seed;
    }

    public ViewEntityTargetSummary getTarget() {
        return target;
    }

    public void setTarget(ViewEntityTargetSummary target) {
        this.target = target;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
