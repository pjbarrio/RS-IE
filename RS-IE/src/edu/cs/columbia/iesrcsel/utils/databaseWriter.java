package edu.cs.columbia.iesrcsel.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;


public class databaseWriter {

	public static final String DATABASE_USER = "user";
	public static final String DATABASE_PASSWORD = "password";
	public static final String MYSQL_AUTO_RECONNECT = "autoReconnect";
	public static final String MYSQL_MAX_RECONNECTS = "maxReconnects";

	private String reportExperimentStatus = "UPDATE `AutomaticQueryGeneration`.`ExperimentStatus` SET `status` = ?, computerName = ? WHERE `idDatabase` = ? and `idExperiment` = ?";
	private String isExperimentAvailableString = "INSERT INTO `AutomaticQueryGeneration`.`ExperimentStatus`(`idDatabase`,`idExperiment`,`computerName`)VALUES(?,?,?);";
	private static final String getStoredFileContentString = "SELECT `EstimationParameterFile`.`content` FROM `AutomaticQueryGeneration`.`EstimationParameterFile` where `EstimationParameterFile`.`idEstimationParameterFile` = ?";
	private static final String storeFileContentString = "INSERT INTO `AutomaticQueryGeneration`.`EstimationParameterFile` (`idEstimationParameterFile`, `content`) VALUES (?,?);";

	
	public static final int INCONSISTENT_PARAMETERS_FILE = -1;
	public static final int FIRST_TIME = 0;
	public static final int NOT_RUNNABLE = -2;
	
	public static final int FINISHED_VALUE = -3;

	private Connection conn;
	private String computername;
	private String getRelationExtractionSystemIdString = "Select idRelationExtractionSystem from RelationExtractionSystem where idRelationConfiguration = ? and idInformationExtractionSystem = ?";

	private String isFirstTimeRunningExperimentString = "INSERT INTO `AutomaticQueryGeneration`.`EstimationExperiment` (`idWebsite`, `parameterFile`, `runningOn`, `outputFile`, `outputSummaryFile`) VALUES (?,?,?,?,?);";
	private String checkIfEstimationCanContinueString = "UPDATE `AutomaticQueryGeneration`.`EstimationExperiment` SET `runningOn` = ? WHERE `idWebsite` = ? AND `parameterFile` = ? AND `runningOn` = 'none' AND `status` != -3 AND `outputFile` = ? AND `outputSummaryFile` = ?;";
	private String nextEvaluationString = "SELECT `EstimationExperiment`.`status` FROM `AutomaticQueryGeneration`.`EstimationExperiment` WHERE `EstimationExperiment`.`idWebsite` = ? AND `EstimationExperiment`.`parameterFile` = ? AND `EstimationExperiment`.`runningOn` = ? AND `outputFile` = ? AND `EstimationExperiment`.`outputSummaryFile` = ?;";
	private String updateEstimationStatusString = "UPDATE `AutomaticQueryGeneration`.`EstimationExperiment` SET `status` = ? WHERE `idWebsite` = ? AND `parameterFile` = ? AND `runningOn` = ? AND `outputFile` = ? AND `outputSummaryFile` = ?;";
	private String setCurrentLogSplitString = "UPDATE `AutomaticQueryGeneration`.`EstimationExperiment` SET `currentLogSplit` = ? WHERE `idWebsite` = ? AND `parameterFile` = ? AND `runningOn` = ? AND `outputFile` = ? AND `outputSummaryFile` = ?;";
	private String getCurrentLogSplitString = "SELECT `EstimationExperiment`.`currentLogSplit` FROM `AutomaticQueryGeneration`.`EstimationExperiment` WHERE `EstimationExperiment`.`idWebsite` = ? AND `EstimationExperiment`.`parameterFile` = ? AND `EstimationExperiment`.`runningOn` = ? AND `outputFile` = ? AND `EstimationExperiment`.`outputSummaryFile` = ?;";
	private String getCurrentSplitForFileString = "SELECT `EstimationOutputfileSplit`.`split` FROM `AutomaticQueryGeneration`.`EstimationOutputfileSplit` where `EstimationOutputfileSplit`.`outputFileName` = ? ;";
	private String existsSplitForFileString = "INSERT INTO `AutomaticQueryGeneration`.`EstimationOutputfileSplit` (`outputFileName`,`split`) VALUES (?,?);";

	public databaseWriter() {

		conn = null;

	}

	public synchronized void closeConnection() {
		try {
			
			getConnection().close();
			System.out.println("Disconnected from database");
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private synchronized Connection getConnection() {
		if (conn == null){
			openConnection();
		}

		return conn;
	}


	public synchronized  void openConnection() {

		conn = null;
		String url = "jdbc:mysql://db-files.cs.columbia.edu:3306/";
		String dbName = "AutomaticQueryGeneration";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "pjbarrio"; 
		String password = "test456";
		try {

			Class.forName(driver).newInstance();

			java.util.Properties connProperties = new java.util.Properties();

			connProperties.put(DATABASE_USER, userName);

			connProperties.put(DATABASE_PASSWORD, password);

			connProperties.put(MYSQL_AUTO_RECONNECT, "true");

			connProperties.put(MYSQL_MAX_RECONNECTS, "500");

			conn = DriverManager.getConnection(url+dbName,connProperties);

			System.out.println("Connected to the database");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isExperimentAvailable(int idExperiment, int idDatabase, String computerName) {

		try {

			//			if (PStmtisExperimentAvailable == null){
			//				PStmtisExperimentAvailable = getConnection().prepareStatement(isExperimentAvailableString);
			//			}else
			//				PStmtisExperimentAvailable.clearParameters();

			PreparedStatement PStmtisExperimentAvailable = getConnection().prepareStatement(isExperimentAvailableString);

			PStmtisExperimentAvailable.setInt(1, idDatabase);
			PStmtisExperimentAvailable.setInt(2, idExperiment);
			PStmtisExperimentAvailable.setString(3, computerName);

			PStmtisExperimentAvailable.execute();

			PStmtisExperimentAvailable.close();

		} catch (SQLException e) {
			return false; //duplicated key, someone has already written it.
		}

		return true;

	}
	
	public void reportExperimentStatus(int idExperiment, int idDatabase,String computerName,
			int status) {

		try {

			//			if (PStmtreportExperimentStatus == null){
			//				PStmtreportExperimentStatus = getConnection().prepareStatement(reportExperimentStatus);
			//			}else
			//				PStmtreportExperimentStatus.clearParameters();

			PreparedStatement PStmtreportExperimentStatus = getConnection().prepareStatement(reportExperimentStatus);

			PStmtreportExperimentStatus.setInt(1, status);
			PStmtreportExperimentStatus.setString(2,computerName);
			PStmtreportExperimentStatus.setInt(3, idDatabase);
			PStmtreportExperimentStatus.setInt(4, idExperiment);

			PStmtreportExperimentStatus.execute();

			PStmtreportExperimentStatus.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public int isRunnable(String idDatabase, String parametersFile, String outDetailed,
			String outSummary) {
		
		//First I need to check that the file is consistent and add it if does not exist.
		
		String fileContent = getStoredFileContent(parametersFile);
		
		boolean validExperiment = true;
		
		try{
			
			if (fileContent == null){
				if (!parametersFile.equals("default") && !parametersFile.equals("empty"))
					storeFileContent(parametersFile,FileUtils.readFileToString(new File(parametersFile)));
			} else {
				validExperiment = fileContent.equals(FileUtils.readFileToString(new File(parametersFile)));
			}
			
			if (!validExperiment){
				return INCONSISTENT_PARAMETERS_FILE; //Add if not added. Report 'inconsistency' if incorrect.
			}else{
				
				//Check that the execution can be made by inserting
				
				if (isFirstTimeRunningExperiment(idDatabase,parametersFile,getComputerName(), outDetailed,outSummary)){
					
					return FIRST_TIME;
					
				}else{
					
					//If it's busy (i.e., runningOn!='none') return 'busy', otherwise, return what is the first estimation that can be made.

					boolean canContinue = checkIfEstimationCanContinue(idDatabase,parametersFile,getComputerName(),outDetailed,outSummary);

					//see if I can continue running...

					if (canContinue){
						return nextEvaluation(idDatabase,parametersFile,outDetailed,outSummary);
					}else{
						return NOT_RUNNABLE;
					}
					
				}
				
			}
			
			
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return NOT_RUNNABLE;
	}
	
	private int nextEvaluation(String idDatabase, String parametersFile,
			String outDetailed, String outSummary) {
		
		int ret = FIRST_TIME;

		try {
			
			PreparedStatement PStmtnextEvaluation = getConnection().prepareStatement(nextEvaluationString );

			PStmtnextEvaluation.setString(1, idDatabase);
			PStmtnextEvaluation.setString(2, parametersFile);
			PStmtnextEvaluation.setString(3, getComputerName());
			PStmtnextEvaluation.setString(4, outDetailed);
			PStmtnextEvaluation.setString(5, outSummary);
			
			ResultSet RSnextEvaluation = PStmtnextEvaluation.executeQuery();

			while (RSnextEvaluation.next()) {

				ret = RSnextEvaluation.getInt(1);

			}

			RSnextEvaluation.close();

			PStmtnextEvaluation.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}

	private void storeFileContent(String parametersFile, String content) {
		
		try {

			PreparedStatement PStmtStoreFileContent = getConnection().prepareStatement(storeFileContentString );

			PStmtStoreFileContent.setString(1, parametersFile);
			PStmtStoreFileContent.setString(2, content);
			
			PStmtStoreFileContent.execute();

			PStmtStoreFileContent.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private String getStoredFileContent(String parametersFile) {
		
		String ret = null;

		try {
			
			PreparedStatement PStmtgetStoredFileContent = getConnection().prepareStatement(getStoredFileContentString);

			PStmtgetStoredFileContent.setString(1, parametersFile);

			ResultSet RSgetStoredFileContent = PStmtgetStoredFileContent.executeQuery();

			while (RSgetStoredFileContent.next()) {

				ret = RSgetStoredFileContent.getString(1);

			}

			RSgetStoredFileContent.close();

			PStmtgetStoredFileContent.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}

	private boolean checkIfEstimationCanContinue(String idDatabase,
			String parametersFile, String computerName, String outDetailed,
			String outSummary) {
		
		try {
			
			PreparedStatement PStmtcheckIfEstimationCanContinue = getConnection().prepareStatement(checkIfEstimationCanContinueString);

			PStmtcheckIfEstimationCanContinue.setString(1, computerName);
			PStmtcheckIfEstimationCanContinue.setString(2, idDatabase);
			PStmtcheckIfEstimationCanContinue.setString(3, parametersFile);
			PStmtcheckIfEstimationCanContinue.setString(4, outDetailed);
			PStmtcheckIfEstimationCanContinue.setString(5, outSummary);
			
			int matchedRecords = PStmtcheckIfEstimationCanContinue.executeUpdate();

			PStmtcheckIfEstimationCanContinue.close();

			return (matchedRecords >= 1);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;		
		
	}

	private boolean isFirstTimeRunningExperiment(String idDatabase,
			String parametersFile, String computerName, String outDetailed, String outSummary) {
		
		try {

			PreparedStatement PStmtisFirstTimeRunningExperiment = getConnection().prepareStatement(isFirstTimeRunningExperimentString );

			PStmtisFirstTimeRunningExperiment.setString(1, idDatabase);
			PStmtisFirstTimeRunningExperiment.setString(2, parametersFile);
			PStmtisFirstTimeRunningExperiment.setString(3, computerName);
			PStmtisFirstTimeRunningExperiment.setString(4, outDetailed);
			PStmtisFirstTimeRunningExperiment.setString(5, outSummary);
			
			PStmtisFirstTimeRunningExperiment.execute();

			PStmtisFirstTimeRunningExperiment.close();

		} catch (SQLException e) {
			return false; //duplicated key, someone has already written it.
		}

		return true;		
		
	}

	public String getComputerName() {

		if (computername == null){
		
			try{
				computername = InetAddress.getLocalHost().getHostName();
				System.out.println(computername);
			}catch (Exception e){
				System.out.println("Exception caught ="+e.getMessage());
			}
		}
		return computername;
	}

	public void updateEstimationStatus(String idDatabase, String parametersFile,
			String outDetailed, String outSummary, int status) {
		
		try {
			
			PreparedStatement PStmtupdateEstimationStatus = getConnection().prepareStatement(updateEstimationStatusString);

			PStmtupdateEstimationStatus.setInt(1, status);
			PStmtupdateEstimationStatus.setString(2, idDatabase);
			PStmtupdateEstimationStatus.setString(3, parametersFile);
			PStmtupdateEstimationStatus.setString(4, getComputerName());
			PStmtupdateEstimationStatus.setString(5, outDetailed);
			PStmtupdateEstimationStatus.setString(6, outSummary);
			
			PStmtupdateEstimationStatus.executeUpdate();

			PStmtupdateEstimationStatus.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

//	public int getCurrentLogSplit(String idDatabase, String parametersFile, String outDetailed,
//			String outSummary) {
//		
//		int ret = -1;
//
//		try {
//			
//			PreparedStatement PStmtgetCurrentLogSplit = getConnection().prepareStatement(getCurrentLogSplitString);
//
//			PStmtgetCurrentLogSplit.setString(1, idDatabase);
//			PStmtgetCurrentLogSplit.setString(2, parametersFile);
//			PStmtgetCurrentLogSplit.setString(3, getComputerName());
//			PStmtgetCurrentLogSplit.setString(4, outDetailed);
//			PStmtgetCurrentLogSplit.setString(5, outSummary);
//						
//			ResultSet RSgetCurrentLogSplit = PStmtgetCurrentLogSplit.executeQuery();
//
//			while (RSgetCurrentLogSplit.next()) {
//
//				ret = RSgetCurrentLogSplit.getInt(1);
//
//			}
//
//			RSgetCurrentLogSplit.close();
//
//			PStmtgetCurrentLogSplit.close();
//			
//			
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		return ret;
//		
//	}
//
//	public void setCurrentLogSplit(String idDatabase, String parametersFile, String outDetailed,
//			String outSummary, int currentLogSplit) {
//		
//		try {
//			
//			PreparedStatement PStmtsetCurrentLogSplit = getConnection().prepareStatement(setCurrentLogSplitString);
//
//			PStmtsetCurrentLogSplit.setInt(1, currentLogSplit);
//			PStmtsetCurrentLogSplit.setString(2, idDatabase);
//			PStmtsetCurrentLogSplit.setString(3, parametersFile);
//			PStmtsetCurrentLogSplit.setString(4, getComputerName());
//			PStmtsetCurrentLogSplit.setString(5, outDetailed);
//			PStmtsetCurrentLogSplit.setString(6, outSummary);
//			
//			PStmtsetCurrentLogSplit.executeUpdate();
//
//			PStmtsetCurrentLogSplit.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//	}

	public int getCurrentSplitForFile(String fileName) {
		
		int ret = -1;

		try {
			
			PreparedStatement PStmtgetCurrentLogSplit = getConnection().prepareStatement(getCurrentSplitForFileString );

			PStmtgetCurrentLogSplit.setString(1, fileName);
						
			ResultSet RSgetCurrentLogSplit = PStmtgetCurrentLogSplit.executeQuery();

			while (RSgetCurrentLogSplit.next()) {

				ret = RSgetCurrentLogSplit.getInt(1);

			}

			RSgetCurrentLogSplit.close();

			PStmtgetCurrentLogSplit.close();
			
			boolean isadded = false;
			
			while (!isadded){
				ret++;
				isadded = existsSplitForFile(fileName,ret);
			}
			
			return ret;
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}

	private boolean existsSplitForFile(String fileName, int split) {
		
		try {

			PreparedStatement PStmtexistsSplitForFile = getConnection().prepareStatement(existsSplitForFileString );

			PStmtexistsSplitForFile.setString(1, fileName);
			PStmtexistsSplitForFile.setInt(2, split);
			
			PStmtexistsSplitForFile.execute();

			PStmtexistsSplitForFile.close();

		} catch (SQLException e) {
			return false; //duplicated key, someone has already written it.
		}

		return true;		
		
	}
	
	public String getInformationExtractionSystemName(
			int idRelationExtractionSystem) {

		String ret = "";

		try {

			Statement StmtgetInformationExtractionSystemName = getConnection().createStatement();

			ResultSet RSgetInformationExtractionSystemName = StmtgetInformationExtractionSystemName.executeQuery
					("select name from RelationExtractionSystem where idRelationExtractionSystem = " + idRelationExtractionSystem);

			while (RSgetInformationExtractionSystemName.next()) {

				ret = RSgetInformationExtractionSystemName.getString(1);

			}

			RSgetInformationExtractionSystemName.close();
			StmtgetInformationExtractionSystemName.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	public int getRelationExtractionSystemId(int relationConf,
			int informationExtractionId) {

		int ret = -1;

		try {

			//			if (PStmtgetRelationExtractionSystemId == null){
			//				PStmtgetRelationExtractionSystemId = getConnection().prepareStatement(getRelationExtractionSystemIdString);
			//			}else
			//				PStmtgetRelationExtractionSystemId.clearParameters();

			PreparedStatement PStmtgetRelationExtractionSystemId = getConnection().prepareStatement(getRelationExtractionSystemIdString);

			PStmtgetRelationExtractionSystemId.setInt(1, relationConf);
			PStmtgetRelationExtractionSystemId.setInt(2, informationExtractionId);

			ResultSet RSgetRelationExtractionSystemId = PStmtgetRelationExtractionSystemId.executeQuery();

			while (RSgetRelationExtractionSystemId.next()) {

				ret= RSgetRelationExtractionSystemId.getInt(1);

			}

			RSgetRelationExtractionSystemId.close();
			PStmtgetRelationExtractionSystemId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}
	
	public String getUsefulDocumentsForCollection(String collection,
			String relation, String relationExtractor) {
		String ret = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/collectionList/" + collection + "/" + relation + "/"+relationExtractor+"-useful.extraction";
		return ret;
	}
	
	public String getUselessDocumentsForCollection(String collection,
			String relation, String relationExtractor) {
		String ret =  "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/collectionList/" + collection  + "/" + relation + "/"+relationExtractor+"-useless.extraction";
		return ret;
	}
	
}
