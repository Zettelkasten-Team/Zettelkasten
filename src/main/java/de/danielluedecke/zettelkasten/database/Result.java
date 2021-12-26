package de.danielluedecke.zettelkasten.database;

public class Result {
    Document[] collection = new Document[0];

    public Result() {}

    public Result(Document[]collection) {
        this.collection = collection;
    }

    public int getCount() {return collection.length;}

    public Document getItem(int i) {return collection[i];}
}
