package org.webcurator.core.visualization.networkmap.metadata;

public interface NetworkMapUnlStructure {
    String UNL_FIELDS_SEPARATOR = "\n";

    String toUnlString();

    void toObjectFromUnl(String unl) throws Exception;
}
