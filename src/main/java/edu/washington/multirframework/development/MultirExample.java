package edu.washington.multirframework.development;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.ParseException;

import edu.washington.multirframework.argumentidentification.ArgumentIdentification;
import edu.washington.multirframework.argumentidentification.DefaultSententialInstanceGeneration;
import edu.washington.multirframework.argumentidentification.NERArgumentIdentification;
import edu.washington.multirframework.argumentidentification.NERRelationMatching;
import edu.washington.multirframework.argumentidentification.RelationMatching;
import edu.washington.multirframework.argumentidentification.SententialInstanceGeneration;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.corpus.DefaultCorpusInformationSpecification;
import edu.washington.multirframework.distantsupervision.DistantSupervision;
import edu.washington.multirframework.distantsupervision.NegativeExampleCollection;
import edu.washington.multirframework.distantsupervision.NegativeExampleCollectionByRatio;
import edu.washington.multirframework.extractor.MultirRelationExtractor;
import edu.washington.multirframework.featuregeneration.DefaultFeatureGenerator;
import edu.washington.multirframework.featuregeneration.FeatureGeneration;
import edu.washington.multirframework.featuregeneration.FeatureGenerator;
import edu.washington.multirframework.knowledgebase.KnowledgeBase;
import edu.washington.multirframework.multiralgorithm.Preprocess;
import edu.washington.multirframework.multiralgorithm.Train;
public class MultirExample {
	
	
	public static void main(String[] args) throws SQLException, IOException, InterruptedException, ExecutionException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ParseException{
		
		//load in corpus from corpus directory
		CorpusInformationSpecification cis = new DefaultCorpusInformationSpecification();
    	Corpus c = new Corpus(args[0],cis,false);
    	c.loadCorpus(new File(args[1]), args[2], args[3]);

    	
    	//load Knowledge Base
    	KnowledgeBase kb = new KnowledgeBase(args[4],args[5],args[6]);
    	
    	
    	//set custom algorithms
    	ArgumentIdentification ai = NERArgumentIdentification.getInstance();
    	SententialInstanceGeneration sig = DefaultSententialInstanceGeneration.getInstance();
    	RelationMatching rm = NERRelationMatching.getInstance();
    	NegativeExampleCollection nec = NegativeExampleCollectionByRatio.getInstance(4.0);
    	
    	
    	//run distant supervision
    	DistantSupervision ds = new DistantSupervision(ai,sig,rm,nec);
    	ds.run(args[7], kb, c);
    	
    	
    	//run feature generation
    	FeatureGenerator fg = new DefaultFeatureGenerator();
    	FeatureGeneration featureGeneration = new FeatureGeneration(fg);
    	
    	
    	List<String> dsFiles = new ArrayList<>();
    	List<String> featureFiles = new ArrayList<>();
    	dsFiles.add(args[7]);
    	featureFiles.add(args[8]);
    	
    	
    	featureGeneration.run(dsFiles,featureFiles,c,cis);
    	
    	
    	//create multir model and mappings files
    	Preprocess.run(args[8], args[9], null);
    	
    	
    	//train multir model
    	Train.train(args[9]);
    	
    	
    	MultirRelationExtractor de = new MultirRelationExtractor(args[9], fg, ai, sig,cis);
    	de.batchExtract(args[9]);
	}
}
