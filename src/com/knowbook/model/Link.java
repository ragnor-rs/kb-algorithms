package com.knowbook.model;

public class Link extends AbstractEntity {

    private float distance;

    private KnowledgeUnit start;

    private KnowledgeUnit finish;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public KnowledgeUnit getStart() {
        return start;
    }

    public void setStart(KnowledgeUnit start) {
        this.start = start;
    }

    public KnowledgeUnit getFinish() {
        return finish;
    }

    public void setFinish(KnowledgeUnit finish) {
        this.finish = finish;
    }

}
