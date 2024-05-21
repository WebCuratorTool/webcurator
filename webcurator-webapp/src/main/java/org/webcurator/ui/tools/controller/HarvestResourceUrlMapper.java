package org.webcurator.ui.tools.controller;

import org.webcurator.domain.model.core.HarvestResult;

import java.text.SimpleDateFormat;

public class HarvestResourceUrlMapper {

    //private static Log log = LogFactory.getLog(HarvestResourceUrlMapper.class);

    public static final String NULL_RESULT_RETURN_VAL = "**Error-NULL-HarvestResult**";
    public static final String NULL_URLMAP_RETURN_VAL = "**Error-NULL-UrlMap**";
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMddhhmmss";
    private static final String FIND_CREATE_DATE = "{$HarvestResult.CreationDate";

    private String urlMap;

    public String getUrlMap() {
        return urlMap;
    }

    public void setUrlMap(String map) {
        urlMap = map;
    }

    public String generateUrl(HarvestResult result) {
        if (urlMap == null)
            return NULL_URLMAP_RETURN_VAL;
        if (result == null)
            return NULL_RESULT_RETURN_VAL;

        String retVal = urlMap
                .replaceAll("\\{\\$HarvestResult\\.Oid\\}", (result.getOid() == null) ? "" : String.valueOf(result.getOid()))
                .replaceAll("\\{\\$HarvestResult\\.CreationDate\\}", (result.getCreationDate() == null) ? "" : new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(result.getCreationDate()))
                .replaceAll("\\{\\$HarvestResult\\.DerivedFrom\\}", (result.getDerivedFrom() == null) ? "" : String.valueOf(result.getDerivedFrom()))
                .replaceAll("\\{\\$HarvestResult\\.HarvestNumber\\}", String.valueOf(result.getHarvestNumber()))
                .replaceAll("\\{\\$HarvestResult\\.ProvenanceNote\\}", (result.getProvenanceNote() == null) ? "" : result.getProvenanceNote())
                .replaceAll("\\{\\$HarvestResult\\.State\\}", String.valueOf(result.getState()))
                .replaceAll("\\{\\$HarvestResult\\.Collection\\}", String.format("%d-%d",result.getTargetInstance().getOid(),result.getHarvestNumber()));


        if (retVal.indexOf(FIND_CREATE_DATE) > 0) {
            retVal = doFormattedDates(retVal, result);
        }

        return retVal;
    }

    private String doFormattedDates(String url, HarvestResult result) {
        int ind = url.indexOf(FIND_CREATE_DATE);
        while (ind > 0) {
            int indFormatStart = ind + FIND_CREATE_DATE.length();
            int indFormatEnd = url.indexOf("}", indFormatStart);
            if (indFormatEnd < 0)
                break;
            if (url.substring(indFormatStart, indFormatStart + 1).equals(",")) {
                String format = url.substring(indFormatStart + 1, indFormatEnd);
                SimpleDateFormat df = new SimpleDateFormat(format);
                url = url.substring(0, ind) + df.format(result.getCreationDate()) + url.substring(indFormatEnd + 1, url.length());
                ind = url.indexOf(FIND_CREATE_DATE);
            } else {
                ind = url.indexOf(FIND_CREATE_DATE, indFormatStart);
            }

        }
        return url;
    }

}
