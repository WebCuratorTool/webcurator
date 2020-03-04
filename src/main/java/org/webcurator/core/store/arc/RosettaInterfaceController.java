package org.webcurator.core.store.arc;

import org.springframework.web.bind.annotation.*;
import org.webcurator.core.archive.dps.DPSArchive;
import org.webcurator.core.util.ApplicationContextFactory;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RosettaInterfaceController {

    @CrossOrigin
    @PostMapping(path = "/digital-asset-store/rosettaInterface")
    @ResponseBody
    public String rosettaInterface(@RequestParam(value = "query") String query,
                                   @RequestParam(value = "producerAgent") String producerAgent,
                                   @RequestParam(value = "producerId", required = false) String producerId,
                                   @RequestParam(value = "producer", required = false) String producer,
                                   @RequestParam(value = "fromCache", required = false) String fromCache,
                                   @RequestParam(value = "targetDcType", required = false) String targetDcType,
                                   @RequestParam(value = "agentPassword", required = false) String agentPassword,
                                   @RequestParam(value = "milliSeconds", required = false) String milliseconds,
                                   HttpServletResponse response) throws Exception {

        String body = "";
        DPSArchive dpsArchive = ApplicationContextFactory.getApplicationContext().getBean(DPSArchive.class);
        response.addHeader("Cache-Control", "no-store");

        if ("getProducerName".equals(query)) {

            DPSArchive.DepData prod = dpsArchive.getProducer(producerAgent, producerId);

            if (prod == null) {
                body = "<b>Preset producer does not match available producers for the user " + producerAgent + " in Rosetta</b>";
            }
            else {
                body = "<b>" + prod.description + " (ID: " + prod.id + ")</b>" +
                        "<input title=\"Producer\" name=\"customDepositForm_producerId\" type=\"hidden\" value=\"" + prod.id + "\"/>";
            }
        } else if ("getProducers".equals(query)) {
            boolean fromCacheBool = Boolean.parseBoolean(fromCache);
            DPSArchive.DepData[] producers = dpsArchive.getProducer(producerAgent,fromCacheBool);
            if (producers == null) {
                body = "<b>No producers available in Rosetta for the user " + producerAgent + "</b>";
            } else {

                body = "<b>Select a producer for agent " + producerAgent + "</b>" +
                        "(<a title=\"Retrieves a fresh list of producers from Rosetta for the agent\" style=\"text-decoration:underline;\"" +
                        "href=\"#\" onClick=\"javascript: return getProducers(false);\">Retrieve the list from Rosetta</a>)" +
                        "<p></p>" +
                        "<select title=\"Producer\" name=\"customDepositForm_producerId\" size=\"10\" style=\"width:100%;\">";

                for (int i = 0; i < producers.length; i++) {
                    String prodId = producers[i].id;
                    body = body + "<option title=\"Producer-" + prodId + "\" value=\"" + prodId + "\">" + producers[i].description + " (ID:" +  prodId + ")</option>";
                }

                body = body + "</select>";
            }
        }  else if ("validateProducerAgent".equals(query)) {
            boolean status = dpsArchive.validateProducerAgentName(producerAgent);
            if (!status) {
                body = "Producer agent name " + producerAgent + " is invalid in Rosetta.";
            } else {
                status = dpsArchive.isLoginSuccessful(producerAgent, agentPassword);
                if (!status) {
                    body = "Unable to login.  Password may be invalid.";
                } else {
                    body = "Producer agent name and password are valid.";
                }
            }
        } else if ("validateMaterialFlowAssociation".equals(query)) {
            boolean status = dpsArchive.validateMaterialFlowAssociation(producer, targetDcType);
            if (!status) {
                body = "Producer is not associated with the right material flow.  Please correct this in Rosetta.";
            } else {
                body = "Producer is associated with the right material flow.";
            }
        } else if ("sleep".equals(query)) {
            try {
                long ms = Long.parseLong(milliseconds);
                Thread.sleep(ms);
                body = "Slept over successfully for " + milliseconds + " milliseconds";
            } catch (Exception ex) {
                body = "Sleep was interrupted by exception " + ex + ".";
            }
        }
        return body;
    }
}
