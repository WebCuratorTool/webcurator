package org.webcurator.core.visualization.networkmap.bdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.util.WctUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BDBNetworkMapPool {
    private final static int MAX_SIZE = 10;
    private final static Logger log = LoggerFactory.getLogger(BDBNetworkMapPool.class);
    private final List<BDBRepoHolder> queue = new ArrayList<>();
    private final Map<String, BDBRepoHolder> map = new Hashtable<>();
    private final String dbRootPath;
    private final String dbVersion;

    public BDBNetworkMapPool(String dbRootPath, String dbVersion) {
        this.dbRootPath = dbRootPath;
        this.dbVersion = dbVersion;
    }

    //Create and open a DB
    synchronized public BDBRepoHolder createInstance(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            BDBRepoHolder oldDb = map.get(dbName);
            oldDb.shutdownDB();
            queue.remove(oldDb);
            map.remove(dbName);
        }

        if (queue.size() >= MAX_SIZE) {
            BDBRepoHolder oldDb = queue.get(0);
            queue.remove(0);
            map.remove(oldDb.getDbName());
            oldDb.shutdownDB();
        }

        //Clear the path
        String dbPath = this.getDbPath(job, harvestResultNumber);
        File dbDirectory = new File(dbPath);
        if (dbDirectory.exists()) {
            WctUtils.cleanDirectory(dbDirectory);
        } else {
            log.warn("Recover db files: {}", dbPath);
        }

        BDBRepoHolder db;
        try {
            db = BDBRepoHolder.createInstance(dbPath, dbName);
            queue.add(db);
            map.put(dbName, db);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to open db: {}-->{}, {}", dbPath, dbName, e.getMessage());
            return null;
        }

        return db;
    }

    //Open with read mode
    synchronized public BDBRepoHolder getInstance(long job, int harvestResultNumber) {
        String dbPath = this.getDbPath(job, harvestResultNumber);
        File dbPathFile = new File(dbPath);
        if (!dbPathFile.exists()) {  //
            log.warn("Could not find Index DB: {}", dbPath);
            return null;
        }
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            return map.get(dbName);
        }

        if (queue.size() >= MAX_SIZE) {
            BDBRepoHolder oldDb = queue.get(0);
            queue.remove(0);
            map.remove(oldDb.getDbName());
            oldDb.shutdownDB();
        }

        BDBRepoHolder db;
        try {
            db = BDBRepoHolder.getInstance(dbPath, dbName);
            queue.add(db);
            map.put(dbName, db);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to open db: {}-->{}, {}", dbPath, dbName, e.getMessage());
            return null;
        }

        return db;
    }

    public void close(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        if (map.containsKey(dbName)) {
            BDBRepoHolder oldDb = map.get(dbName);
            oldDb.shutdownDB();
            queue.remove(oldDb);
            map.remove(dbName);
        }
    }

    public String getDbPath(long job, int harvestResultNumber) {
        return String.format("%s%s%d%s%d%s_resource", this.dbRootPath, File.separator, job, File.separator, harvestResultNumber, File.separator);
    }

    public String getDbName(long job, int harvestResultNumber) {
        return String.format("%d_%d", job, harvestResultNumber);
    }

    public String getDbVersion() {
        return dbVersion;
    }
}
