
package com.bosch.bci.ramos;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AspectGuidelineValidator {
    private static final Logger logger = Logger.getLogger(AspectGuidelineValidator.class.getName());

    public static void main(String[] args) {
        boolean continueProcessing = true;

        while (continueProcessing) {
            // Open a file chooser dialog to select the TTL file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select TTL File");
            int userSelection = fileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                System.out.println("No file selected. Exiting.");
                return;
            }

            File selectedFile = fileChooser.getSelectedFile();
            String ttlFilePath = selectedFile.getAbsolutePath();

            String copyrightYear = RDFUtils.checkCopyrightYear(ttlFilePath);

            // Show a warning window recommending to make a copy of the uploaded file
            int response = JOptionPane.showConfirmDialog(null, "It is recommended to make a copy of the uploaded file, before proceeding to evaluate it. Do you want to save a copy?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                // Create a copy of the file
                File copyFile = new File(ttlFilePath.replace(".ttl", "_copy.ttl"));
                try {
                    Files.copy(selectedFile.toPath(), copyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Copy of the file created: " + copyFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // Configure logger to write to a file in the same location as the TTL file
            try {
                String fileName = ttlFilePath.substring(ttlFilePath.lastIndexOf(File.separator) + 1, ttlFilePath.lastIndexOf('.'));
                String logFilePath = ttlFilePath.substring(0, ttlFilePath.lastIndexOf(File.separator)) + File.separator + fileName + "_logs.txt";
                FileHandler fileHandler = new FileHandler(logFilePath, true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            // Create a ValueFactory
            SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

            // Read the TTL file into a model
            Model model;
            try (FileInputStream inputStream = new FileInputStream(ttlFilePath)) {
                model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (UnsupportedRDFormatException e) {
                System.err.println("The file format is not supported or the file is not a valid Turtle file.");
                e.printStackTrace();
                return;
            }

            // List to collect statements to be modified
            List<Statement> statementsToModify = new ArrayList<>();

            // Iterate over statements
            for (Statement stmt : model) {
                // Check and modify preferredName if necessary
                if (stmt.getPredicate().stringValue().equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#preferredName")) {
                    Value preferredName = stmt.getObject();
                    String preferredNamestr = stmt.getObject().stringValue();
	           	     if (preferredNamestr != null || !preferredNamestr.isEmpty()) {
	           	    	Value modifiedPreferredName = RDFUtils.checkAndModifyPreferredName(preferredName, stmt);
	                    if (!preferredName.stringValue().equals(modifiedPreferredName.stringValue())) {
	                        System.out.println("Modified preferredName: " + preferredName + " to " + modifiedPreferredName);
	                        logger.info("Modified preferredName: " + preferredName + " to " + modifiedPreferredName + " for entity: " + stmt.getSubject());
	                        statementsToModify.add(stmt);
	                    } else {
	                        preferredNamestr = stmt.getObject().stringValue();
	                        if (preferredNamestr == null || preferredNamestr.isEmpty()) {
	                            System.out.println("No preferred name found");
	                            logger.info("Modified preferredName not found for entity: " + stmt.getSubject());
	                        }
	                    }
	           	     } else {
	                        logger.warning("Preferred Name of entity: "+ stmt.getSubject()+ "is not present");
	           	     }
                }

                // Check and modify description if necessary
                if (stmt.getPredicate().stringValue().equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#description")) {
                    Value description = stmt.getObject();
                    Value modifiedDescription = RDFUtils.checkAndModifyDescription(description, stmt);

                    if (!description.equals(modifiedDescription)) {
                        System.out.println("Modified description: " + description + " to " + modifiedDescription);
                        logger.info("Modified description: " + description + " to " + modifiedDescription + " for entity: " + stmt.getSubject());
                        statementsToModify.add(stmt);
                    }
                }

                // Check if preferredName and description are the same
                if (stmt.getPredicate().stringValue().equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#preferredName")) {
                    Value preferredName = stmt.getObject();
                    for (Statement descStmt : model.filter(stmt.getSubject(), valueFactory.createIRI("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#description"), null)) {
                        Value description = descStmt.getObject();
                        if (preferredName.stringValue().equals(description.stringValue())) {
                            logger.warning("preferredName and description are the same for entity: " + stmt.getSubject());
                        }
                    }
                }
            }

            // Modify the collected statements
            for (Statement stmt : statementsToModify) {
                if (stmt.getPredicate().stringValue().equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#preferredName")) {
                    Value modifiedPreferredName = RDFUtils.checkAndModifyPreferredName(stmt.getObject(), stmt);
                    model.remove(stmt);
                    model.add(stmt.getSubject(), stmt.getPredicate(), modifiedPreferredName);
                } else if (stmt.getPredicate().stringValue().equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#description")) {
                    Value modifiedDescription = RDFUtils.checkAndModifyDescription(stmt.getObject(), stmt);
                    model.remove(stmt);
                    model.add(stmt.getSubject(), stmt.getPredicate(), modifiedDescription);
                }
            }

            // Check required properties
            try {
                RDFUtils.checkRequiredProperties(model, logger);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Validate example value for properties with primitive datatype characteristics
            try {
                RDFUtils.validateExampleValue(model, logger);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Save the modified model back to the TTL file
            try {
                RDFUtils.saveModel(model, ttlFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Check and add copyright notice if necessary
            try {
                RDFUtils.checkAndAddCopyrightNotice(ttlFilePath, copyrightYear);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Ask the user if they want to analyze another file
            int continueResponse = JOptionPane.showConfirmDialog(
                null,
                "Do you want to analyze another file?",
                "Continue?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (continueResponse != JOptionPane.YES_OPTION) {
                continueProcessing = false;
                System.out.println("Thanks for using the Aspect Model Validator.");
            }
        }
    }
}//end of ReadTTLFileRDF4J
