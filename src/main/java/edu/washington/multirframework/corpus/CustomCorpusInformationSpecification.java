package edu.washington.multirframework.corpus;

import java.util.List;

/**
 * This class extends the basic CorpusInformationSpecification, and can add any
 * customized sentence information, document information, or token information to 
 * the corpus.
 * @author jgilme1
 *
 */
public class CustomCorpusInformationSpecification extends CorpusInformationSpecification {

	
	public void addSentenceInformation(List<SentInformationI> sentInformationList){
		for(SentInformationI sentInformation: sentInformationList){
			this.sentenceInformation.add(sentInformation);
		}
	}
	
	public void addTokenInformation(List<TokenInformationI> tokenInformationList){
		for(TokenInformationI tokenInformation: tokenInformationList){
			this.tokenInformation.add(tokenInformation);
		}
	}
	
	public void addDocumentInformation(List<DocumentInformationI> documentInformationList){
		for(DocumentInformationI documentInformation: documentInformationList){
			this.documentInformation.add(documentInformation);
		}
	}
}
