package org.webcurator.core.visualization.networkmap;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.util.URLResolverFunc;
import org.webcurator.domain.model.core.SeedHistory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceExtractorArc extends ResourceExtractor {
    public ResourceExtractorArc(Map<String, NetworkMapNode> results, Set<SeedHistory> seeds) {
        super(results, seeds);
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess() {
//        results.forEach((fromUrl, fromNode) -> {
//            NetworkNodeUrl node = fromNode;
//            node.getOutlinks().forEach(toUrl -> {
//                String formatToUrl = URLResolverFunc.doResolve(fromUrl, null, toUrl);
//                if (results.containsKey(formatToUrl)) {
//                    NetworkNodeUrl toNode = results.get(formatToUrl);
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

        NetworkMapNode res = new NetworkMapNode(atomicIdGeneratorUrl.incrementAndGet());
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
            results.put(key, res);
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
