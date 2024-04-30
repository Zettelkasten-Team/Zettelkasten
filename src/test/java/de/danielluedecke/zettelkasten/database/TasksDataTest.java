package de.danielluedecke.zettelkasten.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.LinkedList;

public class TasksDataTest {

    @Test
    void testSetReplaceMessageAndGetReplaceMessage() {
        // Test setting and getting replace message
        // Arrange
        TasksData tasksData = new TasksData();
        String expectedMessage = "Test message";

        // Act
        tasksData.setReplaceMessage(expectedMessage);
        String actualMessage = tasksData.getReplaceMessage();

        // Assert
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testSetReplaceCountAndGetReplaceCount() {
        // Test setting and getting replace count
        // Arrange
        TasksData tasksData = new TasksData();
        int expectedCount = 10;

        // Act
        tasksData.setReplaceCount(expectedCount);
        int actualCount = tasksData.getReplaceCount();

        // Assert
        assertEquals(expectedCount, actualCount);
    }

    // Add more test methods to cover other methods in TasksData class

}
