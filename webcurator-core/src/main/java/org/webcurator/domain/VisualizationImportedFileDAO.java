package org.webcurator.domain;

import org.webcurator.domain.model.visualization.VisualizationImportedFile;

public interface VisualizationImportedFileDAO {
    void save(Object obj);
    VisualizationImportedFile findImportedFile(String fileName);
    void deleteImportedFile(VisualizationImportedFile importedFile);
}
