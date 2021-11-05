package org.webcurator.core.store;

import org.webcurator.core.reader.LogReader;


/**
 * Factory to store DigitalAssetStore and LogReader instances that use Restful API to communicate
 * with a remote DigitalAssetStore
 *
 * @author kurwin
 */
public class DigitalAssetStoreFactoryImpl {
    private DigitalAssetStore digitalAssetStore;
    private LogReader logReader;

    public DigitalAssetStore getDAS() {
        return this.digitalAssetStore;
    }

    public void setDAS(DigitalAssetStore digitalAssetStore){
        this.digitalAssetStore = digitalAssetStore;
    }

    public LogReader getLogReader() {
        return this.logReader;
    }

    public void setLogReader(LogReader logReader){
        this.logReader = logReader;
    }
}