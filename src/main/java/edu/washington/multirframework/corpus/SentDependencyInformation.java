package edu.washington.multirframework.corpus;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Triple;

public class SentDependencyInformation implements SentInformationI{

	public static final class DependencyAnnotation implements CoreAnnotation<List<Triple<Integer,String,Integer>>>{
		@Override
		public Class<List<Triple<Integer, String, Integer>>> getType() {
		      return ErasureUtils.uncheckedCast(List.class);	
		}
	}
	@Override
	public void read(String s, CoreMap c) {	
		String[] dependencyInformation = s.split("\\|");
		List<Triple<Integer,String,Integer>> dependencyParseInformation = new ArrayList<>();
		for(String dependencyInfo : dependencyInformation){
			String[] parts = dependencyInfo.split("\\s+");
			if(parts.length ==3){
				Integer governor = Integer.parseInt(parts[0]);
				String depType = parts[1];
				Integer dependent = Integer.parseInt(parts[2]);
				Triple<Integer,String,Integer> triple = new Triple<>(governor,depType,dependent);
				dependencyParseInformation.add(triple);
			}
		}
		c.set(DependencyAnnotation.class, dependencyParseInformation);
	}
	@Override
	public String write(CoreMap c) {
		StringBuilder sb = new StringBuilder();
		List<Triple<Integer,String,Integer>> dependencyParseInformation = c.get(DependencyAnnotation.class);
		for(Triple<Integer,String,Integer> triple : dependencyParseInformation){
			sb.append(String.valueOf(triple.first));
			sb.append(" ");
			sb.append(triple.second);
			sb.append(" ");
			sb.append(String.valueOf(triple.third));
			sb.append("|");
		}
		if(sb.length() > 1){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString().trim();
	}
	@Override
	public String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}

	
}