package member;

import impl.ConvertImpl;
import impl.SlavesComparator;
import impl.WSNComparator;
import intf.ProxyInterface;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.stream.FileImageOutputStream;

import org.deri.nettopo.network.WirelessSensorNetwork;
import org.deri.nettopo.util.Property;
import bean.Information;

public class Master {
	/**
	 * @param args
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */

	/**
	 * Masterֻ��һ��ʵ��
	 */
	private String topoTypeAndFunction;
	private boolean allTasksDistributed = false;
	private List<WirelessSensorNetwork> tasks = null;
	private List<Information> list = null; // slave ��Ϣ�б�
	private List<String> mayDeadSlavesIp = null;// ������slaves ip �б�
	private ProxyInterface pi = null;

	// private ProxyInterface px =null;

	public Master(List<WirelessSensorNetwork> tasks) {
		super();
		this.tasks = tasks;
		System.out.println("tasks:" + tasks.size());
	}

	public String getTopoTypeAndFunction() {
		return topoTypeAndFunction;
	}

	public void setTopoTypeAndFunction(String topoTypeAndFunction) {
		this.topoTypeAndFunction = topoTypeAndFunction;
	}

	public Master() {
		super();
	}

	public void setTasksList(List<WirelessSensorNetwork> tasks) {
		this.tasks = tasks;
	}

	public void initialize() throws RemoteException, MalformedURLException,
			AlreadyBoundException, InterruptedException {
		// System.setSecurityManager(new RMISecurityManager());

		// tasks=inputTasks;
		if (pi == null) {
			pi = new ConvertImpl(); // Զ�̵�����

			// �󶨵��������ϣ���ʵ�Ƿŵ��������б���
			LocateRegistry.createRegistry(1099);
			Naming.bind("hello", pi);

		}

		// c=new ConvertImpl(1);
		// Naming.rebind("hello", c);

	}



	public void runMaster() throws Exception {// ���з���

		initialize(); // ��pi��
		// startBeat(); // ��ʼ�������,����timer�߳�
		heartBeatverity(); // 2s���һ��
	}

	public boolean compute() throws Exception // ����������������ռ�����ȹ��̣��������Ӧ�����ڵ��
											// ���水ť֮��Ҫ���ġ�
	{	
		boolean isComplete = false;
		if (!isPrepared()) {
			return false;
		}
		
		
		File file=new File("./result.txt"); 	//�������ļ����ڣ���ɾ��
		if(file.exists())
			file.delete();
		
		while (!isComplete) {
			System.out
					.println("-------------------------------------start--------------");
			distributeTasks();
			isComplete = collectAllResultAndResetStatus();
			System.out.println("is Complete?  " + isComplete);
			System.out
					.println("-------------------------------------end--------------");
			Thread.sleep(1000); // ��1s��ѯ��
		}

		System.out.println("end in master");
		isComplete = false;
		return true;
	}

	public boolean isPrepared() // �ж��Ƿ�׼���������
	{
		if (list == null || list.size() == 0) {
			System.out.println("û�� slave");
			return false;
		} else if (tasks == null || tasks.size() == 0) {
			System.out.println("û������");
			return false;
		}
		return true;
	}

	public boolean collectAllResultAndResetStatus() throws IOException {
		boolean finish = false;
		Information inf = null;
		getResultAndResetWorking();// �ռ��Ѿ��������Task
		if (!allTasksDistributed) {// �����������û�б�������
			// distributeTasks(list);
			finish = false;
		} else {
			int i = 0;
			if (list == null || list.size() == 0) {
				System.out
						.println("collectAllResultAndResetStatus list is null ");
				return false;
			}
			for (i = 0; i < list.size(); ++i) {
				inf = list.get(i);
				if (inf.isWorking == -1) {
					finish = false;
					break;
				}
			}
			if (i == list.size()) {
				System.out.println("over la");
				finish = true;
			}
		}
		return finish;
	}

	public void heartBeatverity() throws Exception {
		while (true) {
			list = pi.getSlavesList();// ��ȡslaves�б�

			printSlavesIP();// ��ӡslave�б�

			mayDeadSlavesIp = testfyLivingStatus();// ��ȡ����slaves
			printDeadSlavesIP();// ��ӡ����slaves
			deleteDeadSlaves();// ɾ������slaves
			resetLivingStatus();// ��λ����־
			Thread.sleep(2000);
		}
	}

	public void deleteDeadSlaves() throws Exception {// ɾ������slaves
		synchronized (this.list) {
			Information inf = null;
			boolean isAdd = false;
			if (list == null || list.size() == 0 || mayDeadSlavesIp == null
					|| mayDeadSlavesIp.size() == 0)
				return;
			for (String deadIP : mayDeadSlavesIp) {
				for (int i = 0; i < list.size(); ++i) {
					inf = list.get(i);
					if (deadIP.equals(inf.ip)) {
						if (inf.isWorking == 2)// ��������ڵ������Ѿ�����������
						{
							writeResult(inf.result);
							//writeImage(inf.taskName,Util.getImageFromByte(inf.image));//-------------------------------------------------------------------------------------
						
							writeImage(inf.taskName,inf.image);
							list.remove(i);
						} else if (inf.isWorking == 0)
							list.remove(i);
						else {
							tasks.add(byteArrayToObject(inf.runConf)); // ��û�����������������뵽�����б���
							isAdd = true;
							list.remove(i);

						}
						break;
					}
				}
			}
			if (isAdd) {
				Collections.sort(tasks, new WSNComparator());// ��������task
				isAdd = false;
			}

		}
	}

	public void resetLivingStatus()// ������״̬
	{
		Information ls = null;
		if (list == null || list.size() == 0) {
			System.out.println("no slave to reset!");
			return;
		}
		for (int i = 0; i < list.size(); ++i) {
			ls = list.get(i);
			ls.isLive = false;
		}
	}

	public List<String> testfyLivingStatus()// ��������slaves,������IP�б�
	{
		List<String> ls = new ArrayList<String>();
		if (list == null || list.size() == 0) {
			System.out.println("testfyLivingStatus list is null ");
			return null;
		}
		for (Information inf : list) {
			if (!inf.isLive)
				ls.add(inf.ip);
		}
		return ls;
	}

	public void printDeadSlavesIP()// ��ӡ����slaves ip
	{
		if (mayDeadSlavesIp == null || mayDeadSlavesIp.size() == 0) {
			// System.out.println("They are all alive");
			return;
		}

		else {
			for (String str : mayDeadSlavesIp) {
				System.out.println(str + " is dead.");
			}
		}
	}

	public void printSlavesIP()// ��ӡslaves ip
	{
		if (list == null || list.size() == 0)
			System.out.println("No Slaves");
		else {
			for (Information inf : list) {
				System.out.println(inf.toString() + " is in list");

			}
		}
	}

	public void removeSlavesByIP(List<String> IPs, ProxyInterface pi) // ɾ�������ڵ�
			throws RemoteException// ɾ�� slaves ip,
	{
		if (IPs == null || IPs.size() == 0)
			System.out.println("No Slaves going to remove");
		else {
			int index = -1;
			for (String ip : IPs) {
				index = pi.containsSlave(ip);
				if (index > -1)
					pi.getSlavesList().remove(index);

			}
		}
	}

	synchronized public void getResultAndResetWorking() throws IOException // ��ȡ
																			// ����
																			// ��ɵ�task���
																			// д���ļ�
																			// ������
																			// ���
	{
		Information inf = null;
		if (list == null || list.size() == 0) {
			System.out.println("getResultAndResetWorking list is null ");
			return;
		}

		for (int i = 0; i < list.size(); ++i) {
			inf = list.get(i);
			System.out.println("isWorking:" + inf.isWorking);
			System.out.println("runConf:" + inf.runConf);
			System.out.println("result:" + inf.result);
			
			if (inf.isWorking == 1) {
				if (inf.result != null) {
					System.out.println("Image:"+ inf.image.length);
					writeResult(inf.result);
					//writeImage(inf.taskName,Util.getImageFromByte(inf.image));//--------------------------------------------------------------------------------------
					writeImage(inf.taskName,inf.image);
					inf.result = null;
					
					inf.image=null;
					inf.taskName=null;
					
					inf.isWorking = 0;
					// inf.result="EOF";
					list.set(i, inf);
				}
			}
		}
	}

	synchronized public void distributeTasks() throws Exception // ��������,�ǲ���û�б�Ҫʹ��synchronized��
	{

		if (tasks == null) {
			System.out.println("There is no tasks");
			return;
		}
		if (tasks.size() == 0) {
			System.out.println("All tasks has been distribute");
			allTasksDistributed = true;
			return;
		}

		if (list == null || list.size() == 0) {
			System.out.println("distributeTasks no slaves");// û��slave��Ҫ�ȴ�
			allTasksDistributed = false;
			return;
		}
		distributMethodDec();

	}

	/********************************************** ���������� *********************************/

	public void distributMethodDec() throws Exception // FIFS �������Ѷȵݼ�
	{
		Information inf = null;

		for (int i = 0; i < list.size(); ++i) {
			inf = list.get(i);
			System.out.println("distribute:==========" + inf.isWorking);
			if (inf.isWorking == 0) {// ------------------
				inf.runConf = objectToByteArray(tasks.get(0));
				inf.taskName=tasks.get(0).getName();
				inf.nameAndFunction = topoTypeAndFunction;
				inf.isWorking = 2; // �����Ѿ��������
				if (tasks.size() != 0) {
					tasks.remove(0);
				}
				list.set(i, inf);
			}
		}
		System.out.println("runconf:" + list.get(0).runConf);
	}

	public void distributMethodInc() throws Exception // FIFS �������Ѷȵ���
	{
		Information inf = null;

		for (int i = 0; i < list.size(); ++i) {
			inf = list.get(i);
			System.out.println("distribute:==========" + inf.isWorking);
			if (inf.isWorking == 0) {// ------------------
				inf.runConf = objectToByteArray(tasks.get(tasks.size() - 1));
				inf.nameAndFunction = topoTypeAndFunction;
				inf.isWorking = 2; // �����Ѿ��������
				if (tasks.size() != 0) {
					tasks.remove(0);
				}
				list.set(i, inf);
			}
		}
		System.out.println("runconf:" + list.get(0).runConf);
	}

	public void distributMethodByCapacity() throws Exception {	//���ݼ���������������
		Collections.sort(list, new SlavesComparator());// ���ܽ�������
		if (Property.flag)// ������������
		{
			distributMethodInc();
		} else // //����������
		{
			distributMethodDec();
		}
	}

	/********************************************** ����������end *********************************/
	public void writeResult(String result) throws IOException // �����д���ļ�
	{
		File file = new File("./result.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		bw.append(result);
		bw.flush();
		bw.close();
	}
	
	
	private void writeImage(String wsnName,byte[] data)	//get the image from slaves
	{
		System.out.println("data.length:"+data.length);
		 if(data.length<3) return;//�ж������byte�Ƿ�Ϊ��
		    try{
		    	String imagePath="./image/";
		    FileImageOutputStream imageOutput = new FileImageOutputStream(new File(imagePath+"/"+wsnName + ".bmp"));//��������
		    System.out.println("imagePath:"+imagePath);
		    imageOutput.write(data, 0, data.length);//��byteд��Ӳ��
		    imageOutput.close();
		    } catch(Exception ex) {
		      System.out.println("Exception: " + ex);
		      ex.printStackTrace();
		    }
		
		
	}

	static public  byte[] objectToByteArray(WirelessSensorNetwork wsn0)
			throws Exception // ���л�
	{
		byte[] bytes = null;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		// ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
		// new File("E:/Person.txt")));
		oo.writeObject(wsn0);
		bytes = bo.toByteArray();
		System.out.println("wsn length:" + bytes.length);
		oo.close();
		bo.close();
		return bytes;
	}

	static public WirelessSensorNetwork byteArrayToObject(byte[] bytesBackup)
			throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				bytesBackup));
		WirelessSensorNetwork wsn0 = (WirelessSensorNetwork) ois.readObject();
		ois.close();
		return wsn0;
	}

}
