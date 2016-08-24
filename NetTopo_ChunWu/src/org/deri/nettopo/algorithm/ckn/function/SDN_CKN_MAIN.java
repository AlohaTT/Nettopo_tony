package org.deri.nettopo.algorithm.ckn.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.sdn.Controller_SinkNode;
import org.deri.nettopo.node.sdn.FlowTable;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.Util;



public class SDN_CKN_MAIN implements AlgorFunc {
	private static Logger logger=Logger.getLogger(SDN_CKN_MAIN.class);

	private Algorithm algorithm;
	private WirelessSensorNetwork wsn;
	private NetTopoApp app;
	private HashMap<Integer,Integer[]> neighbors;
	private HashMap<Integer,Boolean> awake;
	private int k;// the least awake neighbors
	boolean needInitialization;
	private HashMap<Integer, FlowTable> flowTable;
	
	public SDN_CKN_MAIN(Algorithm algorithm){
		this.algorithm = algorithm;
		Controller_SinkNode controller = new Controller_SinkNode();
		neighbors = controller.getNeighbors();
		awake = controller.getAwakeNodes();
		k = 1;
		needInitialization = true;
	}

	
	public SDN_CKN_MAIN(){
		this(null);
	}
	
	public void run() {
		/*
		 * 初始化
		 */
		if(isNeedInitialization()){
			initializeWork();
		}
		SDN_CKN_Function();
		resetColorAfterCKN();
		
		app.getPainter().rePaintAllNodes();
		app.getDisplay().asyncExec(new Runnable(){
			public void run(){
				app.refresh();
			}
		});
		
	}
	
	public void runForStatistics(){
		if(isNeedInitialization()){
			initializeWork();
		}
		SDN_CKN_Function();
		SDN_CKN_Function();
	}

	/****************************************************************************/
	
	
	
	private void initializeAwake(){
		int ids[] = wsn.getAllSensorNodesID();
		for(int i=0;i<ids.length;i++){
			setAwake(ids[i],true);
		}
	}
	
	private void setAwake(int id, boolean isAwake){
		Integer ID = new Integer(id);
		awake.put(ID, isAwake);
		if(wsn.nodeSimpleTypeNameOfID(id).contains("Sensor")){
			((SensorNode)wsn.getNodeByID(id)).setActive(isAwake);
			((SensorNode)wsn.getNodeByID(id)).setAvailable(isAwake);
		}
	}
	
	/**
	 * 初始化邻居节点表
	 */
	private void initializeNeighbors(){
		int[] ids = wsn.getAllSensorNodesID();
		for(int i=0;i<ids.length;i++){
			Integer ID = new Integer(ids[i]);
			Integer[] neighbor = getNeighbor(ids[i]);
			neighbors.put(ID, neighbor);
		}
	} 
	private Integer[] getNeighbor(int id){
		
		int[] ids = wsn.getAllSensorNodesID();
		ArrayList<Integer> neighbor = new ArrayList<Integer>();
		int maxTR = Integer.parseInt(wsn.getNodeByID(id).getAttrValue("Max TR"));
		Coordinate coordinate = wsn.getCoordianteByID(id);
		for(int i=0;i<ids.length;i++){
			Coordinate tempCoordinate = wsn.getCoordianteByID(ids[i]);
			if(ids[i] != id && Coordinate.isInCircle(tempCoordinate, coordinate, maxTR)){
				neighbor.add(new Integer(ids[i]));
			}
		}
		return neighbor.toArray(new Integer[neighbor.size()]);
	} 
	

	/**
	 * 初始化工作，令所有节点为工作状态，并保存1-hop邻居节点
	 * 
	 */
	private void initializeWork(){
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		initializeAwake();//at first all are true
		setNeedInitialization(false);
		initializeNeighbors();
	}
	
	/************the above methods are to initialise the CKN fields***************/
	
	/************the following methods are to be used in CKN_Function*************/
	
	/*
	 * 获得邻居节点中处于工作状态的节点id集合
	 */
	private Integer[] getAwakeNeighbors(int id){
		HashSet<Integer> nowAwakeNeighbor = new HashSet<Integer>();
		Integer[] neighbor = neighbors.get(new Integer(id));
		for(int i=0;i<neighbor.length;i++){
			if(awake.get(neighbor[i]).booleanValue()){
				nowAwakeNeighbor.add(neighbor[i]);
			}
		}
		return nowAwakeNeighbor.toArray(new Integer[nowAwakeNeighbor.size()]);
	}
	
	/*
	 * 判断处于工作状态的邻居节点的数量是否小于k
	 */
	private boolean isOneOfAwakeNeighborsNumLessThanK(int id){
		boolean result = false;
		Integer[] nowAwakeNeighbors = getAwakeNeighbors(id);
		for(int i=0;i<nowAwakeNeighbors.length;i++){
			if(getAwakeNeighbors(nowAwakeNeighbors[i].intValue()).length < k){
				result = true;
				break;
			}
		}
		return result;
	} 
	
	
	

	
	/**
	 * any node in Nu has at least k neighbours from Cu
	 * @param Nu
	 * @param Cu
	 * @return
	 */
	private boolean atLeast_k_Neighbors(Integer[] Nu, Integer[] Cu){
		if(Cu.length < k){
			return false;
		}
		int[] intCu = Util.IntegerArray2IntArray(Cu);
		for(int i=0;i<Nu.length;i++){
			int[] neighbor = Util.IntegerArray2IntArray(neighbors.get(Nu[i]));
			int[] neighborInCu = Util.IntegerArrayInIntegerArray(neighbor, intCu);
			if(neighborInCu.length < k+1){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * node in Cu is directly connect or indirectly connect within u's 2-hop wake neighbours that rank < ranku
	 * @param Cu
	 * @param u
	 * @return
	 */
	private boolean qualifiedConnectedInCu(Integer[] Cu, int[] awakeNeighborsOf2HopsLessThanRanku){
		if(Cu.length == 0){
			return false;
		}
		
		if(Cu.length == 1){
			return true;
		}

		Integer[] connectionOfCuElementFromCu1 = getConnection(Cu[1], awakeNeighborsOf2HopsLessThanRanku);
		if(!Util.isIntegerArrayInIntegerArray(Util.IntegerArray2IntArray(Cu), Util.IntegerArray2IntArray(connectionOfCuElementFromCu1))){
			return false;
		}
		return true;
	}
	
	/**
	 * to get connection of id with id's neighbour in array
	 * @param beginning
	 * @param array
	 * @return
	 */
	private Integer[] getConnection(int beginning, int[] array){
		ArrayList<Integer> connectedCu = new ArrayList<Integer>();
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(new Integer(beginning));
		while(!queue.isEmpty()){
			Integer head = queue.poll();
			connectedCu.add(head);
			int[] CuNeighbor = Util.IntegerArrayInIntegerArray(Util.IntegerArray2IntArray(neighbors.get(head)),array);
			for(int i=0;i<CuNeighbor.length;i++){
				if(!connectedCu.contains(new Integer(CuNeighbor[i])) && !queue.contains(new Integer(CuNeighbor[i]))){
					queue.offer(new Integer(CuNeighbor[i]));
				}
			}
		}
		return connectedCu.toArray(new Integer[connectedCu.size()]);
	}
	
	/****************************************************************************/
	
	private void resetColorAfterCKN(){
		Iterator<Integer> iter = awake.keySet().iterator();
		while(iter.hasNext()){
			Integer id = iter.next();
			Boolean isAwake = awake.get(id);
			if(isAwake.booleanValue()){
				wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.AwakeNodeColorRGB);
			}else{
				wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.SleepNodeColorRGB);
			}
		}
	}
	/**
	 * SDN_CKN主要步骤
	 */
	private void SDN_CKN_Function(){
		int[] disordered = Util.generateDisorderedIntArrayWithExistingArray(wsn.getAllSensorNodesID());
		for(int i=0;i<disordered.length;i++){
			int currentID = disordered[i];
			Integer[] Nu = getAwakeNeighbors(currentID);// Nu is the currentAwakeNeighbor
		}
	}
	
	public Algorithm getAlgorithm(){
		return algorithm;
	}

	public boolean isNeedInitialization() {
		return needInitialization;
	}

	
	public void setNeedInitialization(boolean needInitialization) {
		this.needInitialization = needInitialization;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
}
