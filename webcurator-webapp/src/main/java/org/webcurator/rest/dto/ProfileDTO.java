package org.webcurator.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.webcurator.domain.model.core.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ProfileDTO {

    String harvesterType;
    @NotNull(message = "id is required")
    Long id;
    Boolean imported;
    String name;
    @Valid
    List<Override> overrides = new ArrayList<>();


    public ProfileDTO() {
    }

    public String getHarvesterType() {
        return harvesterType;
    }

    public void setHarvesterType(String harvesterType) {
        this.harvesterType = harvesterType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getImported() {
        return imported;
    }

    public void setImported(Boolean imported) {
        this.imported = imported;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Override> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<Override> overrides) {
        this.overrides = overrides;
    }

    private void init(Profile p, ProfileOverrides o) {
        harvesterType = p.getHarvesterType();
        id = p.getOid();
        imported = p.isImported();
        name = p.getName();

        // FIXME what do we do in the case of an imported profile (which doesn't have overrides, but is overridden entirely by a new profile)?
        if (p.isHeritrix3Profile()) {
            Override documentLimit = new Override();
            documentLimit.setId("documentLimit");
            documentLimit.setValue(o.getH3DocumentLimit());
            documentLimit.setEnabled(o.isOverrideH3DocumentLimit());
            overrides.add(documentLimit);
            Override dataLimit = new Override();
            dataLimit.setId("dataLimit");
            dataLimit.setValue(o.getH3DataLimit());
            dataLimit.setUnit(o.getH3DataLimitUnit());
            dataLimit.setEnabled(o.isOverrideH3DataLimit());
            overrides.add(dataLimit);
            Override timeLimit = new Override();
            timeLimit.setId("timeLimit");
            timeLimit.setValue(o.getH3TimeLimit());
            timeLimit.setUnit(o.getH3TimeLimitUnit());
            timeLimit.setEnabled(o.isOverrideH3TimeLimit());
            overrides.add(timeLimit);
            Override maxPathDepth = new Override();
            maxPathDepth.setId("maxPathDepth");
            maxPathDepth.setValue(o.getH3MaxPathDepth());
            maxPathDepth.setEnabled(o.isOverrideH3MaxPathDepth());
            overrides.add(maxPathDepth);
            Override maxHops = new Override();
            maxHops.setId("maxHops");
            maxHops.setValue(o.getH3MaxHops());
            maxHops.setEnabled(o.isOverrideH3MaxHops());
            overrides.add(maxHops);
            Override maxTransitiveHops = new Override();
            maxTransitiveHops.setId("maxTransitiveHops");
            maxTransitiveHops.setValue(o.getH3MaxTransitiveHops());
            maxTransitiveHops.setEnabled(o.isOverrideH3MaxTransitiveHops());
            overrides.add(maxTransitiveHops);
            Override ignoreRobots = new Override();
            ignoreRobots.setId("ignoreRobots");
            ignoreRobots.setValue(o.isH3IgnoreRobots());
            ignoreRobots.setEnabled(o.isOverrideH3IgnoreRobots());
            overrides.add(ignoreRobots);
            Override extractJs = new Override();
            extractJs.setId("extractJs");
            extractJs.setValue(o.isH3ExtractJs());
            extractJs.setEnabled(o.isOverrideH3ExtractJs());
            overrides.add(extractJs);
            Override ignoreCookies = new Override();
            ignoreCookies.setId("ignoreCookies");
            ignoreCookies.setValue(o.isH3IgnoreCookies());
            ignoreCookies.setEnabled(o.isOverrideH3IgnoreCookies());
            overrides.add(ignoreCookies);
            Override blockedUrls = new Override();
            blockedUrls.setId("blockedUrls");
            blockedUrls.setValue(o.getH3BlockedUrls());
            blockedUrls.setEnabled(o.isOverrideH3BlockedUrls());
            overrides.add(blockedUrls);
            Override includedUrls = new Override();
            includedUrls.setId("includedUrls");
            includedUrls.setValue(o.getH3IncludedUrls());
            includedUrls.setEnabled(o.isOverrideH3IncludedUrls());
            overrides.add(includedUrls);
        } else { // Legacy H1 overrides
            Override robotsHonouringPolicy = new Override();
            robotsHonouringPolicy.setId("robotsHonouringPolicy");
            robotsHonouringPolicy.setValue(o.getRobotsHonouringPolicy());
            robotsHonouringPolicy.setEnabled(o.isOverrideRobotsHonouringPolicy());
            overrides.add(robotsHonouringPolicy);
            Override maxTimeSec = new Override();
            maxTimeSec.setId("maxTimeSec");
            maxTimeSec.setValue(o.getMaxTimeSec());
            maxTimeSec.setEnabled(o.isOverrideMaxTimeSec());
            overrides.add(maxTimeSec);
            Override maxBytesDownload = new Override();
            maxBytesDownload.setId("maxBytesDownload");
            maxBytesDownload.setValue(o.getMaxBytesDownload());
            maxBytesDownload.setEnabled(o.isOverrideMaxBytesDownload());
            overrides.add(maxBytesDownload);
            Override maxHarvestDocuments = new Override();
            maxHarvestDocuments.setId("maxHarvestDocuments");
            maxHarvestDocuments.setValue(o.getMaxHarvestDocuments());
            maxHarvestDocuments.setEnabled(o.isOverrideMaxHarvestDocuments());
            overrides.add(maxHarvestDocuments);
            Override maxPathDepth = new Override();
            maxPathDepth.setId("maxPathDepth");
            maxPathDepth.setValue(o.getMaxPathDepth());
            maxPathDepth.setEnabled(o.isOverrideMaxPathDepth());
            overrides.add(maxPathDepth);
            Override maxLinkHops = new Override();
            maxLinkHops.setId("maxLinkHops");
            maxLinkHops.setValue(o.getMaxLinkHops());
            maxLinkHops.setEnabled(o.isOverrideMaxLinkHops());
            overrides.add(maxLinkHops);
            Override excludeFilters = new Override();
            excludeFilters.setId("excludeFilters");
            excludeFilters.setValue(o.getExcludeUriFilters());
            excludeFilters.setEnabled(o.isOverrideExcludeUriFilters());
            overrides.add(excludeFilters);
            Override includeFilters = new Override();
            includeFilters.setId("includeFilters");
            includeFilters.setValue(o.getIncludeUriFilters());
            includeFilters.setEnabled(o.isOverrideIncludeUriFilters());
            overrides.add(includeFilters);
            Override excludedMimeTypes = new Override();
            excludedMimeTypes.setId("excludedMimeTypes");
            excludedMimeTypes.setValue(o.getExcludedMimeTypes());
            excludedMimeTypes.setEnabled(o.isOverrideExcludedMimeTypes());
            overrides.add(excludedMimeTypes);
            Override credentials = new Override();
            credentials.setId("credentials");
            credentials.setValue(o.getCredentials());
            credentials.setEnabled(o.isOverrideCredentials());
            overrides.add(credentials);
        }
    }

    public ProfileDTO(TargetInstance targetInstance) {
        Profile p = targetInstance.getProfile();
        ProfileOverrides o = targetInstance.getProfileOverrides();
        init(p, o);
    }

    public ProfileDTO(AbstractTarget abstractTarget) {

        Profile p = abstractTarget.getProfile();
        ProfileOverrides o = abstractTarget.getProfileOverrides();
        init(p, o);
    }

    public static class Override {
        @NotBlank(message = "id is required")
        String id;
        Object value;
        @NotNull(message = "enabled is required")
        Boolean enabled;
        // FIXME we need better validation of the unit attribute
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String unit;

        public Override() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
