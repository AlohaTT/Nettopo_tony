package test;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
//import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.deri.nettopo.util.DuplicateCoordinateException;

//�����ʽ��GG gasRadius sensorNodeNum ����   ԭ���  �����
//CalculateMapû��ʹ��
public class CalculateMap {
	public static class CalMapper extends MapReduceBase implements Mapper<LongWritable,Text,Text,NullWritable> {
		
		
		public void map(LongWritable key, Text value, OutputCollector<Text,NullWritable> output,
				Reporter reporter) throws IOException {
			// TODO Auto-generated method stub
			//��ȡ�ļ�
			String DataInfo=value.toString();
			//�� �� �ָ�
			StringTokenizer takeLine=new StringTokenizer(DataInfo, "\n");
			while (takeLine.hasMoreTokens()) {
				//�� �ո� �ָ�
				StringTokenizer tokenizer=new StringTokenizer(takeLine.nextToken(), "\t");
				String GType=tokenizer.nextToken();//�еĵ�һ��ֵ
				int GasRadius=Integer.parseInt(tokenizer.nextToken());//�ڶ���ֵ
				int sensorNodenum=Integer.parseInt(tokenizer.nextToken());
				int np=Integer.parseInt(tokenizer.nextToken());
				int seed=Integer.parseInt(tokenizer.nextToken());
				int t=Integer.parseInt(tokenizer.nextToken());
				CalculateTest ca=new CalculateTest();
				try {
					
					ca.run(output, GType,GasRadius,sensorNodenum,np,seed,t);
				} catch (DuplicateCoordinateException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
		}
		
	}
	public static class CalReducer extends MapReduceBase implements Reducer<Text, NullWritable, Text, NullWritable>{

		private MultipleOutputs mos;
		public void configure(JobConf conf) {
			mos=new MultipleOutputs(conf);
		}
		@SuppressWarnings("unchecked")
		public void reduce(Text key, Iterator<NullWritable> values,
				OutputCollector<Text, NullWritable> output, Reporter Reporter)
				throws IOException {
			// TODO Auto-generated method stub
			String[] s=key.toString().trim().split("\t");
			output=mos.getCollector(s[0], Reporter);
			output.collect(key, NullWritable.get());
		}
		public void close() throws IOException {
			mos.close();
		}
	}
	@SuppressWarnings("deprecation")
	public int run(String[] args) throws Exception{
		//String[]Ŀ���ǣ���hadoop jar����ʱ��Ĭ����������������·������
	    	String[] staticArgs={"hdfs://192.168.100.100:9000/10000files","hdfs://192.168.100.100:9000/newdata1"};
	    	if(args.length<2)
	    		args=staticArgs;
	        JobConf conf = new JobConf(CalculateMap.class);
	        conf.set("mapreduce.task.timeout", "0");
	        conf.set("mapreduce.tasktracker.map.tasks.maximum", "7");
	        conf.set("mapreduce.tasktracker.reduce.tasks.maximum", "7");
	        conf.setMapperClass(CalMapper.class);
	        conf.setJobName("MultiNewData");          
	        conf.setOutputKeyClass(Text.class);    
	        conf.setOutputValueClass(NullWritable.class);  
	        conf.setReducerClass(CalReducer.class);
	        conf.setNumReduceTasks(14);//ͳ�Ƴ�����������ҪReduce��Լ����
	        conf.setInputFormat(TextInputFormat.class);    
	        conf.setOutputFormat(TextOutputFormat.class);  
	        Path path1 = new Path(args[0]);																													 
	        Path path2 = new Path(args[1]);
	        FileSystem fs=(new Path(args[1]).getFileSystem(conf));
	        if (fs.exists(path2)){
				fs.delete(path2);
			}
	        FileInputFormat.setInputPaths(conf, path1);
	        FileOutputFormat.setOutputPath(conf, path2);
	        
	        MultipleOutputs.addNamedOutput(conf, "GG", TextOutputFormat.class, Text.class, Text.class);
	        MultipleOutputs.addNamedOutput(conf, "YG", TextOutputFormat.class, Text.class, Text.class);
	        MultipleOutputs.addNamedOutput(conf, "RNG", TextOutputFormat.class, Text.class, Text.class);
	        MultipleOutputs.addNamedOutput(conf, "DEL", TextOutputFormat.class, Text.class, Text.class);
	        MultipleOutputs.addNamedOutput(conf, "LDEL", TextOutputFormat.class, Text.class, Text.class);
	        
	        JobClient.runJob(conf); 

	        return 0;
		
	}
	public static void main(String[] args) throws Exception {
		
		CalculateMap sr=new CalculateMap();
		long t1=System.currentTimeMillis();
		
		sr.run(args);
		long t2=System.currentTimeMillis();
		long t=t2-t1;
		System.out.println("Finished...");
		System.out.println("Total time:"+t/(1000*60));
		System.exit(-1);
	}

}
