package org.deri.nettopo.node;

import java.io.Serializable;
import org.eclipse.swt.graphics.RGB;

public interface VNode extends Serializable {
	public String[] getAttrNames();
	public boolean setAttrValue(String attrName, String value);
	public String getAttrValue(String attrName);
	public String getAttrErrorDesciption();
	public int getID();
	public void setAvailable(boolean available);
	public void setID(int id);
	public RGB getColor();
	public void setColor(RGB rgb);
}
