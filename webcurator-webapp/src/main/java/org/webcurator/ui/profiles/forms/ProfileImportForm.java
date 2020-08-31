package org.webcurator.ui.profiles.forms;

import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.webcurator.ui.profiles.command.ProfileListCommand;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ProfileImportForm {
    String importAgency;
    String importType;
    String importName;
    @NotNull
    CommonsMultipartFile uploadedFile;

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

    public CommonsMultipartFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(CommonsMultipartFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public ProfileListCommand getCommand() {
        return command;
    }

    public void setCommand(ProfileListCommand command) {
        this.command = command;
    }
}
