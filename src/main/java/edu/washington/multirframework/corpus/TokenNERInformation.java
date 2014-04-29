package edu.washington.multirframework.corpus;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class TokenNERInformation implements TokenInformationI{	
	@Override
	public void read(String line, List<CoreLabel> tokens) {
		String [] tokenValues = line.split("\\s+");
		if(tokenValues.length == tokens.size()){
			for(int i =0; i < tokens.size(); i++){
				CoreLabel token = tokens.get(i);
				String tokenValue = tokenValues[i];
				token.set(CoreAnnotations.NamedEntityTagAnnotation.class, tokenValue);
			}
		}
		else{
			for(CoreLabel token: tokens){
				token.set(CoreAnnotations.NamedEntityTagAnnotation.class,null);
			}
		}
	}

	@Override
	public String write(List<CoreLabel> tokens) {
		StringBuilder sb = new StringBuilder();
		for(CoreLabel token: tokens){
			String tokenValue = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			if(tokenValue != null){
				sb.append(tokenValue);
			}
			else{
				return "";
			}
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	@Override
	public String name() {
		return "TOKENNERINFORMATION";
	}

}
