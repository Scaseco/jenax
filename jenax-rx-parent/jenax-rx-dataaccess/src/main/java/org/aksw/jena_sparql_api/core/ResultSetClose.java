package org.aksw.jena_sparql_api.core;

import java.io.Closeable;
import java.io.IOException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:54 PM
 *
 * Unlike JCDB, Jena does not provide the close method on the ResultSet, but on the object that
 * created the result set. Therefore:
 *
 *
 */
//public class ResultSetClose
//        extends ResultSetDecorator
//        implements Closeable
//{
//    private static final Logger logger = LoggerFactory.getLogger(ResultSetClose.class);
//
//    private boolean isClosed = false;
//    private boolean closeOnException = true;
//
//
//    public ResultSetClose(ResultSet decoratee, boolean skipCheckClose) {
//        super(decoratee);
//
//        if(!skipCheckClose) {
//            checkClose();
//        }
//    }
//
//
//    public ResultSetClose(ResultSet decoratee, boolean isClosed, boolean skipCheckClose) {
//        super(decoratee);
//        this.isClosed = isClosed;
//
//        if(!skipCheckClose) {
//            checkClose();
//        }
//    }
//
//    public ResultSetClose(ResultSet decoratee, boolean isClosed, boolean closeOnException, boolean skipCheckClose) {
//        super(decoratee);
//        this.isClosed = isClosed;
//        this.closeOnException = closeOnException;
//
//        if(!skipCheckClose) {
//            checkClose();
//        }
//    }
//
//
//    /*
//    public ResultSetClose(ResultSet decoratee, IClosable closable) {
//        super(decoratee);
//        //this.closable = closable;
//        checkClose();
//    }
//    */
//
//
//    protected boolean checkClose() {
//        if(!isClosed) {
//            boolean hasNext;
//
//            // If an exception occurs, still try to close first
//            Exception tmp = null;
//            try {
//                hasNext = decoratee.hasNext();
//            } catch(Exception e) {
//                hasNext = false;
//                tmp = e;
//            }
//
//            if(!hasNext) {
//                try {
//                    isClosed = true ;
//                    close();
//                }
//                catch(Exception e) {
//                    logger.error("Error closing an object supposedly underlying a Jena ResultSet", e);
//                }
//            }
//
//            if(tmp != null) {
//                throw new RuntimeException(tmp);
//            }
//        }
//        return isClosed;
//    }
//
//    @Override
//    public boolean hasNext() {
//        return !checkClose();
//    }
//
//    @Override
//    public void remove() {
//        decoratee.remove();
//        checkClose();
//    }
//
//
//    @Override
//    public QuerySolution nextSolution() {
//        try {
//            QuerySolution result = decoratee.nextSolution();
//            checkClose();
//            return result;
//        } catch(Exception e) {
//            if(closeOnException) {
//                try {
//                    close();
//                } catch(Exception f) {
//                    throw new RuntimeException(f);
//                }
//            }
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public Binding nextBinding() {
//        try {
//            Binding result = decoratee.nextBinding();
//            checkClose();
//            return result;
//        } catch(Exception e) {
//            if(closeOnException) {
//                try {
//                    close();
//                } catch(Exception f) {
//                    throw new RuntimeException(f);
//                }
//            }
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//        if(!isClosed) {
//            if(decoratee instanceof Closeable) {
//                Closeable closeable = (Closeable)decoratee;
//                closeable.close();
//                isClosed = true;
//            }
//        }
//    }
//}

