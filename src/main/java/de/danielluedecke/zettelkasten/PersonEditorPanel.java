package de.danielluedecke.zettelkasten;

import javax.swing.*;

public class PersonEditorPanel extends JPanel {
    private JPanel PersonEditorPanel;
    private JTextField firstNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    // @todo - add more fields later

    private Person person;

    public PersonEditorPanel(JPanel personEditorPanel) {
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
        if (this.person == null) {
            this.firstNameField.setText("");
            this.lastNameField.setText("");
        } else {
            this.firstNameField.setText(this.person.getFirstName(  ));
            this.lastNameField.setText(this.person.getLastName(  ));
        }
        updateEnabledStates(  );
    }

    JTextField getFirstNameField(  ) {
        return this.firstNameField;
    }

    JTextField getLastNameField(  ) {
        return this.lastNameField;
    }

    private void updateEnabledStates(  ) {
        this.firstNameField.setEnabled(person != null);
        this.lastNameField.setEnabled(person != null);
    }
}
