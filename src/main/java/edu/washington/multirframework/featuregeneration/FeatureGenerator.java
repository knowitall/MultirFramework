package edu.washington.multirframework.featuregeneration;

import java.util.List;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Feature Generator interface
 * @author jgilme1
 *
 */
public interface FeatureGenerator {

	/**
	 * Generates features for a pair of arguments
	 * in a given sentence and document
	 * @param arg1StartOffset
	 * @param arg1EndOffset
	 * @param arg2StartOffset
	 * @param arg2EndOffset
	 * @param sentence
	 * @param document
	 * @return
	 */
	public List<String> generateFeatures(Integer arg1StartOffset, Integer arg1EndOffset, 
			Integer arg2StartOffset, Integer arg2EndOffset, String arg1Id, String arg2Id,
			CoreMap sentence, Annotation document );
}
