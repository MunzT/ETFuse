package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ColorInfoFrame extends JDialog {

    private class StatusColumnCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            l.setBackground((Color) value);
            l.setText("");

            return l;

        }
    }

    private JTable table;
    public ColorInfoFrame(VideoFrame parentFrame, ArrayList<String> entity, ArrayList<Color> color) {

        super(parentFrame, "Farb-Legende", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                        RowSpec.decode("default:grow"),}));

        Object[][] model = new Object[entity.size()][2];

        for (int i = 0; i < entity.size(); i++) {
            model[i][0] = entity.get(i);
            model[i][1] = color.get(i);
        }

        table = new JTable();
        table.setFillsViewportHeight(true);
        table.setToolTipText("Links: Entit\u00E4t / Voraussetzung, Rechts: Zugeh\u00F6rige Farbe");
        table.setEnabled(false);
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(false);
        table.setModel(new DefaultTableModel(
                model,
                new String[] {
                        "Entity", "Color"
                }
                ) {
            Class[] columnTypes = new Class[] {
                    String.class, Color.class
            };
            @Override
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        });

        table.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());

        table.getColumnModel().getColumn(0).setResizable(false);
        table.getColumnModel().getColumn(1).setResizable(false);
        getContentPane().add(table, "1, 1, fill, fill");

        resizeColumnWidth(table);

        this.pack();

        setLocationRelativeTo(null);
        setResizable(false);
    }

    // https://stackoverflow.com/questions/17627431/auto-resizing-the-jtable-column-widths
    public void resizeColumnWidth(JTable table) {

        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 25; // Min width

            for (int row = 0; row < table.getRowCount(); row++) {

                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }

            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
}
