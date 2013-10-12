package com.knowbook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KnowledgeUnit extends AbstractEntity {

    private String url;

    private Category profession;

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

    public Category getProfession() {
        return profession;
    }

    public void setProfession(Category profession) {
        this.profession = profession;
    }

    @Override
    public String toString() {
        return name;
    }

}

