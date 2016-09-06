package test;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.astar.Algor_AStar;
import org.deri.nettopo.algorithm.astar.function.Connect_Delauany;
import org.deri.nettopo.algorithm.astar.function.Connect_Graphic;
import org.deri.nettopo.algorithm.astar.function.Connect_LDelauany;
import org.deri.nettopo.algorithm.astar.function.NodeProbabilistic;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_Planarization_BoundaryArea;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_Planarization_GG;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_Planarization_RNG;
import org.deri.nettopo.app.NetTopoApp;
import org.deri.nettopo.gas.Gas;
import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.node.SinkNode;
import org.deri.nettopo.node.astar.SensorNode_Graphic;
import org.deri.nettopo.node.tpgf.SensorNode_TPGF;
import org.deri.nettopo.util.Coordinate;
import org.deri.nettopo.util.DuplicateCoordinateException;

/**
 * @author lizhen
 *
 */
public class StasTest_Parallel {
	private static final int SEEDNUM = 100;
	private static final int SENSOR_NODE_NUM = 1000;
	private static final int NET_LENGHT = 600;
	private static final int NET_WIDTH = 600;
	private static final int SENSORS_STEPS = 100;
	private static final int TR = 60;
	private static final int MAXTR = 90;
	private static final double MAY = 20;

	private WirelessSensorNetwork wsn;
	private Coordinate netsize;

	public StasTest_Parallel() {
		wsn = new WirelessSensorNetwork();
		netsize = new Coordinate(NET_LENGHT, NET_WIDTH, 0);
		if (netsize.x < 50 || netsize.y < 50) {
			netsize = new Coordinate(50, 50, 0);
		}
		wsn.setSize(netsize);
		NetTopoApp.getApp().setNetwork(wsn);
	}

	@SuppressWarnings("unchecked")
	//mos ���ڽ�����࣬GGͼ���ݷ���GG�����ļ�
	public void run(OutputCollector<Text, NullWritable> output, MultipleOutputs mos,String Gtype,Reporter reporter)
			throws DuplicateCoordinateException, IOException, InterruptedException {
		Text txtkey = new Text();
		double A_rng, A_gg, A_del, A_ldel, A_yg, m;
		double A_rng_1, A_gg_1, A_del_1, A_ldel_1, A_yg_1;
		int i, j, r;
		if (Gtype.equals("GG")) {
			for (r = 60; r <= MAXTR; r += 5) {// gas�뾶��60������90��ÿ�μ�5��
				for (i = 100; i <= SENSOR_NODE_NUM; i += SENSORS_STEPS) {// �ڵ�����100-1000��ÿ�μ�100
					for (j = 1; j <= SEEDNUM; j++) {// �������100�Σ�100�����˽ṹ
						Coordinate[] coordinates = getCoordinates(j, i);
						for (m = 5; m <= MAY; m += 5) {// ���ýڵ����ʧЧ����
							if (wsn.getAllNodes() != null) {
								wsn.deleteAllNodes();
								wsn.deleteAllGas();
							}
							SinkNode sink = new SinkNode();							
							sink.setMaxTR(TR);
							wsn.addNode(sink, new Coordinate(netsize.x - 1,
									netsize.y - 1, 0));							
							for (int l = 0; l < coordinates.length; l++) {
								SensorNode_TPGF sen = new SensorNode_TPGF();
								sen.setMaxTR(TR);
								wsn.addNode(sen, coordinates[l]);
							}
							Gas gas = new Gas();
							gas.setRadius(r);
							wsn.addGas(gas, new Coordinate(netsize.x / 2,
									netsize.y / 2, 0));
							Algor_AStar tpgf = new Algor_AStar();
							AlgorFunc[] functions = tpgf.getFunctions();
							TPGF_Planarization_GG gg = (TPGF_Planarization_GG) functions[1];
							NodeProbabilistic np = (NodeProbabilistic) functions[6];
							TPGF_Planarization_BoundaryArea tpa = (TPGF_Planarization_BoundaryArea) functions[0];
							gg.Planarization("GG", false);
							tpa.Planarization(false);
							A_gg = tpa.getArea();
							// ʧЧ
							np.setMAY(m / 100);
							for (int t = 0; t < 100; t++) {// ͬһ���������100�Σ�����100�ֲ�ͬ�ڵ�ʧЧ���
								np.CalculateNodeProbabilistic(false);
								gg.Planarization("GG", false);
								tpa.Planarization(false);
								A_gg_1 = tpa.getArea();
								// ��Ϊmap
								//ͼ����/60-90 gas�뾶/100-1000�ڵ���/5-20���ʰٷֱ�/ԭ���/�仯�����
								output=mos.getCollector(Gtype, reporter);
								txtkey.set(Gtype + "\t" + r + "\t" + i + "\t" + m+"\t"+A_gg + "\t" + A_gg_1);
								
								output.collect(txtkey, NullWritable.get());
							}

						}
					}
				}
			}

		}
		if (Gtype.equals("RNG")) {
			for (r = 60; r <= MAXTR; r += 5) {// gas�뾶��60������90��ÿ�μ�5
				for (i = 100; i <= SENSOR_NODE_NUM; i += SENSORS_STEPS) {// �ڵ�����100-1000��ÿ�μ�100
					for (j = 1; j <= SEEDNUM; j++) {// �������100�Σ�100�����˽ṹ
						Coordinate[] coordinates = getCoordinates(j, i);
						for (m = 5; m <= MAY; m += 5) {// ���ýڵ����ʧЧ����
							if (wsn.getAllNodes() != null) {
								wsn.deleteAllNodes();
								wsn.deleteAllGas();
							}
							SinkNode sink = new SinkNode();							
							sink.setMaxTR(TR);
							wsn.addNode(sink, new Coordinate(netsize.x - 1,
									netsize.y - 1, 0));							
							for (int l = 0; l < coordinates.length; l++) {
								SensorNode_TPGF sen = new SensorNode_TPGF();
								sen.setMaxTR(TR);
								wsn.addNode(sen, coordinates[l]);
							}
							Gas gas = new Gas();
							gas.setRadius(r);
							wsn.addGas(gas, new Coordinate(netsize.x / 2,
									netsize.y / 2, 0));
							Algor_AStar tpgf = new Algor_AStar();
							AlgorFunc[] functions = tpgf.getFunctions();
							TPGF_Planarization_RNG rng = (TPGF_Planarization_RNG) functions[2];
							NodeProbabilistic np = (NodeProbabilistic) functions[6];
							TPGF_Planarization_BoundaryArea tpa = (TPGF_Planarization_BoundaryArea) functions[0];
							rng.Planarization("RNG", false);
							tpa.Planarization(false);
							A_rng = tpa.getArea();
							// ʧЧ
							np.setMAY(m / 100);
							for (int t = 0; t < 100; t++) {// ͬһ���������100�Σ�����100�ֲ�ͬ�ڵ�ʧЧ���
								np.CalculateNodeProbabilistic(false);
								rng.Planarization("RNG", false);
								tpa.Planarization(false);
								A_rng_1 = tpa.getArea();
								// ��Ϊmap
								//60-90 gas�뾶/100-1000�ڵ���/100������/5-20���ʰٷֱ�/ԭ���/�仯�����
								output=mos.getCollector(Gtype, reporter);
								txtkey.set(Gtype + "\t" + r + "\t" + i + "\t" + m+"\t"+A_rng + "\t" + A_rng_1);
								output.collect(txtkey, NullWritable.get());
							}
						}
					}
				}
			}
		}
		if (Gtype.equals("DEL")) {
			for (r = 60; r <= MAXTR; r += 5) {// gas�뾶��60������90��ÿ�μ�5
				for (i = 100; i <= SENSOR_NODE_NUM; i += SENSORS_STEPS) {// �ڵ�����100-1000��ÿ�μ�100
					for (j = 1; j <= SEEDNUM; j++) {// �������100�Σ�100�����˽ṹ
						Coordinate[] coordinates = getCoordinates(j, i);
						for (m = 5; m <= MAY; m += 5) {// ���ýڵ����ʧЧ����
							if (wsn.getAllNodes() != null) {
								wsn.deleteAllNodes();
								wsn.deleteAllGas();
							}
							SinkNode sink = new SinkNode();	
							sink.setMaxTR(TR);
							wsn.addNode(sink, new Coordinate(netsize.x - 1,
									netsize.y - 1, 0));
							for (int l = 0; l < coordinates.length; l++) {
								SensorNode_TPGF sen = new SensorNode_TPGF();
								sen.setMaxTR(TR);
								wsn.addNode(sen, coordinates[l]);
							}
							Gas gas = new Gas();
							gas.setRadius(r);
							wsn.addGas(gas, new Coordinate(netsize.x / 2,
									netsize.y / 2, 0));
							Algor_AStar tpgf = new Algor_AStar();
							AlgorFunc[] functions = tpgf.getFunctions();
							Connect_Delauany del = (Connect_Delauany) functions[4];
							NodeProbabilistic np = (NodeProbabilistic) functions[6];
							TPGF_Planarization_BoundaryArea tpa = (TPGF_Planarization_BoundaryArea) functions[0];
							del.connect(false);
							tpa.Planarization(false);
							A_del = tpa.getArea();
							// ʧЧ
							np.setMAY(m / 100);
							for (int t = 0; t < 100; t++) {// ͬһ���������100�Σ�����100�ֲ�ͬ�ڵ�ʧЧ���
								np.CalculateNodeProbabilistic(false);
								del.connect(false);
								tpa.Planarization(false);
								A_del_1 = tpa.getArea();
								// ��Ϊmap
								//60-90 gas�뾶/100-1000�ڵ���/100������/5-20���ʰٷֱ�/ԭ���/�仯�����
								output=mos.getCollector(Gtype, reporter);
								txtkey.set(Gtype + "\t" + r + "\t" + i + "\t" + m+"\t"+A_del + "\t" + A_del_1);
								output.collect(txtkey, NullWritable.get());
							}

						}
					}
				}
			}

		}
		if (Gtype.equals("LDEL")) {
			for (r = 60; r <= MAXTR; r += 5) {// gas�뾶��60������90��ÿ�μ�5
				for (i = 100; i <= SENSOR_NODE_NUM; i += SENSORS_STEPS) {// �ڵ�����100-1000��ÿ�μ�100
					for (j = 1; j <= SEEDNUM; j++) {// �������100�Σ�100�����˽ṹ
						Coordinate[] coordinates = getCoordinates(j, i);
						for (m = 5; m <= MAY; m += 5) {// ���ýڵ����ʧЧ����
							if (wsn.getAllNodes() != null) {
								wsn.deleteAllNodes();
								wsn.deleteAllGas();
							}
							SinkNode sink = new SinkNode();		
							sink.setMaxTR(TR);
							wsn.addNode(sink, new Coordinate(netsize.x - 1,
									netsize.y - 1, 0));							
							for (int l = 0; l < coordinates.length; l++) {
								SensorNode_TPGF sen = new SensorNode_TPGF();
								sen.setMaxTR(TR);
								wsn.addNode(sen, coordinates[l]);
							}
							Gas gas = new Gas();
							gas.setRadius(r);
							wsn.addGas(gas, new Coordinate(netsize.x / 2,
									netsize.y / 2, 0));
							Algor_AStar tpgf = new Algor_AStar();
							AlgorFunc[] functions = tpgf.getFunctions();
							Connect_LDelauany ldel = (Connect_LDelauany) functions[5];
							NodeProbabilistic np = (NodeProbabilistic) functions[6];
							TPGF_Planarization_BoundaryArea tpa = (TPGF_Planarization_BoundaryArea) functions[0];
							ldel.del(false);
							tpa.Planarization(false);
							A_ldel = tpa.getArea();
							// ʧЧ
							np.setMAY(m / 100);
							for (int t = 0; t < 100; t++) {// ͬһ���������100�Σ�����100�ֲ�ͬ�ڵ�ʧЧ���
								np.CalculateNodeProbabilistic(false);
								ldel.del(false);
								tpa.Planarization(false);
								A_ldel_1 = tpa.getArea();
								// ��Ϊmap
								//60-90 gas�뾶/100-1000�ڵ���/100������/5-20���ʰٷֱ�/ԭ���/�仯�����
								output=mos.getCollector(Gtype, reporter);
								txtkey.set(Gtype + "\t" + r + "\t" + i + "\t" + m+"\t"+A_ldel + "\t" + A_ldel_1);
								output.collect(txtkey, NullWritable.get());
							}

						}
					}
				}
			}
		}
		if (Gtype.equals("YG")) {
			for (r = 60; r <= MAXTR; r += 5) {// gas�뾶��60������90��ÿ�μ�5
				for (i = 100; i <= SENSOR_NODE_NUM; i += SENSORS_STEPS) {// �ڵ�����100-1000��ÿ�μ�100
					for (j = 1; j <= SEEDNUM; j++) {// �������100�Σ�100�����˽ṹ
						Coordinate[] coordinates = getCoordinates(j, i);
						for (m = 5; m <= MAY; m += 5) {// ���ýڵ����ʧЧ����
							if (wsn.getAllNodes() != null) {
								wsn.deleteAllNodes();
								wsn.deleteAllGas();
							}
							SinkNode sink = new SinkNode();
							sink.setMaxTR(TR);
							wsn.addNode(sink, new Coordinate(netsize.x - 1,
									netsize.y - 1, 0));
							for (int l = 0; l < coordinates.length; l++) {
								SensorNode_Graphic sen = new SensorNode_Graphic();
								sen.setMaxTR(TR);
								wsn.addNode(sen, coordinates[l]);
							}
							Gas gas = new Gas();
							gas.setRadius(r);
							wsn.addGas(gas, new Coordinate(netsize.x / 2,
									netsize.y / 2, 0));
							Algor_AStar tpgf = new Algor_AStar();
							AlgorFunc[] functions = tpgf.getFunctions();
							Connect_Graphic yg = (Connect_Graphic) functions[3];
							NodeProbabilistic np = (NodeProbabilistic) functions[6];
							TPGF_Planarization_BoundaryArea tpa = (TPGF_Planarization_BoundaryArea) functions[0];
							yg.connect(false);
							tpa.Planarization(false);
							A_yg = tpa.getArea();
							// ʧЧ
							np.setMAY(m / 100);
							for (int t = 0; t < 100; t++) {// ͬһ���������100�Σ�����100�ֲ�ͬ�ڵ�ʧЧ���
								np.CalculateNodeProbabilistic(false);
								yg.connect(false);
								tpa.Planarization(false);
								A_yg_1 = tpa.getArea();
								// ��Ϊmap
								output=mos.getCollector(Gtype, reporter);
								txtkey.set(Gtype + "\t" + r + "\t" + i + "\t" + m+"\t"+A_yg + "\t" + A_yg_1);
								output.collect(txtkey, NullWritable.get());
							}

						}
					}
				}

			}

		}
	}

	// �����������
	public Coordinate[] getCoordinates(int seed, int nodeNum) {
		Coordinate[] coordinates = new Coordinate[nodeNum];
		Coordinate displaySize = wsn.getSize();

		Random random = new Random(seed);

		for (int n = 0; n < coordinates.length; n++) {// �ڵ���

			coordinates[n] = new Coordinate(random.nextInt(displaySize.x),
					random.nextInt(displaySize.y), 0);
			for (int j = 0; j < n; j++) {
				if (coordinates[j].equals(coordinates[n])) {
					n--;
					break;
				}
			}
			if (wsn.hasDuplicateCoordinate(coordinates[n])) {
				n--;
			}
		}
		return coordinates;
	}

}
