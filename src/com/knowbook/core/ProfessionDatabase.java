package com.knowbook.core;

import com.knowbook.model.AbstractEntity;
import com.knowbook.model.Category;
import com.knowbook.model.KnowledgeUnit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ProfessionDatabase implements EntityManager {

    private final AtomicLong idSequence = new AtomicLong();

    private final Map<Category, List<KnowledgeUnit>> professionConceptsMap = new HashMap<Category, List<KnowledgeUnit>>();
    private final Map<Category, List<KnowledgeUnit>> branchConceptsMap = new HashMap<Category, List<KnowledgeUnit>>();
    private final Map<Category, List<KnowledgeUnit>> fieldConceptsMap = new HashMap<Category, List<KnowledgeUnit>>();

    private final Map<String, Category> professionMap = new HashMap<String, Category>();
    private final Map<String, Category> branchMap = new HashMap<String, Category>();
    private final Map<String, Category> fieldMap = new HashMap<String, Category>();
    
    private final ProfessionReference professionReference;
    private final ConceptCrawler conceptCrawler;

    public ProfessionDatabase(
            ProfessionReference professionReference,
            ConceptCrawler conceptCrawler
    ) {
        this.professionReference = professionReference;
        this.conceptCrawler = conceptCrawler;
    }

    public final void reduceToFields() {
        prepare(fieldMap, fieldConceptsMap);
        reduce(branchConceptsMap, fieldConceptsMap, 0.1f);
    }

    public final void reduceToBranches() {
        prepare(branchMap, branchConceptsMap);
        reduce(professionConceptsMap, branchConceptsMap, 0.1f);
    }

    private static void prepare(Map<String, Category> src, Map<Category, List<KnowledgeUnit>> dst) {
        for (Category category : src.values()) {
            dst.put(category, null);
        }
    }

    private static void reduce(Map<Category, List<KnowledgeUnit>> srcMap, Map<Category, List<KnowledgeUnit>> dstMap, float threshold) {

        for (Category dstCategory : dstMap.keySet()) {

            // count srcCategory
            Map<KnowledgeUnit, Integer> conceptCountMap = new HashMap<KnowledgeUnit, Integer>();
            int srcCategoryCount = 0;
            for (Map.Entry<Category, List<KnowledgeUnit>> srcEntry : srcMap.entrySet()) {

                // ensure srcCategory belongs to this dstCategory
                Category srcCategory = srcEntry.getKey();
                if (!srcCategory.getParent().equals(dstCategory)) {
                    continue;
                }

                // count each concept of the srcCategory
                List<KnowledgeUnit> srcConcepts = srcEntry.getValue();
                for (KnowledgeUnit unit : srcConcepts) {
                    Integer count = conceptCountMap.get(unit);
                    if (count == null) {
                        conceptCountMap.put(unit, 1);
                    } else {
                        conceptCountMap.put(unit, count + 1);
                    }
                }

                srcCategoryCount++;

            }

            // filter concepts and save
            List<KnowledgeUnit> concepts = new ArrayList<KnowledgeUnit>();
            for (Map.Entry<KnowledgeUnit, Integer> statsEntry : conceptCountMap.entrySet()) {
                KnowledgeUnit concept = statsEntry.getKey();
                int count = statsEntry.getValue();
                float frequency = count / ((float) srcCategoryCount);
                if (frequency >= threshold) {
                    concepts.add(concept);
                }
            }
            dstMap.put(dstCategory, concepts);

            System.out.println(dstCategory.getName() + ": " + Arrays.toString(dstMap.get(dstCategory).toArray()));

        }

        System.out.println();

    }

    public final void mapToProfessions() {

        int count = 0;

        professionReference.start();
        while (professionReference.readNextProfession()) {

            Category field = getField(professionReference.getCurrentFieldName());

            Category branch = getBranch(professionReference.getCurrentBranchName());
            branch.setParent(field);

            Category profession = getProfession(professionReference.getCurrentProfessionName());
            profession.setParent(branch);

            List<KnowledgeUnit> concepts = conceptCrawler.gatherConceptsForProfession(this, profession);
            professionConceptsMap.put(profession, concepts);

            System.out.println(profession.getName() + ": " + Arrays.toString(professionConceptsMap.get(profession).toArray()));

            count++;
            //if (count == 10) break;

        }
        professionReference.finish();

        System.out.println();

    }

    private Category getCategory(Map<String, Category> map, String name) {
        Category category = map.get(name);
        if (category == null) {
            category = createEntity(Category.class);
            category.setName(name);
            map.put(name, category);
        }
        return category;
    }

    private Category getField(String name) {
        return getCategory(fieldMap, name);
    }

    private Category getBranch(String name) {
        return getCategory(branchMap, name);
    }

    private Category getProfession(String name) {
        return getCategory(professionMap, name);
    }

    @Override
    public final KnowledgeUnit getKnowledgeUnit(String title) {
        for (Map.Entry<Category, List<KnowledgeUnit>> entry : professionConceptsMap.entrySet()) {
            List<KnowledgeUnit> units = entry.getValue();
            for (KnowledgeUnit unit : units) {
                if (title.equals(unit.getName())) {
                    return unit;
                }
            }
        }
        return null;
    }

    @Override
    public <T extends AbstractEntity> T createEntity(Class<T> entityClass) {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entity.setId(idSequence.incrementAndGet());
        return entity;
    }

}
