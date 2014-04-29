package edu.washington.multirframework.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.washington.multirframework.database.CorpusDatabase;

/**
 * The Corpus class supports a Corpus Interface abstraction
 * with data stored in an abstracted Derby Db and loaded in from
 * a specified disk format.
 * @author jgilme1
 *
 */
public class Corpus {
	
	private CorpusDatabase cd;
	private CorpusInformationSpecification cis;
	private static String documentColumnName = "DOCNAME";
	private static String sentIDColumnName = "SENTID";
	
	private boolean train = false;
	private boolean test = false;
	private String testDocumentName = null;
	/**
	 * 
	 * @param name - String to location of local Corpus Database or name of new Corpus Database,
	 * 				 alternatively a URL for a remote DerbyDB will work as well
	 * @param cis  
	 * @param load  - boolean is true if loading in a target Corpus Database, false if creating one
	 * @throws SQLException
	 */
	public Corpus(String name, CorpusInformationSpecification cis, boolean load) throws SQLException{
	  this.cis = cis;
      cd = load ? CorpusDatabase.loadCorpusDatabase(name) : CorpusDatabase.newCorpusDatabase(name,getSentenceTableSQLSpecification(), getDocumentTableSQLSpecification());
	}
	
	//SQL Sentence Table Specification comes from all the user specified information
	// in the CorpusInformationSpecification instance
	private String getSentenceTableSQLSpecification(){
		StringBuilder sqlTableSpecificationBuilder = new StringBuilder();
		sqlTableSpecificationBuilder.append("( " + sentIDColumnName + " int,\n");
		sqlTableSpecificationBuilder.append(documentColumnName +" VARCHAR(128),\n");
		
		for(int i =2; i < cis.sentenceInformation.size(); i++){
			SentInformationI sentenceInformation = cis.sentenceInformation.get(i);
			String columnName = sentenceInformation.name();
			sqlTableSpecificationBuilder.append(columnName + " VARCHAR(20000),\n");
		}
		for(TokenInformationI tokenInformation : cis.tokenInformation){
			String columnName = tokenInformation.name();
			sqlTableSpecificationBuilder.append(columnName + " VARCHAR(20000),\n");
		}
		sqlTableSpecificationBuilder.append("PRIMARY KEY (" + sentIDColumnName+"))");
		return sqlTableSpecificationBuilder.toString();
	}
	
	private String getDocumentTableSQLSpecification(){
		StringBuilder sqlTableSpecificationBuilder = new StringBuilder();
		sqlTableSpecificationBuilder.append("( " + documentColumnName + " VARCHAR(128),\n");
		//add Document Information types
		for(DocumentInformationI docInformation: cis.documentInformation){
			String columnName = docInformation.name();
			sqlTableSpecificationBuilder.append(columnName + " CLOB,\n");
		}
		sqlTableSpecificationBuilder.append("PRIMARY KEY (DOCNAME))");
		return sqlTableSpecificationBuilder.toString();
	}
	
	//getDocuments takes a list of docNames and gets all of the Annotation information
	// from the corpus
	private List<Annotation> getDocuments(List<String> docNames) throws SQLException{
		ResultSet sentenceResults = cd.getSentenceRows(documentColumnName, docNames);
		ResultSet documentResults = cd.getDocumentRows(documentColumnName, docNames);
		
		List<Annotation> documents = new ArrayList<Annotation>();
		List<CoreMap> sentences = new ArrayList<CoreMap>();
		String lastDocument = "";
		if(sentenceResults.next()){
			lastDocument = sentenceResults.getString(documentColumnName);
			sentences.add(parseSentence(sentenceResults));
		}
		
		
		while(sentenceResults.next()){
			String document = sentenceResults.getString(documentColumnName);
			if(!lastDocument.equals(document)){
				Annotation a = new Annotation(sentences);
				documentResults.next();
				parseDocument(documentResults,a);
				documents.add(a);
				sentences = new ArrayList<CoreMap>();
				lastDocument = document;
			}
			sentences.add(parseSentence(sentenceResults));				
		}
		if(!sentences.isEmpty()){
			Annotation a = new Annotation(sentences);
			documents.add(a);
		}
		return documents;
	}
	
	public Annotation getDocument(String docName) throws SQLException{
		List<CoreMap> sentences = getSentences(docName);
		if(sentences.size() > 0){
		  Annotation doc = new Annotation(sentences);
		  //set doc values
		  List<String> docNames = new ArrayList<>();
		  docNames.add(docName);
		  ResultSet documentResults = cd.getDocumentRows(documentColumnName, docNames);
		  parseDocument(documentResults,doc);
		  return doc;
		}
		else
		  return null;
	}
	

	private List<CoreMap> getSentences(String docName) throws SQLException{
		List<CoreMap> sentences = new ArrayList<CoreMap>();
		List<String> docNames = new ArrayList<String>();
		docNames.add(docName);
		ResultSet sentenceResults = cd.getSentenceRows(documentColumnName,docNames);
		//iterate over sentence results
		while(sentenceResults.next()){
				sentences.add(parseSentence(sentenceResults));
		}
			
		return sentences;
	}
	
	/**
	 * Parses some documentInformationI from a  certain document in the corpus
	 * @param documentResults SQL Row
	 * @param doc Annotation doc to be modified 
	 * @throws SQLException 
	 */
	private void parseDocument(ResultSet documentResults, Annotation doc) throws SQLException {
		for(DocumentInformationI di : cis.documentInformation){
			String x = documentResults.getString(di.name());
			di.read(x, doc);
		}
		return;
	}
	
	/** 
	 * sentenceResults should already be at the correct cursor
	 * @param sentenceResults
	 * @return
	 * @throws SQLException 
	 */
    private CoreMap parseSentence(ResultSet sentenceResults) throws SQLException,IllegalArgumentException {
    	Annotation a = new Annotation("");
    	
    	//read in all specified sentenceInformation
    	for(SentInformationI si : cis.sentenceInformation){
    		String x = sentenceResults.getString(si.name());
    		si.read(x,a);
    	}
 
    	List<CoreLabel> tokens = a.get(CoreAnnotations.TokensAnnotation.class);

    	//read in all specified tokenInformation
    	for(TokenInformationI ti: cis.tokenInformation){
    		String tokenInformation =sentenceResults.getString(ti.name());
    		ti.read(tokenInformation, tokens);
    	}
    	return a;
	}
	
	//CachingDocumentIterator reduces the number of SQL queries that 
	//are executed when iterating over documents in the corpus.
	private class CachingDocumentIterator implements Iterator<Annotation>{
    	private ResultSet documents;
    	private List<Annotation> cachedDocuments;
    	private Set<String> documentsToIgnore;
    	
    	private static final int CACHED_LIMIT = 1000;
    	
    	private boolean doNext() throws SQLException{
    		int i =0;
    		List<String> docNames = new ArrayList<String>();
    		while((i < CACHED_LIMIT) && documents.next()){
    			String docName = documents.getString(documentColumnName);
    			if(documentsToIgnore!=null){
    				if(!documentsToIgnore.contains(docName)){
    	    			docNames.add(docName);
    	    			i++;
    				}
    			}
    			else{
	    			docNames.add(docName);
	    			i++;
    			}
    		}
    		if(i == 0){
    			return false;
    		}
    		else{
    		  cachedDocuments.addAll(getDocuments(docNames));
    		  return true;
    		}
    	}
    	
    	public CachingDocumentIterator() throws SQLException{
    		documents = cd.getDocumentRows();
    		cachedDocuments = new ArrayList<Annotation>();
    	}
    	
    	public CachingDocumentIterator(boolean train, String testDocumentFile) throws SQLException, IOException{
    		Set<String> testDocumentNames = getDocumentsFromFile(testDocumentFile);
    		if(train){
        		documents = cd.getDocumentRows();
    			documentsToIgnore =testDocumentNames;
        		cachedDocuments = new ArrayList<Annotation>();
    		}
    		else{
    			documents = cd.getDocumentRows(documentColumnName, new ArrayList<String>(testDocumentNames));
        		cachedDocuments = new ArrayList<Annotation>();
    		}
    	}
		@Override
		public boolean hasNext() {
			if(!cachedDocuments.isEmpty()){
				return true;
			}
			else{
				try{
					//doing next involves doing up to a limit of nexts
					//and doing one Derby query....
					return doNext();
				}
				catch(SQLException e){
					e.printStackTrace();
					return false;
				}
			}
		}
		@Override
		public Annotation next() {
			if(cachedDocuments.isEmpty()){
				try{
					if(doNext()){
						return next();
					}
					else{
						return null;
					}
				}
				catch(SQLException e){
					e.printStackTrace();
					return null;
				}
			}
			else{
				Annotation d = cachedDocuments.remove(0);
				return d;
			}
		}

		@Override
		public void remove() {
		}
		
		
		private Set<String> getDocumentsFromFile(String documentFile) throws IOException{
			Set<String> documentNames = new HashSet<String>();
			BufferedReader br = new BufferedReader(new FileReader(new File(documentFile)));
			String nextLine;
			while((nextLine = br.readLine())!=null){
				documentNames.add(formatValueForComparison(nextLine.trim()));
			}
			br.close();
			return documentNames;
		}
		

	}
	
	/**
	 * Returns iterator over all documents in corpus. If boolean fields train or test
	 * are set to positive then only a certain partition of the documents in the corpus
	 * will be iterated over.
	 * @return <code>Iterator</code> of <code>Annotation</code> over Documents in Corpus
	 * @throws SQLException
	 * @throws IOException
	 */
    public Iterator<Annotation> getDocumentIterator() throws SQLException, IOException{
    	if(train){
    		return getTrainDocumentIterator(testDocumentName);
    	}
    	else if (test){
    		return getTestDocumentIterator(testDocumentName);
    	}
    	else{
        	return new CachingDocumentIterator();    		
    	}
    }
    
    /**
     * Only selects rows corresponding to the documents in the testDocumentFile
     * @param testDocumentFile
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    private Iterator<Annotation> getTestDocumentIterator(String testDocumentFile) throws SQLException, IOException{
    	return new CachingDocumentIterator(false,testDocumentFile);
    }
    
    /**
     * Ignores all documents specified in the argumet testDocumentFile
     * @param testDocumentFile
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    private Iterator<Annotation> getTrainDocumentIterator(String testDocumentFile) throws SQLException, IOException{
    	return new CachingDocumentIterator(true,testDocumentFile);
    }
    

    //formatValue adds important Derby DB parsing data to strings
    //for batch insertion
    private static String formatValueForInset(String s){
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(s.replaceAll("%", "%%").replaceAll("_", "__"));
    	sb.insert(0,"%");
    	sb.append("%");
    	return sb.toString();
    }
    
    private static String formatValueForComparison(String s){
    	StringBuilder sb = new StringBuilder();  	
    	sb.append(s.replaceAll("%", "%%").replaceAll("_", "__"));
    	return sb.toString();
    }
    

    /**
     * 
     * @param path - File representing directory of NLP data representing Corpus,
     * 				 Files sentences.meta and documents.meta must exist there,
     *               in addition any class instances of <code>SentInformationI</code> <code>TokenInformationI</code>
     *               or <code>DocumentInformationI</code> that have been specified in <code>CorpusInformationSpecification</code>
     * @param sentenceDBFileName - String for temporary file of sentence data representation
     * @param documentDBFileName - String for temporary file of document data representation
     * @throws IOException
     * @throws SQLException
     */
	public void loadCorpus(File path, String sentenceDBFileName,
			String documentDBFileName) throws IOException, SQLException {

		File dbsentencesFile = new File(sentenceDBFileName);
		File dbdocumentsFile = new File(documentDBFileName);

		// if these two db files don't exist they need to be created
		if (!(dbsentencesFile.exists() && dbdocumentsFile.exists())) {

			if (path.isDirectory()) {
				File[] filesInDirectory = path.listFiles();

				// check for all required files as specified in
				// CopursInformationSpecification instance
				if (requiredFilesExist(Arrays.asList(filesInDirectory))) {

					// load in data from each file iteratively as rows into db.
					List<BufferedReader> sentenceDataLineReaders = new ArrayList<BufferedReader>();
					List<BufferedReader> tokenDataLineReaders = new ArrayList<BufferedReader>();
					List<BufferedReader> documentDataLineReaders = new ArrayList<BufferedReader>();
					BufferedReader sentenceMetaLineReader = new BufferedReader(new FileReader(new File(path.getPath()+ "/sentences.meta")));
					BufferedReader documentMetaLineReader = new BufferedReader(new FileReader(new File(path.getPath()+"/documents.meta")));

					List<SentInformationI> sentenceInformationSpecifications = new ArrayList<SentInformationI>();
					List<TokenInformationI> tokenInformationSpecifications = new ArrayList<TokenInformationI>();
					List<DocumentInformationI> documentInformationSpecifications = new ArrayList<DocumentInformationI>();

					// for all SentInformationI instances that require separate
					// files
					// add to the sentence information list
					for (SentInformationI si : cis.sentenceInformation) {
						if (!(si.name().equals("DOCNAME")
								|| si.name().equals("SENTID") || si.name()
								.equals("SENTTOKENSINFORMATION"))) {
							sentenceInformationSpecifications.add(si);
							sentenceDataLineReaders.add(new BufferedReader(
									new FileReader(new File(path + "/"
											+ si.name()))));
						}
					}
					for (TokenInformationI ti : cis.tokenInformation) {
						tokenInformationSpecifications.add(ti);
						tokenDataLineReaders
								.add(new BufferedReader(new FileReader(
										new File(path + "/" + ti.name()))));
					}
					
					for (DocumentInformationI di : cis.documentInformation){
						documentInformationSpecifications.add(di);
						documentDataLineReaders.
							add(new BufferedReader(new FileReader(
									new File(path + "/" + di.name()))));
					}

					int linesProcessed = 0;

					// iterate over corpus
					BufferedWriter sentenceWriter = new BufferedWriter(
							new PrintWriter(new File(sentenceDBFileName)));
					BufferedWriter documentWriter = new BufferedWriter(
							new PrintWriter(new File(documentDBFileName)));
					List<String> cachedSentenceLines = new ArrayList<String>();
					List<String> cachedDocumentLines = new ArrayList<String>();

					String nextMetaLine = sentenceMetaLineReader.readLine();
					while (nextMetaLine != null) {
						String[] metaLineValues = nextMetaLine.split("\t");

						StringBuilder newLine = new StringBuilder();

						// add first columns from meta.sentences file
						for (String metaLineValue : metaLineValues) {
							newLine.append(formatValueForInset(metaLineValue));
							newLine.append("_");
						}

						int sentLineIteratorIndex = 0;
						// get remaining SentInformationI values
						while (sentLineIteratorIndex < sentenceDataLineReaders
								.size()) {
							String nextLine = sentenceDataLineReaders.get(
									sentLineIteratorIndex).readLine();
							String[] splitValues = nextLine.split("\t");
							String values = "";
							if (splitValues.length > 1) {
								values = splitValues[1];
							}
							newLine.append(formatValueForInset(values));
							newLine.append("_");
							sentLineIteratorIndex++;
						}

						// get tokenInformation values
						int tokenLineIteratorIndex = 0;
						while (tokenLineIteratorIndex < tokenDataLineReaders
								.size()) {
							String nextLine = tokenDataLineReaders.get(
									tokenLineIteratorIndex).readLine();
							String[] splitValues = nextLine.split("\t");
							String values = " ";
							if (splitValues.length > 1) {
								values = splitValues[1];
							}
							newLine.append(formatValueForInset(values));
							newLine.append("_");
							tokenLineIteratorIndex++;
						}
						newLine.deleteCharAt(newLine.length() - 1);
						newLine.append("\n");
						cachedSentenceLines.add(newLine.toString());

						if (linesProcessed % 10000 == 0) {
							System.out.println("Processed " + linesProcessed
									+ " sentence lines");
							StringBuilder sentenceBuilder = new StringBuilder();
							for (String sentenceLine : cachedSentenceLines) {
								sentenceBuilder.append(sentenceLine);
							}
							sentenceWriter.write(sentenceBuilder.toString());
							cachedSentenceLines = new ArrayList<String>();
							cachedDocumentLines = new ArrayList<String>();

						}
						linesProcessed++;
						nextMetaLine = sentenceMetaLineReader.readLine();
					}
					
					
					//load in document information
					linesProcessed =0;
					nextMetaLine = documentMetaLineReader.readLine();
					while(nextMetaLine != null){
						String[] metaLineValues = nextMetaLine.split("\t");
						String docName = metaLineValues[1];
						Integer currentDocId = Integer.parseInt(metaLineValues[0]);
						StringBuilder documentLine = new StringBuilder();
						documentLine.append(formatValueForInset(docName));
						documentLine.append("_");
						
						int documentLineIteratorIndex = 0;
						while(documentLineIteratorIndex < documentDataLineReaders.size()){
							BufferedReader diReader = documentDataLineReaders.get(documentLineIteratorIndex);
							diReader.mark(2000);
							StringBuilder values = new StringBuilder();
							String nextLine = diReader.readLine();
							String[] splitValues = nextLine.split("\t");
							Integer docID = Integer.parseInt(splitValues[0]);
							while(docID.equals(currentDocId)){
								values.append(nextLine);
								values.append("\n");
								diReader.mark(2000);
								nextLine = diReader.readLine();
								if(nextLine != null){
									docID = Integer.parseInt(nextLine.split("\t")[0]);
								}
								else{
									docID = -1;
								}
							}
							diReader.reset();
							if(values.length()>0){
								documentLine.append(formatValueForInset(values.substring(0, values.length()-1).toString()));
								documentLine.append("_");
							}
							else{
								documentLine.append(formatValueForInset("NULL"));
								documentLine.append("_");
							}
							documentLineIteratorIndex++;
						}
						documentLine.deleteCharAt(documentLine.length() -1);
						documentLine.append("\n");
						cachedDocumentLines.add(documentLine.toString());	
						nextMetaLine = documentMetaLineReader.readLine();
						
						linesProcessed++;
						if (linesProcessed % 10000 == 0) {
							System.out.println("Processed " + linesProcessed
									+ " document lines");
							StringBuilder documentBuilder = new StringBuilder();
							for (String docLine : cachedDocumentLines) {
								documentBuilder.append(docLine);
							}
							documentWriter.write(documentBuilder.toString());
							cachedDocumentLines = new ArrayList<String>();
						}
					}
					
					// do final remaining writes
					StringBuilder sentenceBuilder = new StringBuilder();
					StringBuilder documentBuilder = new StringBuilder();
					for (String sentenceLine : cachedSentenceLines) {
						sentenceBuilder.append(sentenceLine);
					}
					for (String documentLine : cachedDocumentLines) {
						documentBuilder.append(documentLine);
					}
					sentenceWriter.write(sentenceBuilder.toString());
					documentWriter.write(documentBuilder.toString());

					// close all open resources
					cachedSentenceLines.clear();
					cachedDocumentLines.clear();
					sentenceWriter.close();
					documentWriter.close();
					sentenceMetaLineReader.close();
					documentMetaLineReader.close();
					for (BufferedReader br : sentenceDataLineReaders) {
						br.close();
					}
					for (BufferedReader br : tokenDataLineReaders) {
						br.close();
					}
					for (BufferedReader br : documentDataLineReaders) {
						br.close();
					}
				}
			}
		}

		// after files are converted to db format, batch load them into derby
		cd.batchSentenceTableLoad(cis, dbsentencesFile);
		cd.batchDocumentTableLoad(cis, dbdocumentsFile);
	}
    
	//according to the CorpusInformationSpecification instance
	//make sure all necessary files exist in the files list
    private boolean requiredFilesExist(List<File> files){
    	List<String> requiredFileNames = new ArrayList<String>();
    	List<String> fileNames = new ArrayList<String>();
    	for(File f : files){
    		fileNames.add(f.getName());
    	}
    	requiredFileNames.add("sentences.meta");
    	requiredFileNames.add("documents.meta");
    	//sentences.meta will be sentID docname tokens 
    	for(SentInformationI si :cis.sentenceInformation){
    		if(!(si.name().equals("DOCNAME") || si.name().equals("SENTID") ||  si.name().equals("SENTTOKENSINFORMATION"))){
    			requiredFileNames.add(si.name());
    		}
    	}
    	for(TokenInformationI ti: cis.tokenInformation){
    		requiredFileNames.add(ti.name());
    	}
    	for(DocumentInformationI di : cis.documentInformation){
    		requiredFileNames.add(di.name());
    	}
    	for(String name : requiredFileNames){
    		if(!fileNames.contains(name)){
    			System.err.println(name + "file does not exist");
    			return false;
    		}
    	}
    	return true;
    }
    
    
    //getAnnotationPairsForEachSentence is used to run bulk SQL queries for sentences and document information at the same time
	public Map<Integer,Pair<CoreMap,Annotation>> getAnnotationPairsForEachSentence(Set<Integer> sentIds) throws SQLException {
		
		if(sentIds.size() > 1000){
			throw new IllegalArgumentException("sentIds should have less than 1000 sentences in order for the large SQL query to work");
		}
		
		Map<Integer,Pair<CoreMap,Annotation>> sentIdToAnnotationsMap = new HashMap<>();
		List<Integer> values = new ArrayList<Integer>();
		
		for(Integer sentID: sentIds){
			values.add(sentID);
		}
		ResultSet sentenceResults = cd.getSentenceRowsByID(values);
		
		Set<String> relevantDocuments = new HashSet<String>();
		
		Map<Integer,Pair<CoreMap,String>> mapFromSentToAnnotationAndDocName = new HashMap<>();
		
		if(sentenceResults != null){
			while(sentenceResults.next()){
				//keep track of needed documents.
				String docName = sentenceResults.getString(documentColumnName);
				Integer sentId = sentenceResults.getInt(sentIDColumnName);
				relevantDocuments.add(docName);
				CoreMap s = parseSentence(sentenceResults);
				if(!mapFromSentToAnnotationAndDocName.containsKey(sentId)){
					mapFromSentToAnnotationAndDocName.put(sentId, new Pair<CoreMap,String>(s,docName));
				}
			}
			
			//get document map
			Map<String,Annotation> docNameToAnnoMap = new HashMap<String,Annotation>();
			List<String> docNames = new ArrayList<>();
			for(String docName: relevantDocuments){
				docNames.add(docName);
			}
			List<Annotation> docAnnotations = getDocuments(docNames);
			for(int i =0; i < docAnnotations.size(); i++){
				String docName = docNames.get(i);
				Annotation doc = docAnnotations.get(i);
				docNameToAnnoMap.put(docName,doc);
			}
			for(Integer key : mapFromSentToAnnotationAndDocName.keySet()){
				Pair<CoreMap,String> s = mapFromSentToAnnotationAndDocName.get(key);
				CoreMap sent = s.first;
				Annotation doc = docNameToAnnoMap.get(s.second);
				Pair<CoreMap,Annotation> newPair = new Pair<CoreMap,Annotation>(sent,doc);
				sentIdToAnnotationsMap.put(key, newPair);
			}
		}
		return sentIdToAnnotationsMap;
	}
	
	public CoreMap getSentence(Integer sentId) throws SQLException{
		ResultSet sentenceResult = cd.getSentenceRowByID(sentId);
		sentenceResult.next();
		CoreMap s = parseSentence(sentenceResult);
		return s;

	}
	

	/**
	 * Used to set the documentIterator to iterate over all the 
	 * documents except those specified in the testDocumentName
	 * @param testDocumentName
	 */
	public void setCorpusToTrain(String testDocumentName){
		train = true;
		test = false;
		this.testDocumentName = testDocumentName;
	}
	
	
	/**
	 * Used to set the documentIterator to only iterate
	 * over the documents in testDocumentName
	 * @param testDocumentName
	 */
	public void setCorpusToTest(String testDocumentName){
		train = false;
		test = true;
		this.testDocumentName = testDocumentName;
	}
	
	/**
	 * Use to set the documentIterator to behave as normal
	 * and iterate over all documents
	 */
	public void setCorpusToDefault(){
		train = false;
		test = false;
		testDocumentName = null;
	}
}
