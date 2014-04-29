package edu.washington.multirframework.multiralgorithm;

import java.util.HashMap;
import java.util.Map;

public class Viterbi {

	private Scorer parseScorer;
	private Model model;
	
	public Viterbi(Model model, Scorer parseScorer) {
		this.model = model;
		this.parseScorer = parseScorer;
	}
	
	public Parse parse(MILDocument doc, int mention) {
		int numRelations = model.numRelations;

		// relation X argsReversed
		double[] scores = new double[numRelations];
				
		// lookup signature
		for (int s = 0; s < numRelations; s++)
			scores[s] = parseScorer.scoreMentionRelation(doc, mention, s);

		int bestRel = 0;
		for (int r = 0; r < model.numRelations; r++) {
			if (scores[r] > scores[bestRel]) {
				bestRel = r; }
		}
		//run again to set the featureScoreMap to the one for the highest relation
		parseScorer.scoreMentionRelation(doc, mention, bestRel);

		Parse p = new Parse(bestRel, scores[bestRel]);
		p.scores = scores;
		return p;
	}
	
	
	public Parse parse(MILDocument doc, int mention, Map<Integer,Double> featureScoreMap) {
		int numRelations = model.numRelations;

		// relation X argsReversed
		double[] scores = new double[numRelations];
				
		// lookup signature
		for (int s = 0; s < numRelations; s++)
			scores[s] = parseScorer.scoreMentionRelation(doc, mention, s, featureScoreMap);

		int bestRel = 0;
		for (int r = 0; r < model.numRelations; r++) {
			if (scores[r] > scores[bestRel]) {
				bestRel = r; }
		}
		//run again to set the featureScoreMap to the one for the highest relation
		parseScorer.scoreMentionRelation(doc, mention, bestRel, featureScoreMap);

		Parse p = new Parse(bestRel, scores[bestRel]);
		p.scores = scores;
		return p;
	}
	
	public Parse parseWithFeatureScoreMap(MILDocument doc, int mention, Map<Integer,Map<Integer,Double>> featureScoreMap) {
		int numRelations = model.numRelations;

		// relation X argsReversed
		double[] scores = new double[numRelations];
				
		// lookup signature
		for (int s = 0; s < numRelations; s++){
			Map<Integer,Double> newMap = new HashMap<Integer,Double>();
			scores[s] = parseScorer.scoreMentionRelation(doc, mention, s, newMap);
			featureScoreMap.put(s,newMap);
		}

		int bestRel = 0;
		for (int r = 0; r < model.numRelations; r++) {
			if (scores[r] > scores[bestRel]) {
				bestRel = r; }
		}
		//run again to set the featureScoreMap to the one for the highest relation
		//parseScorer.scoreMentionRelation(doc, mention, bestRel, featureScoreMap);

		Parse p = new Parse(bestRel, scores[bestRel]);
		p.scores = scores;
		return p;
	}
	
	public Parse parse(MILDocument doc, int mention, Map<Integer,Double> featureScoreMap, int rel) {
		int numRelations = model.numRelations;

		// relation X argsReversed
		double[] scores = new double[numRelations];
				
		// lookup signature
		for (int s = 0; s < numRelations; s++)
			scores[s] = parseScorer.scoreMentionRelation(doc, mention, s, featureScoreMap);

		int bestRel = 0;
		for (int r = 0; r < model.numRelations; r++) {
			if (scores[r] > scores[bestRel]) {
				bestRel = r; }
		}
		//run again to set the featureScoreMap to the one for the highest relation
		parseScorer.scoreMentionRelation(doc, mention, rel, featureScoreMap);

		Parse p = new Parse(bestRel, scores[bestRel]);
		p.scores = scores;
		return p;
	}
	
	public static class Parse {
		// MPE
		public int state;
		public double score;
		
		// scores of all assignments
		public double[] scores;
		
		Parse(int state, double score) {
			this.state = state;
			this.score = score;
		}
	}
}
