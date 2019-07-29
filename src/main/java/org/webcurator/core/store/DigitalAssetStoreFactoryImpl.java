package org.webcurator.core.store;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.reader.LogReaderClient;
import org.webcurator.core.reader.LogReader;

/**
 * Factory to create DigitalAssetStore instances that use SOAP to communicate 
 * with a remote DigitalAssetStore
 * @author kurwin
 */
public class DigitalAssetStoreFactoryImpl implements DigitalAssetStoreFactory {
	private DigitalAssetStoreConfig digitalAssetStoreConfig;

    public DigitalAssetStore getDAS() {
        DigitalAssetStoreClient store = new DigitalAssetStoreClient(digitalAssetStoreConfig.getHost(),
                digitalAssetStoreConfig.getPort(),
                new RestTemplateBuilder());

        return store;
    }
    
    public LogReader getLogReader() {
        return new LogReaderClient(digitalAssetStoreConfig.getHost(), digitalAssetStoreConfig.getPort());
    }
    
    public void setDigitalAssetStoreConfig(DigitalAssetStoreConfig digitalAssetStoreConfig)
    {
        this.digitalAssetStoreConfig = digitalAssetStoreConfig;
    }
    
    public DigitalAssetStoreConfig getDigitalAssetStoreConfig()
    {
    	return digitalAssetStoreConfig;
    }
}
