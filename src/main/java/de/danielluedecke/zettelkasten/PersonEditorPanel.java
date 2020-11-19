package de.danielluedecke.zettelkasten;

import javax.swing.*;
import java.awt.*;

public class PersonEditorPanel extends JPanel {
    private JPanel PersonEditorPanel;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private Person person;

    public PersonEditorPanel() {
        layoutGui(  );
        updateDataDisplay(  );
    }

    JTextField getFirstNameField(  ) {
        return this.firstNameField;
    }

    JTextField getLastNameField(  ) {
        return this.lastNameField;
    }

    private void layoutGui() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        add(new JLabel("First Name:"), gbc);
        add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(this.firstNameField, gbc);
        add(this.lastNameField, gbc);
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

    private void updateEnabledStates(  ) {
        this.firstNameField.setEnabled(person != null);
        this.lastNameField.setEnabled(person != null);
    }

    public void setPerson(Person person) {
        this.person = person;
        updateDataDisplay(  );
    }

    public Person getPerson(  ) {
        // @todo - update the person with new information from the fields
        return this.person;
    }

}
