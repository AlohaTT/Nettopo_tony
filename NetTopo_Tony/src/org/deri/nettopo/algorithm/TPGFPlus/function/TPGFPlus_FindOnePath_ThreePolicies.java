//Ŀǰ���а�
package org.deri.nettopo.algorithm.TPGFPlus.function;

import java.util.*;

import org.eclipse.swt.graphics.RGB;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.app.*;

import org.deri.nettopo.node.tpgf.SensorNode_TPGF;
import org.deri.nettopo.node.tpgf.SourceNode_TPGF;
import org.deri.nettopo.display.*;
import org.deri.nettopo.util.*;
import org.deri.nettopo.algorithm.TPGFPlus.function.TPGFPlus_ConnectNeighbors;  //��

import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.VNode;



public class TPGFPlus_FindOnePath_ThreePolicies implements AlgorFunc {
	private WirelessSensorNetwork wsn;
	private Painter painter;
	private Algorithm algorithm;
	private LinkedList<Integer> path;
//	private ArrayList<Integer> searched;
	private HashSet<Integer> searched;//--------------2011-4-13
	private HashSet<Integer> blockednodes;//--------------2011-4-13
	private boolean canFind;
	
	private SinkNode sink;
	private Coordinate sinkPos;
	private int sinkTR;   //��ȡSink�ڵ��������ꡢͨ�Ű뾶
	private TPGFPlus_ConnectNeighbors func_connectNeighbors;
	
	
	public  TPGFPlus_FindOnePath_ThreePolicies (Algorithm algorithm){
		this.algorithm = algorithm;
	}

	public  TPGFPlus_FindOnePath_ThreePolicies (){
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
		painter = NetTopoApp.getApp().getPainter();
		path = new LinkedList<Integer>();
		searched = new HashSet<Integer>();//--------------2011-4-13
		blockednodes = new HashSet<Integer>();//--------------2011-4-13
		canFind = false;	
		func_connectNeighbors = new TPGFPlus_ConnectNeighbors();
		
		if(NetTopoApp.getApp().isFileModified()){
			wsn.resetAllNodesAvailable();
			NetTopoApp.getApp().setFileModified(false);
		}
		
		func_connectNeighbors.ConnectNeighbors_TPGFPlus(false);

		
		if(wsn!=null){
			Collection<VNode> nodes_source = wsn.getNodes("org.deri.nettopo.node.tpgf.SourceNode_TPGF",true);  
			Collection<VNode> nodes_sink = wsn.getNodes("org.deri.nettopo.node.SinkNode",true);
			
			
			if(nodes_sink.size()>0 && nodes_source.size()>0){
				SourceNode_TPGF node_source = (SourceNode_TPGF)nodes_source.iterator().next(); // retrieve one source node
				sink = (SinkNode)nodes_sink.iterator().next();
				sinkPos = wsn.getCoordianteByID(sink.getID());
				sinkTR = sink.getMaxTR();
				node_source.setAvailable(true);  //SensorNode.setAvailable(boolean available)
				
				if(canReachSink(node_source)){				
					canFind = true;
					if(needPainting){
						/* change the color of the intermediate node on the path���м�ת���ڵ��ɫ */
						for(int i=1;i<path.size()-1;i++){
						}
					
						/* paint the path sorted in LinkedList path �������еĽڵ��ɫ*/
						for(int i=0;i<path.size()-1;i++){
							int id1 = ((Integer)path.get(i)).intValue();
							int id2 = ((Integer)path.get(i+1)).intValue();
							painter.paintConnection(id1, id2, new RGB(185,149,86));
						}
					
						/* Add log info */
						final StringBuffer message = new StringBuffer("Path: ");
						for(int i=path.size()-1;i>=0;i--){
							message.append(path.get(i));
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
				}else{
					if(needPainting)
						NetTopoApp.getApp().getDisplay().asyncExec(new Runnable(){
							public void run() {
								NetTopoApp.getApp().addLog("No more paths.");
							}
						});
				}
			}
		}
		return canFind;
	}
	
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	public boolean canReachSink(SensorNode node){
		if(!node.isAvailable())
			return false;
		searched.add(Integer.valueOf(node.getID()));
		/* If the distance between the current node and sinknode is 
		 * in both nodes' transmission radius, the node can reach the 
		 * sink. Return immediately */
		
		if(InOneHop(node)){
			path.add(Integer.valueOf(sink.getID()));
			path.add(Integer.valueOf(node.getID()));
//			painter.paintNode(node.getID(), new RGB(205,149,86));//------------------------------------2011-4-18
			node.setAvailable(false); // the node cannot be used next time
			return true;
		}
		//------------------------------------����������InTwoHop()-------------------------------------------------
		
		else if(InTwoHop(node)){
			ArrayList<Integer> NeighborsID = Sort(OneHop(node));
//			ArrayList<Integer> NeighborsID = Sort_1(node,OneHop(node),sink.getID());
			for(int i=0;i<NeighborsID.size();i++){
			
				int neighborID = ((Integer)NeighborsID.get(i)).intValue();
			    SensorNode neighbor = (SensorNode)wsn.getNodeByID(neighborID);
			    if(InOneHop(neighbor)&&neighbor.isAvailable()){
			    	path.add(Integer.valueOf(sink.getID()));
					path.add(Integer.valueOf(neighbor.getID()));
				    path.add(Integer.valueOf(node.getID()));
				       
//				    painter.paintNode(node.getID(), new RGB(200,0,0));//------------------------------------2011-4-18
//					painter.paintNode(neighbor.getID(), new RGB(205,149,86));
					node.setAvailable(false); // the node cannot be used next time
					neighbor.setAvailable(false);
					return true;
					}
			}
		}
		
		else if(!InOneHop(node)&&!InTwoHop(node)){	//----------------------------������������---------------------------------------------------------
		
			ArrayList<Integer> BeforeRemove=One_And_TwoHop(node);
			for(int i=0;i<searched.size();i++){
				BeforeRemove.remove(searched.toArray()[i]);
				}
			ArrayList<Integer> NeighborsID = Sort(BeforeRemove);//һ���������ڵ���sinknode�ľ�����������-----2011-9-9
//			ArrayList<Integer> NeighborsID = Sort_1(node,BeforeRemove,sink.getID());    //----2011-9-9
		
		/* Then we search from the neighbor that is most near to sink to the neighbor
		* that is least near to sink */		
		if(!NeighborsID.isEmpty()){
		for(int i=0;i<NeighborsID.size();i++){
			    
				int neighborID = ((Integer)NeighborsID.get(i)).intValue();
				SensorNode neighbor =(SensorNode) wsn.getNodeByID(neighborID);
				
				if(canReachSink(neighbor)&&!blockednodes.contains(neighbor)){//------------------------------------2011-4-18
					if(TwoHop(node).contains(neighborID)&&!AvailablejiaojiNode(node,neighborID).isEmpty()){  	//��X�������ڽڵ㣬��Ҫ�ҵ��ܵ���X����һ�����ڽڵ�Y������Y���ݵ�X		
//						painter.paintNode(neighborID,new RGB(0,250,0));
						
	////////////////////////////////////////////////////////////////////////////////////////////////////////////��Ҫ���Ӳ���						
			
						ArrayList<Integer> RelayNode= Sort_3(node,AvailablejiaojiNode(node,neighborID),neighborID);
						
						for(int j=0;j<RelayNode.size();){
							SensorNode_TPGF hui = (SensorNode_TPGF)wsn.getNodeByID(RelayNode.get(j));
							path.add(hui.getID());
							path.add(Integer.valueOf(node.getID()));
							
	//						painter.paintNode(hui.getID(),new RGB(247,50,245));
	//						painter.paintNode(node.getID(),new RGB(0,250,0));
							
							hui.setAvailable(false);  //-----------------2011-3-31  �¼�
						    node.setAvailable(false);
							return true;
							}
						}
					else if(TwoHop(node).contains(neighborID)&&AvailablejiaojiNode(node,neighborID).isEmpty()){
						blockednodes.add(node.getID());//------------------------------------2011-4-18
						break;
						}
					else if(OneHop(node).contains(neighborID)){//��x��һ���ڽڵ㣬��ֱ�ӽ����鴫�ݸ�X
						path.add(Integer.valueOf(node.getID()));
						node.setAvailable(false); // the node cannot be used next time
						return true;
						}						
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////					 		
					}	
				}	
		}
		else if(NeighborsID.isEmpty()){
			blockednodes.add(node.getID());//------------------------------------2011-4-18
		    return false;}
		}
		
		
//####################################################################################################################·�ɴ���		
		
		
		/*		for(int i=0;i<path.size();i++){
			SensorNode PathNode=(SensorNode)wsn.getNodeByID(path.get(i+1));
			RouteCost.put(new Integer(path.get(i+1)),
					new Double(Twofactors(wsn.getCoordianteByID(path.get(i)).distance(wsn.getCoordianteByID(path.get(i+1))),PathNode.getEnergy())));
		}*/
		
//####################################################################################################################·�ɴ���				
		return false;
		}
	
	
/////////////////////////////////////////// canReachSink()���õķ���////////////////////////////////////////////////////////	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ArrayList<Integer> AvailablejiaojiNode(SensorNode node,int hop_2neighbor){
		ArrayList<Integer> liangtiao=TwoHop(node);
		ArrayList<Integer> yitiao=OneHop(node);
		
		ArrayList<Integer> jiaoji=new ArrayList<Integer>();//---------2011-4-13������������ڴӽ�����ѡȡͨ�������ڽڵ��һ���м�ת���ڵ�
		ArrayList<Integer> Availablejiaojinode=new ArrayList<Integer>();
	    for(int i=0;i<liangtiao.size();i++){
	    	hop_2neighbor = ((Integer) liangtiao.get(i)).intValue();
	    	for(int j=0;j<yitiao.size();j++){
	    		SensorNode_TPGF yitiaoneighbor = (SensorNode_TPGF)wsn.getNodeByID(yitiao.get(j));
	    	    if(OneHop(yitiaoneighbor).contains(hop_2neighbor)){jiaoji.add(yitiaoneighbor.getID());}
	    	    }
	    	}
	    for(int j=0;j<jiaoji.size();j++){
	    	SensorNode_TPGF jiaojinode = (SensorNode_TPGF)wsn.getNodeByID((Integer) jiaoji.toArray()[j]);
	    	if(jiaojinode.isAvailable()) 
	    		     Availablejiaojinode.add(jiaojinode.getID());
	    }
	   return Availablejiaojinode;
	    }
////////////////////////////////��һ�����////////////////////////////////////////////////////////////////////////
	public ArrayList<Integer> Sort_1(SensorNode node,ArrayList<Integer> neighborsID,int neighborID){	//ת����׼һ-----ѡ�����Ŀ�Ľڵ�����Ľڵ�ת��	
		/* Then we sort the neighbor list into distance ascending order ����*/
			for(int i=neighborsID.size()-1;i>0;i--){
				for(int j=0; j<i; j++){
					Coordinate c = wsn.getCoordianteByID(neighborID);
				    
					int id1 = ((Integer)neighborsID.toArray()[i]).intValue();
					Coordinate c1 = wsn.getCoordianteByID(id1);
					double dis1 = c1.distance(c);
					
					int id2 = ((Integer)neighborsID.toArray()[j+1]).intValue();
					Coordinate c2 = wsn.getCoordianteByID(id2);
					double dis2 = c2.distance(c);
					
					if(dis1>dis2){
						Integer swap = neighborsID.get(j);
						neighborsID.set(j, neighborsID.get(j+1));
						neighborsID.set(j+1, swap);
						}
					}
				}
				return neighborsID;
				}	
////////////////////////////////��һ�����////////////////////////////////////////////////////////////////////////	
	
	
////////////////////////////////�ڶ������////////////////////////////////////////////////////////////////////////
	private double TwoHopDistance(Coordinate currentnode,Coordinate onehopnode,Coordinate twohopnode){   //��������
		return currentnode.distance(onehopnode)+onehopnode.distance(twohopnode);
	}
		
	public ArrayList<Integer> Sort_2(SensorNode node,ArrayList<Integer> neighborsID,int neighborID){	//ת����׼��-----���·��	
			for(int i=neighborsID.size()-1;i>0;i--){
				for(int j=0; j<i; j++){
					Coordinate a = wsn.getCoordianteByID(node.getID());
					Coordinate c = wsn.getCoordianteByID(neighborID);
				   
					int id1 = ((Integer)neighborsID.toArray()[i]).intValue();
					Coordinate b1 = wsn.getCoordianteByID(id1);
					
					int id2 = ((Integer)neighborsID.toArray()[j+1]).intValue();
					Coordinate b2 = wsn.getCoordianteByID(id2);
					
					if(TwoHopDistance(a,b1,c)>TwoHopDistance(a,b2,c)){
						Integer swap = neighborsID.get(j);
						neighborsID.set(j, neighborsID.get(j+1));
						neighborsID.set(j+1, swap);
						}
					}
				}
				return neighborsID;
	}	
////////////////////////////////�ڶ������////////////////////////////////////////////////////////////////////////
	
	
////////////////////////////////���������////////////////////////////////////////////////////////////////////////
	public double Twofactors(double twohopdistance,double resienergy){
		//return -0.6*twohopdistance+0.4*resienergy;
		return twohopdistance*resienergy;//ֵԽ��Խ��
	}
	
	
	public ArrayList<Integer> Sort_3(SensorNode node,ArrayList<Integer> neighborsID,int neighborID){//ת����׼��-----·�ɴ���
		for(int i=neighborsID.size()-1;i>0;i--){
			for(int j=0; j<i; j++){
				Coordinate a = wsn.getCoordianteByID(node.getID());
				Coordinate c = wsn.getCoordianteByID(neighborID);
				
				int id1 = ((Integer)neighborsID.toArray()[i]).intValue();
				SensorNode id_1=(SensorNode)wsn.getNodeByID(id1);//_TPGF
			    Coordinate b1 = wsn.getCoordianteByID(id1);
//			    id_1.setEnergy((int) (100*Math.random()));//��������
//			    id_1.setEnergy(90);
			    int id1_energy=id_1.getEnergy();
			    
//			    System.out.println("���е���3    ��ȡ��ǰ�ڵ�����  id1_energy "+id1_energy);
			    double value_id1=Twofactors(TwoHopDistance(a,b1,c),id1_energy);
			    
			    
			    int id2 = ((Integer)neighborsID.toArray()[j]).intValue();
			    SensorNode id_2=(SensorNode)wsn.getNodeByID(id2);//_TPGF
			    Coordinate b2 = wsn.getCoordianteByID(id2);
//			    id_2.setEnergy((int) (100*Math.random()));//��������
//			    id_2.setEnergy(90);
			    int id2_energy=id_2.getEnergy();
//			    System.out.println("���е���3    ��ȡ��ǰ�ڵ�����   id2_energy "+id2_energy);
			    double value_id2=Twofactors(TwoHopDistance(a,b2,c),id2_energy);
			    
			    if(value_id1<value_id2){//ȡ��ֵ
					Integer swap = neighborsID.get(j);
					neighborsID.set(j, neighborsID.get(j+1));
					neighborsID.set(j+1, swap);
					}
			    }
			}
		return neighborsID;
	}	
////////////////////////////////���������////////////////////////////////////////////////////////////////////////

	
	
	/*��1-hop��2-hop�ڵ�ֱ����2��Integer[]��,����1-hop��2-hop�ڵ��Ϸ���1��Integer[]��*/
	
	public ArrayList<Integer> OneHop(SensorNode node){     //���һ���ھӽڵ�
	
		Integer[] hop_1=func_connectNeighbors.getNeighbors(node.getID());
		ArrayList<Integer> onehopneighbors = new ArrayList<Integer>();
		for(int i=0;i<hop_1.length;i++){
			onehopneighbors.add(hop_1[i]);
		}
		return onehopneighbors;
	}
	
	public ArrayList<Integer> TwoHop(SensorNode node){     //��������ھӽڵ�
		Integer[] hop_2=func_connectNeighbors.getNeighborsOf2Hops(node.getID());
		ArrayList<Integer> twohopneighbors = new ArrayList<Integer>();
		for(int i=0;i<hop_2.length;i++){
			twohopneighbors.add(hop_2[i]);
		}
		return twohopneighbors;
	}
	public ArrayList<Integer> One_And_TwoHop(SensorNode node){  //�������һ���������ھӽڵ�ļ��ϣ������ظ��ڵ�
		Integer[] neighbors_1=func_connectNeighbors.getNeighbors(node.getID());
		Integer[] neighbors_2=func_connectNeighbors.getNeighborsOf2Hops(node.getID());
		ArrayList<Integer> one_and_two= new ArrayList<Integer>();
		
		for(int i=0;i<neighbors_1.length;i++){
			one_and_two.add(neighbors_1[i]);
		}
		for(int j=0;j<neighbors_2.length;j++){
			one_and_two.add(neighbors_2[j]);
		}
//*********************************************************************************2011-4-7  ��	��ȥ�ظ��ڵ�
		HashSet<Integer> hashSet = new HashSet<Integer>(one_and_two);   
		ArrayList<Integer> one_and_two_hui= new ArrayList<Integer>(hashSet);
//*********************************************************************************2011-4-7  ��
    	return one_and_two_hui;
	}

	/**
	 * 
	 * @param ArrayList<Integer> neighborsID
	 * @return neighborsID
	 */
	/* If the current node is not one-hop from sink, it search it's
	 * neighbor that is most near to sink and find out whether it can
	 * reach the sink. If not, it searches its' neighbor that is second
	 * most near to sink and go on, etc. The neighbors do not include 
	 * any already searched node that is not in one hop  ��һ���������ڵ������б��������*/
	public ArrayList<Integer> Sort(ArrayList<Integer> neighborsID){			
	/* First we remove all searched node id in the neighbor list */
/*		for(int i=0;i<searched.size();i++){
			neighborsID.remove(searched.get(i));
			}	*/
	/* Then we sort the neighbor list into distance ascending order ����*/
		for(int i=neighborsID.size()-1;i>0;i--){
			for(int j=0; j<i; j++){
				int id1 = ((Integer)neighborsID.get(j)).intValue();
				Coordinate c1 = wsn.getCoordianteByID(id1);
				double dis1 = c1.distance(sinkPos);
				int id2 = ((Integer)neighborsID.get(j+1)).intValue();
				Coordinate c2 = wsn.getCoordianteByID(id2);
				double dis2 = c2.distance(sinkPos);
				if(dis1>dis2){
					Integer swap = neighborsID.get(j);
					neighborsID.set(j, neighborsID.get(j+1));
					neighborsID.set(j+1, swap);
					}
				}
			}
			return neighborsID;
			}	
	/**
	 * 
	 * @param node
	 * @return
	 */
	
	public boolean InOneHop(SensorNode node){
		int nodeID = node.getID();
		Coordinate c = wsn.getCoordianteByID(nodeID);
		int tr = node.getMaxTR();
		double distance = 0;
		distance = (double)((c.x-sinkPos.x)*(c.x-sinkPos.x) + (c.y-sinkPos.y)*(c.y-sinkPos.y) + (c.z-sinkPos.z)*(c.z-sinkPos.z));
		distance = Math.sqrt(distance);
		if(distance<=tr && distance<= sinkTR)
			return true;
		return false;
	}	

	public boolean InTwoHop(SensorNode node){    
		if(TwoHop(node).contains(sink.getID())){    
			return true;
			}
		return false;
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
