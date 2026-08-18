package org.webcurator.ui.profiles.forms;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import org.webcurator.ui.profiles.command.ProfileListCommand;

public class ProfileImportForm {
    String importAgency;
    String importType;
    String importName;
    @NotNull
    MultipartFile uploadedFile;

    @Valid ProfileListCommand command;

    public ProfileImportForm() {
    }

    public String getImportAgency() {
        return importAgency;
    }

    public void setImportAgency(String importAgency) {
        this.importAgency = importAgency;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getImportName() {
        return importName;
    }

    public void setImportName(String importName) {
        this.importName = importName;
    }

    public MultipartFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(MultipartFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public ProfileListCommand getCommand() {
        return command;
    }

    public void setCommand(ProfileListCommand command) {
        this.command = command;
    }
}
