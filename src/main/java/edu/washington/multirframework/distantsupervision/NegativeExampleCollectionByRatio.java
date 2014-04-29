package edu.washington.multirframework.distantsupervision;

import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multirframework.data.KBArgument;
import edu.washington.multirframework.data.NegativeAnnotation;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;

public class NegativeExampleCollectionByRatio extends NegativeExampleCollection{
	

	@Override
	public List<NegativeAnnotation> filter(
			List<NegativeAnnotation> negativeExamples,
			List<Pair<Triple<KBArgument, KBArgument, String>, Integer>> positiveExamples,
			KnowledgeBase kb, List<CoreMap> sentences) {
		
		Collections.shuffle(negativeExamples);
		return negativeExamples.subList(0,Math.min(negativeExamples.size(),(int)Math.floor(Math.max(1,positiveExamples.size())*negativeToPositiveRatio)));
	}

	public static NegativeExampleCollection getInstance(double ratio) {
		NegativeExampleCollection nec = new NegativeExampleCollectionByRatio();
		nec.negativeToPositiveRatio = ratio;
		return nec;
	}
}
