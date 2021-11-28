module com.chat.chatclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires common;

    exports com.chat.chatclient.controller;
    exports com.chat.chatclient.model;
    exports com.chat.chatclient;

    opens com.chat.chatclient to javafx.fxml;

    opens com.chat.chatclient.controller to javafx.fxml;
}