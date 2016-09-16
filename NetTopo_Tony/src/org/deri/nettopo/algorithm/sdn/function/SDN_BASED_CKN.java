package org.deri.nettopo.algorithm.sdn.function;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_ConnectNeighbors;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.display.Painter;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.eclipse.swt.internal.cocoa.id;

public class SDN_BASED_CKN implements AlgorFunc {

	private Algorithm algorithm;
	private SDN_CKN_MAIN sdn_Ckn;

	public SDN_BASED_CKN(Algorithm algorithm) {
		this.algorithm = algorithm;
		sdn_Ckn = new SDN_CKN_MAIN();
	}

	public SDN_BASED_CKN() {
		this(null);
	}

	public void run() {
			sdn_Ckn.run();
	}

	public void entry() {
		sdn_Ckn.run();
		final StringBuffer message = new StringBuffer();
		int[] activeSensorNodes = NetTopoApp.getApp().getNetwork().getSensorActiveNodes();
		message.append("k=" + sdn_Ckn.getK() + ", Number of active nodes is:" + activeSensorNodes.length
				+ ", they are: " + Arrays.toString(activeSensorNodes));

		NetTopoApp.getApp().getDisplay().asyncExec(new Runnable() {
			public void run() {
				NetTopoApp.getApp().refresh();
				NetTopoApp.getApp().addLog(message.toString());
			}
		});
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
