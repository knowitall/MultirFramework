package edu.washington.multirframework.corpus;

import edu.stanford.nlp.util.CoreMap;

/**
 * SentInformationI is an interface that describes how
 * to serialize/deserialize corpus information at the
 * sentence level
 * @author jgilme1
 *
 */
public interface SentInformationI{
	
	
	/**
	 * read sets a value on the CoreMap representing
	 * the sentence based on the information in the
	 * parameter s
	 * @param s String in second column of
	 * 			tab-delimited file that is loaded
	 * 			into Derby Db and used to annotate
	 * 			a sentence
	 * @param c The mutable sentence object
	 */
	public void read(String s, CoreMap c);
	
	/**
	 * write serializes information in the 
	 * sentence Annotation object into a string
	 * @param c the sentence object
	 * @return
	 */
	public String write(CoreMap c);
	
	/**
	 * returns a string that defines the 
	 * column name of the internal SQL table
	 * this should always be capitalized
	 * @return
	 */
	public String name();
}
