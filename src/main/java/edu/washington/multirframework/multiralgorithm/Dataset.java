package edu.washington.multirframework.multiralgorithm;

import java.util.Random;

public interface Dataset {
	
	public int numDocs();
	
	public void shuffle(Random random);
	
	public MILDocument next();
	
	public boolean next(MILDocument doc);
	
	public void reset();
}
