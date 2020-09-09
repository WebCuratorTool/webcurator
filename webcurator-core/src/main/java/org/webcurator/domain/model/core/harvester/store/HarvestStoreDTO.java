package org.webcurator.domain.model.core.harvester.store;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class HarvestStoreDTO {
    private String directory="";
    private String path=null;
    private List<String> paths=null;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getPath() {
        return path;
    }

    public Path getPathFromPath() {
        if (this.path == null) {
            return null;
        }
        return new File(this.path).toPath();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPathFromPath(Path path) {
        if (path == null) {
            return;
        }
        this.path = path.toFile().getAbsolutePath();
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<Path> getPathsFromPath() {
        if (this.paths == null) {
            return null;
        }
        return this.paths.stream().map(p -> {
            return new File(p).toPath();
        }).collect(Collectors.toList());
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void setPathsFromPath(List<Path> paths) {
        if (this.paths == null) {
            return;
        }
        this.paths = paths.stream().map(p -> {
            return p.toFile().getAbsolutePath();
        }).collect(Collectors.toList());
    }
}
