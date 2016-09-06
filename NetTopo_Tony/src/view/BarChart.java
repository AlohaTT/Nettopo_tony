package view;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChart {
	public static void PaintChart(String key0,String val0) throws IOException {
		// ����һ����״ͼ
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		ReadResult rr=new ReadResult();
		Map<Integer,Integer> data=rr.readPathNumber(key0,val0);
		 Iterator<Entry<Integer, Integer>> iter = data.entrySet().iterator();
		 while (iter.hasNext()) {
		 @SuppressWarnings("rawtypes")
		Map.Entry entry = (Map.Entry) iter.next();
		 int key = (int) entry.getKey();
		 int val = (int) entry.getValue();
		 dataset.setValue(val, "50", key+"");
		 }
		
		// ������״ͼ
		// JFreeChart chart =
		// ChartFactory.createXYLineChart("����","x���־","y���־","��������","����ͼ����ʾ����",�Ƿ���ʾͼ��,�Ƿ������ʾ,�Ƿ����ñ����ŵ�ַ);
		// ͼ����ʾ����
		// (1)HORIZONTAL:������ʾͼ��
		// (2)VERTICAL:������ʾͼ��
		// 3D��״ͼ
		JFreeChart chart = ChartFactory.createBarChart("����ͳ��ͼ", "��ֵ", "ʱ��",
				dataset, PlotOrientation.VERTICAL, true, true, false);
		

		// �����ܵı�����ɫ
		chart.setBackgroundPaint(ChartColor.WHITE);
		// ���ñ�����ɫ
		chart.getTitle().setPaint(ChartColor.black);
		// ���ͼ�����
		CategoryPlot p = chart.getCategoryPlot();
		// ����ͼ�ı�����ɫ
		p.setBackgroundPaint(ChartColor.WHITE);
		
		p.setRangeGridlinesVisible(false);
		// ���ñ������ɫ
		p.setRangeGridlinePaint(ChartColor.red);
		try {
			// // ����ͼ����ʾ���
			// ChartFrame cf = new ChartFrame("��״ͼ",chart);
			// cf.pack();
			// // ����ͼƬ��С
			// cf.setSize(500,300);
			// // ����ͼ�οɼ�
			// cf.setVisible(true);

			// ����ͼƬ��ָ���ļ���
			ChartUtilities.saveChartAsPNG(new File("./BarChart.png"), chart,
					500, 300);

		} catch (Exception e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
}
