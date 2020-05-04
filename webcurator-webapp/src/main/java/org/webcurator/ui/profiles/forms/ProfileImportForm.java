package org.webcurator.ui.profiles.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.webcurator.ui.profiles.command.ProfileListCommand;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ProfileImportForm {
    String importAgency;
    String importType;
    String importName;
    @NotNull
    CommonsMultipartFile uploadedFile;

    @Valid ProfileListCommand command;
}
