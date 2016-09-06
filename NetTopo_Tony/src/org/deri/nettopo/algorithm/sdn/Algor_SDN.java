/**
 * 
 */
package org.deri.nettopo.algorithm.sdn;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.sdn.function.SDN_CKN_TPGF_ConnectNeighbors;
import org.deri.nettopo.algorithm.sdn.function.SDN_CKN_TPGF_FindAllPaths;
import org.deri.nettopo.algorithm.sdn.function.SDN_CKN_TPGF_FindOnePath;

/**
 * @author tony
 *
 */
public class Algor_SDN implements Algorithm{

	private AlgorFunc[] functions;

	/**
	 * 
	 */
	public Algor_SDN() {
		// TODO Auto-generated constructor stub
		functions=new AlgorFunc[3];
		functions[0]=new SDN_CKN_TPGF_ConnectNeighbors(this);
		functions[1]=new SDN_CKN_TPGF_FindAllPaths(this);
		functions[2]=new SDN_CKN_TPGF_FindOnePath(this);

	}
	

	/* (non-Javadoc)
	 * @see org.deri.nettopo.algorithm.Algorithm#getFunctions()
	 */
	@Override
	public AlgorFunc[] getFunctions() {
		// TODO Auto-generated method stub
		return functions;
	}

}
