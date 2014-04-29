package edu.washington.multirframework.multiralgorithm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.Pair;

public class PreprocessBigFeatureFile {
	
	
	private static Map<String,Integer> keyToIntegerMap = new HashMap<String,Integer>();
    private static Map<Integer,String> intToKeyMap = new HashMap<Integer,String>();
    private static final int FEATURE_THRESHOLD = 2;

    private static final double GIGABYTE_DIVISOR = 1073741824;
	/**
	 * args[0] is path to featuresTrain
	 * args[1] is path directory for new multir files like
	 * 			mapping, model, train, test..
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) 
	throws IOException {
    	long start = System.currentTimeMillis();
    	
    	printMemoryStatistics();

  
		String trainFile = args[0];
		String featureCountFile = args[1];
		String targetRelationsFile = args[2];
		String outDir = args[3];
		String mappingFile = outDir + File.separatorChar + "mapping";
		String modelFile = outDir + File.separatorChar + "model";
		
		System.out.println("GETTING Mapping form training data");
		Mappings mapping = getMappingFromTrainingData(featureCountFile,targetRelationsFile);
		

		
			System.out.println("PREPROCESSING TRAIN FEATURES");
		{
			String output1 = outDir + File.separatorChar + "train";
			convertFeatureFileToMILDocument(trainFile, output1, mapping);
		}
		
			System.out.println("FINISHED PREPROCESSING TRAIN FEATURES");
			printMemoryStatistics();
			keyToIntegerMap.clear();

			intToKeyMap.clear();

	
		System.out.println("Writing model and mapping file");
		printMemoryStatistics();


		
		{
			Model m = new Model();
			m.numRelations = mapping.numRelations();
			m.numFeaturesPerRelation = new int[m.numRelations];
			for (int i=0; i < m.numRelations; i++)
				m.numFeaturesPerRelation[i] = mapping.numFeatures();
			m.write(modelFile);
			mapping.write(mappingFile);
		}
		
    	long end = System.currentTimeMillis();
    	System.out.println("Preprocessing took " + (end-start) + " millisseconds");
	}
	


	/**
	 * Obtain mappings object from training features file onle
	 * @param trainFile
	 * @param mappingFile
	 * @return
	 * @throws IOException
	 */
	private static Mappings getMappingFromTrainingData(String featureCountFile,
			String targetRelationsFile) throws IOException {

		Mappings m = Mappings.loadMappingsFromFeatureCountFile(featureCountFile, FEATURE_THRESHOLD);
		m.getRelationID("NA", true);
		BufferedReader br = new BufferedReader(new FileReader(new File(targetRelationsFile)));
		String line;
		while((line = br.readLine())!=null){
			m.getRelationID(line,true);
		}
		br.close();
		return m;
	}
	/**
	 * Converts featuresTrain or featuresTest to 
	 * train or test by aggregating the entity pairs
	 * into relations and their mentions.
	 * @param input - the test/train file in non-multir
	 * 				  format
	 * @param output - the test/train file in multir,
	 * 					MILDoc format
	 * @param m - the mappings object that keeps track of
	 * 			  relevant relations and features
	 * @throws IOException
	 */
	private static void convertFeatureFileToMILDocument(String input, String output, Mappings m) throws IOException {
		//open input and output streams
		DataOutputStream os = new DataOutputStream
			(new BufferedOutputStream(new FileOutputStream(output)));
	
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		System.out.println("Set up buffered reader");
	    
	    //create MILDocument data map
	    //load feature generation data into map from argument pair keys to
	    //a Pair of List<Integer> relations and a List<List<Integer>> for features
	    //for each instance
	    Map<Integer,Pair<List<Integer>,List<List<Integer>>>> relationMentionMap = new HashMap<>();
	    
	    String line;
	    while((line = br.readLine()) != null){
	    	String[] values = line.split("\t");
	    	String arg1Id = values[1];
	    	String arg2Id = values[2];
	    	String relString = values[3];
	    	String[] rels = relString.split("\\|");
	    	// entity pair key separated by a delimiter
	    	String key = arg1Id+"%"+arg2Id;
	    	Integer intKey = getIntKey(key);
	    	List<String> features = new ArrayList<>();
	    	//add all features
	    	for(int i = 4; i < values.length; i++){
	    		features.add(values[i]);
	    	}
	    	//convert to integer keys from the mappings m object
	    	List<Integer> featureIntegers = convertFeaturesToIntegers(features,m);
	    	
	    	//update map entry
	    	if(relationMentionMap.containsKey(intKey)){
	    		Pair<List<Integer>, List<List<Integer>>> p = relationMentionMap.get(intKey);
	    		List<Integer> oldRelations = p.first;
	    		List<List<Integer>> oldFeatures = p.second;
	    		for(String rel: rels){
	    			Integer relKey = getIntRelKey(rel,m);
	    			if(!oldRelations.contains(relKey)){
	    				oldRelations.add(relKey);
	    			}
	    		}
	    		oldFeatures.add(featureIntegers);
	    	}
	    	
	    	//new map entry
	    	else{
	    		List<Integer> relations = new ArrayList<>();
	    		for(String rel: rels){
	    			relations.add(getIntRelKey(rel,m));
	    		}
	    		List<List<Integer>> newFeatureList = new ArrayList<>();
	    		newFeatureList.add(featureIntegers);
	    		Pair<List<Integer>, List<List<Integer>>> p = new Pair<List<Integer>, List<List<Integer>>>(relations, newFeatureList);
	    		relationMentionMap.put(intKey,p);
	    	}
	    	if(relationMentionMap.size() % 100000 == 0){
	    		System.out.println("Number of entity pairs read in =" + relationMentionMap.size());
	    		printMemoryStatistics();
	    	}
	    }
	    
	    br.close();
	    System.out.println("LOADED MAP!");
	    
	    MILDocument doc = new MILDocument();	    
    	
	    //iterate over keys in the map and create MILDocuments
	    int count =0;
	    for(Integer intKey : relationMentionMap.keySet()){
	    	doc.clear();

	    	String[] keySplit = getStringKey(intKey).split("%");
	    	String arg1 = keySplit[0];
	    	String arg2 = keySplit[1];
	    	Pair<List<Integer>,List<List<Integer>>> p = relationMentionMap.get(intKey);
	    	List<Integer> intRels = p.first;
	    	List<List<Integer>> intFeatures= p.second;
	    	
	    	doc.arg1 = arg1;
	    	doc.arg2 = arg2;
	    	
	//    	System.out.println(arg1+"\t"+arg2);
	    	
	    	// set relations
	    	{
		    	int[] irels = new int[intRels.size()];
		    	for (int i=0; i < intRels.size(); i++)
		    		irels[i] = intRels.get(i);
		    	Arrays.sort(irels);
		    	// ignore NA and non-mapped relations
		    	int countUnique = 0;
		    	for (int i=0; i < irels.length; i++)
		    		if (irels[i] > 0 && (i == 0 || irels[i-1] != irels[i]))
		    			countUnique++;
		    	doc.Y = new int[countUnique];
		    	int pos = 0;
		    	for (int i=0; i < irels.length; i++)
		    		if (irels[i] > 0 && (i == 0 || irels[i-1] != irels[i]))
		    			doc.Y[pos++] = irels[i];
		    	
//		    	System.out.println("Int rels");
//		    	for(int ir: irels){
//		    		System.out.print(ir + " ");
//		    	}
//		    	System.out.println("Original rels ");
//		    	for(Integer ir : intRels){
//		    		System.out.print(ir + " ");
//		    	}
//		    	System.out.println();
//		    	if((irels[0] !=0) && (intFeatures.size() ==1)){
//		    		System.out.println("Singleton =\t" + arg1 + "\t" + arg2);
//		    	}
	    	}
	    	
	    	// set mentions
	    	doc.setCapacity(intFeatures.size());
	    	doc.numMentions = intFeatures.size();
	    	
	    	for (int j=0; j < intFeatures.size(); j++) {
		    	doc.Z[j] = -1;
	    		doc.mentionIDs[j] = j;
	    		SparseBinaryVector sv = doc.features[j] = new SparseBinaryVector();
	    		
	    		List<Integer> instanceFeatures = intFeatures.get(j);
	    		int[] fts = new int[instanceFeatures.size()];
	    		
	    		for (int i=0; i < instanceFeatures.size(); i++)
	    			fts[i] = instanceFeatures.get(i);
	    		Arrays.sort(fts);
		    	int countUnique = 0;
		    	for (int i=0; i < fts.length; i++)
		    		if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    			countUnique++;
		    	sv.num = countUnique;
		    	sv.ids = new int[countUnique];
		    	int pos = 0;
		    	for (int i=0; i < fts.length; i++)
		    		if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    			sv.ids[pos++] = fts[i];
		    	
//		    	System.out.println("Int features");
//		    	for(int ft: fts){
//		    		System.out.print(ft + " ");
//		    	}
//		    	System.out.println();
	    	}
	    	doc.write(os);
	    	count ++;
	    	
	    	if(count % 100000 == 0){
	    		System.out.println(count + " entity pairs processed");
	    		printMemoryStatistics();
	    	}
	    	

	    }
		os.close();
	}

	private static Integer getIntRelKey(String rel, Mappings m) {
		
		return m.getRelationID(rel, false);
		
	}



	private static List<Integer> convertFeaturesToIntegers(
			List<String> features, Mappings m) {
		
		List<Integer> intFeatures = new ArrayList<Integer>();
		
		for(String feature: features){
			Integer intFeature = m.getFeatureID(feature, false);
			if(intFeature != -1){
				intFeatures.add(intFeature);
			}
		}
		
		return intFeatures;
	}





	private static String getStringKey(Integer intKey) {
		if(intToKeyMap.containsKey(intKey)){
			return intToKeyMap.get(intKey);
		}
		else{
			throw new IllegalStateException();
		}
	}

	private static Integer getIntKey(String key) {
		if(keyToIntegerMap.containsKey(key)){
			return keyToIntegerMap.get(key);
		}
		else{
			Integer intKey = keyToIntegerMap.size();
			keyToIntegerMap.put(key, intKey);
			intToKeyMap.put(intKey, key);
			return intKey;
		}
	}
	
	private static void printMemoryStatistics() {
		double freeMemory = Runtime.getRuntime().freeMemory()/GIGABYTE_DIVISOR;
		double allocatedMemory = Runtime.getRuntime().totalMemory()/GIGABYTE_DIVISOR;
		double maxMemory = Runtime.getRuntime().maxMemory()/GIGABYTE_DIVISOR;
		System.out.println("MAX MEMORY: " + maxMemory);
		System.out.println("ALLOCATED MEMORY: " + allocatedMemory);
		System.out.println("FREE MEMORY: " + freeMemory);
	}
}
