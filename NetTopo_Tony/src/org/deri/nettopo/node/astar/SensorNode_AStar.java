package org.deri.nettopo.node.astar;



public class SensorNode_AStar {
	
	/**
	 * 
	 */
	private Integer ID;
	private SensorNode_AStar parentNode;// ����ڵ�
	private double g;// ��ǰ�㵽�����ƶ��ķ�
	private double h;// ��ǰ�㵽�յ���ƶ��ķѣ�(�����ϰ���)
	private double f;// f=g+h
	public SensorNode_AStar(){
		super();
	}
	
	
	
	public SensorNode_AStar getParentNode() {
		return parentNode;
	}
	public void setParentNode(SensorNode_AStar parentNode) {
		this.parentNode = parentNode;
	}
	public double getG() {
		return g;
	}
	public void setG(double g) {
		this.g = g;
	}
	public double getH() {
		return h;
	}
	public void setH(double h) {
		this.h = h;
	}
	public double getF() {
		return f;
	}
	public void setF(double f) {
		this.f = f;
	}



	public Integer getID() {
		return ID;
	}



	public void setID(Integer iD) {
		ID = iD;
	}
	
	
	
	
}
