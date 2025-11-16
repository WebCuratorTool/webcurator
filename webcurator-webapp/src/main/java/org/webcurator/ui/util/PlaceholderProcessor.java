package org.webcurator.ui.util;

import org.webcurator.domain.model.core.HarvestResult;

import java.text.SimpleDateFormat;

/**
 * Utility for replacing placeholders in replay URLs with the corresponding values
 */
public class PlaceholderProcessor {
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMddhhmmss";
    private static final String FIND_CREATE_DATE = "{$HarvestResult.CreationDate";

    public static String generateUrl(String originalUrl, HarvestResult result) {

        if (originalUrl == null)
            return null;

        if (result == null)
            throw new NullPointerException("HarvestResult unexpectedly null"); // Can't happen, really

        String url = originalUrl
                .replaceAll("\\{\\$HarvestResult\\.Oid\\}", (result.getOid() == null) ? "" : String.valueOf(result.getOid()))
                .replaceAll("\\{\\$HarvestResult\\.CreationDate\\}", (result.getCreationDate() == null) ? "" : new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(result.getCreationDate()))
                .replaceAll("\\{\\$HarvestResult\\.DerivedFrom\\}", (result.getDerivedFrom() == null) ? "" : String.valueOf(result.getDerivedFrom()))
                .replaceAll("\\{\\$HarvestResult\\.HarvestNumber\\}", String.valueOf(result.getHarvestNumber()))
                .replaceAll("\\{\\$HarvestResult\\.ProvenanceNote\\}", (result.getProvenanceNote() == null) ? "" : result.getProvenanceNote())
                .replaceAll("\\{\\$HarvestResult\\.State\\}", String.valueOf(result.getState()))
                .replaceAll("\\{\\$TargetInstance\\.Oid\\}", String.valueOf(result.getTargetInstance().getOid()))
                .replaceAll("\\{\\$HarvestResult\\.Collection\\}", String.format("%d-%d",result.getTargetInstance().getOid(),result.getHarvestNumber()))
                ;


        if (url.indexOf(FIND_CREATE_DATE) > 0) {
            url = doFormattedDates(url, result);
        }

        return url;
    }


    private static String doFormattedDates(String url, HarvestResult result) {
        int ind = url.indexOf(FIND_CREATE_DATE);
        while (ind > 0) {
            int indFormatStart = ind + FIND_CREATE_DATE.length();
            int indFormatEnd = url.indexOf("}", indFormatStart);
            if (indFormatEnd < 0)
                break;
            if (url.charAt(indFormatStart) == ',') {
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
