package org.webcurator.domain.db;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SampleRepository extends CrudRepository<SampleEntity, Long> {
    List<SampleEntity> findBySimpleValue(String simpleValue);
}
