package org.webcurator.core.visualization.networkmap;

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
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
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
    protected void extractRecord(ArchiveRecord rec, String fileName) throws IOException {
        String mime = rec.getHeader().getMimetype();
        if (mime.equals("text/dns")) {
            this.writeLog("Skipped MIMEType: " + mime);
            return;
        }

        WARCRecord record = (WARCRecord) rec;
        ArchiveRecordHeader header = record.getHeader();

        // If the URL length is too long for the database, skip adding the URL
        // to the index. This ensures that the harvest completes successfully.
        if (header.getUrl() == null || header.getUrl().length() > MAX_URL_LENGTH) {
            this.writeLog("Invalid URL: " + header.getUrl());
            return;
        }

        String key = header.getUrl();
        NetworkMapNode res = null;
        if (results.containsKey(key)) {
            res = results.get(key);
        } else {
            res = new NetworkMapNode(atomicIdGeneratorUrl.incrementAndGet());
            results.put(key, res);
        }

        res.setFileName(fileName); //Save the warc file name

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
                String err = "Failed to read http status where one " +
                        " was expected: " + new String(statusBytes);
                this.writeLog(err);
                throw new RecoverableIOException(err);
            }
            String statusLine = EncodingUtil.getString(statusBytes, 0,
                    statusBytes.length - eolCharCount, WARCConstants.DEFAULT_ENCODING);
            if (!StatusLine.startsWithHTTP(statusLine)) {
                String err = "Failed parse of http status line.";
                this.writeLog(err);
                throw new RecoverableIOException(err);
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
                } else {
                    res.setSeedType(2);
                }
            }
            res.setHasOutlinks(httpHeaders.get("outlink") != null);
            res.setViaUrl(httpHeaders.getValue("via"));
        }

        this.writeLog("Extracted URL: " + res.getUrl() + ", results.size=" + results.size());
    }
}
