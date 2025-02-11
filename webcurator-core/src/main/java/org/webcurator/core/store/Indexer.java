package org.webcurator.core.store;

import java.io.File;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.store.RunnableIndex.Mode;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class Indexer {
    private static Log log = LogFactory.getLog(Indexer.class);
    private static final Map<String, Map<Long, RunnableIndex>> runningIndexes = new HashMap<String, Map<Long, RunnableIndex>>();
    public static final Object lock = new Object();

    public static void addRunningIndex(RunnableIndex indexer, Long harvestResultOid) {
        synchronized (lock) {
            Map<Long, RunnableIndex> indexerRunningIndexes;

            if (runningIndexes.containsKey(indexer.getName())) {
                indexerRunningIndexes = runningIndexes.get(indexer.getName());
            } else {
                indexerRunningIndexes = new HashMap<Long, RunnableIndex>();
                runningIndexes.put(indexer.getName(), indexerRunningIndexes);
            }

            indexerRunningIndexes.put(harvestResultOid, indexer);
        }
    }

    public static void removeRunningIndex(String indexerName, Long harvestResultOid) {
        synchronized (lock) {
            Map<Long, RunnableIndex> indexerRunningIndexes;

            if (runningIndexes.containsKey(indexerName)) {
                indexerRunningIndexes = runningIndexes.get(indexerName);
                if (indexerRunningIndexes.containsKey(harvestResultOid)) {
                    indexerRunningIndexes.remove(harvestResultOid);
                }
            }
        }
    }

    public static boolean lastRunningIndex(String callingIndexerName, Long harvestResultOid) {
        synchronized (lock) {
            Iterator<String> it = runningIndexes.keySet().iterator();
            while (it.hasNext()) {
                String indexerName = it.next();
                if (!indexerName.equals(callingIndexerName) &&
                        containsRunningIndex(indexerName, harvestResultOid)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean containsRunningIndex(Long harvestResultOid) {
        synchronized (lock) {
            Iterator<String> it = runningIndexes.keySet().iterator();
            while (it.hasNext()) {
                String indexerName = it.next();
                if (containsRunningIndex(indexerName, harvestResultOid)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean containsRunningIndex(String indexerName, Long harvestResultOid) {
        synchronized (lock) {
            if (runningIndexes.containsKey(indexerName)) {
                Map<Long, RunnableIndex> indexerRunningIndexes = runningIndexes.get(indexerName);
                if (indexerRunningIndexes != null && indexerRunningIndexes.containsKey(harvestResultOid)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean doCreate = false;
    private List<RunnableIndex> indexers;

    public Indexer() {
        this(false);
    }

    public Indexer(boolean doCreate) {
        super();
        this.doCreate = doCreate;
    }

    public void runIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.size() <= 0) {
            log.error("No indexers are defined");
        } else {
            Iterator<RunnableIndex> it = indexers.iterator();
            while (it.hasNext()) {
                RunnableIndex indexer = it.next();
                if (indexer.isEnabled()) {
                    try {
                        //Use a new indexer each time to make it thread safe
                        RunnableIndex theCopy = indexer.getCopy();
                        theCopy.initialise(dto, directory);

                        theCopy.setMode(Mode.INDEX);
                        runIndex(dto.getOid(), theCopy);

                    } catch (Exception e) {
                        log.error("Unable to instantiate indexer: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void removeIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.size() <= 0) {
            log.error("No indexers are defined");
        } else {
            Iterator<RunnableIndex> it = indexers.iterator();
            while (it.hasNext()) {
                RunnableIndex indexer = it.next();
                if (indexer.isEnabled()) {
                    try {
                        //Use a new indexer each time to make it thread safe
                        RunnableIndex theCopy = indexer.getCopy();
                        theCopy.initialise(dto, directory);

                        theCopy.setMode(Mode.REMOVE);
                        runIndex(dto.getOid(), theCopy);

                    } catch (Exception e) {
                        log.error("Unable to instantiate indexer: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    public Boolean checkIndexing(Long hrOid) {
        return containsRunningIndex(hrOid);
    }

    private void runIndex(Long hrOid, RunnableIndex indexer) {
        //don't allow the same HR to be indexed concurrently on the same type of indexer multiple times
        if (!containsRunningIndex(indexer.getName(), hrOid)) {
            addRunningIndex(indexer, hrOid);

            new Thread(indexer).start();
        }
    }

    public static class CommandLine {
        Properties props = new Properties();

        public CommandLine(String[] args) {
            for (int i = 0; i < args.length; i += 2) {
                props.put(args[i].substring(1), args[i + 1]);
            }
        }

        public String getArg(String key) {
            return props.getProperty(key, "");
        }
    }

    public static void main(String[] args) {
        try {
//            BDBNetworkMapPool pool = Objects.requireNonNull(ApplicationContextFactory.getApplicationContext()).getBean(BDBNetworkMapPool.class);
//            pool.close(result.getTargetInstanceOid(), result.getHarvestNumber()); //Close the BDB instance when it is available

            CommandLine cl = new CommandLine(args);

            String host = cl.getArg("host");
            if (StringUtils.isEmpty(host)) {
                System.out.println("Host is not specified, set the value to: localhost");
                host = "localhost";
            }

            String port = cl.getArg("port");
            if (StringUtils.isEmpty(port)) {
                System.out.println("Port is not specified, set the value to: 8080");
                port = "8080";
            }
            String baseUrl = String.format("http://%s:%s/wct", host, port);


            String targetInstanceOid = cl.getArg("ti");
            if (StringUtils.isEmpty(targetInstanceOid)) {
                System.out.println("Target Instance ID must be specified");
                syntax();
            }

            String hrnum = cl.getArg("hrnum");
            if (StringUtils.isEmpty(hrnum)) {
                System.out.println("Harvest Result Number must be specified");
                syntax();
            }

            String baseDir = cl.getArg("baseDir");
            if (StringUtils.isEmpty(baseDir)) {
                System.out.println("Directory must be specified");
                syntax();
            }

            File dir = new File(baseDir);
            if (!dir.exists()) {
                System.out.println("Directory does not exist");
                syntax();
            }

            HarvestResultDTO dto = new HarvestResultDTO();
            dto.setTargetInstanceOid(Long.parseLong(targetInstanceOid));
            dto.setHarvestNumber(Integer.parseInt(hrnum));
            dto.setProvenanceNote("Manual Intervention");
            dto.setCreationDate(new Date());

            BDBNetworkMapPool pool = new BDBNetworkMapPool(dir.getAbsolutePath(), "4.0.1");

            Indexer indexer = new Indexer(true);
            WCTIndexer wctIndexer = new WCTIndexer(baseUrl, new RestTemplateBuilder());
            wctIndexer.setDoCreate(true);
            wctIndexer.setEnabled(true);
            wctIndexer.setPool(pool);
            List<RunnableIndex> indexers = new ArrayList<RunnableIndex>();
            indexers.add(wctIndexer);
            indexer.setIndexers(indexers);
            indexer.runIndex(dto, dir);
            boolean retContains = Indexer.containsRunningIndex(45L);
            System.out.println("Contains: " + retContains);
        } catch (Exception ex) {
            log.error(ex);
            syntax();
        }
    }

    private static void syntax() {
        System.out.println("Syntax: ");
        System.out.println(" -host hostname -port portnumber -ti tiOid -hrnum 1 -baseDir basedir");
        System.exit(1);
    }

    public boolean isDoCreate() {
        return doCreate;
    }

    public void setIndexers(List<RunnableIndex> indexers) {
        this.indexers = indexers;
    }

    public List<RunnableIndex> getIndexers() {
        return indexers;
    }
}
