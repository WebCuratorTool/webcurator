package org.webcurator.core.visualization.networkmap.processor;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlDTO;
import org.webcurator.core.util.URLResolverFunc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class IndexProcessorArc extends IndexProcessor {
    public IndexProcessorArc(BDBNetworkMapPool pool, long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        super(pool, targetInstanceId, harvestResultNumber);
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess() {
//        this.urls.forEach((fromUrl, fromNode) -> {
//            NetworkNodeUrl node = fromNode;
//            node.getOutlinks().forEach(toUrl -> {
//                String formatToUrl = URLResolverFunc.doResolve(fromUrl, null, toUrl);
//                if (this.urls.containsKey(formatToUrl)) {
//                    NetworkNodeUrl toNode = this.urls.get(formatToUrl);
//                    toNode.setViaUrl(fromUrl);
//                }
//            });
//            node.clear();
//        });
    }

    @Override
    protected void extractRecord(ArchiveRecord rec, String fileName) throws IOException {
        ARCRecord record = (ARCRecord) rec;
        ArchiveRecordHeader header = record.getHeader();


        // If the URL length is too long for the database, skip adding the URL
        // to the index. This ensures that the harvest completes successfully.
        if (header.getUrl().length() > MAX_URL_LENGTH) {
            return;
        }

        NetworkMapNodeUrlDTO res = new NetworkMapNodeUrlDTO(atomicIdGeneratorUrl.getAndIncrement());
        res.setUrlAndDomain(header.getUrl());
        res.setOffset(header.getOffset());
        res.setStatusCode(record.getStatusCode());

        // Calculate the length.
        long length = header.getLength() - header.getContentBegin();
        res.setContentLength(length);

        if (header.getMimetype() != null && header.getMimetype().length() > 0) {
            res.setContentType(header.getMimetype());
        }

        String key = URLResolverFunc.doResolve(null, null, res.getUrl());
        if (key != null) {
            this.urls.put(key, res);
        }

        if (res.getContentType().startsWith("text/html")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            record.skipHttpHeader();
            record.dump(out);
            String html = out.toString();
//            extractLinks(html, res.getOutlinks());
            out.close();
        }
    }

    private void extractLinks(String html, final List<String> outlinks) {
        String[] JSOUP_SELECTORS = {"href", "src"};
        Document doc = Jsoup.parse(html);
        for (String key : JSOUP_SELECTORS) {
            Elements links = doc.select(String.format("[%s]", key));
            links.stream().map(link -> link.attributes().get(key)).forEach(outlinks::add);
            links.clear();
        }
        doc.clearAttributes();
    }
}
