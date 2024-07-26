package org.webcurator.core.harvester.agent.filter;

import java.io.File;
import java.util.regex.Pattern;

public class RegexNameFilter implements FileFilter {

    private Pattern pattern;
    public RegexNameFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean accepts(File f) {
        return pattern.matcher(f.getName()).find();
    }}
