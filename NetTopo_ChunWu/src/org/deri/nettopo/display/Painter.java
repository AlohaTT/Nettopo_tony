package org.deri.nettopo.display;

import java.util.Vector;

import org.deri.nettopo.util.AttractorField;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.DistrictedArea;
import org.eclipse.swt.graphics.RGB;

public interface Painter {
	public void paintNode(int id);
	public void paintNode(int id, RGB rgb);
	/**
	 * paint the nodes' path
	 * @param path the first element of every single array is the node id
	 * 		  	   Then the followings are the x and y of every position.
	 */
	public void paintMobileNodes(double[][] path);
	public void removeNode(int id);
	public void removeNode(Coordinate c);
	public void paintNodes(int[] nodes);
	public void paintAllNodes();
	public void rePaintAllNodes();
	public void paintNodeFocus(int id);
	public void paintNodeFocus(Coordinate c);
	public void removeNodeFocus(int id);
	public void removeNodeFocus(Coordinate c);
	public void removeAllNodeFocus();
	public void paintConnection(int id1, int id2);
	public void paintConnection(int id1, int id2, RGB rgb);
	public void paintAttractors(AttractorField fields);
	public void removeAttractors(AttractorField fields);
	public void paintDistrictedArea(DistrictedArea area);
	public void paintDistrictedAreas(Vector<DistrictedArea> areas);
	public void removeDistrictedArea(DistrictedArea area);
	public void removeDistrictedAreas(Vector<DistrictedArea> areas);
}
