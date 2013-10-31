package com.knowbook.model;

import java.util.HashSet;
import java.util.Set;

public class KnowledgeGraph {

    private Set<Link> links = new HashSet<Link>();

    public void add(Link link) {
        links.add(link);
    }

    public int numberOfLinks() {
        return links.size();
    }

    public int numberOfKnowledgeUnits() {

        int result = 0;

        Set<KnowledgeUnit> processedUnits = new HashSet<KnowledgeUnit>();

        for (Link link : links) {

            KnowledgeUnit start = link.getStart();
            if (start != null && !processedUnits.contains(start)) {
                result++;
                processedUnits.add(start);
            }

            KnowledgeUnit finish = link.getFinish();
            if (finish != null && !processedUnits.contains(finish)) {
                result++;
                processedUnits.add(finish);
            }

        }

        return result;

    }

}
