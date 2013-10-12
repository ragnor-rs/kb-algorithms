package com.knowbook;

import com.csvreader.CsvReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Crawler {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.run();
    }

    private void run() {

        Map<String, Category> fields = new HashMap<String, Category>();
        Map<String, Category> branches = new HashMap<String, Category>();
        Map<String, Category> professions = new HashMap<String, Category>();

        CsvReader csvReader = null;

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("professions.csv");
            csvReader = new CsvReader(inputStream, Charset.forName("utf-8"));
            while (csvReader.readRecord()) {

                String fieldName = csvReader.get(0).trim();
                String branchName = csvReader.get(1).trim();
                String professionName = csvReader.get(2).trim();

                Category field = fields.get(branchName);
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

                Category profession = new Category();
                profession.setName(professionName);
                profession.setParent(branch);
                professions.put(professionName, profession);

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
        class_.setUrl("http://en.wikipedia.org/wiki/Class_(computer_programming)");

        KnowledgeUnit inheritance = new KnowledgeUnit();
        inheritance.setName("Inheritance");
        inheritance.setProfession(programmer);
        inheritance.setUrl("http://en.wikipedia.org/wiki/Inheritance_(object-oriented_programming)");

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
