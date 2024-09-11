package org.webcurator.core.visualization.networkmap.processor;

import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.lang.StringUtils;
import org.archive.format.http.HttpHeaderParser;
import org.archive.format.http.HttpHeaders;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.springframework.context.ApplicationContext;
import org.webcurator.common.util.Utils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.Indexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("all")
public class IndexProcessorWarc extends IndexProcessor {
    public IndexProcessorWarc(BDBNetworkMapPool pool, long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        super(pool, targetInstanceId, harvestResultNumber);
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
        if (StringUtils.isEmpty(mime)) {
            log.warn("The MimeType of ArchiveRecord is empty in file: " + fileName);
            this.writeLog("The MimeType of ArchiveRecord is empty in file:" + fileName);
            return;
        }
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
        NetworkMapNodeUrlDTO res = null;
        if (this.urls.containsKey(key)) {
            res = this.urls.get(key);
        } else {
            res = new NetworkMapNodeUrlDTO(atomicIdGeneratorUrl.incrementAndGet());
            this.urls.put(key, res);
        }

        res.setFileName(fileName); //Save the warc file name

        String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();

        if (type.equals(org.archive.format.warc.WARCConstants.WARCRecordType.request.toString())) {
            res.setRequestParseFlag(true);
            HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
            String referer = httpHeaders.getValue("Referer");
            if (res.isMetadataParseFlag() && res.isSeed() && !Utils.isEmpty(referer)) { //Correct the viaUrl if Metadata is processed before this
                res.setSeed(false);
                res.setViaUrl(referer);
            } else if (!res.isMetadataParseFlag()) {
                res.setViaUrl(referer);
            }
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

            if (StatusLine.startsWithHTTP(statusLine)) {
                StatusLine status = new StatusLine(statusLine);
                res.setStatusCode(status.getStatusCode());
            } else {
                res.setStatusCode(0);
            }


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

            if (seeds.containsKey(key)) {
                res.setSeed(true);
                if (seeds.get(key)) {
                    res.setSeedType(NetworkMapNodeUrlDTO.SEED_TYPE_PRIMARY);  //Primary Seed Url
                } else {
                    res.setSeedType(NetworkMapNodeUrlDTO.SEED_TYPE_SECONDARY);  //Secondary Seed Url
                }
            } else {
                res.setSeed(httpHeaders.get("seed") != null);
                if (res.isSeed()) {
                    if (res.isRequestParseFlag() && !Utils.isEmpty(res.getViaUrl())) {
                        res.setSeed(false); //Correct it to un-seed if there is referer field exists in request record.
                    } else {
                        res.setSeedType(NetworkMapNodeUrlDTO.SEED_TYPE_OTHER); //Other kind Seed Url. e.g. patching source urls.}
                    }
                } else {
                    res.setViaUrl(httpHeaders.getValue("via"));
                }
            }
            res.setHasOutlinks(httpHeaders.get("outlink") != null);

            this.writeLog("Extracted URL: " + res.getUrl() + ", this.urls.size=" + this.urls.size());
        }
    }
}
