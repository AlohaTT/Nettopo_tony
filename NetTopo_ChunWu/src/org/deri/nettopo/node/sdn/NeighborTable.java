/**
 * 
 */
package org.deri.nettopo.node.sdn;

/**
 * @author tony
 *
 */
public class NeighborTable {

	/**
	 * 
	 */
	private int id;
	private double rank;
	private boolean state;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the rank
	 */
	public double getRank() {
		return rank;
	}
	/**
	 * @param rank the rank to set
	 */
	public void setRank(double rank) {
		this.rank = rank;
	}
	/**
	 * @return the state
	 */
	public boolean isState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(boolean state) {
		this.state = state;
	}
	public NeighborTable() {
		// TODO Auto-generated constructor stub
	}

}
