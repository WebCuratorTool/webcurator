package org.webcurator.core.visualization.networkmap;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapSimpleNodeCommand;

import java.util.ArrayList;
import java.util.List;

public class NetworkMapClientLocalTest extends IndexProcessorTest {
    @Test
    public void test() throws Exception {
        this.indexer.processInternal();
        queryChildrenRecursivelyCrawl();
        queryChildrenRecursivelyFolder();
    }

    private void queryChildrenRecursivelyCrawl() {
        BDBRepoHolder db = this.pool.getInstance(this.targetInstanceId, this.harvestResultNumber);
        assert db != null;

        List<Long> allIDs = db.tblUrl.getAllEntities();
        if (allIDs.size() == 0) {
            return;
        }
        int randomIndex = this.random(allIDs.size());
        log.debug("size={}, randomIndex={}", allIDs.size(), randomIndex);

        List<ModifyRowFullData> nodes = new ArrayList<>();
        ModifyRowFullData cmd = new ModifyRowFullData();
        cmd.setId(allIDs.get(randomIndex));
        nodes.add(cmd);

        NetworkMapResult rst = this.networkMapClient.queryChildrenRecursivelyCrawl(this.targetInstanceId, this.harvestResultNumber, nodes);
        assert !StringUtils.isEmpty(rst.getPayload());

//        this.pool.close(this.targetInstanceId, this.harvestResultNumber);
    }

    private void queryChildrenRecursivelyFolder() {
        BDBRepoHolder db = this.pool.getInstance(this.targetInstanceId, this.harvestResultNumber);
        assert db != null;

        List<Long> allIDs = db.tblFolder.getAllEntities();
        if (allIDs.size() == 0) {
            return;
        }
        int randomIndex = this.random(allIDs.size() - 1);
        log.debug("size={}, randomIndex={}", allIDs.size(), randomIndex);

        List<ModifyRowFullData> nodes = new ArrayList<>();
        ModifyRowFullData cmd = new ModifyRowFullData();

        cmd.setId(allIDs.get(randomIndex));
        cmd.setFolder(true);
        nodes.add(cmd);

        NetworkMapResult rst = this.networkMapClient.queryChildrenRecursivelyFolder(this.targetInstanceId, this.harvestResultNumber, nodes);
        assert !StringUtils.isEmpty(rst.getPayload());

//        this.pool.close(this.targetInstanceId, this.harvestResultNumber);
    }
}
