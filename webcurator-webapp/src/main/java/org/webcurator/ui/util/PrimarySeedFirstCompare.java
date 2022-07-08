package org.webcurator.ui.util;

import org.webcurator.domain.model.core.SeedHistory;

import java.util.Comparator;

public class PrimarySeedFirstCompare {
    public static Comparator<SeedHistory> getComparator() {
        return (s0, s1) -> {
            int i0 = s0.isPrimary() ? 0 : 1;
            int i1 = s1.isPrimary() ? 0 : 1;
            return i0 - i1;
        };
    }
}
