package org.webcurator.rest.queue;

import org.webcurator.domain.model.core.Flag;

import javax.validation.constraints.NotNull;

public class FlagDTO {
    public FlagDTO(Flag flag) {
        this.oid = flag.getOid();
        this.name = flag.getName();
        this.rgb = flag.getRgb();
        this.complementRgb = flag.getComplementRgb();
    }

    private Long oid;
    private String name;
    private String rgb;
    private String complementRgb;

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRgb() {
        return rgb;
    }

    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

    public String getComplementRgb() {
        return complementRgb;
    }

    public void setComplementRgb(String complementRgb) {
        this.complementRgb = complementRgb;
    }
}
