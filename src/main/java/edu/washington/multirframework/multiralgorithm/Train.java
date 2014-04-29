package edu.washington.multirframework.multiralgorithm;

import java.io.File;
import java.io.IOException;
import java.util.Random;


public class Train {

	public static void train(String dir) throws IOException {
		
		Random random = new Random(1);
		
		Model model = new Model();
		model.read(dir + File.separatorChar + "model");
		
		AveragedPerceptron ct = new AveragedPerceptron(model, random);
		
		Dataset train = new MemoryDataset(dir + File.separatorChar + "train");

		System.out.println("starting training");
		
		long start = System.currentTimeMillis();
		Parameters params = ct.train(train);
		long end = System.currentTimeMillis();
		System.out.println("training time " + (end-start)/1000.0 + " seconds");

		params.serialize(dir + File.separatorChar + "params");
	}
	
	
	public static void main(String [] args) throws IOException{
		train(args[0]);
	}
}
