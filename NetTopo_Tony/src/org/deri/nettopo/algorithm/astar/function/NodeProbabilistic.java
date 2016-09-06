package org.deri.nettopo.algorithm.astar.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.NodeConfiguration;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.display.Painter;

public class NodeProbabilistic implements AlgorFunc {
	private WirelessSensorNetwork wsn;
	private Painter paint;
	private ArrayList<SensorNode> deadAllList;
	private Algorithm algorithm;
	private Collection<VNode> sensorNodes;
	private int count;
	private double MAY = 0.05;
private Random rand;
	public NodeProbabilistic(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
//get the NodeProbabilistic,��ȡ�ڵ�ʧЧ����
	public double getMAY() {
		return MAY;
	}
//set the NodeProbabilistic,���ýڵ�ʧЧ����
	public void setMAY(double mAY) {
		MAY = mAY;
	}
//count is node failure's num,but not use now
	public int getCount() {
		return count;
	}

	public void setCount(int n) {
		this.count = n;
	}

	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public void run() {

		CalculateNodeProbabilistic(true);

		NetTopoApp.getApp().refresh();

	}

	/**
	 * �ڵ����ʧЧ
	 * @param needPaint
	 * 
	 */
	public void CalculateNodeProbabilistic(boolean needPaint) {
		wsn = NetTopoApp.getApp().getNetwork();
		sensorNodes = wsn.getNodes(
				"org.deri.nettopo.node.tpgf.SensorNode_TPGF", true);
		deadAllList = new ArrayList<SensorNode>();
		rand = new Random();
		deadAllList=getAllDeadNodeList(needPaint);
	}
	// ����ΪMAY���жϽڵ��Ƿ�ʧЧ
	public boolean outDeadProbabilistic(Integer nodeID) {	
		if (rand.nextDouble() <= MAY) {
			return true;
		} else {
			return false;
		}
	}

	// ��ȡAllGasʧЧ�ڵ��б�
	public  ArrayList<SensorNode> getAllDeadNodeList(
			boolean needPaint) {

		ArrayList<SensorNode> deadNodess = new ArrayList<SensorNode>();// ȫ��Gas��ʧЧ�ڵ㼯����һ���б���
		HashMap<Integer, Coordinate> deadCoors = new HashMap<Integer, Coordinate>();
		// ����ʧЧ���ʣ�������gas���ⲿ�ڵ�
		Iterator<VNode> iter = sensorNodes.iterator();
		while (iter.hasNext()) {
			SensorNode sn = (SensorNode) iter.next();
			if (outDeadProbabilistic(sn.getID())) {
				deadCoors.put(sn.getID(), wsn.getCoordianteByID(sn.getID()));
				deadNodess.add(sn);
			}
		}
		wsn.setDeadCors(deadCoors);//��ʧЧ�ڵ����괫����ǰWSN
		wsn.setDeadList(deadNodess);//��ʧЧ�ڵ�ID������ǰWSN
		if (needPaint) {//�ж���ͼ�λ�����ʱ�����û�ͼ��
			paint = NetTopoApp.getApp().getPainter();
			paint.rePaintAllNodes();
			NetTopoApp.getApp().addLog("Some nodes are made invilid randomly");
		}
		//��ʧЧ�ڵ�ɾ��������ɫ��ΪDeadNodeRGB��ɫ
		for (Iterator<SensorNode> iterator = deadNodess.iterator(); iterator
				.hasNext();) {
			SensorNode sensorNode = (SensorNode) iterator.next();
			sensorNode.setColor(NodeConfiguration.DeadNodeRGB);
			wsn.deleteNodeByID2(sensorNode.getID());
			if (needPaint) {//Ϊ���Ч�������û�ͼ������ʧЧ�ڵ���ʾ�ڽ����ϣ����Ǵ˽ڵ�ʵ���ǲ����ڵ�
				paint.paintNodeByCoors(sensorNode.getID(),
						sensorNode.getColor());
			}
		}
		return deadAllList;
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
