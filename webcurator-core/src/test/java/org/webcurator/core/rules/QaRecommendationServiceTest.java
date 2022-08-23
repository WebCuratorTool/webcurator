package org.webcurator.core.rules;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.test.BaseWCTTest;

public class QaRecommendationServiceTest extends BaseWCTTest<QaRecommendationService> {
    public QaRecommendationServiceTest() {
        super(QaRecommendationService.class, "", false);

    }

    @Before
    public void init() {
        testInstance = new QaRecommendationService();

//        File fileRules = new File("src/main/resources/rules.drl");
        testInstance.setRulesFileName("rules.drl");
    }

    @Ignore
    @Test
    public void testBuildKnowledgeSession() {
        try {
            testInstance.buildKnowledgeSession();
        } catch (Exception e) {
            assert false;
        }
        assert true;
    }
}
