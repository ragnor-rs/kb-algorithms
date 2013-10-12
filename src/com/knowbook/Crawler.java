package com.knowbook;

import com.csvreader.CsvReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

public class Crawler {

    private final Map<String, Category> fields = new HashMap<String, Category>();
    private final Map<String, Category> branches = new HashMap<String, Category>();
    private final Map<String, Category> professions = new HashMap<String, Category>();

    private final Map<Category, Set<KnowledgeUnit>> professionConcepts = new HashMap<Category, Set<KnowledgeUnit>>();

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.run();
    }

    private void run() {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("professions.csv");
        CsvReader csvReader = new CsvReader(inputStream, Charset.forName("utf-8"));

        try {
            while (csvReader.readRecord()) {

                String fieldName = csvReader.get(0).replace((char)160, ' ').trim();
                String branchName = csvReader.get(1).replace((char)160, ' ').trim();
                String professionName = csvReader.get(2).replace((char)160, ' ').trim();

                Category field = fields.get(fieldName);
                if (field == null) {
                    field = new Category();
                    field.setName(fieldName);
                    fields.put(branchName, field);
                }

                Category branch = branches.get(branchName);
                if (branch == null) {
                    branch = new Category();
                    branch.setName(branchName);
                    branch.setParent(field);
                    branches.put(branchName, branch);
                }

                crawlForProfession(professionName, branch);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (csvReader != null) {
                csvReader.close();
            }
        }

    }

    private void crawlForProfession(String professionName, Category branch) throws IOException {

        Category profession = new Category();
        profession.setName(professionName);
        profession.setParent(branch);
        professions.put(professionName, profession);

        URL searchProfessionUrl = new URL("http://ru.wikipedia.org/w/api.php?format=json&action=query&list=search&srsearch=" + URLEncoder.encode(profession.getName(), "utf-8") + "&srprop=score&srwhat=text");

        JSONObject resultJson = loadJson(searchProfessionUrl);

        JSONArray searchArray = resultJson.getJSONObject("query").getJSONArray("search");

        try {

            Set<KnowledgeUnit> concepts = professionConcepts.get(profession);
            if (concepts == null) {
                concepts = new HashSet<KnowledgeUnit>();
                professionConcepts.put(profession, concepts);
            }

            if (searchArray.length() > 0) {
                String searchItemTitle = searchArray.getJSONObject(0).getString("title");
                profession.setUrl("http://ru.wikipedia.org/w/index.php?title=" + URLEncoder.encode(searchItemTitle, "utf-8"));
                crawlForKnowledgeUnits(concepts, searchItemTitle);
            }

            System.out.println("Profession: " + professionName + " | Concepts: " + Arrays.toString(concepts.toArray()));

        } catch (JSONException e) {

            System.out.println("Error parsing: " + resultJson.toString());

            throw e;

        }

    }

    private void crawlForKnowledgeUnits(Set<KnowledgeUnit> concepts, String searchItemTitle) throws IOException {

        URL linksConceptsUrl = new URL("http://ru.wikipedia.org/w/api.php?format=json&action=query&prop=links&titles=" + URLEncoder.encode(searchItemTitle, "utf-8"));

        JSONObject resultJson = loadJson(linksConceptsUrl);

        try {
            JSONObject pagesJson = resultJson.getJSONObject("query").getJSONObject("pages");
            JSONArray linksArray = pagesJson.getJSONObject(pagesJson.keys().next().toString()).getJSONArray("links");
            for (int i = 0; i < linksArray.length(); i++) {

                String linkTitle = linksArray.getJSONObject(i).getString("title");

                KnowledgeUnit knowledgeUnit = new KnowledgeUnit();
                knowledgeUnit.setName(linkTitle);
                knowledgeUnit.setUrl("http://ru.wikipedia.org/w/index.php?title=" + URLEncoder.encode(linkTitle, "utf-8"));

                concepts.add(knowledgeUnit);

            }
        } catch (JSONException e) {
            System.out.println("Error parsing: " + resultJson.toString());
            throw e;
        }

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
