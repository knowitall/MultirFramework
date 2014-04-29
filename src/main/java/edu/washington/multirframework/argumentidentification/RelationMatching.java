package edu.washington.multirframework.argumentidentification;

import java.util.List;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multirframework.data.Argument;
import edu.washington.multirframework.data.KBArgument;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;

/**
 * Method <code>matchRelations</code> must be defined by another
 * class when running <code>DistantSupervision</code>
 * @author jgilme1
 *
 */
public interface RelationMatching {
	
	/**
	 * Overriding methods should create an algorithm that uses the information in the
	 * sentence, the document, and the KB to assign relations to the given sententialInstances
	 * @param sententialInstances all candidate <code>Pair</code>s of <code>Argument</code>s
	 * @param KB <code>KnowledgeBase</code>
	 * @param sentence <code>CoreMap</code> representation of sentence
	 * @param doc <code>Annotation</code> representation of document
	 * @return <code>List</code> of <code>Triple</code> of 2 <code>KBArgument</code>s and a <code>String<code> representing relation triples
	 */
	public List<Triple<KBArgument,KBArgument,String>> matchRelations(
			List<Pair<Argument,Argument>> sententialInstances, 
			KnowledgeBase KB, CoreMap sentence, Annotation doc);
}
