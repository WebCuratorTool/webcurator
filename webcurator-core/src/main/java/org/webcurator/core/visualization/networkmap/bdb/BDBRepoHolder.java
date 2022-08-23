package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.metadata.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BDBRepoHolder {
    private static final Logger log = LoggerFactory.getLogger(BDBRepoHolder.class);
    /**
     * Maximum BDBJE file size
     */
    private final static String MAX_DB_FILE_SIZE = "256000000";


    private final String dbPath;
    private String dbName;

    public Environment env;

    public RepoAccessProperty tblAccessProp;
    public RepoNetworkNodeDomain tblDomain;
    public RepoNetworkNodeUrl tblUrl;
    public RepoNetworkNodeFolder tblFolder;

    private BDBRepoHolder(String dbPath, String dbName) {
        this.dbPath = dbPath;
        this.dbName = dbName;
    }

    public static BDBRepoHolder createInstance(String dbPath, String repoName) throws IOException {
        log.debug("Open BDB: {}", dbPath);
        BDBRepoHolder repo = new BDBRepoHolder(dbPath, repoName);

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setCacheSize(1024 * 1024);
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        environmentConfig.setConfigParam("je.log.fileMax", MAX_DB_FILE_SIZE);
        File file = new File(repo.dbPath);
        if (!file.exists() || !file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new IOException("failed mkdirs(" + dbPath + ")");
            }
        }
        repo.env = new Environment(file, environmentConfig);
        repo.tblAccessProp = new RepoAccessProperty(repo.env, true);
        repo.tblDomain = new RepoNetworkNodeDomain(repo.env, true);
        repo.tblUrl = new RepoNetworkNodeUrl(repo.env, true);
        repo.tblFolder = new RepoNetworkNodeFolder(repo.env, true);
        return repo;
    }

    public static BDBRepoHolder getInstance(String dbPath, String repoName) throws IOException {
        log.debug("Open BDB: {}", dbPath);
        BDBRepoHolder repo = new BDBRepoHolder(dbPath, repoName);

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setCacheSize(1024 * 1024);
        environmentConfig.setAllowCreate(false);
        environmentConfig.setTransactional(true);
        environmentConfig.setConfigParam("je.log.fileMax", MAX_DB_FILE_SIZE);
        File file = new File(repo.dbPath);
        if (!file.exists() || !file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new IOException("failed mkdirs(" + dbPath + ")");
            }
        }
        repo.env = new Environment(file, environmentConfig);
        repo.tblAccessProp = new RepoAccessProperty(repo.env, false);
        repo.tblDomain = new RepoNetworkNodeDomain(repo.env, false);
        repo.tblUrl = new RepoNetworkNodeUrl(repo.env, false);
        repo.tblFolder = new RepoNetworkNodeFolder(repo.env, false);
        return repo;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void shutdownDB() {
        try {
            this.tblAccessProp.close();
            this.tblDomain.close();
            this.tblUrl.close();
            this.tblFolder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.env.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NetworkMapAccessPropertyEntity insertAccProp(NetworkMapAccessPropertyEntity entity) {
        return this.tblAccessProp.save(entity);
    }

    public NetworkMapAccessPropertyEntity getAccProp() {
        return this.tblAccessProp.get();
    }

    public String getDbVersionStamp() {
        NetworkMapAccessPropertyEntity accProp = getAccProp();
        if (accProp == null) {
            return "0.0.0";
        } else {
            return accProp.getVersion();
        }
    }

    public long nextUrlId() {
        return this.tblUrl.nextId();
    }

    public NetworkMapNodeUrlEntity insertUrl(NetworkMapNodeUrlEntity entity) {
        if (entity.getId() < 0) {
            log.error("Can not insert new entity with existing ID");
            return entity;
        }

        return this.tblUrl.insert(entity);
    }

    public NetworkMapNodeUrlEntity updateUrl(NetworkMapNodeUrlEntity entity) {
        return this.tblUrl.update(entity);
    }

    public NetworkMapNodeUrlEntity getUrlById(long id) {
        return this.tblUrl.getById(id);
    }

    public NetworkMapNodeUrlEntity getUrlByName(String urlName) {
        return tblUrl.getUrlByName(urlName);
    }

    public void deleteUrlById(long id) {
        this.tblUrl.deleteById(id);
    }

    public EntityCursor<NetworkMapNodeUrlEntity> openUrlCursor() {
        return this.tblUrl.openCursor();
    }

    public NetworkMapDomain insertDomain(NetworkMapDomain entity) {
        return this.tblDomain.insert(entity);
    }

    public NetworkMapDomain updateDomain(NetworkMapDomain entity) {
        return this.tblDomain.update(entity);
    }

    public NetworkMapDomain getDomainById(long id) {
        return this.tblDomain.getById(id);
    }

    public void deleteDomainById(long id) {
        this.tblDomain.deleteById(id);
    }

    public NetworkMapNodeFolderEntity insertFolder(NetworkMapNodeFolderEntity entity) {
        return this.tblFolder.insert(entity);
    }

    public NetworkMapNodeFolderEntity updateFolder(NetworkMapNodeFolderEntity entity) {
        return this.tblFolder.update(entity);
    }

    public NetworkMapNodeFolderEntity getFolderById(long id) {
        return this.tblFolder.getById(id);
    }

    public NetworkMapNodeFolderEntity getFolderByTitle(String title) {
        return this.tblFolder.getByTitle(title);
    }

    public NetworkMapDomain getRootDomain() {
        NetworkMapAccessPropertyEntity accProp = this.tblAccessProp.get();
        long rootDomainId = accProp.getId();
        return this.getDomainById(rootDomainId);
    }

    public List<Long> getSeedUrls() {
        NetworkMapAccessPropertyEntity accProp = this.tblAccessProp.get();
        return accProp.getSeedUrlIDs();
    }

    public List<Long> getMalformedUrls() {
        NetworkMapAccessPropertyEntity accProp = this.tblAccessProp.get();
        return accProp.getMalformedUrlIDs();
    }
}