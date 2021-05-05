package org.webcurator.core.visualization.networkmap.bdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.common.util.Utils;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomain;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapTreeViewPath;

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
    private final static String KEY_DB_VERSION = "Version";
    private final static String KEY_ROOT_URLS = "RootUrls";
    private final static String KEY_MALFORMED_URLS = "MalformedUrls";
    public final static String KEY_URL_COUNT = "UrlCount";
    private final static String KEY_INDIVIDUAL_DOMAIN = "domain_";
    private final static String KEY_GROUP_BY_DOMAIN = "GroupByDomain";

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
    private String dbVersion;
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
    public void initializeDB(final String thePath, final String theDbName, final String dbVersion) throws IOException {
        this.path = thePath;
        this.dbName = theDbName;
        this.dbVersion = dbVersion;

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
    private static byte[] stringToBytes(String s) {
        return s.getBytes(UTF8);
    }

    /**
     * @param ba
     * @return String of UTF-8 encoded bytes ba
     */
    private static String bytesToString(byte[] ba) {
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

    private void put(String keyStr, String valueStr) throws DatabaseException {
//        System.out.println(keyStr + ": " + valueStr);
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry(stringToBytes(valueStr));
        db.put(null, key, data);
    }

    private void put(long id, String valueStr) throws DatabaseException {
        put(Long.toString(id), valueStr);
    }

    private void put(String keyStr, Object obj) throws DatabaseException {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        put(keyStr, json);
    }

    private void put(long id, Object obj) throws DatabaseException {
        put(Long.toString(id), obj);
    }

    private String get(String keyStr) throws DatabaseException {
        String result = null;
        DatabaseEntry key = new DatabaseEntry(stringToBytes(keyStr));
        DatabaseEntry data = new DatabaseEntry();
        if (db.get(null, key, data, LockMode.DEFAULT)
                == OperationStatus.SUCCESS) {

            result = bytesToString(data.getData());
        }
        return result;
    }


    private String get(long id) throws DatabaseException {
        return get(Long.toString(id));
    }

    /**
     * @param keyStr
     * @throws DatabaseException
     */
    public void delete(String keyStr) throws DatabaseException {
        db.delete(null, new DatabaseEntry(stringToBytes(keyStr)));
    }

    private List<String> searchKeys(String substring) {
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

    public Cursor openCursor() {
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


    private void putIdList(String key, List<Long> idList) {
        this.put(key, idList);
    }

    private List<Long> getIdList(String key) {
        String json = this.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void putUrl(NetworkMapNodeDTO url) {
        String key = "uid_" + url.getId();
        String unl = url.toUnlString();
        this.put(key, unl);
    }

    public NetworkMapNodeDTO getUrl(long id) {
        String key = "uid_" + id;
        String unl = this.get(key);

        NetworkMapNodeDTO n = new NetworkMapNodeDTO();
        try {
            n.toObjectFromUnl(unl);
        } catch (Exception e) {
            return null;
        }
        return n;
    }

    public String getUrlString(long id) {
        String key = "uid_" + id;
        return this.get(key);
    }

    public void putUrlNamePairUrlId(String urlName, long urlId) {
        String key = "url_" + urlName;
        this.put(key, urlId);
    }

    public long getUrlIdByUrlName(String urlName) {
        String key = "url_" + urlName;
        String urlId = this.get(key);
        return Long.parseLong(urlId);
    }

    public NetworkMapNodeDTO getUrlByUrlName(String urlName) {
        String key1 = "url_" + urlName;
        String urlId = this.get(key1);
        String key2 = "uid_" + urlId;
        String unl = this.get(key2);
        if (Utils.isEmpty(unl)) {
            return null;
        }
        NetworkMapNodeDTO n = new NetworkMapNodeDTO();
        try {
            n.toObjectFromUnl(unl);
        } catch (Exception e) {
            return null;
        }
        return n;
    }

    public void putTreeViewPath(NetworkMapTreeViewPath path) {
        String key = "path_" + path.getId();
        String unl = path.toUnlString();
        log.debug("TreeViewPath: {}", unl);
        this.put(key, unl);
    }

    public NetworkMapTreeViewPath getTreeViewPath(long id) {
        String key = "path_" + id;
        String unl = this.get(key);
        NetworkMapTreeViewPath path = new NetworkMapTreeViewPath();
        try {
            path.toObjectFromUnl(unl);
        } catch (Exception e) {
            return null;
        }
        return path;
    }

    public void putRootUrlIdList(List<Long> list) {
        this.put(KEY_ROOT_URLS, list);
    }

    public List<Long> getRootUrlIdList() {
        return this.getIdList(KEY_ROOT_URLS);
    }

    public void putMalformedUrlIdList(List<Long> list) {
        this.put(KEY_MALFORMED_URLS, list);
    }

    public List<Long> getMalformedUrlIdList() {
        return this.getIdList(KEY_MALFORMED_URLS);
    }

    public void putIndividualDomainIdList(long domainId, List<Long> list) {
        String key = KEY_INDIVIDUAL_DOMAIN + "_" + domainId;
        this.put(key, list);
    }

    public List<Long> getIndividualDomainIdList(long domainId) {
        String key = KEY_INDIVIDUAL_DOMAIN + "_" + domainId;
        return this.getIdList(key);
    }

    public void putRootDomain(NetworkMapDomain domain) {
        this.put(KEY_GROUP_BY_DOMAIN, domain);
    }

    public NetworkMapDomain getRootDomain() {
        String json = this.get(KEY_GROUP_BY_DOMAIN);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, NetworkMapDomain.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getRootDomainString() {
        return this.get(KEY_GROUP_BY_DOMAIN);
    }

    public void putDbVersionStamp(String dbVersion) {
        this.put(KEY_DB_VERSION, dbVersion);
    }

    public String getDbVersionStamp() {
        return this.get(KEY_DB_VERSION);
    }

    public void putUrlCount(long urlCount) {
        this.put(KEY_URL_COUNT, urlCount);
    }

    public long getUrlCount() {
        String val = this.get(KEY_URL_COUNT);
        return Long.parseLong(val);
    }
}
