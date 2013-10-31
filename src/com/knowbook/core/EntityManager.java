package com.knowbook.core;

import com.knowbook.model.AbstractEntity;
import com.knowbook.model.KnowledgeUnit;

public interface EntityManager {

    KnowledgeUnit getKnowledgeUnit(String name);

    <T extends AbstractEntity> T createEntity(Class<T> entityClass);

}
