package org.aksw.jena_sparql_api.dataset.simple;

import java.nio.ByteBuffer;

import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionalComponentLifecycle;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.query.ReadWrite;

class State {
	String symlinkFile;
	String targetPath;
}



public class SymbolicLinkIndexer
	extends TransactionalComponentLifecycle<State>
{

	protected SymbolicLinkIndexer(ComponentId componentId) {
		super(componentId);
	}

	@Override
	public void cleanStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startRecovery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recover(ByteBuffer ref) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishRecovery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected State _begin(ReadWrite readWrite, TxnId txnId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected State _promote(TxnId txnId, State oldState) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ByteBuffer _commitPrepare(TxnId txnId, State state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void _commit(TxnId txnId, State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _commitEnd(TxnId txnId, State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _abort(TxnId txnId, State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _complete(TxnId txnId, State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _shutdown() {
		// TODO Auto-generated method stub
		
	}	
}
