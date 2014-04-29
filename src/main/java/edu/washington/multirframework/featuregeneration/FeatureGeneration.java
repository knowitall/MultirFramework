package edu.washington.multirframework.featuregeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.corpus.CorpusInformationSpecification.SentGlobalIDInformation.SentGlobalID;
import edu.washington.multirframework.util.BufferedIOUtils;

public class FeatureGeneration {
	
	private FeatureGenerator fg;
	public FeatureGeneration(FeatureGenerator fg){
		this.fg = fg;
	}
	
	public void run(List<String> dsFileNames, List<String> featureFileNames, Corpus c, CorpusInformationSpecification cis) throws FileNotFoundException, IOException, SQLException, InterruptedException, ExecutionException{
		
		long originalStart = System.currentTimeMillis();
    	long start = System.currentTimeMillis();
		//initialize variables
    	
    	List<SententialArgumentPair> saps = getSaps(dsFileNames,featureFileNames);
    	long end = System.currentTimeMillis();
    	System.out.println("Sentential Argument Pair collection took " + (end-start) + "milliseconds");
    	
    	//get map from SentID to Sap
    	start = System.currentTimeMillis();
    	Map<Integer,List<SententialArgumentPair>> sapMap = new HashMap<>();
    	for(SententialArgumentPair sap : saps){
    		Integer id = sap.sentID;
    		if(sapMap.containsKey(id)){
    			sapMap.get(id).add(sap);
    		}
    		else{
    			List<SententialArgumentPair> sameIdSaps = new ArrayList<>();
    			sameIdSaps.add(sap);
    			sapMap.put(id,sameIdSaps);
    		}
    	}
    	end = System.currentTimeMillis();
    	System.out.println("Map from sentence ids to saps created in " + (end-start) + "milliseconds");
    	
    	//initialize feature Writers
    	Map<String,BufferedWriter> writerMap = new HashMap<>();
    	for(int i =0; i < dsFileNames.size(); i++){
    		String dsFileName = dsFileNames.get(i);
    		String featureFileName = featureFileNames.get(i);
    		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(featureFileName)));
    		writerMap.put(dsFileName, bw);
    	}
    	
    	//iterate over corpus
    	Iterator<Annotation> di = c.getDocumentIterator();
    	int docCount =0;
    	start = System.currentTimeMillis();
    	while(di.hasNext()){
    		Annotation doc = di.next();
    		List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
    		for(CoreMap sentence: sentences){
    			Integer currSentID = sentence.get(SentGlobalID.class);
    			if(sapMap.containsKey(currSentID)){
    				List<SententialArgumentPair> sentenceSaps = sapMap.get(currSentID);
    				writeFeatures(sentenceSaps,doc,sentence,writerMap);
    			}
    		}
    		docCount++;
    		if(docCount % 100000 == 0){
    			end = System.currentTimeMillis();
    			System.out.println(docCount + " documents processed in " + (end-start) + "milliseconds");
    			start = System.currentTimeMillis();
    		}
    	}
    	
    	
    	//close writers
    	for(String key : writerMap.keySet()){
    		BufferedWriter bw = writerMap.get(key);
    		bw.close();
    	}
		
    	end = System.currentTimeMillis();
    	System.out.println("Feature Generation took " + (end-originalStart) + " millisseconds");
    	
	}

	private void writeFeatures(List<SententialArgumentPair> currentSaps,
			Annotation doc, CoreMap sentence,
			Map<String, BufferedWriter> writerMap) throws IOException {
		
		for(SententialArgumentPair sap : currentSaps){
			BufferedWriter bw = writerMap.get(sap.partitionID);
			List<String> features = fg.generateFeatures(sap.arg1Offsets.first,sap.arg1Offsets.second
					,sap.arg2Offsets.first,sap.arg2Offsets.second,sap.arg1ID,sap.arg2ID,sentence,doc);
			bw.write(makeFeatureString(sap,features)+"\n");
		}
	}

	private List<SententialArgumentPair> getSaps(List<String> dsFileNames,
			List<String> featureFileNames) throws FileNotFoundException, IOException {
    	List<SententialArgumentPair> saps = new ArrayList<>();
    	
    	for(int i =0; i < dsFileNames.size(); i++){
    		
    		String dsFileName = dsFileNames.get(i);
    		String featureFileName = featureFileNames.get(i);
			BufferedReader in;
			BufferedWriter bw;
			in = BufferedIOUtils.getBufferedReader(new File(dsFileName));
			bw = BufferedIOUtils.getBufferedWriter(new File(featureFileName));
	
			String nextLine = in.readLine();
			List<SententialArgumentPair> currentSaps = new ArrayList<>();
			while(nextLine != null){
				SententialArgumentPair sap = SententialArgumentPair.parseSAP(nextLine);
				sap.setPartitionId(dsFileName);
				boolean mergeSap = false;
				if(currentSaps.size()>0){
					if(currentSaps.get(currentSaps.size()-1).matchesSAP(sap)){
						mergeSap = true;
					}
				}
				if(mergeSap){
					currentSaps.get(currentSaps.size()-1).mergeSAP(sap);
				}
				else{
					currentSaps.add(sap);
				}			
				nextLine = in.readLine();
			}
			bw.close();
			in.close();
			saps.addAll(currentSaps);
    	}
		
    	//sort saps by global sentence id
    	Collections.sort(saps,new Comparator<SententialArgumentPair>(){
			@Override
			public int compare(SententialArgumentPair arg0,
					SententialArgumentPair arg1) {
				return (arg0.sentID - arg1.sentID);
			}
    		
    	});
    	
		return saps;
	}

	private   String makeFeatureString(SententialArgumentPair sap,
			List<String> features) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sap.getSentID()));
		sb.append("\t");
		sb.append(sap.getArg1Id());
		sb.append("\t");
		sb.append(sap.getArg2Id());
		sb.append("\t");
		for(String rel : sap.getRelations()){
			sb.append(rel);
			sb.append("&&");
		}
		sb.setLength(sb.length()-2);
		sb.append("\t");
		for(String f: features){
			sb.append(f);
			sb.append("\t");
		}
		return sb.toString().trim();
	}
	
	public static class SententialArgumentPair{
		
		private Integer sentID;
		private Pair<Integer,Integer> arg1Offsets;
		private Pair<Integer,Integer> arg2Offsets;
		private List<String> relations;
		private String arg1ID;
		private String arg2ID;
		private String partitionID;
		
		private SententialArgumentPair(Integer sentID, Pair<Integer,Integer> arg1Offsets,
										Pair<Integer,Integer> arg2Offsets, String relation,
										String arg1ID, String arg2ID){
			this.sentID = sentID;
			this.arg1Offsets = arg1Offsets;
			this.arg2Offsets = arg2Offsets;
			relations = new ArrayList<String>();
			relations.add(relation);
			this.arg1ID = arg1ID;
			this.arg2ID = arg2ID;
		}
		
		
		public boolean matchesSAP(SententialArgumentPair other){
			if( (other.sentID.equals(this.sentID))
				&& (other.arg1Offsets.equals(this.arg1Offsets))
				&& (other.arg2Offsets.equals(this.arg2Offsets))
				&& (other.arg1ID.equals(this.arg1ID))
				&& (other.arg2ID.equals(this.arg2ID))){
				return true;
			}
			return false;
		}
		
		public void mergeSAP(SententialArgumentPair other){
			if(this.matchesSAP(other)){
				
				for(String rel : other.relations){
					if(!this.relations.contains(rel)){
						this.relations.add(rel);
					}
				}
			}
			else{
				throw new IllegalArgumentException("SententialArgumentPair other must match this SententialArgumentPair");
			}
			
		}
		
		public static SententialArgumentPair parseSAP(String dsLine){
			try{
				String[] values = dsLine.split("\t");
				Integer arg1Start = Integer.parseInt(values[1]);
				Integer arg1End = Integer.parseInt(values[2]);
				Pair<Integer,Integer> arg1Offsets = new Pair<>(arg1Start,arg1End);
				Integer arg2Start = Integer.parseInt(values[5]);
				Integer arg2End = Integer.parseInt(values[6]);
				Pair<Integer,Integer> arg2Offsets = new Pair<>(arg2Start,arg2End);
				Integer sentId = Integer.parseInt(values[8]);
				
				
				return new SententialArgumentPair(sentId,arg1Offsets,arg2Offsets,values[9],values[0],values[4]);
			}
			catch(Exception e){
				throw new IllegalArgumentException("Line cannot be parsed into a SententialArgumentPair");
			}
		}
		
		
		public String getArg1Id(){return arg1ID;}
		public String getArg2Id(){return arg2ID;}
		public List<String> getRelations(){return relations;}
		public Integer getSentID(){return sentID;}
		public Pair<Integer,Integer> getArg1Offsets(){return arg1Offsets;}
		public Pair<Integer,Integer> getArg2Offsets(){return arg2Offsets;}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(arg1ID + "\t"+ arg1Offsets.first+":"+arg1Offsets.second + "\t"+ arg2ID + "\t" +  arg2Offsets.first+":"+arg2Offsets.second + "\t");
			int count =0;
			for(String relation : relations){
				if(count == 0) sb.append(relation);
				else sb.append("&&"+relation);
				count++;
			}
			sb.append("\t" + sentID);
			String partitionString = null;
			if(partitionID !=null){
				partitionString = partitionID;
			}
			sb.append("\t" + partitionString);
			return sb.toString().trim();
		}
		
		public void setPartitionId(String id){
			partitionID = id;
		}
	}
}
