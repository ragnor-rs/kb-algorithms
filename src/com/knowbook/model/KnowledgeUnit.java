package com.knowbook.model;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeUnit extends AbstractEntity {

    private String url;

    // private final List<Category> professions = new ArrayList<Category>();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }

}

