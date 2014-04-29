package edu.washington.multirframework.corpus;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.util.CoreMap;

/**
 * The abstract class CorpusInformationSpecification
 * should be extended when customizing the preprocessed Corpus.
 * The abstract class defines the integral information in a Corpus
 * for the default Multir algorithm to work.
 * @author jgilme1
 *
 */
public abstract class CorpusInformationSpecification {
	
	protected final List<SentInformationI> sentenceInformation;
	protected final List<TokenInformationI>  tokenInformation;
	protected final List<DocumentInformationI> documentInformation;
	private static final LabelFactory coreLabelFactory = CoreLabel.factory();
	
	private List<String> sentenceColumnNames = null;
	private List<String> documentColumnNames = null;


	public CorpusInformationSpecification(){
		//initialize Lists of sentenceInformation and tokenInformation
		sentenceInformation = new ArrayList<SentInformationI>();
		tokenInformation = new ArrayList<TokenInformationI>();
		documentInformation = new ArrayList<DocumentInformationI>();
		
		//set default sentenceInformation
		sentenceInformation.add(sentGlobalIDInformationInstance);
		sentenceInformation.add(sentDocNameInformationInstance);
		sentenceInformation.add(sentTokensInformationInstance);
		sentenceInformation.add(sentTextInformationInstance);
	}
	
	public List<String> getSentenceTableColumnNames(){
		if(sentenceColumnNames == null){
			List<String> names = new ArrayList<String>();
			for(SentInformationI si: sentenceInformation){
				names.add(si.name());
			}
			for(TokenInformationI ti: tokenInformation){
				names.add(ti.name());
			}
			sentenceColumnNames = names;
			return names;
		}
		else{
			return sentenceColumnNames;
		}
	}
	
	public List<String> getDocumentTableColumnNames(){
		if(documentColumnNames == null){
			List<String> names = new ArrayList<String>();
			names.add(sentDocNameInformationInstance.name());
			documentColumnNames = names;
			return names;
		}
		else{
			return documentColumnNames;
		}
	}
	
	private SentGlobalIDInformation sentGlobalIDInformationInstance = new SentGlobalIDInformation();
	public static final class SentGlobalIDInformation implements SentInformationI{
		
	    public static final class SentGlobalID implements CoreAnnotation<Integer>{
			@Override
			public Class<Integer> getType() {
				return Integer.class;
			}
	    }

		@Override
		public void read(String s, CoreMap c) {
			Integer id = Integer.parseInt(s);
			c.set(SentGlobalID.class, id);
		}

		@Override
		public String write(CoreMap s) {
			Integer id = s.get(SentGlobalID.class);
			if(id == null){
				return "";
			}
			else{
				return String.valueOf(id);
			}
		}

		@Override
		public String name() {
			return "SENTID";
		}
	}
	
	private SentDocNameInformation sentDocNameInformationInstance  = new SentDocNameInformation();
    public static final class SentDocNameInformation implements SentInformationI{
	    public static final class SentDocName implements CoreAnnotation<String>{
			@Override
			public Class<String> getType() {
				return String.class;
			}
	    }

		@Override
		public void read(String s, CoreMap c) {
			if(s.equals("")){
				c.set(SentDocName.class,null);
			}
			else{
			 c.set(SentDocName.class, s);
			}
		}

		@Override
		public String write(CoreMap c) {
			String docName = c.get(SentDocName.class);
			if(docName == null){
				return "";
			}
			else{
				return docName;
			}
		}

		@Override
		public String name() {
			return "DOCNAME";
		}
    }
    
	private  SentTextInformation sentTextInformationInstance = new SentTextInformation();
	private static final class SentTextInformation implements SentInformationI{

		@Override
		public void read(String s, CoreMap c) {
			if(s.equals("")){
				c.set(CoreAnnotations.TextAnnotation.class,null);
			}
			else{
			 c.set(CoreAnnotations.TextAnnotation.class, s);
			}
		}

		@Override
		public String write(CoreMap c) {
			String text = c.get(CoreAnnotations.TextAnnotation.class);
			if(text == null){
				return "";
			}
			else{
				return text;
			}
		}

		@Override
		public String name() {
			return this.getClass().getSimpleName().toUpperCase();
		}

	}
	
	private SentTokensInformation sentTokensInformationInstance = new SentTokensInformation();
	private static final class SentTokensInformation implements SentInformationI{
		@Override
		public void read(String s, CoreMap c) {
			String[] tokenStrings = s.split("\\s+");
			List<CoreLabel> tokens = new ArrayList<CoreLabel>();
			for(String token : tokenStrings){
				CoreLabel l = (CoreLabel) coreLabelFactory.newLabel(token);
				l.set(CoreAnnotations.TextAnnotation.class, token);
				tokens.add(l);
			}
			c.set(CoreAnnotations.TokensAnnotation.class, tokens);
		}
		@Override
		public String write(CoreMap c) {
			StringBuilder sb = new StringBuilder();
			List<CoreLabel> tokens = c.get(CoreAnnotations.TokensAnnotation.class);
			for(CoreLabel token : tokens){
				sb.append(token);
				sb.append(" ");
			}
			return sb.toString().trim();
		}
		@Override
		public String name() {
			return this.getClass().getSimpleName().toUpperCase();
		}
	}
}
