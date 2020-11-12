package de.danielluedecke.zettelkasten;

public class PersonEditorPanelTest extends SwingTestCase{
    private PersonEditorPanel emptyPanel;
    private PersonEditorPanel tannerPanel;
    private Person tanner;

    protected void setUp(  ) throws Exception {
        // create a panel without a Person
        this.emptyPanel = new PersonEditorPanel(  );

        // create a panel with a Person
        Person tanner = new Person("Tanner", "Burke");
        this.tannerPanel = new PersonEditorPanel(  );
        this.tannerPanel.setPerson(tanner);
    }

    public void testTextFieldsAreInitiallyDisabled(  ) {
        assertTrue("First name field should be disabled", !this.emptyPanel.getFirstNameField().isEnabled());
        assertTrue("Last name field should be disabled", !this.emptyPanel.getLastNameField().isEnabled());
    }

    public void testEnabledStateAfterSettingPerson(  ) {
        assertTrue("First name field should be enabled",
                this.tannerPanel.getFirstNameField().isEnabled(  ));
        assertTrue("Last name field should be enabled",
                this.tannerPanel.getLastNameField().isEnabled(  ));
    }

}