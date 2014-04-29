package edu.washington.multirframework.argumentidentification;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Interval;
import edu.stanford.nlp.util.Pair;
import edu.washington.multirframework.data.Argument;
import edu.washington.multirframework.data.KBArgument;

/**
 * Implements <code>SententialInstanceGeneration</code> method <code>generateSententialInstances</code>
 * by returning all pairs of arguments where links are not the same.
 * @author jgilme1
 *
 */
public class DefaultSententialInstanceGeneration implements
		SententialInstanceGeneration {

	private static DefaultSententialInstanceGeneration instance = null;
	
	public static DefaultSententialInstanceGeneration getInstance(){
		if(instance == null){
			instance = new DefaultSententialInstanceGeneration();
		}
		return instance;
	}
	@Override
	public List<Pair<Argument, Argument>> generateSententialInstances(
			List<Argument> arguments, CoreMap sentence) {
		List<Pair<Argument,Argument>> sententialInstances = new ArrayList<>();
		
		
		for(int i =0; i < arguments.size(); i++){
			for(int j = 0; j < arguments.size(); j++){
				if(j != i){
					Argument arg1 = arguments.get(i);
					Argument arg2 = arguments.get(j);
					Interval<Integer> arg1Interval = Interval.toInterval(arg1.getStartOffset(), arg1.getEndOffset());
					Interval<Integer> arg2Interval = Interval.toInterval(arg2.getStartOffset(), arg2.getEndOffset());
					if(arg1Interval.intersect(arg2Interval) == null){
						
						boolean makePair = true;
						if((arg1 instanceof KBArgument) && (arg2 instanceof KBArgument)){
							KBArgument kbArg1 = (KBArgument)arg1;
							KBArgument kbArg2 = (KBArgument)arg2;
							if(kbArg1.getKbId().equals(kbArg2.getKbId())){
								makePair=false;
							}
						}
						if(makePair){
							Pair<Argument,Argument> p = new Pair<>(arg1,arg2);
							sententialInstances.add(p);
						}
					}
				}
			}
		}
		return sententialInstances;
	}

}
