package de.danielluedecke.zettelkasten;

import javax.swing.*;

public class PersonEditorPanel extends JPanel {
    private JTextField firstNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    // @todo - add more fields later

    private Person person;

    public PersonEditorPanel(  ) {
        layoutGui(  );
        updateDataDisplay(  );
    }

    public void setPerson(Person p) {
        this.person = person;
        updateDataDisplay(  );
    }

    public Person getPerson(  ) {
        // @todo - update the person with new information from the fields
        return this.person;
    }

    private void layoutGui(  ) {
        // @todo - define the layout
    }

    private void updateDataDisplay(  ) {
        // @todo - ensure the fields are properly enabled, also set
        //         data on the fields.
    }

    public JTextField getFirstNameField() {
        return this.firstNameField;
    }

    public JTextField getLastNameField() {
        return this.lastNameField;
    }
}
