package org.aksw.commons.allocation.api;

/**
 * An allocatable resource. Useful for implementing retry on actions
 * that require certain resources to be allocated which however may get lost due to
 * concurrent modifications.
 *
 * An example use case is to wrap directory creation / removal:
 * (.) Allocating the resource creates the folder
 * (.) Deallocation may remove the folder if it is empty
 * (.) Whether the folder is allocated can be checked with isAllocated()
 * (.) Allocation may fail if e.g. there exists a file with the same path as the folder to be allocated
 *
 *
 * @author raven
 *
 */
public interface Allocatable {
    void allocate() throws Exception;
    boolean isAllocated() throws Exception;
    void deallocate() throws Exception;
}
