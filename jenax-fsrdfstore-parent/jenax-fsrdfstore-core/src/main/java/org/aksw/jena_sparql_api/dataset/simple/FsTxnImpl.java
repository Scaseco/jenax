package org.aksw.jena_sparql_api.dataset.simple;

import java.nio.file.Path;



class TxnInfo {
	protected Path path;
	
}


/**
 * Transaction protocol implementation for atomic updates of the content
 * of a set of files
 * 
 * 
 * @author raven
 *
 */
public class FsTxnImpl {
	
	protected Path repoPath;         // The root
	protected Path dataPath;         // resolved against repoPath
	protected Path defaultGraphPath; // resolved against dataPath

	protected Path txnPath;          // resolved against repoPath
	
	protected String defaultGraphName = "DEFAULT";

	
	
	public void runRecovery() {
	
		
	}
	
	
	public void getStaleTxns() {
		
	}
	
	

}
