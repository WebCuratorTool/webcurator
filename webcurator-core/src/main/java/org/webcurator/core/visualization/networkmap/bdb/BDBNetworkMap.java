package org.webcurator.core.visualization.networkmap.bdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Borrow(copy) from openwayback
 */
@SuppressWarnings("all")
public class BDBNetworkMap {
    private static final Logger log = LoggerFactory.getLogger(BDBNetworkMap.class);
    public final static String PATH_ROOT_URLS = "rootUrls";
    public final static String PATH_MALFORMED_URLS = "malformedUrls";
    public final static String PATH_COUNT_DOMAIN = "countDomain";
    public final static String PATH_COUNT_URL = "countUrl";

    public final static String PATH_GROUP_BY_DOMAIN = "keyGroupByDomain";
    public final static String PATH_GROUP_BY_CONTENT_TYPE = "keyGroupByContentType";
    public final static String PATH_GROUP_BY_STATUS_CODE = "keyGroupByStatusCode";
    public final static String PATH_METADATA_DOMAIN_NAME = "keyMetadataDomainName";
    public final static String PATH_METADATA_CONTENT_TYPE = "keyMetadataContentType";
    public final static String PATH_METADATA_STATUS_CODE = "keyMetadataStatusCode";

    public final static Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * Maximum BDBJE file size
     */
    private final static String JE_LOG_FILEMAX = "256000000";
    /**
     * path to directory containing the BDBJE files
     */
    private String path;

    /**
     * name of BDBJE db within the path directory
     */
    private String dbName;

    /**
     * BDBJE Environment
     */
    Environment env = null;

    /**
     * BDBJE Database
     */
    Database db = null;

    /**
     * @param thePath   Directory where BDBJE files are stored
     * @param theDbName Name of files in thePath
     * @throws IOException for usual reasons, plus as database exceptions
     */
    public void initializeDB(final String thePath, final String theDbName)
            throws IOException {
        path = thePath;
        dbName = theDbName;

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setCacheSize(1024 * 1024);
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        environmentConfig.setConfigParam("je.log.fileMax", JE_LOG_FILEMAX);
        File file = new File(path);
        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new IOException("failed mkdirs(" + path + ")");
            }
        }
        env = new Environment(file, environmentConfig);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);
        databaseConfig.setTransactional(true);
        // perform other database configurations

        db = env.openDatabase(null, dbName, databaseConfig);
    }

    /**
     * shut down the BDB.
     *
     * @throws DatabaseException
     */
    public void shutdownDB() throws DatabaseException {

        if (db != null) {
            db.close();
            db = null;
        }

        if (env != null) {
            env.close();
            env = null;
        }
    }

    /**
     * @param s
     * @return byte array representation of String s in UTF-8
     */
    public static byte[] stringToBytes(String s) {
        return s.getBytes(UTF8);
    }

    /**
     * @param ba
     * @return String of UTF-8 encoded bytes ba
     */
    public static String bytesToString(byte[] ba) {
        return new String(ba, UTF8);
    }

    /**
     * @param itr
     */
    public void insertRecords(final Iterator<NetworkMapNode> itr) {
        OperationStatus status = null;
        try {
            Transaction txn = env.beginTransaction(null, null);
            try {
                Cursor cursor = db.openCursor(txn, null);
                while (itr.hasNext()) {
                    NetworkMapNode node = itr.next();
                    DatabaseEntry key = new DatabaseEntry(Long.toString(node.getId()).getBytes());
                    DatabaseEntry value = new DatabaseEntry(node.toString().getBytes());
                    status = cursor.put(key, value);
                    if (status != OperationStatus.SUCCESS) {
                        throw new RuntimeException("put() non-success status");
                    }
                }
                cursor.close();
                txn.commit();
            } catch (DatabaseException e) {
                if (txn != null) {
                    txn.abort();
                }
                e.printStackTrace();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    public void put(String keyStr, String valueStr) throws DatabaseException {
//        System.out.println(keyStr + ": " + valueStr);
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry(stringToBytes(valueStr));
        db.put(null, key, data);
    }

    public void put(long id, String valueStr) throws DatabaseException {
        put(Long.toString(id), valueStr);
    }

    public void put(String keyStr, Object obj) throws DatabaseException {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        put(keyStr, json);
    }

    public void put(long id, Object obj) throws DatabaseException {
        put(Long.toString(id), obj);
    }

    public String get(String keyStr) throws DatabaseException {
        String result = null;
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry();
        if (db.get(null, key, data, LockMode.DEFAULT)
                == OperationStatus.SUCCESS) {

            result = bytesToString(data.getData());
        }
        return result;
    }


    public String get(long id) throws DatabaseException {
        return get(Long.toString(id));
    }

    /**
     * @param keyStr
     * @throws DatabaseException
     */
    public void delete(String keyStr) throws DatabaseException {
        db.delete(null, new DatabaseEntry(stringToBytes(keyStr)));
    }

    public List<String> searchKeys(String substring) {
        List<String> result = new ArrayList<>();

        // Open the cursor.
        Cursor cursor = db.openCursor(null, null);

        // Cursors need a pair of DatabaseEntry objects to operate. These hold
        // the key and data found at any given position in the database.
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        // To iterate, just call getNext() until the last database record has been
        // read. All cursor operations return an OperationStatus, so just read
        // until we no longer see OperationStatus.SUCCESS
        while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            // getData() on the DatabaseEntry objects returns the byte array
            // held by that object. We use this to get a String value. If the
            // DatabaseEntry held a byte array representation of some other data
            // type (such as a complex object) then this operation would look
            // considerably different.
            String keyString = new String(foundKey.getData());
            if (keyString.toUpperCase().contains(substring.toUpperCase())) {
                result.add(keyString);
            }
        }

        //Close the cursor
        cursor.close();

        return result;
    }

    public Cursor openCursor(){
       return db.openCursor(null, null);
    }

    /**
     * @return Returns the dbName.
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }


//    public static void main(String[] args) throws IOException {
//        BDBNetworkMap db = new BDBNetworkMap();
//        db.initializeDB("/usr/local/wct/store/_db_temp", "resource.db");
//
//        long MAX_TRY = 1000000;
//
//        long startTime = System.currentTimeMillis();
//        for (long id = 1; id <= MAX_TRY; id++) {
//            db.put(id, "Content:" + id);
//            if (id % 1000 == 0) {
//                long endTime = System.currentTimeMillis();
//                System.out.println("Running write:" + id + ", time used:" + (endTime - startTime));
//                startTime = endTime;
//            }
//        }
//
//        db.shutdownDB();
//
//        db.initializeDB("/usr/local/wct/store/_db_temp", "resource.db");
//
//        startTime = System.currentTimeMillis();
//        for (long id = 1; id <= MAX_TRY; id++) {
//            String str = db.get(id);
//            if (str == null) {
//                System.out.println("Error:" + id);
//            }
//
//            if (id % 1000 == 0) {
//                long endTime = System.currentTimeMillis();
//                System.out.println("Running read:" + id + ", time used:" + (endTime - startTime));
//                startTime = endTime;
//            }
//        }
//
//        db.shutdownDB();
//    }
}
