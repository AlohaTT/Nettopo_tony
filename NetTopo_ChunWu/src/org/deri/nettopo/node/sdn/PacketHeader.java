/**
 * 
 */
package org.deri.nettopo.node.sdn;

import java.util.HashMap;

/**
 * @author Tony
 *
 */
public class PacketHeader {

	/**
	 * 
	 */
	private int source; // 标志sensor是否只有一个neighbor
	private int destination; // 标志sensor是否可以进入睡眠状态
	private int type;
	private int numberOfNeighbor;
	private int flag;
	private int state;
	private int priority;
	private int nextHopId;

	/**
	 * @return the source
	 */
	public int getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(int source) {
		this.source = source;
	}

	/**
	 * @return the destination
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(int destination) {
		this.destination = destination;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the numberOfNeighbor
	 */
	public int getNumberOfNeighbor() {
		return numberOfNeighbor;
	}

	/**
	 * @param numberOfNeighbor the numberOfNeighbor to set
	 */
	public void setNumberOfNeighbor(int numberOfNeighbor) {
		this.numberOfNeighbor = numberOfNeighbor;
	}

	/**
	 * @return the flag
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the nextHopId
	 */
	public int getNextHopId() {
		return nextHopId;
	}

	/**
	 * @param nextHopId the nextHopId to set
	 */
	public void setNextHopId(int nextHopId) {
		this.nextHopId = nextHopId;
	}

	public PacketHeader() {
	}

}
