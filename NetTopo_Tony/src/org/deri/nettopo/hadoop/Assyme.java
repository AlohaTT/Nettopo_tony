package org.deri.nettopo.hadoop;


import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
/**
 * TextInputFormat���������������һ��InputSplit��InputSplit�Ǹ��ݴ�С���ֵġ�
 * NLineInputFormat����ÿ��Mapper����ļ�¼������ͬ�ġ�
 */
public class Assyme {
	private static final String INPUT_PATH = "hdfs://192.168.100.100:9000/in ";
	private static final String OUT_PATH = "hdfs://192.168.100.100:9000/out";

	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		//����ÿ��map���Դ����������¼
		conf.setInt("mapreduce.input.lineinputformat.linespermap", 1);
		final FileSystem filesystem = FileSystem.get(new URI(OUT_PATH), conf);
		filesystem.delete(new Path(OUT_PATH), true);
		
		@SuppressWarnings("deprecation")
		final Job job = new Job(conf , Assyme.class.getSimpleName());
		job.setJarByClass(Assyme.class);
		
		FileInputFormat.setInputPaths(job, INPUT_PATH);
		job.setInputFormatClass(NLineInputFormat.class);
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setReducerClass(MyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileOutputFormat.setOutputPath(job, new Path(OUT_PATH));
		
		job.waitForCompletion(true);
	}
	
	public static class MyMapper extends Mapper<LongWritable, Text, Text, Text>{
		//����Դ�ļ������2����ֵ�ԣ��ֱ���<0,hello you><10,hello me>������map�����ᱻ����2��
		protected void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,Text>.Context context) throws java.io.IOException ,InterruptedException {
			//ΪʲôҪ��hadoop����ת��Ϊjava���ͣ�
			String line = value.toString();
			if(line.equals(""))
				return;
		};
	}
	
	//map����ִ�н�����map�����<k,v>һ����4�����ֱ���<hello,1><you,1><hello,1><me,1>
	//������Ĭ��ֻ��һ����
	//�����Ľ����<hello,1><hello,1><me,1><you,1>
	//�����Ľ����<hello,{1,1}>  <me,{1}>  <you,{1}>
	//��Լ(��ѡ)
	
	
	
	//map������<k,v>�ַ���reduce�Ĺ��̳���shuffle
	public static class MyReducer extends Reducer<Text, Text, Text, Text>{
		//ÿһ�����һ��reduce������һ��������3��
		//�����������reduce�����ĵ��ô�����ʲô��ϵ��
		//reduce�����ĵ��ô����������<k,v>��������ʲô��ϵ��
		protected void reduce(Text key, java.lang.Iterable<Text> values, org.apache.hadoop.mapreduce.Reducer<Text,Text,Text,Text>.Context context) throws java.io.IOException ,InterruptedException {
			String temp=null;
			for (Text str : values) {
				temp=str.toString();
				if(temp.contains("\tGG"))
				{
					temp=temp.replace("\tGG", "");
					context.write(new Text("GG"), new Text(temp));
				}
			}
			
		};
	}
}


