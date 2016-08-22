package org.deri.nettopo.algorithm.dvhop.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.dvhop.Algor_DVHOP;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SensorNode;
import org.deri.nettopo.node.VNode;
import org.deri.nettopo.node.localization.DVHopNode;
import org.deri.nettopo.node.localization.dvhop.AnchorNode;
import org.deri.nettopo.node.localization.dvhop.HopItem;
import org.deri.nettopo.node.localization.dvhop.UnknownNode;
import org.deri.nettopo.util.Coordinate;

public class DVHOP_Run_DVHop implements AlgorFunc{


	private Algorithm algorithm;
	private long isOver;//�������Ҫ�㲥��hopitem��ô+1,�㲥��һ��-1������ʶ�Ƿ�㲥����
	DVHopNode[] nodes;
	Hashtable<Integer, Integer> nodeIndex;
	
	public DVHOP_Run_DVHop(Algorithm algorithm){
		this.algorithm = algorithm;
		this.isOver=0;
		nodeIndex=((Algor_DVHOP)(this.algorithm)).getNodeIndex();
		((Algor_DVHOP)(this.algorithm)).setNodes(nodes);
	}
	
	public DVHOP_Run_DVHop(){
		this.algorithm = null;
		this.isOver=0;
		nodeIndex=((Algor_DVHOP)(this.algorithm)).getNodeIndex();
		((Algor_DVHOP)(this.algorithm)).setNodes(nodes);
	}
	
	public Algorithm getAlgorithm(){
		return this.algorithm;
	}
	
	private Collection<VNode> getActiveSensorNode(Collection<VNode> sensorNodes){
		Collection<VNode> result =  new LinkedList<VNode>();
		Iterator<VNode> iter = sensorNodes.iterator();
		while(iter.hasNext()){
			SensorNode node = (SensorNode)iter.next();
			if(node.isActive()){
				result.add(node);
			}
		}
		return result;
	}
	
	//@Override
	public void run() {
		final NetTopoApp app = NetTopoApp.getApp();
		WirelessSensorNetwork wsn = app.getNetwork();
		
		Collection<VNode> DVnodes=getDVNodes(wsn);
		nodes = new DVHopNode[DVnodes.size()];
		
		
		if(nodes.length>0){
			nodes = (DVHopNode[])DVnodes.toArray(nodes);
			
			//Ϊ��ǰ�������е��㷨��������wsn�Ľڵ㸱��
			((Algor_DVHOP)(this.algorithm)).setNodes(nodes);
			
			/*Ϊ����wsn�Ľڵ㸱��׼������������ʼ��anchor�ڵ�*/
			InitNodeIndex();
			InitAnchors();
			
			/**
			 * �㲥��������ʼ
			 * */
			while(isOver>0){
				for(int i=0;i<nodes.length;i++){
					//��ÿһ���ڵ�i
					Hashtable<Integer,HopItem> hopTable=nodes[i].getHopTable();
					Enumeration<HopItem> items=hopTable.elements();
					/*dc is for debug 
					//int dc=0;
					Enumeration<HopItem> di=hopTable.elements();
					while(di.hasMoreElements())
						System.out.println("node "+nodes[i].getID()
								+":"+di.nextElement().toString());*/
					
					while(items.hasMoreElements()){
						//��i��ÿһ��HopItem
						HopItem it=items.nextElement();
						if(it.isCasted()==false){
							HopItem tmp=it.Copy();
							tmp.HopsIncrease();//������1
							//tmp.setCasted(false);
							BroardCast(tmp,nodes[i].getNeighbors(),i);
							it.setCasted(true);
							isOver--;
						}
						//dc++;
						//System.out.println("node "+nodes[i].getID()+" "+it.toString());
						//System.out.println("\tisOver is "+isOver+"");
					}
					//for debug
					/*System.out.println("Scan node "+nodes[i].getID()+" over,handle "
							+dc+" HopItems");
					Enumeration<HopItem> dii=hopTable.elements();
					while(dii.hasMoreElements()){
						System.out.println("node "+nodes[i].getID()
								+":"+dii.nextElement().toString());
					}*/
				}
			}
			/*for debug*/
			System.out.println("end of sending hop package");
			//end �㲥����
			
			/**
			 * dv-hop�ڶ��׶ο�ʼ
			 */
			AnchorsColculateDPH();//anchors����ƽ��ÿ������DPH
			
			BroadcastDPHs();
			
			System.out.println("dv-hop�ڶ��׶����");
			
			/**
			 * dv-hop�����׶�
			 */
			if(nodes.length>0){
				for(int i=0;i<nodes.length;i++){
					if(nodes[i] instanceof UnknownNode){
						UnknownNode unode=(UnknownNode)nodes[i];
						Coordinate c=unode.DeduceCoordinate();
						System.out.println("Unknown Node "+unode.getID()
								+":("+c.x+","+c.y+")");
					}
				}
			}//end if
		}
	}
	
	/**
	 * ��neighbors�е�ÿһ���ڵ㷢��HopItem
	 * */
	private void BroardCast(HopItem item,ArrayList<Integer> neighbors,int srcNode){
		for(int i=0;i<neighbors.size();i++){
			int index = nodeIndex.get(neighbors.get(i));
			DVHopNode nei=nodes[index];
			/*for debug
			System.out.println("node "+nodes[srcNode].getID()
					+" send to its neighbor "+nei.getID()+": ");*/
			/**
			 * һ��Ҫʹ�ö�ݿ������ھ�ÿ��һ�����������ô��ݻ�ʹ��
			 * �ھ�֮һ�޸�������е�һ�����ԣ������ھӵĸ�������Ҳ����֮�仯
			 * */
			long inum=nei.HandleHopItem(item.Copy());
			isOver+=inum;
			
		}
	}
	
	/**
	 * ��ʼ��anchor�ڵ��hopTable����ӵ�һ��������Ա㽫��Ϊ���������ͳ�ȥ
	 * */
	private void InitAnchors(){
		
		//AnchorNode[] nodes = new AnchorNode[anchors.size()];
		if(nodes.length>0){
			for(int i=0;i<nodes.length;i++){
				if(nodes[i] instanceof AnchorNode){
					Hashtable<Integer,HopItem> ht=nodes[i].getHopTable();
					HopItem e = new HopItem(nodes[i].getID(),0,nodes[i].getCoordinate());
					ht.clear();
					ht.put(nodes[i].getID(),e);
					isOver++;
					//for debug
					System.out.println("Anchor:"+nodes[i].toString()
							+" add HopItem->"+e.toString());
				}
			}//end for
		}//end if
		System.out.println("\tisOver is set to "+isOver+"\n");
	}
	
	/*�õ�anchor�ڵ��unknown�ڵ�*/
	private Collection<VNode> getDVNodes(WirelessSensorNetwork wsn){
		Collection<VNode> dvnodes = 
			wsn.getNodes("org.deri.nettopo.node.localization.DVHopNode",true);
		dvnodes = getActiveSensorNode(dvnodes);
		return dvnodes;
	}
	
	/***
	 * Ϊ��Ա����nodes�������������ڲ��ҵ�idΪnodes[i].getID()�Ľڵ���nodes���index(i)
	 * */
	private void InitNodeIndex(){
		nodeIndex.clear();
		for(int i=0;i<nodes.length;i++){
			nodeIndex.put(nodes[i].getID(), i);
		}
	}
	
	/**
	 *dv-hop�ڶ��׶����е�anchor�ڵ� ����distance per hop(DPH)
	 */
    private void AnchorsColculateDPH(){
		
		if(nodes.length>0){
			for(int i=0;i<nodes.length;i++){
				if(nodes[i] instanceof AnchorNode){
					((AnchorNode)nodes[i]).CalculateDisPerHop(algorithm);
					//for debug
					System.out.println("Anchor "+nodes[i].getID()+" : "
							+((AnchorNode)nodes[i]).getDisPerHop());
				}
			}
		}//end if
	}
    /**
     * dv-hop�ڶ��׶Σ�anchor�㲥DPH���������д���
     * */
    private void BroadcastDPHs(){
    	
    	int DPHcount=0;//Ϊ�Ѿ�ӵ��DPH�Ľڵ�����������е�
    	
    	/*���������е�anchor���Լ���DPH�㲥����anchor�ھ�*/
    	if(nodes.length>0){
			for(int i=0;i<nodes.length;i++){
				if(nodes[i] instanceof AnchorNode){
					DPHcount++;//anchors�������DPH
					AnchorNode anchor=(AnchorNode)nodes[i];
					for(int j=0;j<anchor.getNeighbors().size();j++){
						int neiId=anchor.getNeighbors().get(j);
						DVHopNode nei=(DVHopNode)(nodes[nodeIndex.get(neiId)]);
						if(nei instanceof UnknownNode){
							DPHcount+=((UnknownNode) nei).HandleDPH(anchor.getDisPerHop());
						}//end if nei instanceof UnknownNode
					}//endo for
				}//end if
			}
		}//end
    	
    	//for debug
    	System.out.println("anchors has cast their DPHs to Unknown " +
    			"neighbors & DPHcount is "+DPHcount);
    	
    	/*��anchor��unknown�ھӽڵ������������д���DPH��*/
    	while(DPHcount<nodes.length){
    		for(int i=0;i<nodes.length;i++){
    			if(nodes[i] instanceof UnknownNode){
    				UnknownNode unode=(UnknownNode)nodes[i];
    				if(unode.isRecievedDPH()&&(unode.isCastDPH()==false)){
    					for(int j=0;j<unode.getNeighbors().size();j++){
    						DVHopNode nei=(DVHopNode)
    							nodes[nodeIndex.get(unode.getNeighbors().get(j))];
    						if(nei instanceof UnknownNode){
    							DPHcount+=((UnknownNode) nei).
    								HandleDPH(unode.getDisPerHop());
    						}//end if ��δ֪�ڵ�
    					}//end for �����ھ�
    					unode.setCastDPH(true);
    				}
    			}//end if
    		}
    	}
    }

}
