package edu.washington.multirframework.data;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.stanford.nlp.util.Pair;

/**
 * Represents Extraction
 * @author jgilme1
 *
 */
public class Extraction {
	Argument arg1;
	Argument arg2;
	String docName;
	String relation;
	Integer sentNum;
	Double score;
	String senText;
	Map<Integer,Double> featureScores;
	List<Pair<String,Double>> featureScoreList;
	
	public Extraction(Argument arg1, Argument arg2, String docName, String relation, Integer sentNum, String senText){
		this(arg1,arg2,docName,relation,sentNum,0.0,senText);
	}
	public Extraction(Argument arg1, Argument arg2, String docName, String relation, Integer sentNum, Double score,String senText){
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.docName = docName;
		this.relation = relation;
		this.sentNum = sentNum;
		this.score = score;
		this.senText = senText;
		featureScores = null;
	}
	
	@Override
	public boolean equals(Object other){
		Extraction e = (Extraction)other;
		if((arg1.equals(e.arg1)) &&
			(arg2.equals(e.arg2)) &&
			(docName.equals(e.docName)) &&
			(relation.equals(e.relation)) &&
			(senText.equals(e.senText))){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder(37,41).append(arg1).
				append(arg2).append(docName).append(relation)
				.append(senText).toHashCode();
	}
	
	/**
	 * Load an Extraction from a String
	 * @param s
	 * @return
	 */
	public static Extraction deserialize(String s){
		String[] values = s.split("\t");
		if(values.length != 10){
			throw new IllegalArgumentException("There should be 10 columns of data");
		}
		String arg1String = values[0] + "\t" + values[1] + "\t" + values[2];
		String arg2String = values[3] + "\t" + values[4] + "\t" + values[5];
		Argument arg1 = Argument.deserialize(arg1String);
		Argument arg2 = Argument.deserialize(arg2String);
		String senText = values[9];
		return new Extraction(arg1,arg2,values[6],values[7],Integer.parseInt(values[8]),senText);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(arg1.toString());
		sb.append("\t");
		sb.append(arg2.toString());
		sb.append("\t");
		sb.append(docName);
		sb.append("\t");
		sb.append(relation);
		sb.append("\t");
		sb.append(String.valueOf(sentNum));
		sb.append("\t");
		sb.append(senText);
		return sb.toString();
	}
	
	public Double getScore(){
		return score;
	}
	
	public String getRelation(){
		return relation;
	}
	
	public void setFeatureScores(Map<Integer,Double> fScores){
		featureScores = fScores;
	}
	
	public Map<Integer,Double> getFeatureScores(){
		return featureScores;
	}
	
	public void setFeatureScoreList(List<Pair<String,Double>> featureScoreList){
		this.featureScoreList = featureScoreList;
	}
	
	public List<Pair<String,Double>>  getFeatureScoreList() {return featureScoreList;}

}
