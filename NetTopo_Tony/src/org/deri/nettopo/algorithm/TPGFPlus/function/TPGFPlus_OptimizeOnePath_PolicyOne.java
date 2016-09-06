package org.deri.nettopo.algorithm.TPGFPlus.function;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.*;
import org.deri.nettopo.network.*;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.tpgf.SensorNode_TPGF;
import org.deri.nettopo.display.*;
import org.eclipse.swt.graphics.RGB;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

import org.deri.nettopo.algorithm.TPGFPlus.function.TPGFPlus_ConnectNeighbors;  //��
import org.deri.nettopo.algorithm.TPGFPlus.function.TPGFPlus_FindOnePath_PolicyOne;  //��

public class TPGFPlus_OptimizeOnePath_PolicyOne implements AlgorFunc {
	private Algorithm algorithm;
	private WirelessSensorNetwork wsn = null;
	private Painter painter = null;	
	List<Integer> opPath = null;
	List<Integer> release = null;
	
	private SinkNode sink;//---------------------------------------------2011-4-19
	
	
	public TPGFPlus_OptimizeOnePath_PolicyOne(Algorithm algorithm){
		this.algorithm = algorithm;
	}
	
	public Algorithm getAlgorithm(){
		return this.algorithm;
	}
	
	public List<Integer> getOpPath(){
		return opPath;
	}
	
	public int getHopNum(){
		return (opPath.size()-1);
	}
	
	public void run(){
		optimizeOnePath(true);
	}
	
	public void optimizeOnePath(boolean needPainting){
		wsn = NetTopoApp.getApp().getNetwork();
		painter = NetTopoApp.getApp().getPainter();
		
		TPGFPlus_ConnectNeighbors func_connectNeighbors = (TPGFPlus_ConnectNeighbors)getAlgorithm().getFunctions()[0];
		func_connectNeighbors.ConnectNeighbors_TPGFPlus(false);
		
		TPGFPlus_FindOnePath_PolicyOne func_find = (TPGFPlus_FindOnePath_PolicyOne)getAlgorithm().getFunctions()[4];
		List<Integer> path = func_find.getPath();
		if(path==null)
			return;
		if(path.size()<2)
			return;		
		
		/* a list of node's id constituting the optimal path */
		opPath = new LinkedList<Integer>();
		/* a list of node's id with which the node is to be released */
		release = new LinkedList<Integer>();
		
		opPath.add(path.get(0)); // Optimal path always have the sink node
		opPath.add(path.get(1)); // Optimal path always have the node in one hop to sink node
		int label = 1;
		while(label < path.size()-1){
			/* get the neighbor nodes of the node at current index position */
			
			int nodeID = ((Integer)path.get(label)).intValue();
//			SensorNode_TPGF node = (SensorNode_TPGF)wsn.getNodeByID(nodeID);//------------------------------2011-4-1 ��
//	        SensorNode neighbor =(SensorNode) wsn.getNodeByID(nodeID);  //------------------------------2011-4-1 ��
			
			int nextOpIndex = label + 1;
//			List<Integer> neighborsID = node.getNeighbors();     //------------------------------2011-4-1 ��
			
			
			Integer[] neighborsID=func_connectNeighbors.getNeighbors(nodeID);//------------------------------2011-4-1 ��
			/* For each neighbor node, compare it's id with that of all path's nodes
			 * with index that is at least 2 larger than current label */
			for(int i=0;i<neighborsID.length;i++){
				Integer neighborID = (Integer)neighborsID[i];

				
				for(int j=label+2; j<path.size(); j++){
					Integer id = (Integer)path.get(j);
					int result = neighborID.compareTo(id);
					if(result == 0){ // two ids are identical
						if(j > nextOpIndex)
							nextOpIndex = j;
					}
				}
			}
			/* add the optimized node's id to the opPath */
			opPath.add(path.get(nextOpIndex));
			/* add intermediate nodes's id to the release list
			 * and make the nodes available */
			for(int i=label+1; i<nextOpIndex; i++){
				
				Integer id = (Integer)path.get(i);
				release.add(id);
				
				Collection<VNode> nodes_sink = wsn.getNodes("org.deri.nettopo.node.SinkNode",true);//-----------------------2011-4-19
				sink = (SinkNode)nodes_sink.iterator().next();
				
//				SensorNode_TPGF releaseNode = (SensorNode_TPGF)wsn.getNodeByID(id.intValue());//-------------2011-4-12  ��
				
				if(id!=sink.getID()){
					SensorNode_TPGF releaseNode = (SensorNode_TPGF)wsn.getNodeByID(id.intValue());//-------------2011-4-12  ��
					releaseNode.setAvailable(true);
					}
			
				else if((id=sink.getID())!= null){//---------------------------------------------2011-4-19
					SinkNode releaseNode = (SinkNode)wsn.getNodeByID(id.intValue());
					releaseNode.setAvailable(true);
				         }
				
				if(needPainting)
					painter.paintNode(id.intValue());
			}
			/* update the label */
			label = nextOpIndex;
		}
		
		if(needPainting){			
			/* paint the optimized path stored in opPath */
			for(int i=0;i<opPath.size()-1;i++){
				int id1 = ((Integer)opPath.get(i)).intValue();
				int id2 = ((Integer)opPath.get(i+1)).intValue();
				painter.paintConnection(id1, id2, new RGB(240,56,208));
			}
			
			/* Add log info */
			final StringBuffer message = new StringBuffer("Optimized Path: ");
			for(int i=opPath.size()-1;i>=0;i--){
				message.append(opPath.get(i));
				message.append(" ");
			}
			message.append("\tHops: " + getHopNum());
			NetTopoApp.getApp().getDisplay().asyncExec(new Runnable(){
				public void run() {
					NetTopoApp.getApp().addLog(message.toString());
					NetTopoApp.getApp().refresh();
				}
			});
		}
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
