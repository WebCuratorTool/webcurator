package org.webcurator.core.visualization.networkmap.bdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.util.WctUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BDBNetworkMapPool {
    private final static int MAX_SIZE = 10;
    private final static Logger log = LoggerFactory.getLogger(BDBNetworkMapPool.class);
    private final Map<String, BDBRepoHolder> map = new Hashtable<>();
    private final String dbRootPath;
    private final String dbVersion;

    public BDBNetworkMapPool(String dbRootPath, String dbVersion) {
        this.dbRootPath = dbRootPath;
        this.dbVersion = dbVersion;
    }

    //Create and open a DB
    public BDBRepoHolder createInstance(long job, int harvestResultNumber) {
        String dbPath = this.getDbPath(job, harvestResultNumber);
        String dbName = getDbName(job, harvestResultNumber);

        BDBRepoHolder oldDb;
        synchronized (this) {
            oldDb = map.remove(dbName);
        }

        if (oldDb != null) {
            oldDb.shutdownDB();
        }

        //Clear the path
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            File dbDirectory = new File(dbPath);
            boolean normalDeleteResult = WctUtils.cleanDirectory(dbDirectory);
            if (!normalDeleteResult) {
                WctUtils.forceDeleteDirectory(dbDirectory);
            }
        });
        try {
            future.get(BDBRepoHolder.MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            log.error("Failed to open bdb: {} {}", job, harvestResultNumber, ex);
        }

        return openInstance(dbPath, dbName);
    }

    //Open with read mode
    public BDBRepoHolder getInstance(long job, int harvestResultNumber) {
        String dbPath = this.getDbPath(job, harvestResultNumber);
        File dbPathFile = new File(dbPath);
        if (!dbPathFile.exists()) {  //
            log.warn("Could not find Index DB: {}", dbPath);
            return null;
        }
        String dbName = getDbName(job, harvestResultNumber);
        synchronized (this) {
            if (map.containsKey(dbName)) {
                return map.get(dbName);
            }
        }
        return openInstance(dbPath, dbName);
    }

    private BDBRepoHolder openInstance(String dbPath, String dbName) {
        synchronized (this) {
            while (map.size() >= MAX_SIZE) {
                String minKey = getKeyOfEarliestDB();
                if (minKey == null) {
                    break;
                }

                BDBRepoHolder oldDb = map.remove(minKey);
                if (oldDb != null) {
                    oldDb.shutdownDB();
                }
            }

            BDBRepoHolder db = null;
            try {
                db = BDBRepoHolder.getInstance(dbPath, dbName);
            } catch (IOException ex) {
                log.error("Failed to open db: {}-->{}", dbPath, dbName, ex);
            }

            if (db != null) {
                map.put(dbName, db);
            }

            return db;
        }
    }

    private String getKeyOfEarliestDB() {
        String minKey = null;
        long minValue = System.currentTimeMillis();

        for (String key : map.keySet()) {
            BDBRepoHolder db = map.get(key);
            if (db == null) {
                continue;
            }

            if (db.getLatestTouch() <= minValue) {
                minKey = key;
                minValue = db.getLatestTouch();
            }
        }

        return minKey;
    }

    public void shutdownRepo(BDBRepoHolder db) {
        if (db == null) {
            return;
        }
        db.shutdownDB();
        String dbName = db.getDbName();
        BDBRepoHolder oldDb;
        synchronized (this) {
            oldDb = map.remove(dbName);
        }
        if (oldDb != null) {
            oldDb.shutdownDB();
        }
    }

    public void close(long job, int harvestResultNumber) {
        String dbName = getDbName(job, harvestResultNumber);
        BDBRepoHolder oldDb;
        synchronized (this) {
            oldDb = map.remove(dbName);
        }
        if (oldDb != null) {
            oldDb.shutdownDB();
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
