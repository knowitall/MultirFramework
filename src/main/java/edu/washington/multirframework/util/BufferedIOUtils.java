package edu.washington.multirframework.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BufferedIOUtils {
	public static BufferedReader getBufferedReader(File inputFile) throws FileNotFoundException, IOException{
		if(inputFile.getName().endsWith(".gz")){
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile))));
		}
		else{
			return new BufferedReader(new FileReader(inputFile));
		}
	}
	public static BufferedWriter getBufferedWriter(File outputFile) throws FileNotFoundException, IOException{
		if(outputFile.getName().endsWith(".gz")){
			return new BufferedWriter (new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile))));
		}
		else{
			return new BufferedWriter(new FileWriter(outputFile));
		}
	}
}
