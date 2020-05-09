package org.webcurator.core.networkmap;

import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.format.http.HttpHeaderParser;
import org.archive.format.http.HttpHeaders;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.SeedHistory;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class ResourceExtractorWarc extends ResourceExtractor {
    public ResourceExtractorWarc(Map<String, NetworkMapNode> results, Set<SeedHistory> seeds) {
        super(results, seeds);
    }

    @Override
    protected void preProcess() {
        //Do nothing
    }

    @Override
    protected void postProcess() {
        //Do nothing
    }

    @Override
    protected void extractRecord(ArchiveRecord rec) throws IOException {
        String mime = rec.getHeader().getMimetype();
        if (mime.equals("text/dns")) {
            return;
        }

        WARCRecord record = (WARCRecord) rec;
        ArchiveRecordHeader header = record.getHeader();

        // If the URL length is too long for the database, skip adding the URL
        // to the index. This ensures that the harvest completes successfully.
        if (header.getUrl() == null || header.getUrl().length() > MAX_URL_LENGTH) {
            return;
        }

//        String key = null;
//        String warcRecordId = header.getHeaderValue(WARCConstants.HEADER_KEY_ID).toString();
//        if (warcRecordId == null) {
//            return;
//        }
//        warcRecordId = warcRecordId.substring(1, warcRecordId.length() - 1);
//        if (header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO) != null) {
//            String warcConcurrentTo = header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO).toString();
//            int lenDelta = warcConcurrentTo.length() - warcRecordId.length() - 2;
//            warcConcurrentTo = warcConcurrentTo.substring(1, warcConcurrentTo.length() - 1 - lenDelta);
//            key = warcConcurrentTo;
//        } else {
//            key = warcRecordId;
//        }

        String key = header.getUrl();

        NetworkMapNode res = null;
        if (results.containsKey(key)) {
            res = results.get(key);
        } else {
            res = new NetworkMapNode(atomicIdGeneratorUrl.incrementAndGet());
            results.put(key, res);
        }

        String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
        if (type.equals(org.archive.format.warc.WARCConstants.WARCRecordType.request.toString())) {
            res.setRequestParseFlag(true);
        } else if (type.equals(org.archive.format.warc.WARCConstants.WARCRecordType.response.toString())) {
            res.setResponseParseFlag(true);
            res.setUrlAndDomain(header.getUrl());
            res.setOffset(header.getOffset());

            // need to parse the documents HTTP message and headers here: WARCReader
            byte[] statusBytes = HttpParser.readRawLine(record);
            int eolCharCount = getEolCharsCount(statusBytes);
            if (eolCharCount <= 0) {
                throw new RecoverableIOException("Failed to read http status where one " +
                        " was expected: " + new String(statusBytes));
            }
            String statusLine = EncodingUtil.getString(statusBytes, 0,
                    statusBytes.length - eolCharCount, WARCConstants.DEFAULT_ENCODING);
            if (!StatusLine.startsWithHTTP(statusLine)) {
                throw new RecoverableIOException("Failed parse of http status line.");
            }
            StatusLine status = new StatusLine(statusLine);
            res.setStatusCode(status.getStatusCode());

            // Calculate the length.
            long length = header.getLength() - header.getContentBegin();
            res.setContentLength(length);

            HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
            String contentType = httpHeaders.getValue(WARCConstants.CONTENT_TYPE);
            if (contentType != null && contentType.length() > 0) {
                res.setContentType(httpHeaders.getValue(WARCConstants.CONTENT_TYPE));
            }
            httpHeaders.clear();
        } else if (type.equals(org.archive.format.warc.WARCConstants.WARCRecordType.metadata.toString())) {
            res.setMetadataParseFlag(true);
            HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
            String sFetchTimeMs = httpHeaders.getValue("fetchTimeMs");
            if (sFetchTimeMs != null) {
                res.setFetchTimeMs(Long.parseLong(sFetchTimeMs));
            }
            res.setSeed(httpHeaders.get("seed") != null);
            if (res.isSeed()) {
                if (seeds.containsKey(key)) {
                    if (seeds.get(key)) {
                        res.setSeedType(0);
                    } else {
                        res.setSeedType(1);
                    }
                }else{
                    res.setSeedType(2);
                }
            }
            res.setHasOutlinks(httpHeaders.get("outlink") != null);
            res.setViaUrl(httpHeaders.getValue("via"));
        }
    }

}
