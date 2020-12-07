package ru.nsu.g.akononov.snakesGame.view;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ColorTheme {
    private static volatile ColorTheme instance;

    //private final static String path = "src/main/resources/dark_coloration.properties";
    private final static String path = "src/main/resources/light_coloration.properties";

    public int gap;

    public Color background;
    public Color ownBody;
    public Color ownHead;
    public Color otherBody;
    public Color otherHead;
    public Color zombieBody;
    public Color zombieHead;
    public Color food;
    public Color pairBlock;
    public Color impairBlock;
    public Color mainElements;
    public Color otherElements;

    private final Properties properties = new Properties();

    public static ColorTheme getInstance() {
        ColorTheme result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ColorTheme.class) {
            if (instance == null) {
                instance = new ColorTheme();
            }
            return instance;
        }
    }

    private ColorTheme() {
        try (InputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
            loadColors();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void loadColors() {
        gap = getIntValue("block_gap");
        background = getColor("background");
        ownBody = getColor("own_body");
        ownHead = getColor("own_head");
        otherBody = getColor("other_body");
        otherHead = getColor("other_head");
        food = getColor("food");
        mainElements = getColor("main_elements");
        otherElements = getColor("other_elements");
        pairBlock = getColor("pair_block");
        impairBlock = getColor("impair_block");
        zombieBody = getColor("zombie_body");
        zombieHead = getColor("zombie_head");
    }

    private Color getColor(String keyName){
        Color color =  new Color(getIntValue(keyName + "_r"), getIntValue(keyName + "_g"), getIntValue(keyName + "_b"));
        if(color.equals(new Color(0, 0, 0))){
            return null;
        }
        return color;
    }

    private int getIntValue(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}