package edu.washington.multirframework.corpus;

import edu.stanford.nlp.pipeline.Annotation;

public interface DocumentInformationI {

	/**
	 * Read sets Document-level information onto
	 * the Annotation object doc
	 * @param s - The serialization of the document-
	 * level information
	 * @param doc - The mutable view of the document
	 * and its sentences.
	 */
	void read (String s, Annotation doc);
	
	/**
	 * Returns a string representation of 
	 * this type of document-level information
	 * @param doc
	 * @return
	 */
	public String write(Annotation doc);
	
	
	/**
	 * returns a string that defines the 
	 * column name of the internal SQL table
	 * this should always be capitalized
	 * @return
	 */
	public String name();
	
}
