package edu.washington.multirframework.corpus;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class TokenOffsetInformation implements TokenInformationI{
	
	public static final class SentenceRelativeCharacterOffsetBeginAnnotation implements CoreAnnotation<Integer>{
		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}
	}
	
	public static final class SentenceRelativeCharacterOffsetEndAnnotation implements CoreAnnotation<Integer>{
		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}
	}
	
	@Override
	public void read(String line, List<CoreLabel> tokens) {
		String[] tokenValues = line.split("\\s+");
		if(tokenValues.length != tokens.size()){
			for(CoreLabel token : tokens){
				token.set(SentenceRelativeCharacterOffsetBeginAnnotation.class, null);
				token.set(SentenceRelativeCharacterOffsetEndAnnotation.class,null);
			}
		}
		else{
		  for(int i =0; i < tokens.size(); i++){
			  String tokenValue = tokenValues[i];
			  CoreLabel token = tokens.get(i);
			  String[] offsetValues = tokenValue.split(":");
			  if(offsetValues.length == 2){
				Integer start = Integer.parseInt(offsetValues[0]);
				Integer end = Integer.parseInt(offsetValues[1]);
				token.set(SentenceRelativeCharacterOffsetBeginAnnotation.class,start);
				token.set(SentenceRelativeCharacterOffsetEndAnnotation.class,end);
			  }
			  else{
				token.set(SentenceRelativeCharacterOffsetBeginAnnotation.class, null);
				token.set(SentenceRelativeCharacterOffsetEndAnnotation.class,null); 
			  }
		  }
		}
	}

	@Override
	public String write(List<CoreLabel> tokens) {
		StringBuilder sb = new StringBuilder();
		
		for(CoreLabel token : tokens){
			Integer start = token.get(SentenceRelativeCharacterOffsetEndAnnotation.class);
			Integer end = token.get(SentenceRelativeCharacterOffsetEndAnnotation.class);
			if(start != null && end != null){
				sb.append(String.valueOf(start));
				sb.append(String.valueOf(";"));
				sb.append(String.valueOf(end));
				sb.append(String.valueOf(" "));
			}
			else{
				sb.append("");
			}
		}
		
		return sb.toString().trim();

	}

	@Override
	public String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}
	
}