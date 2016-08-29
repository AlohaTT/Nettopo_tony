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
	public int source; // 标志sensor是否只有一个neighbor
	public int destination; // 标志sensor是否可以进入睡眠状态
	public int type;
	public int numberOfNeighbor;
	public int flag;
	public int state;
	public int priority;
	public int nextHopId;

	public PacketHeader() {
	}

}
