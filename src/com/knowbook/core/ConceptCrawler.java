package com.knowbook.core;

import com.knowbook.model.Category;
import com.knowbook.model.KnowledgeUnit;

import java.util.List;

public interface ConceptCrawler {

    /**
     * Should also update profession's URL
     */
    List<KnowledgeUnit> gatherConceptsForProfession(EntityManager entityManager, Category profession);

}
