package edu.washington.multirframework.argumentidentification;

import java.util.List;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.washington.multirframework.data.Argument;

/**
 * Implementing classes need to implement method <code>generateSententialInstances</code> in
 * order to run <code>DistantSupervision</code> and <code>DocumentExtractor</code>
 * @author jgilme1
 *
 */
public interface SententialInstanceGeneration {
	
	/**
	 * Overriding implementations need to write an algorithm that will create <code>Pair</code>s of
	 * <code>Argument</code>s from the input list of arguments.
	 * @param arguments
	 * @param sentence
	 * @return
	 */
    public List<Pair<Argument,Argument>> generateSententialInstances(List<Argument> arguments, CoreMap sentence);
}
