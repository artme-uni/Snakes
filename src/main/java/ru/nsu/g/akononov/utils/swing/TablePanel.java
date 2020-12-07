package ru.nsu.g.akononov.utils.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablePanel extends JTable {
    private final static Color defaultColor = new Color(235, 235, 235);
    private final static int defaultRowHeight = 25;

    private Color headerColor = defaultColor;
    private Color cellColor = null;
    private final DefaultTableModel model = new DefaultTableModel();

    public TablePanel(String[] columnsName) {
        this(columnsName, defaultRowHeight);
    }

    public TablePanel(String[] columnsName, int rowHeight) {
        this.rowHeight = rowHeight;

        model.setColumnIdentifiers(columnsName);
        setRenders();
        setShowGrid(false);
        setModel(model);

        getTableHeader().setVisible(true);
        setRowHeight(rowHeight);
    }


    private void setRenders(){
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cellRenderer.setVerticalAlignment(SwingConstants.CENTER);
        if(cellColor!= null) {
            cellRenderer.setBackground(cellColor);
        }
        setDefaultRenderer(Object.class, cellRenderer);

        DefaultTableCellRenderer headerRender = new DefaultTableCellRenderer();
        headerRender.setHorizontalAlignment(SwingConstants.CENTER);
        headerRender.setVerticalAlignment(SwingConstants.CENTER);
        headerRender.setBackground(headerColor);
        getTableHeader().setDefaultRenderer(headerRender);
    }

    public void setColoration(Color headerColor, Color cellColor){
        this.headerColor = defaultColor;
        this.cellColor = null;

        if(headerColor != null){
            this.headerColor = headerColor;
            setRenders();
        }

        if(cellColor != null){
            this.cellColor = cellColor;
            setBackground(cellColor);
        }
    }

    public void addRecord(String ... values) {
        if(values.length != model.getColumnCount()){
            throw new RuntimeException();
        }
        model.addRow(values);
    }

    public void removeRecord(int index){
        model.removeRow(index);
    }

    public void clearTable(){
        model.setRowCount(0);
    }

    public String getRecord(int rowIndex, int columnIndex){
        return model.getValueAt(rowIndex, columnIndex).toString();
    }
}
