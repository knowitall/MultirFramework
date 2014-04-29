package edu.washington.multirframework.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.Pair;

/**
 * Defines a mapping from argument type signatures
 * to candidate relations
 * @author jgilme1
 *
 */
public class TypeSignatureRelationMap {
	
	
	private static Map<Pair<String,String>,List<String>> typeSignatureRelationMap;

	public static void init(String pathToMapFile) throws IOException{
		BufferedReader br= new BufferedReader(new FileReader(new File(pathToMapFile)));
		String nextLine;
		typeSignatureRelationMap = new HashMap<>();
		while((nextLine = br.readLine())!=null){
			String[] values  = nextLine.split("\t");
			String arg1Type = values[0];
			String arg2Type = values[1];
			List<String> relations = new ArrayList<>();
			for(int i = 2; i < values.length; i++){
				String rel = values[i];
				relations.add(rel);
			}
			typeSignatureRelationMap.put(new Pair<String,String>(arg1Type,arg2Type), relations);
		}
		
		
		br.close();
	}
	
	public static List<String> getRelationsForTypeSignature(Pair<String,String> typeSignature){
		if(typeSignatureRelationMap.containsKey(typeSignature)){
			return typeSignatureRelationMap.get(typeSignature);
		}
		return null;
	}

}
