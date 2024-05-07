package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.DublinCore;
import org.webcurator.domain.model.core.Target;

public class DescriptionDTO {

    String identifier;
    String description;
    String subject;
    String creator;
    String publisher;
    String type;
    String format;
    String language;
    String source;
    String relation;
    String contributor;
    String coverage;
    String issn;
    String isbn;

    public DescriptionDTO() {
    }

    public DescriptionDTO(Target target) {
        DublinCore metadata = target.getDublinCoreMetaData();
        if (metadata != null) {
            identifier = metadata.getIdentifier();
            description = metadata.getDescription();
            subject = metadata.getSubject();
            creator = metadata.getCreator();
            publisher = metadata.getPublisher();
            type = metadata.getType();
            format = metadata.getFormat();
            source = metadata.getSource();
            language = metadata.getLanguage();
            relation = metadata.getRelation();
            contributor = metadata.getContributor();
            coverage = metadata.getCoverage();
            issn = metadata.getIssn();
            isbn = metadata.getIsbn();
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
