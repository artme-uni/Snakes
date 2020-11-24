package ru.nsu.g.akononov.utils.swing;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NumberFieldsPanel extends JPanel{

    GridLayout gridLayout = new GridLayout(1,1);
    Map<String, JSpinner> fields = new HashMap<>();
    Map<String, JLabel> labels = new HashMap<>();

    int fieldsCount = 0;

    public NumberFieldsPanel() {
        setLayout(gridLayout);
    }

    public void addField(String fieldName, double defaultValue, double minVal, double maxVal, double step){
        JPanel rowField = new JPanel(new GridLayout(1, 2));

        JLabel label = new JLabel(fieldName, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        rowField.add(label);
        labels.put(fieldName, label);

        SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minVal, maxVal, step);
        JSpinner field = new JSpinner(model);
        JComponent editor = field.getEditor();
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
        spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);

        fields.put(fieldName, field);
        rowField.add(field);

        gridLayout.setRows(++fieldsCount);
        add(rowField);
    }

    public JSpinner getField(String fieldName){
        return fields.get(fieldName);
    }

    public void setColorTheme(Color mainElementsColor, Color otherElementsColor, Color background){
        setBackground(background);

        for(JLabel label : labels.values()){
            label.setOpaque(true);
            label.setBackground(mainElementsColor);
        }

        for(JSpinner spinner : fields.values()){
            spinner.setOpaque(true);
            spinner.setBackground(mainElementsColor);
            ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setBackground(otherElementsColor);
        }
    }
}
