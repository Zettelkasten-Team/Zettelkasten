package de.danielluedecke.zettelkasten.database;

public class Document {

    String author;
    String title;
    String year;

    // Create a class constructor for the Document class
    public Document(String author, String title, String year){
        this.author = author;
        this.title = title;
        this.year = year;
    }

     String getAuthor() {
        return author;
    }

    String getTitle() {
        return title;
    }

    String getYear() {
        return year;
    }

}
