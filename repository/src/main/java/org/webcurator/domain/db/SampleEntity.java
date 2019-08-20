package org.webcurator.domain.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * NOTE: This sample class would not normally be here. The POJO would be defined in webcurator-core and annotated
 * with @code{@Entity} and the repository itself with all the operations for that entity would be defined in this
 * project.
 */
@Entity
public class SampleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simpleValue;

    public SampleEntity(String simpleValue) {
        this.simpleValue = simpleValue;
    }
}
