package edu.washington.multirframework.argumentidentification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multirframework.data.Argument;
import edu.washington.multirframework.data.KBArgument;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;

/**
 * Implements <code>RelationMatching</code> method <code>matchRelations</code>
 * to use the <code>KnowledgeBase</code> to identify candidate entity Ids
 * for the NER Arguments.
 * @author jgilme1
 *
 */
public class NERRelationMatching implements RelationMatching {
	
	private static NERRelationMatching instance = null;
	
	public static NERRelationMatching getInstance(){
		if(instance == null){
			instance = new NERRelationMatching();
		}
		return instance;
	}
	private NERRelationMatching(){}

	@Override
	public List<Triple<KBArgument,KBArgument,String>> matchRelations(
			List<Pair<Argument,Argument>> sententialInstances,
			KnowledgeBase KB, CoreMap sentence, Annotation doc) {
		
		Map<String,List<String>> entityMap =KB.getEntityMap();
		List<Triple<KBArgument,KBArgument,String>> distantSupervisionAnnotations = new ArrayList<>();
		
		for(Pair<Argument,Argument> si : sententialInstances){
			Argument arg1 = si.first;
			Argument arg2 = si.second;
			String arg1Name = arg1.getArgName();
			String arg2Name = arg2.getArgName();
			Set<String> relationsFound = new HashSet<String>();
			
			if(entityMap.containsKey(arg1Name)){
				if(entityMap.containsKey(arg2Name)){
					List<String> arg1Ids = entityMap.get(arg1Name);
					List<String> arg2Ids = entityMap.get(arg2Name);
					for(String arg1Id : arg1Ids){
						for(String arg2Id: arg2Ids){
							List<String> relations = KB.getRelationsBetweenArgumentIds(arg1Id,arg2Id);
							for(String rel : relations){
								if(!relationsFound.contains(rel)){
									KBArgument kbarg1 = new KBArgument(arg1,arg1Id);
									KBArgument kbarg2 = new KBArgument(arg2,arg2Id);
									Triple<KBArgument,KBArgument,String> t = 
											new Triple<>(kbarg1,kbarg2,rel);
									distantSupervisionAnnotations.add(t);
									relationsFound.add(rel);
								}
							}
						}
					}
				}
			}
		}
		return distantSupervisionAnnotations;
	}
}
