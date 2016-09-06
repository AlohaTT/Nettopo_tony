package org.deri.nettopo.util;

import java.util.Comparator;

import org.deri.nettopo.node.astar.SensorNode_AStar;


public class NodeFComparator implements Comparator<SensorNode_AStar>{
    @Override
    public int compare(SensorNode_AStar o1, SensorNode_AStar o2) {	//С�ڷ��ظ������ǵ���
        if(o1.getF()<o2.getF())
        	return -1;
        else if(o1.getF()>o2.getF())
        	return 1;
        else return 0;
    }
    
}
