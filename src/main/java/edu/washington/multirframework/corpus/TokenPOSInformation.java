package edu.washington.multirframework.corpus;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class TokenPOSInformation implements TokenInformationI{

	@Override
	public void read(String line, List<CoreLabel> tokens) {
		String [] tokenValues = line.split("\\s+");
		if(tokenValues.length != tokens.size()){
			for(CoreLabel token : tokens){
				token.set(CoreAnnotations.PartOfSpeechAnnotation.class,null);
			}
		}
		else{
			for(int i =0; i < tokens.size(); i++){
				String posTag = tokenValues[i];
				CoreLabel token = tokens.get(i);
				token.set(CoreAnnotations.PartOfSpeechAnnotation.class, posTag);
			}
		}
	}

	@Override
	public String write(List<CoreLabel> tokens) {
		StringBuilder sb = new StringBuilder();
		for(CoreLabel token : tokens){
			String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			if(pos != null){
				sb.append(pos);
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}

	@Override
	public String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}
}
