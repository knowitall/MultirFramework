package edu.washington.multirframework.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import edu.washington.multirframework.corpus.CorpusInformationSpecification;

/**
 * This class handles most of the SQL formatting
 * @author jgilme1
 *
 */
public final class CorpusDatabase {
	
	private DerbyDb db;
	private static final String sentenceInformationTableName = "SENTENCETABLE";
	private static final String documentInformationTableName = "DOCUMENTTABLE";
	
	private static PreparedStatement bigSentenceQuery = null;
	
	private List<List<Object>> cachedSentenceValues;
	private List<List<Object>> cachedDocumentValues;
	
	private CorpusDatabase(DerbyDb db){
		this.db =db;
		cachedSentenceValues = new ArrayList<List<Object>>();
		cachedDocumentValues = new ArrayList<List<Object>>();
	}
	
	/**
	 * Loads Corpus Database from argument name, looks for a local file,
	 * if it doesnt find it, it tries a remote database
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static CorpusDatabase loadCorpusDatabase(String name) throws SQLException{
		File derbyDbFile = new File(name);
		if(derbyDbFile.exists() && derbyDbFile.isDirectory()){
			DerbyDb db = new DerbyDb(true,name);
			return new CorpusDatabase(db);
		}
		else{
			DerbyDb db = new DerbyDb(false,name);
			return new CorpusDatabase(db);
		}
	}
	
	/**
	 * Creates a new Derby DB representing the corpus, creates SENTENCETABLE
	 * and DOCUMENTTABLE and sets their indices
	 * @param name
	 * @param sentenceTableSQLSpecification
	 * @param documentTableSQLSpecification
	 * @return
	 * @throws SQLException
	 */
	public static CorpusDatabase newCorpusDatabase(String name, String sentenceTableSQLSpecification, String documentTableSQLSpecification) throws SQLException{
		DerbyDb db = new DerbyDb(true,name);
		deleteTable(db.connection,sentenceInformationTableName);
		deleteTable(db.connection,documentInformationTableName);
		createTable(db.connection,sentenceInformationTableName,sentenceTableSQLSpecification);
		createTable(db.connection,documentInformationTableName,documentTableSQLSpecification);
		//add Document index on sentence table
		db.connection.prepareStatement("CREATE INDEX DOCNAMEINDEX ON " + sentenceInformationTableName + " (DOCNAME)").execute();
		return new CorpusDatabase(db);
	}
	private static void deleteTable(Connection connection, String tableName) throws SQLException{
		try{
			connection.prepareStatement("DROP TABLE " + tableName).execute();
		}
		catch(SQLException e){
			return;
		}
	}
	private static void createTable(Connection connection, String tableName, String tableSpecification) throws SQLException{
		System.out.println(tableSpecification);
		connection.prepareStatement("CREATE TABLE " + tableName + " " + tableSpecification).execute();
	}
	
	public ResultSet getDocumentRows() throws SQLException {
		return issueQuery("SELECT * FROM " + documentInformationTableName);
	}
	
	public ResultSet getDocumentRows(String columnName, List<String> docNames) throws SQLException{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM " + documentInformationTableName + " WHERE ");
		for(String docName: docNames){
			query.append(columnName + "='");
			query.append(docName);
			query.append("' OR ");
		}
		return issueQuery(query.substring(0,query.length()-4).toString());
	}
	
	public ResultSet getSentenceRows(String columnName, List<String> values) throws SQLException{
		if(values.size() == 0){
			return null;
		}
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM " + sentenceInformationTableName + " WHERE ");
		for(String value: values){
			query.append(columnName + "='");
			query.append(value);
			query.append("'");
			query.append(" OR ");
		}
		String sqlCommand = query.substring(0, query.length()-4);
		return issueQuery(sqlCommand);
	}
	
	public ResultSet getSentenceRowByID(Integer id) throws SQLException{
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM " + sentenceInformationTableName + " WHERE ");
		query.append("SENTID" + "=");
		query.append(id);
		String sqlCommand = query.toString();
		return issueQuery(sqlCommand);
		
	}
	
	public ResultSet getSentenceRowsByID(List<Integer> ids) throws SQLException{
		if(ids.size() == 0){
			return null;
		}
		
		if(bigSentenceQuery == null){
			StringBuilder query = new StringBuilder();
			query.append("SELECT * FROM " + sentenceInformationTableName + " WHERE ");
			for(int i =0; i < 1000; i++){
				query.append("SENTID=? OR ");
			}
			query.setLength(query.length()-4);
			bigSentenceQuery = db.connection.prepareStatement(query.toString());
		}
				
		try{
			bigSentenceQuery.clearParameters();
			int j =1;
			String firstId = String.valueOf(ids.get(0));
			for(Integer id: ids){
				bigSentenceQuery.setString(j, String.valueOf(id));
				j++;
			}
			while(j <= 1000){
				bigSentenceQuery.setString(j, firstId);
				j++;
			}
			return bigSentenceQuery.executeQuery();
		}
		
		catch(Exception e){
			return null;
		}


	}
	
	public ResultSet issueQuery(String queryString) throws SQLException{
		return db.connection.prepareStatement(queryString).executeQuery();
	}
	
	private void insert(String tableName, List<String> columnNames, List<List<Object>> columnValues) throws SQLException{
		StringBuilder command = new StringBuilder();
		command.append("INSERT INTO " + tableName + " (");
		for(String columnName: columnNames){
			command.append(columnName + ",");
		}
		command.deleteCharAt(command.length()-1);
		command.append(") VALUES ");
	    for(List<Object> values: columnValues){
	    	command.append(" (");
	    	for(Object value : values){
		    	if(value instanceof String){
		    		command.append("'");
		    		command.append(value.toString().replaceAll("'", "''"));
		    		command.append("'");
		    	}
		    	else{
		    		command.append(value.toString());
		    	}
		    	command.append(",");
	    	}
	    	command.deleteCharAt(command.length()-1);
	    	command.append("),");
	    }
	    command.deleteCharAt(command.length()-1);
		try{
		 db.connection.prepareStatement(command.toString()).execute();
		}
		catch(SQLException e){
			System.err.println(command.toString());
			throw e;
		}
	}
	
	private void bulkInsert(String tableName, List<String> columnNames, List<Object> values) throws SQLException{
		List<List<Object>> columnValues = tableName.equals(sentenceInformationTableName) ? cachedSentenceValues : cachedDocumentValues;
		if(columnValues.size() < 2000){
			columnValues.add(values);
		}
		else{
			//do insert
			columnValues.add(values);
			insert(tableName,columnNames,columnValues);
			columnValues.clear();
		}
	}
	
	public void bulkInsertToSentenceTable(List<String> columnNames, List<Object> values) throws SQLException{
		bulkInsert(sentenceInformationTableName,columnNames,values);
	}
	public void bulkInsertToDocumentTable(List<String> columnNames, List<Object> values) throws SQLException{
		bulkInsert(documentInformationTableName,columnNames,values);
	}
	
	public void insertToSentenceTable(List<String> columnNames, List<Object> values) throws SQLException{
		  List<List<Object>> columnValues = new ArrayList<List<Object>>();
		  columnValues.add(values);
		  insert(sentenceInformationTableName,columnNames,columnValues);
	}
	public void insertToDocumentTable(List<String> columnNames, List<Object> values) throws SQLException{
		  List<List<Object>> columnValues = new ArrayList<List<Object>>();
		  columnValues.add(values);
		  insert(documentInformationTableName,columnNames,columnValues);
	}
	
	public List<List<String>> issueQuery(String queryStatement, List<Integer> columnIds) throws SQLException{
		ResultSet results = db.connection.prepareStatement(queryStatement).executeQuery();
		List<List<String>> resultList = new ArrayList<List<String>>();
		while(results.next()){
			List<String> information = new ArrayList<String>();
			for(Integer i : columnIds){
				information.add(results.getString(i));
			}
			resultList.add(information);
		}
		return resultList;
	}
	public void turnOffAutoCommit() throws SQLException {
		db.connection.setAutoCommit(false);
	}
	public void turnOnAutoCommit() throws SQLException{
		db.connection.setAutoCommit(true);
	}
	
	private void batchTableLoad(String tableName,CorpusInformationSpecification ci, File dbFile ) throws SQLException{
		PreparedStatement loadTable = db.connection.prepareStatement("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(?,?,?,?,?,?,?)");
		loadTable.setString(1, null);
		loadTable.setString(2,tableName);
		loadTable.setString(3,dbFile.getPath());
		loadTable.setString(4,"_");
		loadTable.setString(5,"%");
		loadTable.setString(6,null);
		loadTable.setInt(7, 0);
		loadTable.execute();	
	}
	public void batchSentenceTableLoad(CorpusInformationSpecification ci, File dbSentencesFile) throws SQLException {
		batchTableLoad(sentenceInformationTableName,ci,dbSentencesFile);
	}
	public void batchDocumentTableLoad(CorpusInformationSpecification ci,
			File dbdocumentsFile) throws SQLException {
		batchTableLoad(documentInformationTableName,ci,dbdocumentsFile);
	}
	
}
