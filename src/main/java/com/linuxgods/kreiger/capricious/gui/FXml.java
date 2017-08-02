package com.linuxgods.kreiger.capricious.gui;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class FXml {
    static void init(Object controller) {
        try {
            Class<?> controllerClass = controller.getClass();
            FXMLLoader fxmlLoader = new FXMLLoader(controllerClass.getResource(controllerClass.getSimpleName()+".fxml"));
            fxmlLoader.setController(controller);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
