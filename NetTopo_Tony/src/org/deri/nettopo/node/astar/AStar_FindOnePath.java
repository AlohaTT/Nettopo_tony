package org.deri.nettopo.node.astar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_ConnectNeighbors;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.tpgf.SensorNode_TPGF;
import org.deri.nettopo.node.tpgf.SourceNode_TPGF;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.NodeFComparator;





public class AStar_FindOnePath implements AlgorFunc{
	private WirelessSensorNetwork wsn;
	private LinkedList<Integer> path;	//·��
	//private ArrayList<SensorNode_AStar> searched;
	private ArrayList<SensorNode_AStar> openList;
	private ArrayList<SensorNode_AStar> closeList;
	/*************************�Ƿ��Ѿ�������ھӽڵ�*******************/
	//public static boolean flag=false;
	
	private SinkNode sink;
	private Coordinate sinkPos;
	private Algorithm algorithm;
	
	
	public AStar_FindOnePath(Algorithm algorithm){
		this.algorithm = algorithm;
	}

	public AStar_FindOnePath(){
		this(null);
	}
	
	public Algorithm getAlgorithm(){
		return this.algorithm;
	}
	
	public List<Integer> getPath(){
		return path;
	}
	
	public int getHopNum(){
		return (path.size() - 1); 
	}
	
	public void run(){
			findOnePath(true);
	}
	
	public boolean findOnePath(boolean needPainting){
		wsn = NetTopoApp.getApp().getNetwork();
		NetTopoApp.getApp().getPainter();
		path = new LinkedList<Integer>();//���ڴ洢·��
		//searched = new ArrayList<SensorNode_AStar>();//���ڴ洢�Ѿ���ѯ���Ľڵ�
		openList = new ArrayList<SensorNode_AStar>();
		closeList = new ArrayList<SensorNode_AStar>();
		boolean canFind = false;
		
		
		if(NetTopoApp.getApp().isFileModified()){
			wsn.resetAllNodesAvailable();
			NetTopoApp.getApp().setFileModified(false);
		}
	
		TPGF_ConnectNeighbors func_connectNeighbors = new TPGF_ConnectNeighbors();
		func_connectNeighbors.connectNeighbors(false);
		
		
		if(wsn!=null){
			Collection<VNode> nodes_source = wsn.getNodes("org.deri.nettopo.node.tpgf.SourceNode_TPGF",true);	//���source�ڵ�
			Collection<VNode> nodes_sink = wsn.getNodes("org.deri.nettopo.node.SinkNode",true);
			if(nodes_sink.size()>0 && nodes_source.size()>0){
				SourceNode_TPGF node_source = (SourceNode_TPGF)nodes_source.iterator().next(); // retrieve one source node
				sink = (SinkNode)nodes_sink.iterator().next();
				sinkPos = wsn.getCoordianteByID(sink.getID());
				sink.getMaxTR();
				openList.add(convertToSensorNode_AStar(node_source));
				//searched.add(convertToSensorNode_AStar(node_source));
				boolean isFind = false;
				VNode sn=null;
				SensorNode_AStar sna=null;
				while(openList.size()>0)
				{
					sna=openList.get(0);
					sn = wsn.getNodeByID(sna.getID());//��ȡ��һ���ڵ����ϸ��Ϣ
					
					if(sn.getID()==sink.getID())//��������һ���ڵ㣬�ҵ��ˡ�
					{
						isFind=true;
						break;
					}
					List<Integer> neighbors =((SensorNode_TPGF) sn).getNeighbors();
					//sna=convertToSensorNode_AStar(sn);
					for (Integer i : neighbors) {
						checkPath(i, sna);
						
					}
					closeList.add(openList.remove(0));
					Collections.sort(openList, new NodeFComparator());
					
				}
				if(isFind)
				{
					getPath(path, sna);
				}
				else
				{
					System.out.println("No path");
				}
			}
		}
		return canFind;
	}
	
	
	
	
	private void checkPath(Integer id, SensorNode_AStar sn) {
		// TODO Auto-generated method stub
		if (isListContains(closeList, id) != -1) {//�ڹر��б��ɶ������
			return;
		}
		SensorNode_AStar nextNode = new SensorNode_AStar();//���id�ڵ�
		nextNode.setID(id);
		Coordinate currentCoordinate = wsn.getCoordianteByID(sn.getID());
		double cost = currentCoordinate.distance(wsn.getCoordianteByID(id));//�����ھӽڵ�(ID)�͵�ǰ�ڵ�ľ���
		int index = isListContains(openList, id);
		if (index == -1)// ���ڿ�������
		{
			count(nextNode, cost);//�������ֵ
			nextNode.setParentNode(sn);
			openList.add(nextNode);
		} else/* ����ڿ�������** */
		{
			if ((sn.getG() + cost) < nextNode.getG()) {//���Ը��¾��룬��f��С
				nextNode.setParentNode(sn);
				countG(nextNode, cost);
				countF(nextNode);
				openList.set(index, nextNode);//�滻��
			}
		}
	}

	private SensorNode_AStar convertToSensorNode_AStar(VNode node)
	{
		SensorNode_AStar sna=new SensorNode_AStar();
		sna.setID(node.getID());
		return sna;
	}
	
	
	
	private void getPath(List<Integer> resultList, SensorNode_AStar node) {
		if (node.getParentNode() != null) {
			getPath(resultList, node.getParentNode());
		}
		resultList.add(node.getID());
	}

	
	public int isListContains(List<SensorNode_AStar> list, int id) {
		for (int i = 0; i < list.size(); ++i) {
			if (list.get(i).getID() == id)
				return i;
		}
		return -1;
	}
	
	
	
	private void count(SensorNode_AStar node, double cost) {
		countG(node, cost);
		countH(node);
		countF(node);
	}

	// ����Gֵ
	private void countG(SensorNode_AStar node, double cost) {
		if (node.getParentNode() == null) {
			node.setG(cost);
		} else {
			node.setG(node.getParentNode().getG() + cost);
		}
	}

	// ����Hֵ,����Ŀ��ڵ��ŷʽ����
	private void countH(SensorNode_AStar node) {
		node.setH(sinkPos.distance(wsn.getCoordianteByID(node.getID())));
	}

	// ����Fֵ
	private void countF(SensorNode_AStar node) {
		node.setF(node.getG() + node.getH());
	}
	
	
	
	
	@Override
	public String getResult() {
		return path.toString()+"\nhopNum:"+getHopNum();
		// TODO Auto-generated method stub
		
	}

}
