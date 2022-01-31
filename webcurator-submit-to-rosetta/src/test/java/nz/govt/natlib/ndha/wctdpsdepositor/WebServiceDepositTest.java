package nz.govt.natlib.ndha.wctdpsdepositor;

import javax.xml.namespace.QName;

import com.exlibris.core.infra.svc.api.locator.WebServiceLocator;
import com.exlibris.dps.DepositWebServices;
import com.exlibris.dps.DepositWebServices_Service;
//import com.exlibris.dps.sdk.deposit.DepositWebServices;
//import com.exlibris.dps.sdk.pds.HeaderHandlerResolver;
import com.exlibris.dps.sdk.pds.PdsClient;
import nz.govt.natlib.ndha.wctdpsdepositor.dpsdeposit.dspresult.DepositResultConverter;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.archive.dps.DpsDepositFacade;

import java.net.URL;

public class WebServiceDepositTest {
    private static final String DEPOSIT_WSDL_URL = "http://localhost:8888/dpsws/deposit/DepositWebServices?wsdl";
    private static final String institution = "INS00";
    private static final String userName = "test";
    private static final String password = "test";

    @Ignore
    @Test
    public void testDeposit() throws Exception {
        PdsClient pdsClient = PdsClient.getInstance();
        pdsClient.init("http://localhost:8888/dpsws/pds", true);
        String pdsHandler = pdsClient.login(institution, userName, password);

        com.exlibris.dps.sdk.deposit.DepositWebServices services = WebServiceLocator.getInstance().lookUp(com.exlibris.dps.sdk.deposit.DepositWebServices.class, DEPOSIT_WSDL_URL);

        String rstString = services.submitDepositActivity(pdsHandler, "1", "/home/test", "1", "1");
        System.out.println(rstString);
    }

    @Ignore
    @Test
    public void testDeposit2() throws Exception {
        PdsClient pdsClient = PdsClient.getInstance();
        pdsClient.init("http://localhost:8888/dpsws/pds", true);
        String pdsHandler = pdsClient.login(institution, userName, password);

        DepositWebServices_Service depWS = new DepositWebServices_Service(new URL(DEPOSIT_WSDL_URL), new QName("http://dps.exlibris.com/", "DepositWebServices"));
//        depWS.setHandlerResolver(new HeaderHandlerResolver(userName, password, institution));

        DepositWebServices services = depWS.getDepositWebServicesPort();

        String rstString = services.submitDepositActivity(pdsHandler, "1", "/home/test", "1", "1");
        System.out.println(rstString);

        DepositResultConverter resultConverter = new DepositResultConverter();
        DpsDepositFacade.DepositResult rst = resultConverter.unmarshalFrom(rstString);

        assert (748183L == rst.getSipId());
    }
}
