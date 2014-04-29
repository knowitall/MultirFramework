package edu.washington.multirframework.extractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.washington.multirframework.multiralgorithm.FullInference;
import edu.washington.multirframework.multiralgorithm.MILDocument;
import edu.washington.multirframework.multiralgorithm.Parse;
import edu.washington.multirframework.multiralgorithm.SparseBinaryVector;
import edu.washington.multirframework.multiralgorithm.Mappings;
import edu.washington.multirframework.multiralgorithm.Model;
import edu.washington.multirframework.multiralgorithm.Parameters;
import edu.washington.multirframework.multiralgorithm.Scorer;
import edu.washington.multirframework.argumentidentification.ArgumentIdentification;
import edu.washington.multirframework.argumentidentification.SententialInstanceGeneration;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.data.Argument;
import edu.washington.multirframework.data.KBArgument;
import edu.washington.multirframework.featuregeneration.FeatureGenerator;
/**
 * An extractor that provides extractions
 * from a document based on a trained
 * Multir model. Based on the DEFT framework
 * @author jgilme1
 *
 */
public class MultirRelationExtractor {
	
	private FeatureGenerator fg;
	private ArgumentIdentification ai;
	private SententialInstanceGeneration sig;
	private CorpusInformationSpecification cis;
	
	private String dir;
	private Mappings mapping;
	private Model model;
	private Parameters params;
	private Scorer scorer;
	
	private Map<Integer, String> relID2rel = new HashMap<Integer, String>();


	public MultirRelationExtractor(String pathToMultirFiles, FeatureGenerator fg,
			ArgumentIdentification ai, SententialInstanceGeneration sig, CorpusInformationSpecification cis){
		this.fg = fg;
		this.ai = ai;
		this.sig = sig;
		this.cis = cis;
		dir = pathToMultirFiles;
		try {
			mapping = new Mappings();
			mapping.read(dir + "/mapping");

			model = new Model();
			model.read(dir + "/model");

			params = new Parameters();
			params.model = model;
			params.deserialize(dir + "/params");

			scorer = new Scorer();
			
			for(String key :mapping.getRel2RelID().keySet()){
				Integer id = mapping.getRel2RelID().get(key);
				relID2rel.put(id, key);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Triple<String,Double,Double> extractFromSententialInstance(Argument arg1, Argument arg2, CoreMap sentence, Annotation doc){
		String senText = sentence.get(CoreAnnotations.TextAnnotation.class);
		String arg1ID = null;
		String arg2ID = null;
		if(arg1 instanceof KBArgument){
			arg1ID = ((KBArgument)arg1).getKbId();
		}
		if(arg2 instanceof KBArgument){
			arg2ID = ((KBArgument)arg2).getKbId();
		}
		List<String> features = 
				fg.generateFeatures(arg1.getStartOffset(), arg1.getEndOffset(), 
						arg2.getStartOffset(), arg2.getEndOffset(), 
						arg1ID,arg2ID,sentence, doc);
		Pair<Triple<String,Double,Double>,Map<Integer,Double>> p = getPrediction(features,arg1,arg2,senText);
		return p.first;
	}
	

	/**
	 * Conver features and args to MILDoc
	 * and run Multir sentential extraction
	 * algorithm, return null if no extraction
	 * was predicted.
	 * @param features
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	private Pair<Triple<String,Double,Double>,Map<Integer,Double>> getPrediction(List<String> features, Argument arg1,
			Argument arg2, String senText) {
		
		MILDocument doc = new MILDocument();
		
		doc.arg1 = arg1.getArgName();
		doc.arg2 = arg2.getArgName();
		doc.Y = new int[1];
		doc.numMentions = 1;// sentence level prediction
		doc.setCapacity(1);
		SparseBinaryVector sv = doc.features[0] = new SparseBinaryVector();
		
		
		SortedSet<Integer> ftrset = new TreeSet<Integer>();
		for (String f : features) {
			int ftrid = mapping.getFeatureID(f, false);
			if (ftrid >= 0) {
				ftrset.add(ftrid);
			}
		}
		
		sv.num = ftrset.size();
		sv.ids = new int[sv.num];
		
		int k = 0;
		for (int f : ftrset) {
			sv.ids[k++] = f;
		}
		
		String relation = "";
		Double conf = 0.0;
		Map<Integer,Map<Integer,Double>> mentionFeatureScoreMap = new HashMap<>();
		Parse parse = FullInference.infer(doc, scorer, params,mentionFeatureScoreMap);

		if (parse.Z[0] > 0) {
			relation = relID2rel.get(parse.Z[0]);
			Arrays.sort(parse.allScores[0]);
			double combinedScore = parse.score;
			
			for(int i =0; i < parse.allScores[0].length-1; i++){
				double s = parse.allScores[0][i];
				if( s > 0.0){
					combinedScore +=s;
				}
			}
			double confidence = (combinedScore <= 0.0 || parse.score <= 0.0) ? .1 : (parse.score/combinedScore);
			if(combinedScore == parse.score && parse.score > 0.0){
				confidence = .001;
			}
			conf = confidence;
		} else {
			Map<Integer,Map<Integer,Double>> negMentionFeatureScoreMap = new HashMap<>();
			FullInference.infer(doc, scorer, params,negMentionFeatureScoreMap,0);
			Triple<String,Double,Double> t = new Triple<>("NA",conf,parse.score);
			Pair<Triple<String,Double,Double>,Map<Integer,Double>> p = new Pair<>(t,negMentionFeatureScoreMap.get(0));
			return p;
		}

		Triple<String,Double,Double> t = new Triple<>(relation,conf,parse.score);
		Pair<Triple<String,Double,Double>,Map<Integer,Double>> p = new Pair<>(t,mentionFeatureScoreMap.get(0));
		return p;
	}
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws SQLException 
	 */
	public void batchExtract(String corpusDB) throws SQLException, IOException {
		Corpus c = new Corpus(corpusDB,cis,true);
		Iterator<Annotation> docIter = c.getDocumentIterator();
		while(docIter.hasNext()){
			Annotation doc = docIter.next();
			List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
			for(CoreMap s :sentences){
				List<Pair<Argument,Argument>> sigs = sig.generateSententialInstances(ai.identifyArguments(doc, s), s);
				for(Pair<Argument,Argument> si : sigs){
					List<String> features = 
							fg.generateFeatures(si.first.getStartOffset(), si.first.getEndOffset(), 
							si.second.getStartOffset(), si.second.getEndOffset(),
							null, null, s, doc);
					Pair<Triple<String,Double,Double>,Map<Integer,Double>> prediction =
					getPrediction(features, si.first, si.second, s.get(CoreAnnotations.TextAnnotation.class));
					System.out.println(si.first + "\t" + si.second + "\t" + prediction.first.first);					
				}
			}
		}
	}

}
