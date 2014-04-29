package edu.washington.multirframework.data;

import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.stanford.nlp.util.Interval;

/**
 * Argument represents an offset interval
 * with optional meta information like
 * name and id.
 * @author jgilme1
 *
 */
public class Argument {
	int startOffset;
	int endOffset;
	String argName;

	public int getStartOffset(){return startOffset;}
	public String getArgName(){return argName;}
	public int getEndOffset(){return endOffset;}
	
	public Argument(String name, int startOffset, int endOffset){
		this.argName = name;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}
	
	protected Argument (Argument a){
		this.startOffset = a.startOffset;
		this.endOffset = a.endOffset;
		this.argName = a.argName;
	}
	public boolean intersectsWithList(List<Argument> args) {
		
		for(Argument arg: args){
			if(this.intersects(arg)){
				return true;
			}
		}
		return false;
	}
	private boolean intersects (Argument other){
		Interval<Integer> thisInterval = Interval.toInterval(this.startOffset,this.endOffset);
		Interval<Integer> otherInterval = Interval.toInterval(other.startOffset, other.endOffset);
		if(thisInterval.intersect(otherInterval) != null){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean containedInList(List<Argument> args) {
		
		for(Argument other : args){
			if(this.hasSameOffsets(other)){
				return true;
			}
		}
		return false;

	}
	
	private boolean hasSameOffsets(Argument other){
		if(
		   (this.getStartOffset() == other.getStartOffset()) &&
		   (this.getEndOffset()  == other.getEndOffset()) && 
		   (this.getArgName().equals(other.getArgName()))){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean isContainedIn(Argument other){
		
		if(
			((this.getStartOffset() > other.getStartOffset()) && (this.getEndOffset() <= other.getEndOffset())) ||
			((this.getStartOffset() >= other.getStartOffset()) && (this.getEndOffset() < other.getEndOffset())) ||
			((this.getStartOffset() > other.getStartOffset()) && (this.getEndOffset() < other.getEndOffset()))){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other){
		Argument o = (Argument)other;
		if((this.startOffset == o.startOffset) &&
		    (this.endOffset == o.endOffset) &&
		    (this.argName.equals(o.argName))){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder(17,19).append(this.startOffset).append(this.endOffset).append(this.argName).toHashCode();
	}
	
	public static Argument deserialize(String s){
		String [] values = s.split("\t");
		if(values.length != 3){
			throw new IllegalArgumentException("There should be 3 columns of data");
		}
		String name = values[0];
		Integer start = Integer.parseInt(values[1]);
		Integer end = Integer.parseInt(values[2]);
		return new Argument(name,start,end);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(argName);
		sb.append("\t");
		sb.append(String.valueOf(startOffset));
		sb.append("\t");
		sb.append(String.valueOf(endOffset));
		return sb.toString();
	}
}
