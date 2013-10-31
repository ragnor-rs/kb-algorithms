package com.knowbook;

import com.knowbook.core.internals.CsvProfessionReference;
import com.knowbook.core.ProfessionDatabase;
import com.knowbook.core.internals.GoogleNameResolver;
import com.knowbook.core.internals.WikipediaCrawler;
import com.knowbook.model.Category;
import com.knowbook.model.KnowledgeGraph;
import com.knowbook.model.KnowledgeUnit;
import com.knowbook.model.Link;

public class Main {

    public static void main(String args[]) {
        ProfessionDatabase professionDatabase = new ProfessionDatabase(
                new CsvProfessionReference(),
                new WikipediaCrawler(new GoogleNameResolver())
        );
        professionDatabase.mapToProfessions();
        professionDatabase.reduceToBranches();
        professionDatabase.reduceToFields();
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
        //class_.setProfession(programmer);
        class_.setUrl("http://ru.wikipedia.org/wiki/Class_(computer_programming)");

        KnowledgeUnit inheritance = new KnowledgeUnit();
        inheritance.setName("Inheritance");
        //inheritance.setProfession(programmer);
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
