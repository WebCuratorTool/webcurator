package org.webcurator.core.visualization.modification.service;

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.webcurator.domain.model.visualization.VisualizationImportedFile;

@Repository
public interface VisualizationImportedFileRepository extends JpaRepository<VisualizationImportedFile, Long> {
    VisualizationImportedFile findByFileName(String fileName);
    void deleteByFileName(String fileName);
    void deleteByUploadDate(Long uploadDate);
}
