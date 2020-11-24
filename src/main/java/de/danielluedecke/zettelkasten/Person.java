package de.danielluedecke.zettelkasten;

public class Person {
    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            throw new IllegalArgumentException("Both names cannot be null");
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFullName() {
        String first = (this.firstName != null) ? this.firstName : "?";
        String last = (this.lastName != null) ? this.lastName : "?";

        return first + " " + last;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }
}
