package com.knowbook.core.internals;

import com.knowbook.core.*;
import com.knowbook.model.Category;
import com.knowbook.model.KnowledgeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class WikipediaCrawler implements ConceptCrawler {

    private final NameNormalizer nameNormalizer;

    public WikipediaCrawler(NameNormalizer nameNormalizer) {
        this.nameNormalizer = nameNormalizer;
    }

    @Override
    public List<KnowledgeUnit> gatherConceptsForProfession(EntityManager entityManager, Category profession) {

        List<KnowledgeUnit> concepts = new ArrayList<KnowledgeUnit>();

        String normalizedName = nameNormalizer.normalizeProfessionName(profession.getName());

        String urlString = HttpUtils.createUrl(
                "http://ru.wikipedia.org/w/api.php",
                "format", "json",
                "action", "query",
                "prop", "links",
                "titles", normalizedName
        );

        URL linksConceptsUrl;
        try {
            linksConceptsUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // get articles for the given profession name
        JSONObject resultJson;
        try {
            resultJson = HttpUtils.loadJson(linksConceptsUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject pagesJson = resultJson.getJSONObject("query").getJSONObject("pages");
        String pageId = pagesJson.keys().next().toString();

        JSONObject pageJson = pagesJson.getJSONObject(pageId);
        if (!pageJson.has("links")) {
            return concepts;
        }

        // for each article, create a concept
        JSONArray linksArray = pageJson.getJSONArray("links");
        outer: for (int i = 0; i < linksArray.length(); i++) {

            String linkTitle = linksArray.getJSONObject(i).getString("title");

            KnowledgeUnit knowledgeUnit = entityManager.getKnowledgeUnit(linkTitle);
            if (knowledgeUnit != null) {
                concepts.add(knowledgeUnit);
                continue;
            }

            for (KnowledgeUnit concept : concepts) {
                if (concept.getName().equals(linkTitle)) {
                    concepts.add(concept);
                    continue outer;
                }
            }

            knowledgeUnit = entityManager.createEntity(KnowledgeUnit.class);
            knowledgeUnit.setName(linkTitle);
            knowledgeUnit.setUrl(HttpUtils.createUrl("http://ru.wikipedia.org/w/index.php", "title", linkTitle));
            concepts.add(knowledgeUnit);

        }

        return concepts;

    }

}
