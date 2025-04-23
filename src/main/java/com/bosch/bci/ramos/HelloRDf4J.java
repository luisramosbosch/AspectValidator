/**
 * 
 */
package com.bosch.bci.ramos;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

/**
 * 
 */
public class HelloRDf4J {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedRDFormatException 
	 * @throws RDFParseException 
	 */
	public static void main(String[] args) throws RDFParseException, UnsupportedRDFormatException, IOException {
		// TODO Auto-generated method stub
		String filename = "example-data-artists.ttl";

		// read the file 'example-data-artists.ttl' as an InputStream.
		InputStream input = HelloRDf4J.class.getResourceAsStream("/" + filename);

		// Rio also accepts a java.io.Reader as input for the parser.
		Model model = Rio.parse(input, "", RDFFormat.TURTLE);

		// To check that we have correctly read the file, let's print out the model to the screen again
		model.forEach(System.out::println);	
		

	}//end of main

}//end of class
