package de.danielluedecke.zettelkasten;

import javax.swing.*;

public class PersonEditorPanelTest extends SwingTestCase{
    private PersonEditorPanel emptyPanel;
    private PersonEditorPanel tannerPanel;
    private final JTextField firstNameField;

    public PersonEditorPanelTest(JTextField firstNameField) {
        this.firstNameField = firstNameField;
    }

    protected void setUp(  ) throws Exception {
        // create a panel without a Person
        this.emptyPanel = new PersonEditorPanel(  );

        // create a panel with a Person
        Person tanner = new Person("Tanner", "Burke");
        this.tannerPanel = new PersonEditorPanel(  );
        this.tannerPanel.setPerson(tanner);
    }

    public void testTextFieldsAreInitiallyDisabled(  ) {
        assertFalse("First name field should be disabled", this.emptyPanel.getFirstNameField().isEnabled());
        assertFalse("Last name field should be disabled", this.emptyPanel.getLastNameField().isEnabled());
    }

    public void testEnabledStateAfterSettingPerson(  ) {
        assertTrue("First name field should be enabled",
                this.tannerPanel.getFirstNameField().isEnabled(  ));
        assertTrue("Last name field should be enabled",
                this.tannerPanel.getLastNameField().isEnabled(  ));
    }

}