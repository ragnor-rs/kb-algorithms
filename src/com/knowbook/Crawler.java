package com.knowbook;

import com.csvreader.CsvReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

public class Crawler {

    private final Map<String, Category> fields = new HashMap<String, Category>();
    private final Map<String, Category> branches = new HashMap<String, Category>();

    private final Map<Category, Set<KnowledgeUnit>> professionConceptsMap = new HashMap<Category, Set<KnowledgeUnit>>();
    private final Map<Category, Set<KnowledgeUnit>> branchConceptsMap = new HashMap<Category, Set<KnowledgeUnit>>();

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.map();
        crawler.reduce();
    }

    // TODO persist reduce results
    private void reduce() {

        for (Map.Entry<String, Category> branchMapEntry : branches.entrySet()) {

            Category branch = branchMapEntry.getValue();

            // calculate frequencies for concepts of the branch and the number of professions in the branch
            Map<KnowledgeUnit, Integer> conceptCountMap = new HashMap<KnowledgeUnit, Integer>();
            int professionCount = 0;
            for (Map.Entry<Category, Set<KnowledgeUnit>> professionConceptsEntry : professionConceptsMap.entrySet()) {

                // ensure the profession belongs to this branch
                Category profession = professionConceptsEntry.getKey();
                if (!profession.getParent().equals(branch)) {
                    continue;
                }

                // count each concept of the profession
                Set<KnowledgeUnit> professionConcepts = professionConceptsEntry.getValue();
                for (KnowledgeUnit unit : professionConcepts) {
                    Integer count = conceptCountMap.get(unit);
                    if (count == null) {
                        conceptCountMap.put(unit, 1);
                    } else {
                        conceptCountMap.put(unit, count + 1);
                    }
                }

                professionCount++;

            }

            // save branch concepts
            Set<KnowledgeUnit> concepts = new HashSet<KnowledgeUnit>();
            for (Map.Entry<KnowledgeUnit, Integer> statsEntry : conceptCountMap.entrySet()) {
                KnowledgeUnit concept = statsEntry.getKey();
                int count = statsEntry.getValue();
                float frequency = count / ((float) professionCount);
                if (frequency >= 0.3f) {   // TODO concept threshold should not be hardcoded
                    concepts.add(concept);
                }
            }
            branchConceptsMap.put(branch, concepts);

            System.out.println(branch.getName() + ": " + Arrays.toString(concepts.toArray()));

        }

        System.out.println();

    }

    // TODO persist map results
    private void map() {

        int count = 0;

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("professions.csv");
        CsvReader csvReader = new CsvReader(inputStream, Charset.forName("utf-8"));

        try {
            while (csvReader.readRecord()) {

                // normalize entry values
                String fieldName = csvReader.get(0).replace((char)160, ' ').trim();
                String branchName = csvReader.get(1).replace((char)160, ' ').trim();
                String professionName = csvReader.get(2).replace((char)160, ' ').trim();

                // ensure a field exists
                Category field = fields.get(fieldName);
                if (field == null) {
                    field = new Category();
                    field.setName(fieldName);
                    fields.put(branchName, field);
                }

                // ensure a branch exists
                Category branch = branches.get(branchName);
                if (branch == null) {
                    branch = new Category();
                    branch.setName(branchName);
                    branch.setParent(field);
                    branches.put(branchName, branch);
                }

                crawlForProfession(professionName, branch);

                count ++;
                if (count == 30) break;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            csvReader.close();
        }

        System.out.println();

    }

    private void crawlForProfession(String professionName, Category branch) throws IOException {

        Category profession = new Category();
        profession.setName(professionName);
        profession.setParent(branch);

        // look up the profession name
        URL searchProfessionUrl = new URL("http://ru.wikipedia.org/w/api.php?format=json&action=query&list=search&srsearch=" + URLEncoder.encode(profession.getName(), "utf-8") + "&srprop=score&srwhat=text");
        JSONObject resultJson = loadJson(searchProfessionUrl);
        JSONArray searchArray = resultJson.getJSONObject("query").getJSONArray("search");
        if (searchArray.length() == 0) {
            return; // TODO what should we do if we fail to find concepts for the profession?
        }

        // get a normalized name for the profession
        String normalizedName = searchArray.getJSONObject(0).getString("title");
        profession.setUrl("http://ru.wikipedia.org/w/index.php?title=" + URLEncoder.encode(normalizedName, "utf-8"));

        crawlForKnowledgeUnits(profession, normalizedName);

    }

    private void crawlForKnowledgeUnits(Category profession, String normalizedName) throws IOException {

        Set<KnowledgeUnit> concepts = new HashSet<KnowledgeUnit>();
        professionConceptsMap.put(profession, concepts);

        // get articles for the given profession name
        URL linksConceptsUrl = new URL("http://ru.wikipedia.org/w/api.php?format=json&action=query&prop=links&titles=" + URLEncoder.encode(normalizedName, "utf-8"));
        JSONObject resultJson = loadJson(linksConceptsUrl);
        JSONObject pagesJson = resultJson.getJSONObject("query").getJSONObject("pages");
        JSONArray linksArray = pagesJson.getJSONObject(pagesJson.keys().next().toString()).getJSONArray("links");

        // for each article
        for (int i = 0; i < linksArray.length(); i++) {

            String linkTitle = linksArray.getJSONObject(i).getString("title");

            // ensure a concept exists
            KnowledgeUnit knowledgeUnit = getKnowledgeUnit(linkTitle);
            if (knowledgeUnit == null) {
                knowledgeUnit = new KnowledgeUnit();
                knowledgeUnit.setName(linkTitle);
                knowledgeUnit.setUrl("http://ru.wikipedia.org/w/index.php?title=" + URLEncoder.encode(linkTitle, "utf-8"));
            }

            concepts.add(knowledgeUnit);

        }

        System.out.println(profession.getName() + ": " + Arrays.toString(concepts.toArray()));

    }

    private KnowledgeUnit getKnowledgeUnit(String title) {
        for (Map.Entry<Category, Set<KnowledgeUnit>> entry : professionConceptsMap.entrySet()) {
            Set<KnowledgeUnit> units = entry.getValue();
            for (KnowledgeUnit unit : units) {
                if (title.equals(unit.getName())) {
                    return unit;
                }
            }
        }
        return null;
    }

    private JSONObject loadJson(URL url) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br =  new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            br.close();
        }
        return new JSONObject(sb.toString());
    }

    public static void testDataStructure() {

        Category computerScience = new Category();
        computerScience.setName("Computer science");

        Category softwareDevelopment = new Category();
        computerScience.setName("Software development");
        computerScience.setParent(computerScience);

        Category programmer = new Category();
        programmer.setParent(softwareDevelopment);
        programmer.setName("Programmer");

        KnowledgeUnit class_ = new KnowledgeUnit();
        class_.setName("Class");
        class_.setProfession(programmer);
        class_.setUrl("http://ru.wikipedia.org/wiki/Class_(computer_programming)");

        KnowledgeUnit inheritance = new KnowledgeUnit();
        inheritance.setName("Inheritance");
        inheritance.setProfession(programmer);
        inheritance.setUrl("http://ru.wikipedia.org/wiki/Inheritance_(object-oriented_programming)");

        Link link = new Link();
        link.setStart(class_);
        link.setFinish(inheritance);
        link.setDistance(1f);

        KnowledgeGraph knowledgeGraph = new KnowledgeGraph();
        knowledgeGraph.add(link);

        System.out.println("Number of links: " + knowledgeGraph.numberOfLinks());
        System.out.println("Number of knowledge units: " + knowledgeGraph.numberOfKnowledgeUnits());

    }

}
