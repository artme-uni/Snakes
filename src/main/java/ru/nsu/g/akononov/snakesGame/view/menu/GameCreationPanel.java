package ru.nsu.g.akononov.snakesGame.view.menu;

import ru.nsu.g.akononov.snakesGame.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.ColoredButton;
import ru.nsu.g.akononov.utils.swing.NumberFieldsPanel;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;

public class GameCreationPanel extends StripLayoutPanel {
    NumberFieldsPanel fieldsPanel = new NumberFieldsPanel();
    ColorTheme colorTheme = ColorTheme.getInstance();

    ColoredButton creationButton = new ColoredButton("CREATE");

    public GameCreationPanel() {
        super(true);

        fieldsPanel.addField("Playground width", 20, 10, 100, 1);
        fieldsPanel.addField("Playground height", 20, 10, 100, 1);
        fieldsPanel.addField("Food static", 1, 0, 100, 1);
        fieldsPanel.addField("Food per player", 1, 0, 100, 0.5);
        fieldsPanel.addField("State delay (ms)", 200, 1, 10000, 100);
        fieldsPanel.addField("Dead food prob", 0.5, 0, 1, 0.1);
        fieldsPanel.addField("Ping delay (ms)", 100, 1, 10000, 100);
        fieldsPanel.addField("Node timeout (ms)", 800, 1, 10000, 100);

        setBackground(colorTheme.background);
        fieldsPanel.setColorTheme(colorTheme.mainElements, colorTheme.otherElements, colorTheme.background);
        getCreationButton().setColoration(colorTheme.mainElements);

        addEmptyPanel(1);
        addPanel(14, fieldsPanel);
        addEmptyPanel(1);
        addPanel(1, creationButton);
        addEmptyPanel(1);
    }

    public ColoredButton getCreationButton() {
        return creationButton;
    }

    public void setPlaygroundWidth(double value){
        fieldsPanel.getField("Playground width").getModel().setValue(value);
    }

    public double getPlaygroundWidth(){
       return (double) fieldsPanel.getField("Playground width").getValue();
    }

    public void setPlaygroundHeight(double value){
        fieldsPanel.getField("Playground height").getModel().setValue(value);
    }

    public double getPlaygroundHeight(){
        return (double) fieldsPanel.getField("Playground height").getValue();
    }

    public void setFoodStatic(double value){
        fieldsPanel.getField("Food static").getModel().setValue(value);
    }

    public double getFoodStatic(){
        return (double) fieldsPanel.getField("Food static").getValue();
    }

    public void setFoodPerPlayer(double value){
        fieldsPanel.getField("Food per player").getModel().setValue(value);
    }

    public double getFoodPerPlayer(){
        return (double) fieldsPanel.getField("Food per player").getValue();
    }

    public void setStateDelay(double value){
        fieldsPanel.getField("State delay (ms)").getModel().setValue(value);
    }

    public double getStateDelay(){
        return (double) fieldsPanel.getField("State delay (ms)").getValue();
    }

    public void setDeadFood(double value){
        fieldsPanel.getField("Dead food prob").getModel().setValue(value);
    }

    public double getDeadFood(){
        return (double) fieldsPanel.getField("Dead food prob").getValue();
    }

    public void setPingDelay(double value){
        fieldsPanel.getField("Ping delay (ms)").getModel().setValue(value);
    }

    public double getPingDelay(){
        return (double) fieldsPanel.getField("Ping delay (ms)").getValue();
    }

    public void setNodeTimeout(double value){
        fieldsPanel.getField("Node timeout (ms)").getModel().setValue(value);
    }

    public double getNodeTimeout() {
        return (double) fieldsPanel.getField("Node timeout (ms)").getValue();
    }
}
