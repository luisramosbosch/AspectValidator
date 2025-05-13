package com.bosch.bci.ramos;

/** 
 * Copyright (C) {Robert Bosch GmbH} - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by {Luis Enrique ramos Garcia} <{LuisEnrique.RamosGarcia@bosch.com}>, {13.05.2025}
 */





import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONObject;


public class RDFUtils {
    private static final Logger logger = Logger.getLogger(RDFUtils.class.getName());
    
    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    
 // List of primitive datatypes
    private static final List<String> primitiveDatatypes = Arrays.asList(
        "xsd:string", "xsd:boolean", "xsd:decimal", "xsd:integer", "xsd:double", "xsd:float",
        "xsd:date", "xsd:time", "xsd:dateTime", "xsd:dateTimeStamp", "xsd:gYear", "xsd:gMonth",
        "xsd:gDay", "xsd:gYearMonth", "xsd:gMonthDay", "xsd:duration", "xsd:yearMonthDuration",
        "xsd:dayTimeDuration"
    );



	 // Function to check and modify preferredName
	 public static Value checkAndModifyPreferredName(Value preferredName, Statement stmt) {
	     String preferredNamestr = stmt.getObject().stringValue();
	     if (preferredNamestr == null || preferredNamestr.isEmpty()) {
	         return preferredName;
	     }
	
	     // Check if the preferredName has a language tag
	     Literal preferredNameLit = (Literal) preferredName;
	     Optional<String> langTag = preferredNameLit.getLanguage();
	
	     // Replace special characters with spaces
	     preferredNamestr = preferredNamestr.replaceAll("[_\\-+.^:,]", " ");
	
	     
	     if (langTag.toString().equals("Optional[en]")) {
		     preferredNamestr = preferredNamestr.toString().toLowerCase();
		 }//only lower case to English language
	
	     Literal literal;
	     if (langTag.isPresent()) {
	         // Create a literal with the language tag
	         literal = valueFactory.createLiteral(preferredNamestr, langTag.get());
	     } else {
	         literal = valueFactory.createLiteral(preferredNamestr);
	     }
	
	     return literal;
	 }


    // Function to check and add copyright notice if necessary

	 public static void checkAndAddCopyrightNotice(String filePath, String copyrightYear) throws IOException {
		    // Read the file content
		    String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

		  
		    // Get the current year
		    String currentYear = Year.now().toString();
		    
		    //preparing logger message:
		  //short if then else, to decide actual year
		    String logg_message =(copyrightYear==null)? "Attention!, non copyright year found, added current year" : "Added exiting copyright year found in file";
		    
		    
		    //short if then else, to decide actual year
		    copyrightYear =(copyrightYear==null)? currentYear : copyrightYear;

		    // Create the copyright notice
		    String copyrightNotice = String.format(
		        "#\n" +
		        "# Copyright (c) %s Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.\n" +
		        "#\n\n", copyrightYear);

		    // Check if the copyright notice is present at the beginning of the file
		    if (!content.startsWith(copyrightNotice)) {
		        // Prepend the copyright notice to the file content
		        content = copyrightNotice + content;

		        // Write the modified content back to the file
		        Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));

		        // Log the modification
		        logger.info("Added copyright notice to the file: " + filePath +". "+logg_message);
		    }
		}
	//getting the first publication year
	 
	 private static String getFirstPublicationYear(String repoUrl) throws IOException {
		    URL url = new URL(repoUrl);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

		    if (connection.getResponseCode() == 200) {
		        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
		            StringBuilder response = new StringBuilder();
		            String line;
		            while ((line = reader.readLine()) != null) {
		                response.append(line);
		            }
		            JSONObject json = new JSONObject(response.toString());
		            String createdAt = json.getString("created_at");
		            return createdAt.substring(0, 4); // Extract the year
		        }
		    } else {
		        throw new IOException("Failed to fetch file metadata from GitHub API. Response code: " + connection.getResponseCode());
		    }
		}

    // Function to save the model with pretty printing
    public static void saveModel(Model model, String filePath) throws IOException {
        // Write the model to a string with pretty printing
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, outputStream);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        writer.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);
        Rio.write(model, writer);
        String modelContent = outputStream.toString(StandardCharsets.UTF_8);

        // Write the content back to the file
        Files.write(Paths.get(filePath), modelContent.getBytes(StandardCharsets.UTF_8));

        // Log the modification
        logger.info("Saved model to file: " + filePath);
    }

 // Function to check and modify description
    public static Value checkAndModifyDescription(Value description, Statement stmt ) {
    	
    	//
    	String descriptionStr = stmt.getObject().stringValue();
        if (descriptionStr == null || descriptionStr.isEmpty()) {
            return description;
        }


     // Check if the preferredName has a language tag
        Literal descriptionLit = (Literal) description;
        Optional<String> langTag =  descriptionLit.getLanguage();

        // Capitalize the first letter if it is lowercase
        if (Character.isLowerCase(descriptionStr.charAt(0))) {
        	descriptionStr = Character.toUpperCase(descriptionStr.charAt(0)) + descriptionStr.substring(1);
        }

        // Ensure the sentence ends with a dot if the last character is a letter or a number
        char lastChar = descriptionStr.charAt(descriptionStr.length() - 1);
        if (lastChar != '.' && (Character.isLetterOrDigit(lastChar))) {
        	descriptionStr += ".";
        }

        // Remove leading articles and capitalize the noun
        String[] articles = {"the ", "a ", "an "};
        for (String article : articles) {
            if (descriptionStr.toLowerCase().startsWith(article)) {
            	descriptionStr = descriptionStr.substring(article.length());
            	descriptionStr = Character.toUpperCase(descriptionStr.charAt(0)) + descriptionStr.substring(1);
                break;
            }
        }
        
      
        Literal literal;
        if (langTag != null) {
        	// Create a literal with the language tag
            literal = valueFactory.createLiteral(descriptionStr, langTag.get());
		} else {
            literal = valueFactory.createLiteral(descriptionStr);
		}
        // Append the language tag if it was present
        return literal;
    }

    // Function to check for acronyms
    public static boolean containsAcronym(Statement stmt) {
    	String text = stmt.getObject().stringValue();
        if (text == null || text.isEmpty()) {
            return false;
        }

        // Regular expression to match acronyms that are separated by spaces or punctuation
        Pattern pattern = Pattern.compile("\\b[A-Z]{2,}\\b");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
    

	 // Function to modify acronym to camel case
	 public static String modifyAcronym(Statement stmt) {
	     String text = stmt.getObject().stringValue();
	     if (text == null || text.isEmpty()) {
	         return text;
	     }
	
	     // Regular expression to match acronyms that are separated by spaces or punctuation
	     Pattern pattern = Pattern.compile("\\b[A-Z]{2,}\\b");
	     Matcher matcher = pattern.matcher(text);
	
	     if (matcher.find()) {
	         String acronym = matcher.group();
	         // Convert the acronym to camel case
	         String camelCaseAcronym = acronym.charAt(0) + acronym.substring(1).toLowerCase();
	         return camelCaseAcronym;
	     }
	
	     return text;
	 }


	// Function to normalize a string by converting to lowercase and removing the period at the end
	 private static String normalizeValue(String value) {
	     if (value == null || value.isEmpty()) {
	         return value;
	     }
	     value = value.toLowerCase();
	     if (value.endsWith(".")) {
	         value = value.substring(0, value.length() - 1);
	     }
	     return value;
	 }

	 // Updated checkRequiredProperties function
	 public static void checkRequiredProperties(Model model, Logger logger) {
	     for (Statement stmt : model) {
	         String subject = stmt.getSubject().stringValue();
	         String predicate = stmt.getPredicate().stringValue();
	         String object = stmt.getObject().stringValue();

	         if (predicate.equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#preferredName") && (object == null || object.isEmpty())) {
	             logger.warning("Missing preferredName for entity: " + subject);
	         }

	         if (predicate.equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#description") && (object == null || object.isEmpty())) {
	             logger.warning("Missing description for entity: " + subject);
	         }

	         // Check if preferredName and description are equal after normalization
	         if (predicate.equals("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#preferredName")) {
	             String preferredName = object;
	             for (Statement descStmt : model.filter(stmt.getSubject(), valueFactory.createIRI("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#description"), null)) {
	                 String description = descStmt.getObject().stringValue();
	                 if (normalizeValue(preferredName).equals(normalizeValue(description))) {
	                     logger.warning("Normalized preferredName and description are equal for entity: " + subject);
	                     
	                 }
	             }
	         }
	     }
	 }
    


	// Function to validate example value for properties with primitive datatype characteristics
	public static void validateExampleValue(Model model, Logger logger) {
	    for (Statement stmt : model.filter(null, valueFactory.createIRI("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#characteristic"), null)) {
	        Resource property = stmt.getSubject();
	        Value object = stmt.getObject();

	        // Ensure the object is an IRI before casting
	        if (object instanceof IRI) {
	            IRI characteristic = (IRI) object;

	            // Check if the characteristic has a primitive datatype
	            boolean hasPrimitiveDatatype = false;
	            for (Statement charStmt : model.filter(characteristic, valueFactory.createIRI("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#dataType"), null)) {
	                String dataType = charStmt.getObject().stringValue();
	                if (primitiveDatatypes.contains(dataType)) {
	                    hasPrimitiveDatatype = true;
	                    break;
	                }
	            }

	            // If the characteristic has a primitive datatype, check for exampleValue
	            if (hasPrimitiveDatatype) {
	                boolean hasExampleValue = model.contains(property, valueFactory.createIRI("urn:samm:org.eclipse.esmf.samm:meta-model:2.1.0#exampleValue"), null);
	                if (!hasExampleValue) {
	                    logger.warning("Missing exampleValue for property with primitive datatype characteristic: " + property);
	                }
	            }
	        } else {
	            // Log a warning if the object is not an IRI
	            logger.warning("The object is not an IRI but a " + object.getClass().getSimpleName() + ": " + object);
	        }
	    }
	}
	
	public static String checkCopyrightYear(String ttlFilePath) {
	    // Define the regex pattern for "Copyright (c) 20XX"
	    String regex = "Copyright \\(c\\) (20\\d{2})";
	    Pattern pattern = Pattern.compile(regex);

	    try {
	        // Read the file content as a single string
	        String fileContent = new String(Files.readAllBytes(Paths.get(ttlFilePath)));

	        // Match the pattern in the file content
	        Matcher matcher = pattern.matcher(fileContent);
	        if (matcher.find()) {
	            // Return the matched year (group 1)
	            return matcher.group(1);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    // Return null if no match is found
	    return null;
	}//end of checkCopyrightYear

}//end of class
