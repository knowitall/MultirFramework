package edu.washington.multirframework.multiralgorithm;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MemoryDataset implements Dataset {
	
	private MILDocument[] docs;
	private int cursor = 0;
	
	public MemoryDataset() {  }
	
	public MemoryDataset(String file) 
		throws IOException {
		MILDocument d = new MILDocument();
		List<MILDocument> l = new ArrayList<MILDocument>();
		DataInputStream dis = new DataInputStream(new BufferedInputStream
				(new FileInputStream(file)));
		while (d.read(dis)) {
			l.add(d);
			d = new MILDocument();
		}
		dis.close();
		docs = l.toArray(new MILDocument[0]);
	}
	
	public int numDocs() { return docs.length; }

	public void shuffle(Random random) {
		for (int i=0; i < docs.length; i++) {
			// pick element that we want to swap with
			int e = i + random.nextInt(docs.length - i);
			MILDocument tmp = docs[e];
			docs[e] = docs[i];
			docs[i] = tmp;
		}
	}

	public MILDocument next() { 
		if (cursor < docs.length) 
			return docs[cursor++]; 
		else return null;
	}

	public boolean next(MILDocument doc) {
		if (cursor < docs.length) {
			MILDocument d = docs[cursor++];
			doc.arg1 = d.arg1;
			doc.arg2 = d.arg2;
			doc.features = d.features;
			doc.mentionIDs = d.mentionIDs;
			doc.numMentions = d.numMentions;
			doc.Y = d.Y;
			doc.Z = d.Z;
			return true;
		}
		return false;
	}
	
	public void orderByMentionCount(){
		
		List<MILDocument> positiveInstances = new ArrayList<MILDocument>();
		List<MILDocument> negativeInstances = new ArrayList<MILDocument>();
		
		for(MILDocument md: docs){
			if(md.Y.length == 0){
				negativeInstances.add(md);
			}
			else{
				positiveInstances.add(md);
			}
		}
		System.out.println("positive isntances = " + positiveInstances.size());
		System.out.println("negative instances = " + negativeInstances.size());

		Collections.sort(positiveInstances, new Comparator<MILDocument>(){
			@Override
			public int compare(MILDocument o1, MILDocument o2) {
				
				if(o1.numMentions < 5 && o2.numMentions >= 5){
					return 1;
				}
				
				else if(o1.numMentions >= 5 && o2.numMentions <5 ){
					return -1;
				}
				
				else if(o1.numMentions<5 && o2.numMentions <5){
					
					int diff = o2.numMentions - o1.numMentions;
					if(diff != 0){
						return diff;
					}
					else{
						return (o1.arg1+o1.arg2).compareTo(o2.arg1+o2.arg2);
					}
				}
				
				//bot numMentions are >= 5
				else{
					
					int diff = o1.numMentions - o2.numMentions;
					if(diff != 0){
						return diff;
					}
					else{
						return (o1.arg1+o1.arg2).compareTo(o2.arg1+o2.arg2);
					}
				}
				
			}
		});
		
		Collections.sort(negativeInstances,new Comparator<MILDocument>(){
			@Override
			public int compare(MILDocument o1, MILDocument o2) {
				
				int diff = o1.numMentions - o2.numMentions;
				if(diff != 0){
					return diff;
				}
				else{
					return (o1.arg1+o1.arg2).compareTo(o2.arg1+o2.arg2);
				}
				
			}
		});
		
//		int ratio = (int)Math.floor((double)negativeInstances.size()/(double)positiveInstances.size());
//		int posIndex =0;
//		int negIndex =0;
//		for(int i =0; i < docs.length; i++){
//			
//			if( i % ratio == 0){
//				if(posIndex < positiveInstances.size()){
//					docs[i] = positiveInstances.get(posIndex);
//					posIndex++;
//				}
//				else{
//					docs[i] = negativeInstances.get(negIndex);
//					negIndex++;
//				}
//			}
//			else{
//				
//				if(negIndex < negativeInstances.size()){
//					docs[i] = negativeInstances.get(negIndex);
//					negIndex++;
//				}
//				else{
//					docs[i] = positiveInstances.get(posIndex);
//					posIndex++;
//				}
//			}
//		}
		
		int posIndex =0;
		int negIndex =0;
		for(int i =0; i < docs.length; i++){
			if(i % 2 == 0){
				if(posIndex < positiveInstances.size()){
					docs[i] = positiveInstances.get(posIndex);
					posIndex++;
				}
				else{
					docs[i] = negativeInstances.get(negIndex);
					negIndex++;
				}
			}
			else{
				if(negIndex < negativeInstances.size()){
					docs[i] = negativeInstances.get(negIndex);
					negIndex++;
				}
				else{
					docs[i] = positiveInstances.get(posIndex);
					posIndex++;
				}
			}
		}
		
		
	}

	public void reset() {
		cursor = 0;
	}

}