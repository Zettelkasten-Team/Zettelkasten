package de.danielluedecke.zettelkasten;

import javax.swing.*;

public class PersonEditorPanelTest extends SwingTestCase{
    private PersonEditorPanel emptyPanel;
    private PersonEditorPanel tannerPanel;
    private Person tanner;
    private JPanel personEditorPanel;

    @Override
    protected void setUp(  ) {
        // create a panel without a Person
        this.emptyPanel = new PersonEditorPanel(personEditorPanel);

        // create a panel with a Person
        this.tanner = new Person("Tanner", "Burke");
        this.tannerPanel = new PersonEditorPanel(personEditorPanel);
        this.tannerPanel.setPerson(this.tanner);
    }

    public void testTextFieldsAreInitiallyDisabled(  ) {
        assertTrue("First name field should be disabled",
                !this.emptyPanel.getFirstNameField().isEnabled());
        assertTrue("Last name field should be disabled",
                !this.emptyPanel.getLastNameField().isEnabled());
    }

    public void testEnabledStateAfterSettingPerson(  ) {
        assertTrue("First name field should be enabled",
                this.tannerPanel.getFirstNameField().isEnabled(  ));
        assertTrue("Last name field should be enabled",
                this.tannerPanel.getLastNameField().isEnabled(  ));
    }

    public void testFirstName(  ) {
        assertEquals("First name", "",
                this.emptyPanel.getFirstNameField().getText(  ));
        assertEquals("First name", this.tanner.getFirstName(  ),
                this.tannerPanel.getFirstNameField().getText(  ));
    }

    public void testLastName(  ) {
        assertEquals("Last name", "",
                this.emptyPanel.getLastNameField().getText(  ));
        assertEquals("Last name", this.tanner.getLastName(  ),
                this.tannerPanel.getLastNameField().getText(  ));
    }
}