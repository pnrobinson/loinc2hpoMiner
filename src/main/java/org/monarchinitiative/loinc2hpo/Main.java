package org.monarchinitiative.loinc2hpo;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String [] args) {
        Application.launch(Loinc2HpoApplication.class, args);
    }
}