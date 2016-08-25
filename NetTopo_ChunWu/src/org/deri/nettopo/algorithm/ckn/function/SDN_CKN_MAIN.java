package org.deri.nettopo.algorithm.ckn.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.Util;

/*
 * 
 */
public class SDN_CKN_MAIN implements AlgorFunc {

	private Algorithm algorithm;
	private WirelessSensorNetwork wsn;
	private NetTopoApp app;
	private HashMap<Integer, Double> ranks;
	private HashMap<Integer, Integer[]> neighbors;
	private HashMap<Integer, Boolean> flagS; // 标志sensor是否只有一个neighbor
	private HashMap<Integer, Boolean> flagM; // 标志sensor是否可以进入睡眠状态
	private HashMap<Integer, Boolean> awake;
	private int k;// the least awake neighbors
	boolean needInitialization;

	public SDN_CKN_MAIN(Algorithm algorithm) {
		this.algorithm = algorithm;
		neighbors = new HashMap<Integer, Integer[]>();
		awake = new HashMap<Integer, Boolean>();
		flagS = new HashMap<Integer, Boolean>();
		flagM = new HashMap<Integer,Boolean>();
		k = 1;
		needInitialization = true;
	}

	public SDN_CKN_MAIN() {
		this(null);
	}

	public void run() {
		if (isNeedInitialization()) {
			initializeWork();
		}
		CKN_Function();
		resetColorAfterCKN();

		app.getPainter().rePaintAllNodes();
		app.getDisplay().asyncExec(new Runnable() {
			public void run() {
				app.refresh();
			}
		});

	}

	public void runForStatistics() {
		if (isNeedInitialization()) {
			initializeWork();
		}
		CKN_Function();
		CKN_Function();
	}

	/****************************************************************************/

	/**
	 * 
	 * @return the ranks between 0 and 1, and with id as the key
	 */
	private HashMap<Integer, Double> getRankForAllNodes() {
		HashMap<Integer, Double> tempRanks = new HashMap<Integer, Double>();
		int[] ids = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			double rank = Math.random();
			tempRanks.put(new Integer(id), new Double(rank));
		}
		return tempRanks;
	}

	private void initializeAwake() {
		int ids[] = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			setAwake(ids[i], true);
		}
	}

	private void setAwake(int id, boolean isAwake) {
		Integer ID = new Integer(id);
		awake.put(ID, isAwake);
		if (wsn.nodeSimpleTypeNameOfID(id).contains("Sensor")) {
			((SensorNode) wsn.getNodeByID(id)).setActive(isAwake);
			((SensorNode) wsn.getNodeByID(id)).setAvailable(isAwake);
		}
	}

	private void initializeNeighbors() {
		int[] ids = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			Integer ID = new Integer(ids[i]);
			Integer[] neighbor = getNeighbor(ids[i]);
			neighbors.put(ID, neighbor);
		}
	}

	private Integer[] getNeighbor(int id) {
		int[] ids = wsn.getAllSensorNodesID();
		ArrayList<Integer> neighbor = new ArrayList<Integer>();
		int maxTR = Integer.parseInt(wsn.getNodeByID(id).getAttrValue("Max TR"));
		Coordinate coordinate = wsn.getCoordianteByID(id);
		for (int i = 0; i < ids.length; i++) {
			Coordinate tempCoordinate = wsn.getCoordianteByID(ids[i]);
			if (ids[i] != id && Coordinate.isInCircle(tempCoordinate, coordinate, maxTR)) {
				neighbor.add(new Integer(ids[i]));
			}
		}
		return neighbor.toArray(new Integer[neighbor.size()]);
	}

	
	private void initializeWork() {
		app = NetTopoApp.getApp();
		wsn = app.getNetwork();
		initializeAwake();// at first all are true
		initializeS();
		initializeM();
		setNeedInitialization(false);
	}

	/**
	 * 初始化M，初始值为false
	 */
	private void initializeM() {
		int[] ids = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			setM(ids[i], false);
		}
	}

	/**
	 * 初始化令S都为false
	 */
	private void initializeS() {
		int ids[] = wsn.getAllSensorNodesID();
		for (int i = 0; i < ids.length; i++) {
			if (getNeighbor(ids[i]).length == 1) {
				setS(ids[i], true);
			} else {
				setS(ids[i], false);
			}
		}
	}

	/**
	 * @param id
	 * @param S
	 */
	private void setS(int id, Boolean s) {
		Integer ID = new Integer(id);
		flagS.put(ID, s);
	}

	/************
	 * the above methods are to initialise the CKN fields
	 ***************/

	/************
	 * the following methods are to be used in CKN_Function
	 *************/

	private Integer[] getAwakeNeighbors(int id) {
		HashSet<Integer> nowAwakeNeighbor = new HashSet<Integer>();
		Integer[] neighbor = neighbors.get(new Integer(id));
		for (int i = 0; i < neighbor.length; i++) {
			if (awake.get(neighbor[i]).booleanValue()) {
				nowAwakeNeighbor.add(neighbor[i]);
			}
		}
		return nowAwakeNeighbor.toArray(new Integer[nowAwakeNeighbor.size()]);
	}

	private boolean isOneOfAwakeNeighborsNumLessThanK(int id) {
		boolean result = false;
		Integer[] nowAwakeNeighbors = getAwakeNeighbors(id);
		for (int i = 0; i < nowAwakeNeighbors.length; i++) {
			if (getAwakeNeighbors(nowAwakeNeighbors[i].intValue()).length < k) {
				result = true;
				break;
			}
		}
		return result;
	}

	private Integer[] getCu(int id) {
		Integer ID = new Integer(id);
		LinkedList<Integer> result = new LinkedList<Integer>();
		List<Integer> availableAwakeNeighbors = Arrays.asList(getAwakeNeighbors(id));
		Iterator<Integer> iter = availableAwakeNeighbors.iterator();
		while (iter.hasNext()) {
			Integer neighbor = iter.next();
			if (ranks.get(neighbor) < ranks.get(ID)) {
				result.add(neighbor);
			}
		}
		return result.toArray(new Integer[result.size()]);
	}

	/**
	 * any node in Nu has at least k neighbors from Cu
	 * 
	 * @param Nu
	 * @param Cu
	 * @return
	 */
	private boolean atLeast_k_Neighbors(Integer[] Nu, Integer[] Cu) {
		if (Cu.length < k) {
			return false;
		}
		int[] intCu = Util.IntegerArray2IntArray(Cu);
		for (int i = 0; i < Nu.length; i++) {
			int[] neighbor = Util.IntegerArray2IntArray(neighbors.get(Nu[i]));
			int[] neighborInCu = Util.IntegerArrayInIntegerArray(neighbor, intCu);
			if (neighborInCu.length < k + 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * node in Cu is directly connect or indirectly connect within u's 2-hop
	 * wake neighbours that rank < ranku
	 * 
	 * @param Cu
	 * @param u
	 * @return
	 */
	private boolean qualifiedConnectedInCu(Integer[] Cu, int[] awakeNeighborsOf2HopsLessThanRanku) {
		if (Cu.length == 0) {
			return false;
		}

		if (Cu.length == 1) {
			return true;
		}

		Integer[] connectionOfCuElementFromCu1 = getConnection(Cu[1], awakeNeighborsOf2HopsLessThanRanku);
		if (!Util.isIntegerArrayInIntegerArray(Util.IntegerArray2IntArray(Cu),
				Util.IntegerArray2IntArray(connectionOfCuElementFromCu1))) {
			return false;
		}
		return true;
	}

	/**
	 * to get connection of id with id's neighbor in array
	 * 
	 * @param beginning
	 * @param array
	 * @return
	 */
	private Integer[] getConnection(int beginning, int[] array) {
		ArrayList<Integer> connectedCu = new ArrayList<Integer>();
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(new Integer(beginning));
		while (!queue.isEmpty()) {
			Integer head = queue.poll();
			connectedCu.add(head);
			int[] CuNeighbor = Util.IntegerArrayInIntegerArray(Util.IntegerArray2IntArray(neighbors.get(head)), array);
			for (int i = 0; i < CuNeighbor.length; i++) {
				if (!connectedCu.contains(new Integer(CuNeighbor[i])) && !queue.contains(new Integer(CuNeighbor[i]))) {
					queue.offer(new Integer(CuNeighbor[i]));
				}
			}
		}
		return connectedCu.toArray(new Integer[connectedCu.size()]);
	}

	/****************************************************************************/

	private void resetColorAfterCKN() {
		Iterator<Integer> iter = awake.keySet().iterator();
		while (iter.hasNext()) {
			Integer id = iter.next();
			Boolean isAwake = awake.get(id);
			if (isAwake.booleanValue()) {
				wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.AwakeNodeColorRGB);
			} else {
				wsn.resetNodeColorByID(id.intValue(), NodeConfiguration.SleepNodeColorRGB);
			}
		}
	}

	private void CKN_Function() {
		ranks = getRankForAllNodes();
		initializeNeighbors();
		int[] disordered = Util.generateDisorderedIntArrayWithExistingArray(wsn.getAllSensorNodesID());
		for (int i = 0; i < disordered.length; i++) {
			int currentID = disordered[i];
			Integer[] Nu = getAwakeNeighbors(currentID);
			/*
			 * if(Nu.length < k ||
			 * isOneOfAwakeNeighborsNumLessThanK(currentID)){
			 * this.setAwake(currentID, true); }else{ Integer[] Cu =
			 * getCu(currentID); int[] awakeNeighborsOf2HopsLessThanRanku =
			 * Util.IntegerArray2IntArray(getAwakeNeighborsOf2HopsLessThanRanku(
			 * currentID)); if(atLeast_k_Neighbors(Nu, Cu) &&
			 * qualifiedConnectedInCu(Cu,awakeNeighborsOf2HopsLessThanRanku)){
			 * setAwake(currentID, false); }else{ setAwake(currentID, true); } }
			 */
			// 判断S是否为false，如果为false，检查S是否为1
			if (flagS.get(currentID))
				setM(getNeighbor(currentID)[0], true);
			else {
				// 判断M,如果M为true，则可以进入睡眠状态，如果为false则不能进入睡眠状态
				if (flagM.get(currentID)) {

				} else {

				}
			}
		}
	}

	/**
	 * @param id
	 * @param flagM
	 */
	private void setM(int id, Boolean m) {
		Integer ID = new Integer(id);
		flagM.put(ID, m);
	}

	/**
	 * 如果sensor的neighbor数等于1，置S标志位为true
	 * 
	 * @param currentID
	 */
	private void checkNeighborsNumber(int currentID) {
		if (getNeighbor(currentID).length == 1)
			setS(currentID, true);

	}

	public Algorithm getAlgorithm() {
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
