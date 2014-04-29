package edu.washington.multirframework.multiralgorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {

	private Map<String,Integer> rel2relID = new HashMap<String,Integer>();
	private Map<String,Integer> ft2ftID = new HashMap<String,Integer>();
	
	public int getRelationID(String relation, boolean addNew) {
		Integer i = rel2relID.get(relation);
		if (i == null) {
			if (!addNew) return -1;
			i = rel2relID.size();
			rel2relID.put(relation, i);
		}
		return i;
	}
	
	public Map<String,Integer> getRel2RelID() {
		return rel2relID;
	}
	
	public Map<String,Integer> getFt2ftId(){
		return ft2ftID;
	}
	
	public int getFeatureID(String feature, boolean addNew) {
		Integer i = ft2ftID.get(feature);
		if (i == null) {
			if (!addNew) return -1;
			i = ft2ftID.size();
			ft2ftID.put(feature, i);
		}
		return i;
	}
	
	public int numRelations() {
		return rel2relID.size();
	}
	
	public int numFeatures() {
		return ft2ftID.size();
	}
	
	public void write(String file) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(file), "utf-8"));
		writeMap(rel2relID, w);
		writeMap(ft2ftID, w);
		w.close();
	}
	
	public void read(String file) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader
				(new FileInputStream(file), "utf-8"));
		readMap(rel2relID, r);
		readMap(ft2ftID, r);
		r.close();
	}
	
	private void writeMap(Map<String,Integer> m, BufferedWriter w) 
		throws IOException {
		w.write(m.size() + "\n");
		List<Map.Entry<String,Integer>> l = new
			ArrayList<Map.Entry<String,Integer>>(m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String,Integer>>() {
			public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
				return e1.getValue() - e2.getValue(); } } );		
		for (Map.Entry<String,Integer> e : l)
			w.write(e.getValue() + "\t" + e.getKey() + "\n");
	}
	
	private void readMap(Map<String,Integer> m, BufferedReader r)
		throws IOException {
		int count = Integer.parseInt(r.readLine());
		for (int i=0; i < count; i++) {
			String[] t = r.readLine().split("\t");
			m.put(t[1], Integer.parseInt(t[0]));
		}
	}
	
	public static Mappings loadMappingsFromFeatureCountFile(String featureCountFile, int featureThreshold) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(featureCountFile));
		Mappings m = new Mappings();
		String line;
		int lineCount =0;
		int featCount =0;
		while((line = br.readLine())!=null){
			String[] values = line.split("\t");
			String feature = values[0];
			Integer count = Integer.parseInt(values[1]);
			if(count >= featureThreshold){
				m.getFeatureID(feature, true);
				featCount++;
			}
			lineCount++;
			if(lineCount % 100000 == 0){
				System.out.println("Read " + lineCount + " lines");
				System.out.println("Loaded " + featCount + " features");
			}
		}
		br.close();
		return m;
	}
	
	//used for testing loadMappingsFromFeatureCountFile
	public static void main(String[] args) throws IOException{
		loadMappingsFromFeatureCountFile("NELAndCorefTrain-NELDS-DefaultFeaturesWithFIGER-Count",2);
	}
}
