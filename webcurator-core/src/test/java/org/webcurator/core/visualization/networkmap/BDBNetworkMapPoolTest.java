package org.webcurator.core.visualization.networkmap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BDBNetworkMapPoolTest {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BDBNetworkMapPoolTest.class);
    private static final String DB_ROOT_PATH = "/tmp/bdb";
    private static final String DB_VERSION = "snapshot";
    private static final BDBNetworkMapPool pool = new BDBNetworkMapPool(DB_ROOT_PATH, DB_VERSION);
    private static final NetworkMapClientLocal clientLocal = new NetworkMapClientLocal(pool, null);
    private static final Random rand = new Random();
    private static final int JOB_ACCOUNT = 20;


    static class Worker extends Thread {
        private final long job;
        private final int hrNum;

        public Worker(long job, int hrNum) {
            this.job = job;
            this.hrNum = hrNum;
        }

        public void run() {
            NetworkMapResult dbVersion = clientLocal.getDbVersion(this.job, this.hrNum);
            log.info("Get the version {}-{} {}", this.job, this.hrNum, dbVersion.getPayload());
        }
    }

    private static void runOneRound() {
        List<Thread> threads = new ArrayList<>();
        for (long j = 1; j <= JOB_ACCOUNT; j++) {
            long job = rand.nextInt(JOB_ACCOUNT) + 1;
//            long job = j;
            Worker worker = new Worker(job, 1);
            threads.add(worker);
            worker.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        threads.clear();
    }

    @Test
    public void testBDBNetworkMapPoolMultipleThreads() {
        Logger logger = (Logger) LoggerFactory.getLogger("org.webcurator");

        // Set log level to INFO
        logger.setLevel(Level.INFO);

        for (int i = 0; i < JOB_ACCOUNT; i++) {
            long job = i + 1;
            pool.createInstance(job, 1);
            pool.close(job, 1);
        }

        for (int i = 0; i < 3; i++) {
            runOneRound();
            System.out.printf("Tested round: %d", i);
            System.out.println();
        }
    }

    @Test
    public void testCreateInstance() {
        BDBRepoHolder db = pool.createInstance(1, 1);
        assert db != null;
    }
}
