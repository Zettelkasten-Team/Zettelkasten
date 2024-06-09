package playground.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TableEventSourceExample {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Table Event Source Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Column1", "Column2"}, 0);
        model.addRow(new Object[]{"Row1-Column1", "Row1-Column2"});
        model.addRow(new Object[]{"Row2-Column1", "Row2-Column2"});

        JTable table = new JTable(model);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Object source = e.getSource();
                if (source instanceof JTable) {
                    JTable clickedTable = (JTable) source;
                    System.out.println("Clicked on table: " + clickedTable);
                }
            }
        });

        frame.add(new JScrollPane(table));
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
