package edu.washington.multirframework.corpus;


/**
 * This class extends CustomCorpusInformationSpecification and adds
 * the necessary information for the current baseline Multir System
 * @author jgilme1
 *
 */
public class DefaultCorpusInformationSpecification extends
		CustomCorpusInformationSpecification {
	
	//adds custom sentenceInformation and tokenInformation
	//to the corpus representation
	public DefaultCorpusInformationSpecification(){
		super();
		sentenceInformation.add(new SentOffsetInformation());
	    sentenceInformation.add(new SentDependencyInformation());
		tokenInformation.add(new TokenNERInformation());
		tokenInformation.add(new TokenOffsetInformation());
		tokenInformation.add(new TokenPOSInformation());
	}
}
