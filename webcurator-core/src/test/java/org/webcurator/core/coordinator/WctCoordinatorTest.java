package org.webcurator.core.coordinator;


import com.anotherbigidea.util.Base64;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.webcurator.core.archive.MockSipBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.HarvesterType;
import org.webcurator.core.harvester.agent.MockHarvestAgent;
import org.webcurator.core.harvester.agent.MockHarvestAgentFactory;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.harvester.coordinator.HarvestQaManager;
import org.webcurator.core.notification.MockInTrayManager;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.scheduler.TargetInstanceManager;

import org.webcurator.core.screenshot.ScreenshotClient;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.core.store.MockDigitalAssetStore;
import org.webcurator.core.store.MockDigitalAssetStoreFactory;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvesterStatusDTO;
import org.webcurator.domain.model.dto.QueuedTargetInstanceDTO;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;
import org.webcurator.test.BaseWCTTest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@SuppressWarnings("all")
@AutoConfigureMockMvc
public class WctCoordinatorTest extends BaseWCTTest<WctCoordinator> {
    private MockHarvestAgentFactory harvestAgentFactory = new MockHarvestAgentFactory();
    private MockTargetInstanceManager mockTargetInstanceManager = null;
    private HarvestAgentManager harvestAgentManager;
    private TargetInstanceDAO tiDao;
    private HarvestLogManager mockHarvestLogManager;
    private DigitalAssetStore mockDigitalAssetStore;
    private MockDigitalAssetStoreFactory mockDigitalAssetStoreFactory;
    private HarvestResultManager mockHarvestResultManager;
    private VisualizationDirectoryManager directoryManager; // = new VisualizationDirectoryManager("/usr/local/wct/webapp", "logs", "reports");
    private ScreenshotClient mockScreenshotClient;

    public WctCoordinatorTest() {
        super(WctCoordinator.class, "/org/webcurator/core/harvester/coordinator/HarvestCoordinatorImplTest.xml");
    }

    // Override BaseWCTTest setup method
    public void setUp() throws Exception {
        // call the overridden method as well
        super.setUp();
        File tempWebAppDirectory = java.nio.file.Files.createTempDirectory("webapp").toFile();
        tempWebAppDirectory.mkdirs();

        directoryManager = new VisualizationDirectoryManager(tempWebAppDirectory.getAbsolutePath(), "logs", "reports");

        //String userDirectory = new File("").getAbsolutePath();
        //System.out.println(userDirectory);
//        long tiId = 5000L;
//        int hrNumber = 1;
//
//        File storeDirectory = new File(tempWebAppDirectory, tiId + File.separator + hrNumber);
//        storeDirectory.mkdirs();

//        Resource resource_warc = new ClassPathResource("org/webcurator/domain/model/core/archiveFiles/IAH-20080610152754-00000-test.warc.gz");
//        File file_warc = new File(storeDirectory, "IAH-20080610152754-00000-test.warc.gz");
//        FileUtils.copyInputStreamToFile(resource_warc.getInputStream(), file_warc);

        // add the extra bits
        mockTargetInstanceManager = new MockTargetInstanceManager(testFile);
        testInstance.setTargetInstanceManager(mockTargetInstanceManager);
        testInstance.setTargetManager(new MockTargetManager(testFile));
        testInstance.setInTrayManager(new MockInTrayManager(testFile));
        testInstance.setSipBuilder(new MockSipBuilder(testFile));

        harvestAgentManager = new HarvestAgentManager();
        harvestAgentManager.setHarvestAgentFactory(harvestAgentFactory);
        harvestAgentManager.setTargetInstanceManager(mockTargetInstanceManager);
        testInstance.setHarvestAgentManager(harvestAgentManager);

        mockHarvestLogManager = mock(HarvestLogManager.class);
        testInstance.setHarvestLogManager(mockHarvestLogManager);

//        tiDao = new MockTargetInstanceDAO(testFile);
        tiDao = mockTargetInstanceManager.getTargetInstanceDAO();
        testInstance.setTargetInstanceDao(tiDao);
        harvestAgentManager.setTargetInstanceDao(tiDao);


        testInstance.setVisualizationDirectoryManager(directoryManager);

        mockDigitalAssetStore = mock(DigitalAssetStore.class);
        mockDigitalAssetStoreFactory = new MockDigitalAssetStoreFactory(mockDigitalAssetStore);
        testInstance.setDigitalAssetStoreFactory(mockDigitalAssetStoreFactory);

        testInstance.setHarvestQaManager(mock(HarvestQaManager.class));

        mockHarvestResultManager = mock(HarvestResultManager.class);
        testInstance.setHarvestResultManager(mockHarvestResultManager);

        mockScreenshotClient = mock(ScreenshotClient.class);
        when(mockScreenshotClient.createScreenshots(any())).thenReturn(true);
        testInstance.setScreenshotClient(mockScreenshotClient);
    }

    private HarvesterStatusDTO getStatusDTO(String aStatus) {
        HarvesterStatusDTO sdto = new HarvesterStatusDTO();
        sdto.setStatus(aStatus);
        return sdto;
    }

    @Test
    public final void testHeartbeatQueuedRunning() {

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_QUEUED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5001", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        testInstance.heartbeat(aStatus);
        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
    }

    @Test
    public final void testHeartbeatPausedRunning() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_PAUSED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5001", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        testInstance.heartbeat(aStatus);
        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
    }

    @Test
    public final void testHeartbeatRunningPaused() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_RUNNING);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5001", getStatusDTO("Paused"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        testInstance.heartbeat(aStatus);
        assertTrue(ti.getState().equals(TargetInstance.STATE_PAUSED));
    }

    @Test
    public final void testHeartbeatRunningFinished() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_RUNNING);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5001", getStatusDTO("Finished"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        testInstance.heartbeat(aStatus);
        assertTrue(ti.getState().equals(TargetInstance.STATE_STOPPING));
    }

    @Test
    public final void testHeartbeatRunningAborted() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_RUNNING);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5001", getStatusDTO("Could not launch job - Fatal InitializationException"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        testInstance.heartbeat(aStatus);
        assertTrue(ti.getState().equals(TargetInstance.STATE_ABORTED));
    }

    @Test
    public final void testHarvestOrQueue() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        aStatus.setHarvesterType(HarvesterType.HERITRIX1.name());
        testInstance.heartbeat(aStatus);

        QueuedTargetInstanceDTO dto = new QueuedTargetInstanceDTO(ti.getOid(), ti.getScheduledTime(), ti.getPriority(),
                ti.getState(), ti.getBandwidthPercent(), ti.getOwningUser().getAgency().getName());
        testInstance.harvestOrQueue(dto);

        ti = tiDao.load(5001L);
        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
    }

    @Test
    public final void testHarvestOrQueuePaused() {
        testInstance.pauseQueue();

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        QueuedTargetInstanceDTO dto = new QueuedTargetInstanceDTO(ti.getOid(), ti.getScheduledTime(), ti.getPriority(),
                ti.getState(), ti.getBandwidthPercent(), ti.getOwningUser().getAgency().getName());

        testInstance.harvestOrQueue(dto);

        assertTrue(ti.getState().equals(TargetInstance.STATE_QUEUED));
    }

    @Test
    public final void testHarvestOrQueueMemoryWarning() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        aStatus.setMemoryWarning(true);
        testInstance.heartbeat(aStatus);

        QueuedTargetInstanceDTO dto = new QueuedTargetInstanceDTO(ti.getOid(), ti.getScheduledTime(), ti.getPriority(),
                ti.getState(), ti.getBandwidthPercent(), ti.getOwningUser().getAgency().getName());

        testInstance.harvestOrQueue(dto);

        ti = tiDao.load(5001L);
        assertTrue(ti.getState().equals(TargetInstance.STATE_QUEUED));
    }

    @Test
    public final void testAgentPaused() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        aStatus.setAcceptTasks(false);
        aStatus.setHarvesterType(HarvesterType.HERITRIX1.name());
        testInstance.heartbeat(aStatus);

        QueuedTargetInstanceDTO dto = new QueuedTargetInstanceDTO(ti.getOid(), ti.getScheduledTime(), ti.getPriority(),
                ti.getState(), ti.getBandwidthPercent(), ti.getOwningUser().getAgency().getName());

        testInstance.harvestOrQueue(dto);

        ti = tiDao.load(5001L);
        assertEquals(TargetInstance.STATE_QUEUED, ti.getState());

        aStatus.setAcceptTasks(true);
        testInstance.harvestOrQueue(dto);

        ti = tiDao.load(5001L);
        assertEquals(TargetInstance.STATE_RUNNING, ti.getState());

    }

    @Test
    public final void testHarvest() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        testInstance.harvest(ti, has);

        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
    }

    @Test
    public final void testHarvestStoreSeedHistory() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        mockTargetInstanceManager.setStoreSeedHistory(true);

        testInstance.harvest(ti, has);

        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
        assertNotNull(ti.getSeedHistory());
        assertTrue(ti.getSeedHistory().size() > 0);
    }

    @Test
    public final void testHarvestDontStoreSeedHistory() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        mockTargetInstanceManager.setStoreSeedHistory(false);

        testInstance.harvest(ti, has);

        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
        assertNotNull(ti.getSeedHistory());
        assertEquals(ti.getSeedHistory().size(), 0);
    }

    @Test
    public final void testHarvestPaused() {
        testInstance.pauseQueue();

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        testInstance.harvest(ti, has);

        assertTrue(ti.getState().equals(TargetInstance.STATE_SCHEDULED));
    }

    @Test
    public final void testHarvestMemoryWarning() {
        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        aStatus.setMemoryWarning(true);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        testInstance.harvest(ti, has);

        assertTrue(ti.getState().equals(TargetInstance.STATE_SCHEDULED));
    }

    @Test
    public final void testHarvestProfilePrefix() throws DigitalAssetStoreException {
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        testInstance.heartbeat(aStatus);

        HarvestAgentStatusDTO has = harvestAgentManager.getHarvestAgents().get("Test Agent");

        testInstance.harvest(ti, has);

        MockHarvestAgent agent = harvestAgentFactory.getMockHarvestAgent();
        String profile = agent.getProfileString();
        String ti_oid = ti.getOid().toString();

        assertTrue(ti.getTarget().getProfile().getProfile().contains("IAH-${TI_OID}"));
        assertFalse(profile.contains("IAH-${TI_OID}"));
        assertTrue(profile.contains("IAH-" + ti_oid));

    }

    @Test
    public final void testCompleteArchiving() {
        String archiveId = "12345";
        MockDigitalAssetStoreFactory factory = new MockDigitalAssetStoreFactory(new MockDigitalAssetStore());
        testInstance.setDigitalAssetStoreFactory(factory);

        TargetInstance ti = tiDao.load(5000L);
        ti.setState(TargetInstance.STATE_ENDORSED);
        assertNull(ti.getArchivedTime());

        testInstance.completeArchiving(5000L, archiveId);
        assertEquals(ti.getState(), TargetInstance.STATE_ARCHIVED);
        assertNotNull(ti.getArchivedTime());
        assertEquals(ti.getArchiveIdentifier(), archiveId);
        MockDigitalAssetStore store = (MockDigitalAssetStore) factory.getDAS();
        assertEquals(2, store.getRemovedIndexes().size());
    }

    @Ignore
    @Test
    public final void testReIndexHarvestResult() {
        MockDigitalAssetStore store = new MockDigitalAssetStore();

        testInstance.setDigitalAssetStoreFactory(new MockDigitalAssetStoreFactory(store));

        {
            TargetInstance ti = tiDao.load(5000L);
            ti.setState(TargetInstance.STATE_HARVESTED);

            List<HarvestResult> results = ti.getHarvestResults();

            int numResults = results.size();
            assertTrue(numResults > 0);

            HarvestResult result = results.get(numResults - 1);
            assertNotNull(result);

            result.setState(HarvestResult.STATE_INDEXING);
            store.setCheckIndexingReturn(true);
            assertFalse(testInstance.reIndexHarvestResult(result));
            assertEquals(ti.getState(), TargetInstance.STATE_HARVESTED);
            assertEquals(result.getState(), HarvestResult.STATE_INDEXING);
            assertEquals(results.size(), numResults);
            assertEquals(results.get(results.size() - 1).getState(), HarvestResult.STATE_INDEXING);
        }
        {
            TargetInstance ti = tiDao.load(5000L);
            ti.setState(TargetInstance.STATE_HARVESTED);

            List<HarvestResult> results = ti.getHarvestResults();

            int numResults = results.size();
            assertTrue(numResults > 0);

            HarvestResult result = results.get(numResults - 1);
            assertNotNull(result);

            result.setState(HarvestResult.STATE_INDEXING);
            store.setCheckIndexingReturn(false);
            assertTrue(testInstance.reIndexHarvestResult(result));
            assertEquals(ti.getState(), TargetInstance.STATE_HARVESTED);
            assertEquals(result.getState(), HarvestResult.STATE_ABORTED);
            assertEquals(results.size(), numResults + 1);
            assertEquals(results.get(results.size() - 1).getState(), HarvestResult.STATE_INDEXING);
        }
    }

    @Test
    public final void testHarvestComplete() {
        MockDigitalAssetStore store = new MockDigitalAssetStore();

        testInstance.setDigitalAssetStoreFactory(new MockDigitalAssetStoreFactory(store));

        TargetInstance ti = tiDao.load(5001L);
        ti.setState(TargetInstance.STATE_STOPPING);
        HarvestResultDTO ahr = new HarvestResultDTO();
        ahr.setCreationDate(new Date());
        ahr.setTargetInstanceOid(ti.getOid());
        ahr.setProvenanceNote("Original Harvest");

        List<HarvestResult> results = ti.getHarvestResults();
        assertTrue(results.isEmpty());

        testInstance.harvestComplete(ahr);

        assertEquals(ti.getState(), TargetInstance.STATE_HARVESTED);
        results = ti.getHarvestResults();
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        testInstance.harvestComplete(ahr);

        assertEquals(ti.getState(), TargetInstance.STATE_HARVESTED);
        results = ti.getHarvestResults();
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    public void testFinaliseIndex() {
        long tiId = 5000L;
        int hrNum = 1;
        testInstance.finaliseIndex(tiId, hrNum, true);
    }

    @Test
    public void testStop() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstance mockTi = mock(TargetInstance.class);
        testInstance.stop(mockTi);
        verify(mockHarvestAgentManager).stop(mockTi);

    }

    @Test
    public void testQueueOptimizableInstancesNoUpcoming() {
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        harvestAgentManager.setTargetInstanceDao(mockTiDao);

        ArrayList<QueuedTargetInstanceDTO> newArrayList = Lists.newArrayList();
        when(mockTiDao.getUpcomingJobs(anyLong())).thenReturn(newArrayList);
        testInstance.setTargetInstanceDao(mockTiDao);
        testInstance.setHarvestOptimizationEnabled(true);
        testInstance.queueOptimisableInstances();
        verify(mockTiDao).getUpcomingJobs(anyLong());
    }

    @Test
    public void testQueueOptimizableInstances() {
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        harvestAgentManager.setTargetInstanceDao(mockTiDao);

        long tiOid = 1234L;
        long abstractTargetOid = 4312L;

        ArrayList<QueuedTargetInstanceDTO> queuedTiList = Lists.newArrayList();
        QueuedTargetInstanceDTO mockQueued = mock(QueuedTargetInstanceDTO.class);
        when(mockQueued.getOid()).thenReturn(tiOid);
        queuedTiList.add(mockQueued);
        when(mockTiDao.getUpcomingJobs(anyLong())).thenReturn(queuedTiList);

        TargetInstance mockTi = mock(TargetInstance.class);
        when(mockTiDao.load(tiOid)).thenReturn(mockTi);
        when(mockTiDao.populate(mockTi)).thenReturn(mockTi);
        Target mockTarget = mock(Target.class);
        when(mockTarget.isAllowOptimize()).thenReturn(true);
        AbstractTarget mockAbstractTarget = mock(AbstractTarget.class);
        when(mockTi.getTarget()).thenReturn(mockAbstractTarget);
        when(mockAbstractTarget.getObjectType()).thenReturn(AbstractTarget.TYPE_TARGET);
        when(mockAbstractTarget.getOid()).thenReturn(abstractTargetOid);

        TargetManager mockTargetManager = mock(TargetManager.class);
        when(mockTargetManager.load(abstractTargetOid)).thenReturn(mockTarget);

        testInstance.setHarvestOptimizationEnabled(true);
        testInstance.setTargetInstanceDao(mockTiDao);
        testInstance.setTargetManager(mockTargetManager);
        testInstance.queueOptimisableInstances();
        verify(mockTiDao).getUpcomingJobs(anyLong());
        verify(mockTiDao).load(tiOid);
        verify(mockTiDao).populate(mockTi);
        verify(mockTargetManager).load(abstractTargetOid);

    }

    @Test
    public void testResume() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        when(mockTargetInstance.isAppliedBandwidthRestriction()).thenReturn(true);
        testInstance.resume(mockTargetInstance);
        verify(mockHarvestAgentManager).resume(mockTargetInstance);
    }

    @Test
    public void testPause() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        testInstance.pause(mockTargetInstance);
        verify(mockHarvestAgentManager).pause(mockTargetInstance);
    }

    @Test
    public void testResumeAll() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        testInstance.resumeAll();
        verify(mockHarvestAgentManager).resumeAll();
    }

    @Test
    public void testPauseAll() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        testInstance.pauseAll();
        verify(mockHarvestAgentManager).pauseAll();
    }

    @Test
    public void testAbort() {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        when(mockTargetInstance.isAppliedBandwidthRestriction()).thenReturn(true);
        testInstance.abort(mockTargetInstance);
        verify(mockHarvestAgentManager).abort(mockTargetInstance);
    }

    @Test
    public void testPurgeDigitalAssetsNone() {
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        List<TargetInstance> purgeableTargetInstances = Arrays.<TargetInstance>asList();
        when(mockTiDao.findPurgeableTargetInstances(any(Date.class))).thenReturn(purgeableTargetInstances);
        testInstance.setTargetInstanceDao(mockTiDao);
        testInstance.purgeDigitalAssets();
    }

    @Test
    public void testPurgeDigitalAssetsOne() throws Exception {
        TargetInstanceManager mockTargetInstanceManager = mock(TargetInstanceManager.class);
        testInstance.setTargetInstanceManager(mockTargetInstanceManager);
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        testInstance.setTargetInstanceDao(mockTiDao);
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        List<TargetInstance> purgeableTargetInstances = Arrays.asList(mockTargetInstance);
        when(mockTiDao.findPurgeableTargetInstances(any(Date.class))).thenReturn(purgeableTargetInstances);
        testInstance.purgeDigitalAssets();
        verify(mockTargetInstanceManager).purgeTargetInstance(mockTargetInstance);
        verify(mockDas).purge(any(List.class));
    }

    @Test
    public void testPurgeDigitalAssetsMultiple() throws Exception {
        TargetInstanceManager mockTargetInstanceManager = mock(TargetInstanceManager.class);
        testInstance.setTargetInstanceManager(mockTargetInstanceManager);
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        testInstance.setTargetInstanceDao(mockTiDao);
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance mockTargetInstance1 = mock(TargetInstance.class);
        TargetInstance mockTargetInstance2 = mock(TargetInstance.class);
        List<TargetInstance> purgeableTargetInstances = Arrays.asList(mockTargetInstance1, mockTargetInstance2);
        when(mockTiDao.findPurgeableTargetInstances(any(Date.class))).thenReturn(purgeableTargetInstances);
        testInstance.purgeDigitalAssets();
        verify(mockTargetInstanceManager).purgeTargetInstance(mockTargetInstance1);
        verify(mockTargetInstanceManager).purgeTargetInstance(mockTargetInstance2);
        verify(mockDas).purge(any(List.class));
    }

    @Test
    public void testPurgeAbortedTargetInstancesNone() throws Exception {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstanceManager mockTargetInstanceManager = mock(TargetInstanceManager.class);
        testInstance.setTargetInstanceManager(mockTargetInstanceManager);
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        testInstance.setTargetInstanceDao(mockTiDao);
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        List<TargetInstance> purgeableTargetInstances = Arrays.asList();
        when(mockTiDao.findPurgeableAbortedTargetInstances(any(Date.class))).thenReturn(purgeableTargetInstances);
        testInstance.purgeAbortedTargetInstances();
        verifyNoMoreInteractions(mockHarvestAgentManager, mockDas, mockTargetInstanceManager);
    }

    @Test
    public void testPurgeAbortedTargetInstances() throws Exception {
        HarvestAgentManager mockHarvestAgentManager = mock(HarvestAgentManager.class);
        testInstance.setHarvestAgentManager(mockHarvestAgentManager);
        TargetInstanceManager mockTargetInstanceManager = mock(TargetInstanceManager.class);
        testInstance.setTargetInstanceManager(mockTargetInstanceManager);
        TargetInstanceDAO mockTiDao = mock(TargetInstanceDAO.class);
        testInstance.setTargetInstanceDao(mockTiDao);
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance mockTargetInstance1 = mock(TargetInstance.class);
        TargetInstance mockTargetInstance2 = mock(TargetInstance.class);
        List<TargetInstance> purgeableTargetInstances = Arrays.asList(mockTargetInstance1, mockTargetInstance2);
        when(mockTiDao.findPurgeableAbortedTargetInstances(any(Date.class))).thenReturn(purgeableTargetInstances);
        testInstance.purgeAbortedTargetInstances();
        verify(mockHarvestAgentManager).purgeAbortedTargetInstances(any(List.class));
        verify(mockDas).purgeAbortedTargetInstances(any(List.class));
        verify(mockTargetInstanceManager).purgeTargetInstance(mockTargetInstance1);
        verify(mockTargetInstanceManager).purgeTargetInstance(mockTargetInstance2);
    }

    @Test
    public void testListLogFiles() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        testInstance.listLogFiles(mockTargetInstance);
        verify(mockHarvestLogManager).listLogFiles(mockTargetInstance);
    }

    @Test(expected = WCTRuntimeException.class)
    public void testListLogFilesException() {
        testInstance.listLogFiles(null);
    }

    @Test
    public void testTailLog() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        int numLines = 123;
        String fileName = "testFile";
        testInstance.tailLog(mockTargetInstance, fileName, numLines);
        verify(mockHarvestLogManager).tailLog(mockTargetInstance, fileName, numLines);
    }

    @Test
    public void testCountLogLines() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        testInstance.countLogLines(mockTargetInstance, fileName);
        verify(mockHarvestLogManager).countLogLines(mockTargetInstance, fileName);
    }

    @Test
    public void testListLogFileAttributes() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        testInstance.listLogFileAttributes(mockTargetInstance);
        verify(mockHarvestLogManager).listLogFileAttributes(mockTargetInstance);
    }

    @Test
    public void testHeadLog() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        int numLines = 123;
        String fileName = "testFile";
        testInstance.headLog(mockTargetInstance, fileName, numLines);
        verify(mockHarvestLogManager).headLog(mockTargetInstance, fileName, numLines);
    }

    @Test
    public void testGetLog() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        int startLine = 1;
        int numLines = 123;
        String fileName = "testFile";
        testInstance.getLog(mockTargetInstance, fileName, startLine, numLines);
        verify(mockHarvestLogManager).getLog(mockTargetInstance, fileName, startLine, numLines);
    }

    @Test
    public void testGetFirstLogLineContaining() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        String match = "match";
        testInstance.getFirstLogLineContaining(mockTargetInstance, fileName, match);
        verify(mockHarvestLogManager).getFirstLogLineContaining(mockTargetInstance, fileName, match);
    }

    @Test
    public void testGetFirstLogLineBeginning() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        String match = "match";
        testInstance.getFirstLogLineBeginning(mockTargetInstance, fileName, match);
        verify(mockHarvestLogManager).getFirstLogLineBeginning(mockTargetInstance, fileName, match);
    }

    @Test
    public void testGetFirstLogLineAfterTimestamp() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        Long timestamp = 123456789L;
        testInstance.getFirstLogLineAfterTimeStamp(mockTargetInstance, fileName, timestamp);
        verify(mockHarvestLogManager).getFirstLogLineAfterTimeStamp(mockTargetInstance, fileName, timestamp);
    }

    @Test
    public void testGetLogFile() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        testInstance.getLogfile(mockTargetInstance, fileName);
        verify(mockHarvestLogManager).getLogfile(mockTargetInstance, fileName);
    }

    @Test
    public void testGetHopPath() {
        TargetInstance mockTargetInstance = mock(TargetInstance.class);
        String fileName = "testFile";
        String match = "match";
        testInstance.getHopPath(mockTargetInstance, fileName, match);
        verify(mockHarvestLogManager).getHopPath(mockTargetInstance, fileName, match);
    }

    @Test
    public void testComprehensiveHarvest() throws DigitalAssetStoreException {
        long tiOid = 5000L;
        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        testInstance.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);

        TargetInstance ti = tiDao.load(tiOid);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        aStatus.setHarvesterType(HarvesterType.HERITRIX1.name());
        testInstance.heartbeat(aStatus);

        QueuedTargetInstanceDTO dto = new QueuedTargetInstanceDTO(ti.getOid(), ti.getScheduledTime(), ti.getPriority(),
                ti.getState(), ti.getBandwidthPercent(), ti.getOwningUser().getAgency().getName());

        testInstance.setHarvestOptimizationEnabled(true);

        testInstance.processSchedule();

        ti = tiDao.load(tiOid);
        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));

        ti.setState(TargetInstance.STATE_SCHEDULED);
        testInstance.harvest(ti, aStatus);
        ti = tiDao.load(tiOid);
        assertTrue(ti.getState().equals(TargetInstance.STATE_RUNNING));
    }

    @Test
    public void testApplyPruneAndImport() {
        TargetInstance ti = tiDao.load(5000);
        List<HarvestResult> hrList = ti.getHarvestResults();
        assertTrue(hrList.size() > 0);

        HarvestResult hr = hrList.get(0);

        ModifyApplyCommand cmd = new ModifyApplyCommand();
        cmd.setTargetInstanceId(ti.getOid());
        cmd.setHarvestResultId(hr.getOid());
        cmd.setHarvestResultNumber(hr.getHarvestNumber());

        {
            ModifyResult result = testInstance.applyPruneAndImport(cmd);
            assertNotEquals(VisualizationConstants.RESP_CODE_SUCCESS, result.getRespCode());
        }

        {
            //Starting from patch crawling
            ModifyRowFullData metadata = new ModifyRowFullData();
            metadata.setOption("recrawl");
            metadata.setUrl("http://a.b.c/");
            cmd.getDataset().clear();
            cmd.getDataset().add(metadata);

            ti.setState(TargetInstance.STATE_HARVESTED);
            tiDao.save(ti);
            ModifyResult result = testInstance.applyPruneAndImport(cmd);
            if (result != null) {
                assertEquals(VisualizationConstants.RESP_CODE_SUCCESS, result.getRespCode());
            }
            assertEquals(TargetInstance.STATE_PATCHING, ti.getState());

            hrList = ti.getHarvestResults();
            assertTrue(hrList.size() > 1);

            HarvestResult newHarvestResult = hrList.get(hrList.size() - 1);
            assertEquals(newHarvestResult.getHarvestNumber(), cmd.getNewHarvestResultNumber());
            assertEquals(HarvestResult.STATE_CRAWLING, newHarvestResult.getState());
        }

        {
            //Going to modification process directly
            ModifyRowFullData metadata = new ModifyRowFullData();
            metadata.setOption("file");
            metadata.setOption("aaa");
            cmd.getDataset().clear();
            cmd.getDataset().add(metadata);

            ti.setState(TargetInstance.STATE_HARVESTED);
            tiDao.save(ti);

            when(mockDigitalAssetStore.initialPruneAndImport(any(ModifyApplyCommand.class))).thenReturn(new ModifyResult());

            ModifyResult result = testInstance.applyPruneAndImport(cmd);
            assertEquals(VisualizationConstants.RESP_CODE_SUCCESS, result.getRespCode());
            assertEquals(TargetInstance.STATE_PATCHING, ti.getState());

            hrList = ti.getHarvestResults();
            assertTrue(hrList.size() > 1);

            HarvestResult newHarvestResult = hrList.get(hrList.size() - 1);
            assertEquals(newHarvestResult.getHarvestNumber(), cmd.getNewHarvestResultNumber());
            assertEquals(HarvestResult.STATE_MODIFYING, newHarvestResult.getState());
        }
    }

    @Test
    public void testProbeMimeType() throws IOException {
        File jpgFile = new File("src/test/resources/org/webcurator/core/coordinator/users.jpg");
        String jpgMimeType = testInstance.probeMimeType(jpgFile);
        assertNotNull(jpgMimeType);
        assertTrue(jpgMimeType.equalsIgnoreCase("image/jpeg"));

        File jpegFile = new File("src/test/resources/org/webcurator/core/coordinator/star.jpeg");
        String jpegMimeType = testInstance.probeMimeType(jpegFile);
        assertNotNull(jpegMimeType);
        assertTrue(jpegMimeType.equalsIgnoreCase("image/jpeg"));

        File pngFile = new File("src/test/resources/org/webcurator/core/coordinator/icon.png");
        String pngMimeType = testInstance.probeMimeType(pngFile);
        assertNotNull(pngMimeType);
        assertTrue(pngMimeType.equalsIgnoreCase("image/png"));
    }

    @Test
    public void testDasDownloadFile() {
        long targetInstanceId = 5010;
        int harvestResultNumber = 1;
        String fileName = "expand.png";
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse rsp = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);

        try {
            when(rsp.getOutputStream()).thenReturn(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        Answer answer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                log.debug("Received");
                return new Object();
            }
        };

        try {
            File fileDirectory = new File(directoryManager.getUploadDir(targetInstanceId));
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
            File fileTest = new File(directoryManager.getUploadDir(targetInstanceId), fileName);
//            Files.write("test".getBytes(), fileTest);
            FileUtils.writeStringToFile(fileTest, "test");
            testInstance.dasDownloadFile(targetInstanceId, harvestResultNumber, fileName, req, rsp);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
        assert true;
    }

    @Test
    public void testDasHeartbeat() {
        List<HarvestResultDTO> harvestResultDTOList = new ArrayList<>();
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTOList.add(harvestResultDTO);

        TargetInstance ti = tiDao.load(5000L);
        HarvestResult hr = ti.getHarvestResult(1);
        harvestResultDTO.setTargetInstanceOid(ti.getOid());
        harvestResultDTO.setHarvestNumber(hr.getHarvestNumber());
        harvestResultDTO.setState(hr.getState());
        harvestResultDTO.setStatus(HarvestResult.STATUS_PAUSED);

        testInstance.dasHeartbeat(harvestResultDTOList);
        verify(mockHarvestResultManager).updateHarvestResultsStatus(harvestResultDTOList);
    }

    @Test
    public void testModificationComplete() throws DigitalAssetStoreException {
        long job = 5000L;
        int harvestResultNumber = 1;

        testInstance.dasModificationComplete(job, harvestResultNumber);

        verify(mockDigitalAssetStore).initiateIndexing(any(HarvestResultDTO.class));
    }

    @Test
    public void testDasQuerySeedHistory() {
        long job = 5000L;
        int harvestResultNumber = 1;

        TargetInstance ti = tiDao.load(job);

        SeedHistorySetDTO seedHistorySetDTO = testInstance.dasQuerySeedHistory(job, harvestResultNumber);

        assertNotEquals(null, seedHistorySetDTO);
        assertEquals(ti.getSeedHistory().size(), seedHistorySetDTO.getSeeds().size());
    }

    @Test
    public void testDasUpdateHarvestResultStatus() {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();

        TargetInstance ti = tiDao.load(5000L);
        HarvestResult hr = ti.getHarvestResult(1);
        harvestResultDTO.setTargetInstanceOid(ti.getOid());
        harvestResultDTO.setHarvestNumber(hr.getHarvestNumber());
        harvestResultDTO.setState(hr.getState());
        harvestResultDTO.setStatus(HarvestResult.STATUS_PAUSED);

        testInstance.dasUpdateHarvestResultStatus(harvestResultDTO);
        verify(mockHarvestResultManager).updateHarvestResultStatus(harvestResultDTO);
    }

    @Test
    public void testPushPruneAndImport() throws IOException {
        long job = 5000L;
        int harvestResultNumber = 1;

        TargetInstance ti = tiDao.load(job);

        {
            ti.setState(TargetInstance.STATE_SCHEDULED);
            tiDao.save(ti);
            boolean result = testInstance.pushPruneAndImport(job, harvestResultNumber);
            assertFalse(result);
        }

        {
            ti.setState(TargetInstance.STATE_HARVESTED);
            tiDao.save(ti);

            PatchUtil.modifier.deleteJob(directoryManager.getBaseDir(), job, harvestResultNumber);

            boolean result = testInstance.pushPruneAndImport(job, harvestResultNumber);
            assertFalse(result);
        }

        {
            ti.setState(TargetInstance.STATE_PATCHING);
            tiDao.save(ti);

            ModifyApplyCommand cmd = new ModifyApplyCommand();
            cmd.setTargetInstanceId(job);
            cmd.setHarvestResultNumber(harvestResultNumber);
            cmd.setNewHarvestResultNumber(harvestResultNumber);
            PatchUtil.modifier.savePatchJob(directoryManager.getBaseDir(), cmd);

            when(mockDigitalAssetStore.initialPruneAndImport(any(ModifyApplyCommand.class))).thenReturn(new ModifyResult());

            boolean result = testInstance.pushPruneAndImport(job, harvestResultNumber);
            assertTrue(result);

            verify(mockDigitalAssetStore).initialPruneAndImport(any(ModifyApplyCommand.class));
        }
    }

    @Test
    public void testSaveImportedFile() {
        long job = 5000L;
        int harvestResultNumber = 1;
        String fileName = "test.png";

        ModifyRowFullData cmd = new ModifyRowFullData();
        cmd.setUploadFileName(fileName);
        cmd.setUploadFileContent("base64:" + Base64.encode("test".getBytes()));

        testInstance.uploadFile(job, harvestResultNumber, cmd);

        File file = new File(directoryManager.getUploadDir(job), cmd.getCachedFileName());
        assert file != null;
        assert file.exists();
    }
}
