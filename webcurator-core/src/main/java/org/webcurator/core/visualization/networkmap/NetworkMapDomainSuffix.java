package org.webcurator.core.visualization.networkmap;

import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkMapDomainSuffix {
    private final NetworkMapDomainSuffixNode ROOT = new NetworkMapDomainSuffixNode();

    public void init(String suffixFilePath) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(suffixFilePath));
        lines.stream().filter(line -> {
            line = line.trim();
            return line.length() > 0 && !line.startsWith("//");
        }).forEach(this::insert);
        lines.clear();
    }

    private void insert(String line) {
        String[] items = line.split("\\.");
        NetworkMapDomainSuffixNode p = ROOT;
        for (int i = items.length - 1; i >= 0; i--) {
            String key = items[i];
            p.putNextNode(key);
            p = p.getNextNode(key);
        }
    }

    public String getTopDomainName(String domainName) {
        if (domainName == null) {
            return "Unknown";
        }
        String[] items = domainName.split("\\.");
        NetworkMapDomainSuffixNode p = ROOT;
        List<String> result = new ArrayList<>();
        int i = 0;
        for (i = items.length - 1; i >= 0; i--) {
            String key = items[i];
            if (p == null || !p.containsKey(key)) {
                break;
            }
            result.add(0, key);
            p = p.getNextNode(key);
        }

        if (i >= 0) {
            result.add(0, items[i]);
        }

        return String.join(".", result);
    }

    public static void main(String[] args) throws Exception {
        NetworkMapDomainSuffix suf = new NetworkMapDomainSuffix();
        suf.init("D:\\Downloads\\public_suffix_list.dat");
        String domain = "www.google.com";
        String topDomain = suf.getTopDomainName(domain);
        System.out.println(domain + ": " + topDomain);
    }
}

class NetworkMapDomainSuffixNode {
    private Map<String, NetworkMapDomainSuffixNode> next = new HashMap<>();

    public NetworkMapDomainSuffixNode getNextNode(String key) {
        return this.next.get(key);
    }

    public void putNextNode(String key) {
        if (!this.next.containsKey(key)) {
            this.next.put(key, new NetworkMapDomainSuffixNode());
        }
    }

    public boolean containsKey(String key) {
        return this.next.containsKey(key);
    }
}