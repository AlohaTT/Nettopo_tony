package org.deri.nettopo.network;

import java.io.*;
import java.util.*;

import org.deri.nettopo.util.Util;
import org.deri.nettopo.node.*;
import org.deri.nettopo.util.*;
import org.eclipse.swt.graphics.RGB;

public class WirelessSensorNetwork implements Serializable {
	
	/** the id of next node to be added */
	private static int currentID = 1;
	
	/** 
	 * the key is the id (Integer) of each node, 
	 * the value is the coordinate (Coordinate) of that id
	 */
	private HashMap<Integer,Coordinate> coordinates = null;
	
	/**
	 * the key stores the id (Integer) of each node, 
	 * the value is the node(VNode) with that id
	 */
	private HashMap<Integer,VNode> allNodes = null;
	
	/** store the length, width and height of the network space */
	private Coordinate size; 

	public static void setCurrentID(int currentID) {
		WirelessSensorNetwork.currentID = currentID;
	}
	
	public int getNodeRange(){
		return currentID;
	}
	
	public WirelessSensorNetwork(){
		coordinates = new HashMap<Integer,Coordinate>();
		allNodes = new HashMap<Integer,VNode>();
		size = new Coordinate();
	}
	
	public Coordinate getSize(){
		synchronized (this.size){
			return size;
		}
	}
	
	public void setSize(Coordinate size){
		synchronized (this.size){
			this.size = size;
		}
	}
	
	/**
	 * c.equals(coordinate)
	 * @param c
	 * @return
	 */
	public boolean hasDuplicateCoordinate(Coordinate c){
		synchronized (this.coordinates){
			Iterator<Coordinate> it = coordinates.values().iterator();
			while(it.hasNext()){
				Coordinate coordinate = (Coordinate)it.next();
				if(c.equals(coordinate)){
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * c!=coordinate && c.equals(coordinate)
	 * @param c
	 * @return
	 */
	public boolean duplicateWithOthers(Coordinate c){
		synchronized (this.coordinates){
			Iterator<Coordinate> it = coordinates.values().iterator();
			while(it.hasNext()){
				Coordinate coordinate = (Coordinate)it.next();
				if(c!=coordinate && c.equals(coordinate)){
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * add the node with coordinate of c to the allNodes and coordinates 
	 * @param node 
	 * @param c the coordinate of the node
	 * @throws DuplicateCoordinateException
	 */
	public void addNode(VNode node, Coordinate c)throws DuplicateCoordinateException{
		if(hasDuplicateCoordinate(c))
			throw new DuplicateCoordinateException("Duplicate Coordinate");
		else{
			node.setID(currentID++);
			Integer ID = Integer.valueOf(node.getID());
			synchronized (this.allNodes){
				allNodes.put(ID, node);
			}
			synchronized (this.coordinates){
				coordinates.put(ID, c);
			}
		}
	}
	
	/**
	 * delete the node by its id and return the coordinate of the deleted node. 
	 * @param id: id of a node
	 * @return deleted node associate with id, or null if there was no mapping for id
	 */
	public Coordinate deleteNodeByID(int id){
		Integer ID = Integer.valueOf(id);
		synchronized (this.allNodes){
			allNodes.remove(ID);
		}
		synchronized (this.coordinates){
			return (Coordinate)coordinates.remove(ID);
		}
	}
	
	/**
	 * node with id will be reset the coordinate to the given coordinate  
	 * @param id node id
	 * @param coordinate given coordinate
	 * @return true if reset successfully
	 */
	public boolean resetNodeCoordinateByID(int id, Coordinate coordinate){
		boolean result = false;
		Integer ID = Integer.valueOf(id);
		try{
			if(coordinates.containsKey(ID)){
				coordinates.remove(ID);
				coordinates.put(ID, coordinate);
				result = true;
			}
		}catch(Exception e){
			result = false;
		}
		return result;
	} 
	
	/**
	 * the color of the node with id will be reset to color 
	 * @param id given id
	 * @param color given color
	 * @return true if reset successfully
	 */
	public boolean resetNodeColorByID(int id, RGB color){
		boolean result = false;
		Integer ID = Integer.valueOf(id);
		if(allNodes.containsKey(ID)){
			VNode node = allNodes.get(ID);
			node.setColor(color);
			allNodes.put(ID, node);
			result = true;
		}
		return result;
	} 
	
	/**
	 * In order to use mobility for more than one time,
	 * to reset the mobile node color to the original color.
	 */
	public void resetAllNodesColor() {
		Iterator<VNode> iter = allNodes.values().iterator();
		while(iter.hasNext()){
			VNode node = iter.next();
			int nodeID = node.getID();
			String name = node.getClass().getSimpleName();
			if(name.contains("Sensor")){
				resetNodeColorByID(nodeID, NodeConfiguration.SensorNodeColorRGB);
			}else if(name.contains("Source")){
				resetNodeColorByID(nodeID, NodeConfiguration.SourceNodeColorRGB);
			}else if(name.contains("Host")){
				resetNodeColorByID(nodeID, NodeConfiguration.HostNodeColorRGB);
			}else if(name.contains("Sink")){
				resetNodeColorByID(nodeID, NodeConfiguration.SinkNodeColorRGB);
			}
		}
	}
	
	/**
	 * to reset all sensor nodes active to help the CKN algorithm
	 * use the sensor nodes for more than one time
	 */
	public void resetAllSensorNodesActive(){
		Iterator<VNode> iter = allNodes.values().iterator();
		while(iter.hasNext()){
			VNode node = iter.next();
			if(node.getClass().getSimpleName().contains("Sensor")){
				((SensorNode)node).setActive(true);
			}
		}
	}
	
	/** return the active sensor nodes array */
	public int[] getSensorActiveNodes(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		Iterator<VNode> iter = allNodes.values().iterator();
		while(iter.hasNext()){
			VNode node = iter.next();
			if(node.getClass().getSimpleName().contains("Sensor"))
				if(((SensorNode)node).isActive())
					list.add(node.getID());
		}
		
		return Util.IntegerArray2IntArray(list.toArray(new Integer[list.size()]));
	}
	
	/** return the active sonsor node num in the wsn*/ 
	public int getSensorNodesActiveNum(){
		return getSensorActiveNodes().length;
	} 
	
	/**
	 * to reset all nodes available to help the algorithm use the nodes for more than one time
	 */
	public void resetAllNodesAvailable(){
		Iterator<VNode> iter = allNodes.values().iterator();
		while(iter.hasNext()){
			VNode node = iter.next();
			node.setAvailable(true);
		}
	}
	
	/**
	 * delete all the node information in the wsn
	 */
	public void deleteAllNodes(){
		synchronized (this.allNodes){
			allNodes = new HashMap<Integer,VNode>();
		}
		synchronized (this.coordinates){
			coordinates = new HashMap<Integer,Coordinate>();
		}
	}
	
	public VNode getNodeByID(int id){
		synchronized (this.allNodes){
			return (VNode)allNodes.get(Integer.valueOf(id));
		}
	}
	
	public String nodeSimpleTypeNameOfID(int id){
		return getNodeByID(id).getClass().getSimpleName();
	} 
	
	/** get all nodes with the same nodeType */
	public Collection<VNode> getNodes(String nodeType){
		synchronized (this.allNodes){
			Collection<VNode> nodes = new ArrayList<VNode>();
			Iterator<VNode> it = allNodes.values().iterator();
			while(it.hasNext()){
				VNode node = it.next();
				String nodeTypeName = node.getClass().getName();
				if(nodeType.equals(nodeTypeName)){
					nodes.add(node);
				}
			}
			return nodes;
		}
	}
	
	/**
	 * This method retrieve all nodes with nodeTypeName. 
	 * @param name: the node type name should include package name,
	 *              such as "org.deri.nettopo.node.SensorNode".
	 * @param derived: whether need get nodes with nodeTypeName 
	 *        as well as nodes derived from this type. 
	 *        If false, this method is equal to "getNodes(String nodeType)".
	 * @return required VNode collection
	 */
	public Collection<VNode> getNodes(String name, boolean derived){
		synchronized (this.allNodes){
			Collection<VNode> nodes = new ArrayList<VNode>();
			Iterator<VNode> it = allNodes.values().iterator();
			while(it.hasNext()){
				VNode node = it.next();
				String nodeTypeName = node.getClass().getName();
				if(nodeTypeName.equals(name)){
					nodes.add(node);
				}
				if(derived){
					if(Util.isDerivedClass(node.getClass(),name))
						nodes.add(node);
				}
			}
			return nodes;
		}
	}
	
	/** get all nodes information */
	public Collection<VNode> getAllNodes(){
		synchronized (this.allNodes){
			return allNodes.values();
		}
	}
	
	/**
	 * @return the original all nodes
	 */
	public HashMap<Integer,VNode> getOriginalAllNodes(){
		return allNodes;
	}
	
	/**
	 * get all nodes ID
	 * @return
	 */
	public int[] getAllNodesID(){
		int[] nodesInfoArray = null;
		String nodesIDStr = new String();
		Iterator<VNode> iter = this.getAllNodes().iterator();
		while(iter.hasNext()){
			nodesIDStr += (iter.next().getID() + " ");
		}
		nodesInfoArray = Util.string2IntArray(nodesIDStr);
		return nodesInfoArray;
	}
	
	public int[] getAllSensorNodesID(){
		int[] allNodesId = this.getAllNodesID();
		LinkedList<Integer> array = new LinkedList<Integer>();
		for(int i=0;i<allNodesId.length;i++){
			if(getNodeByID(allNodesId[i]).getClass().getSimpleName().contains("Sensor")){
				array.add(allNodesId[i]);
			}
		}
		Integer[] result = array.toArray(new Integer[array.size()]);
		
		return Util.IntegerArray2IntArray(result);
	}
	
	public Coordinate getCoordianteByID(int id){
		synchronized (this.coordinates){
			return (Coordinate)coordinates.get(Integer.valueOf(id));
		}
	}
	
	public Collection<Coordinate> getAllCoordinats(){
		synchronized (this.coordinates){
			return coordinates.values();
		}
	}

	protected boolean hasDuplicateID(int id){
		return allNodes.containsKey(Integer.valueOf(id));
	}

	public HashMap<Integer, Coordinate> getOriginalAllCoordinates() {
		return coordinates;
	}

	public String coordinates2String(){
		StringBuffer sb = new StringBuffer();
		HashMap<Integer,Coordinate> coor = this.coordinates;
		Iterator<Integer> iter = coor.keySet().iterator();
		while(iter.hasNext()){
			Integer key = iter.next();
			sb.append("(" + key + ": ");
			sb.append(coor.get(key) + ")");
		}
		
		return sb.toString();
	}
	
	public String allNodesMaxTR2String(){
		StringBuffer sb = new StringBuffer();
		HashMap<Integer,VNode> node = this.allNodes;
		Iterator<Integer> iter = node.keySet().iterator();
		while(iter.hasNext()){
			Integer key = iter.next();
			sb.append("(" + key + ": ");
			sb.append(node.get(key).getAttrValue("Max TR") + ")");
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("coordinates:\n");
		result.append(coordinates2String() + "\n");
		result.append("allNodesMaxTR:\n");
		result.append(allNodesMaxTR2String() + "\n");
		result.append("Network Size: "+size+"\n");

		return result.toString();
	}
	public int[] getSinkNodeId() {
		int[] allNodesId = this.getAllNodesID();
		LinkedList<Integer> array = new LinkedList<Integer>();
		for(int i=0;i<allNodesId.length;i++){
			if(getNodeByID(allNodesId[i]).getClass().getSimpleName().contains("SinkNode")){
				array.add(allNodesId[i]);
			}
		}
		Integer[] result = array.toArray(new Integer[array.size()]);
		
		return Util.IntegerArray2IntArray(result);
		
	}

}
