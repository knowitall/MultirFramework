package edu.washington.multirframework.corpus;

import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;

public interface TokenInformationI {
	/**
	 * Stores Annotation information on the CoreLabels from
	 * the input line.
	 * @param line
	 * @param tokens
	 */
	void read(String line, List<CoreLabel> tokens);
	
	/**
	 * Serializes CoreLabel information for class
	 * to a String.
	 * @param tokens
	 * @return
	 */
	String write(List<CoreLabel> tokens);
	
    /**
     * 
     * @return name of class
     */
	String name();
}
