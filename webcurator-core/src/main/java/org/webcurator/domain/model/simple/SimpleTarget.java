package org.webcurator.domain.model.simple;

/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import org.webcurator.domain.model.core.Optimizable;
import org.webcurator.domain.model.core.Target;

import javax.validation.constraints.Size;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A Target is a set of seeds, schedules and profile overrides that specify
 * what to harvest, at what times, and what harvest profile to use.
 *
 * @author nwaight
 */
// TODO lazy="false"
@Entity
@Table(name = "TARGET")
@PrimaryKeyJoinColumn(name = "T_AT_OID", referencedColumnName = "AT_OID")
public class SimpleTarget extends SimpleAbstractTarget implements Optimizable {

    /**
     * Date at which the target was first nominated or approved
     */
    @Column(name = "T_SELECTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date selectionDate;
    /**
     * The type of the selection
     */
    @Size(max = 255)
    @Column(name = "T_SELECTION_TYPE")
    private String selectionType;
    /**
     * A selection note
     */
    @Size(max = 1000)
    @Column(name = "T_SELECTION_NOTE")
    private String selectionNote;
    /**
     * An evaluation note
     */
    @Size(max = 1000)
    @Column(name = "T_EVALUATION_NOTE")
    private String evaluationNote;
    /**
     * The type of harvest
     */
    @Size(max = 255)
    @Column(name = "T_HARVEST_TYPE")
    private String harvestType;
    /**
     * The seeds.
     **/
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true) // default fetch type is LAZY
    @JoinColumn(name = "S_TARGET_ID")
    private Set<SimpleSeed> seeds = new HashSet<>();

    /**
     * Run the target as soon as approved
     */
    @Column(name = "T_RUN_ON_APPROVAL")
    private boolean runOnApproval = false;

    /**
     * Use Automated Quality Assurance on Harvests derived from this Target
     */
    @Column(name = "T_USE_AQA")
    private boolean useAQA = false;

    /**
     * Run the target in five minutes
     */
    @Transient
    private boolean harvestNow = false;
    @Column(name = "T_ALLOW_OPTIMIZE")
    private boolean allowOptimize;

    /**
     * Protected constructor - all instances should be created through the
     * <code>BusinessObjectFactory</code>.
     */
    public SimpleTarget() {
        super(Target.TYPE_TARGET);
    }

    /**
     * Return the Set of Seeds attached to this target.
     *
     * @return Returns the seeds.
     */
    public Set<SimpleSeed> getSeeds() {
        return seeds;
    }

    /**
     * Set the set of seeds in this target.
     *
     * @param seeds The seeds to set.
     */
    public void setSeeds(Set<SimpleSeed> seeds) {
        this.seeds = seeds;
    }


    /**
     * Is the target now schedulable?
     *
     * @return True if the new state of the target is schedulable.
     */
    public boolean isSchedulable() {
        return Target.isScheduleableState(getState());
    }

    /**
     * Was the target schedulable when initialised?
     *
     * @return True if the original state was schedulable.
     */
    public boolean wasSchedulable() {
        return Target.isScheduleableState(getOriginalState());
    }


    /**
     * Checks if the specified state is schedulable - i.e. should the target
     * have target instances.
     *
     * @param aState The state to test.
     * @return true if schedulable; otherwise false.
     */
    public static boolean isScheduleableState(int aState) {
        return aState == Target.STATE_APPROVED || aState == Target.STATE_COMPLETED;
    }


    /**
     * @return Returns the runOnApproval.
     */
    public boolean isRunOnApproval() {
        return runOnApproval;
    }


    /**
     * @param runOnApproval The runOnApproval to set.
     */
    public void setRunOnApproval(boolean runOnApproval) {
        this.runOnApproval = runOnApproval;
    }

    /**
     * @return Returns the useAQA.
     */
    public boolean isUseAQA() {
        return useAQA;
    }


    /**
     * @param useAQA The useAQA to set.
     */
    public void setUseAQA(boolean useAQA) {
        this.useAQA = useAQA;
    }


    /**
     * @return Returns the evaluationNote.
     */
    public String getEvaluationNote() {
        return evaluationNote;
    }


    /**
     * @param evaluationNote The evaluationNote to set.
     */
    public void setEvaluationNote(String evaluationNote) {
        this.evaluationNote = evaluationNote;
    }


    /**
     * @return Returns the selectionDate.
     */
    public Date getSelectionDate() {
        return selectionDate;
    }


    /**
     * @param selectionDate The selectionDate to set.
     */
    public void setSelectionDate(Date selectionDate) {
        this.selectionDate = selectionDate;
    }


    /**
     * @return Returns the selectionNote.
     */
    public String getSelectionNote() {
        return selectionNote;
    }


    /**
     * @param selectionNote The selectionNote to set.
     */
    public void setSelectionNote(String selectionNote) {
        this.selectionNote = selectionNote;
    }


    /**
     * @return Returns the selectionType.
     */
    public String getSelectionType() {
        return selectionType;
    }


    /**
     * @param selectionType The selectionType to set.
     */
    public void setSelectionType(String selectionType) {
        this.selectionType = selectionType;
    }

    /**
     * @return Returns the harvestType.
     */
    public String getHarvestType() {
        return harvestType;
    }

    /**
     * @param harvestType The harvestType to set.
     */
    public void setHarvestType(String harvestType) {
        this.harvestType = harvestType;
    }

    /**
     * @return Returns the harvestNow.
     */
    public boolean isHarvestNow() {
        return harvestNow;
    }


    /**
     * @param harvestNow The harvestNow to set.
     */
    public void setHarvestNow(boolean harvestNow) {
        this.harvestNow = harvestNow;
    }

    /**
     * @return Returns the harvestType.
     */
    public boolean isAllowOptimize() {
        return allowOptimize;
    }

    public void setAllowOptimize(boolean allowOptimize) {
        this.allowOptimize = allowOptimize;
    }

}
