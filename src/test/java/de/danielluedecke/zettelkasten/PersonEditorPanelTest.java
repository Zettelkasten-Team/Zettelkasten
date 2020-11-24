package de.danielluedecke.zettelkasten;

import junit.framework.*;
import junit.extensions.RepeatedTest;

import javax.swing.*;
import java.awt.*;

public class PersonEditorPanelTest extends SwingTestCase{
    private PersonEditorPanel emptyPanel;
    private PersonEditorPanel tannerPanel;
    private Person tanner;

    public PersonEditorPanelTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new RepeatedTest(new TestSuite(PersonEditorPanelTest.class), 1);
    }

    @Override
    protected void setUp() throws Exception {
        // create a panel without a Person
        this.emptyPanel = new PersonEditorPanel();

        // create a panel with a Person
        this.tanner = new Person("Tanner", "Burke");
        this.tannerPanel = new PersonEditorPanel();
        this.tannerPanel.setPerson(this.tanner);

        getTestFrame().getContentPane().add(this.tannerPanel, BorderLayout.CENTER);
        getTestFrame().pack();
        getTestFrame().setVisible(true);
    }

    public void testTextFieldsAreInitiallyDisabled() {
        assertTrue("First name field should be disabled",
                !this.emptyPanel.getFirstNameField().isEnabled());
        assertTrue("Last name field should be disabled",
                !this.emptyPanel.getLastNameField().isEnabled());
    }

    public void testEnabledStateAfterSettingPerson() {
        assertTrue("First name field should be enabled",
                this.tannerPanel.getFirstNameField().isEnabled(  ));
        assertTrue("Last name field should be enabled",
                this.tannerPanel.getLastNameField().isEnabled(  ));
    }

    public void testFirstName() {
        assertEquals("First name", "",
                this.emptyPanel.getFirstNameField().getText(  ));
        assertEquals("First name", this.tanner.getFirstName(  ),
                this.tannerPanel.getFirstNameField().getText(  ));
    }

    public void testLastName() {
        assertEquals("Last name", "",
                this.emptyPanel.getLastNameField().getText(  ));
        assertEquals("Last name", this.tanner.getLastName(  ),
                this.tannerPanel.getLastNameField().getText(  ));
    }

    public void testTabOrder() {
        JTextField firstNameField = this.tannerPanel.getFirstNameField();

        /* make sure the first name field has focus */
        firstNameField.requestFocusInWindow();

        /* simulate the user hitting tab */
        firstNameField.transferFocus();

        /* wait until the transferFocus() method is processed */
        waitForSwing();

        /* ensure that the last name field now has focus */
        JTextField lastNameField = this.tannerPanel.getLastNameField();
        assertTrue("Expected last name field to have focus", lastNameField.hasFocus( ));
    }
}