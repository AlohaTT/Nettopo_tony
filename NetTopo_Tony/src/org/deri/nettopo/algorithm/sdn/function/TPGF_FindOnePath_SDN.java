package org.deri.nettopo.algorithm.sdn.function;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.display.Painter;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.tpgf.SensorNode_TPGF;
import org.deri.nettopo.util.Coordinate;
import org.eclipse.swt.graphics.RGB;

public class TPGF_FindOnePath_SDN implements AlgorFunc {
	private WirelessSensorNetwork wsn;
	private Painter painter;
	private LinkedList<Integer> path;
	private ArrayList<Integer> searched;
	/************************* �Ƿ��Ѿ�������ھӽڵ� *******************/
	public boolean flag = false;

	private SinkNode sink;
	private Coordinate sinkPos;
	private int sinkTR;

	private Algorithm algorithm;

	public TPGF_FindOnePath_SDN(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public TPGF_FindOnePath_SDN() {
		this(null);
	}

	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public List<Integer> getPath() {
		return path;
	}

	public int getHopNum() {
		return (path.size() - 1);
	}

	public void run() {
		// if(!Property.isTPGF) //����slave�ż���
		// findOnePath(true);
		// else
		// findOnePath(false);
	}

	public boolean findOnePath(boolean needPainting, Integer currentId) {
		wsn = NetTopoApp.getApp().getNetwork();
		painter = NetTopoApp.getApp().getPainter();
		path = new LinkedList<Integer>();// ���ڴ洢·��
		searched = new ArrayList<Integer>();// ���ڴ洢�Ѿ���ѯ���Ľڵ�
		boolean canFind = false;
		sink = (SinkNode) wsn.getNodeByID(wsn.getSinkNodeId()[0]);

		if (NetTopoApp.getApp().isFileModified()) {
			wsn.resetAllNodesAvailable();
			NetTopoApp.getApp().setFileModified(false);
		}


		if (wsn != null) {
			sinkPos = wsn.getCoordianteByID(sink.getID());
			sinkTR = sink.getMaxTR();
			SensorNode_TPGF node_source = (SensorNode_TPGF) wsn.getNodeByID(currentId);
			node_source.setAvailable(true);
			if (canReachSink(node_source)) {
				canFind = true;

			}
		}
		return canFind;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public boolean canReachSink(SensorNode_TPGF node) {
		if (!node.isAvailable())
			return false;
		searched.add(Integer.valueOf(node.getID()));

		/*
		 * If the distance between the current node and sinknode is in both
		 * nodes' transmission radius, the node can reach the sink. Return
		 * immediately
		 */
		if (inOneHop(node)) {
			path.add(Integer.valueOf(sink.getID()));
			path.add(Integer.valueOf(node.getID()));
			node.setAvailable(false); // the node cannot be used next time
			return true;
		}

		/*
		 * If the current node is not one-hop from sink, it search it's neighbor
		 * that is most near to sink and find out whether it can reach the sink.
		 * If not, it searches its' neighbor that is second most near to sink
		 * and go on, etc. The neighbors do not include any already searched
		 * node that is not in one hope
		 */
		List<Integer> neighborsID = new ArrayList<Integer>();
		Integer[] neighbor2 = getNeighbor(node.getID());
		for (int i = 0; i < neighbor2.length; i++) {
			neighborsID.add(neighbor2[i]);
		}
		/* First we remove all searched node id in the neighbor list */
		for (int i = 0; i < searched.size(); i++) {
			neighborsID.remove(searched.get(i));
		}

		/* Then we sort the neighbor list into distance ascending order */
		for (int i = neighborsID.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				int id1 = ((Integer) neighborsID.get(j)).intValue();
				Coordinate c1 = wsn.getCoordianteByID(id1);
				double dis1 = c1.distance(sinkPos);
				int id2 = ((Integer) neighborsID.get(j + 1)).intValue();
				Coordinate c2 = wsn.getCoordianteByID(id2);
				double dis2 = c2.distance(sinkPos);
				if (dis1 > dis2) {
					Integer swap = neighborsID.get(j);
					neighborsID.set(j, neighborsID.get(j + 1));
					neighborsID.set(j + 1, swap);
				}
			}
		}

		/*
		 * Then we search from the neighbor that is most near to sink to the
		 * neighbor that is least near to sink
		 */
		for (int i = 0; i < neighborsID.size(); i++) {
			int neighborID = ((Integer) neighborsID.get(i)).intValue();
			SensorNode_TPGF neighbor = (SensorNode_TPGF) wsn.getNodeByID(neighborID);
			if (canReachSink(neighbor)) {
				// System.out.println(neighbor.getID() + " can get sink");
				path.add(Integer.valueOf(node.getID()));
				node.setAvailable(false); // the node cannot be used next time
				return true;
			}
		}
		return false;
	}

	public boolean inOneHop(SensorNode_TPGF node) {
		int nodeID = node.getID();
		Coordinate c = wsn.getCoordianteByID(nodeID);
		int tr = node.getMaxTR();
		double distance = 0;
		distance = (double) ((c.x - sinkPos.x) * (c.x - sinkPos.x) + (c.y - sinkPos.y) * (c.y - sinkPos.y)
				+ (c.z - sinkPos.z) * (c.z - sinkPos.z));
		distance = Math.sqrt(distance);
		if (distance <= tr && distance <= sinkTR)
			return true;
		return false;
	}

	@Override
	public String getResult() {
		return path.toString() + "\n" + "hopNum:" + getHopNum();
		// TODO Auto-generated method stub

	}

	private Integer[] getNeighbor(int id) {
		int[] ids = wsn.getAllNodesID();
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
}
