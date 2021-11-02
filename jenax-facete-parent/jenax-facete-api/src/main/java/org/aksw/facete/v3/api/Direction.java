package org.aksw.facete.v3.api;

/**
 * Booleans are confusing
 * 
 * NOTE We should also consistently use isForward == true;
 * the parts in the code use true to indicate backwards should be changed
 * 
 */
public enum Direction {
	FORWARD(true), BACKWARD(false);
	
	boolean isForward;
	
	Direction(boolean isForward) {
		this.isForward = isForward; 
	}
	
	public boolean isForward() {
		return isForward;
	}
	
	public boolean isBackward() {
		return !isForward;
	}
	
	public static Direction ofFwd(boolean isFwd) {
		Direction result = isFwd ? FORWARD : BACKWARD;
		return result;
	}
}
