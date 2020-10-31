package com.lyyq;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class CommonFriend {

  public static Set<String> intersect(Set<String> set1, Set<String> set2){
		if(set1==null || set2 == null){
			return null;
		}
		Set<String> result = new TreeSet<String>();
		Set<String> small = null;
		Set<String> big = null;
		if(set1.size() < set2.size()){
			small = set1;
			big = set2;
		}
		else {
			small = set2;
			big = set1;
		}
		for (String String : small) {
			if(big.contains(String)){
				result.add(String);
			}
		}
		return result;
	}
	
	
	static class MyMapper extends Mapper<LongWritable, Text, Text, Text>{
		private static Text outKey = new Text();
		private static Text outValue = new Text();
		
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String [] input = value.toString().split(",");
			if(input.length != 2){
				return;
			}
			outValue.set(input[1]);
			String [] sz = input[1].split(" ");
			for (String string : sz) {
				if(input[0].compareTo(string) < 0){
					outKey.set("[" + input[0] + ", " + string + "]");
				}
				else {
					outKey.set("[" + string + ", " + input[0] + "]");
				}
				context.write(outKey, outValue);
			}
		}
	}//[100,200] 200 300 400 500
	//[100,300] 200 300 400 500
	//[200,100] 300 400
	
	static class MyReducer extends Reducer<Text, Text, Text, Text>{
		//private Text outKey = new Text();
		private Text outValue = new Text();
		
		@Override
		protected void reduce(Text key, Iterable<Text> value, Context context)
				throws IOException, InterruptedException {
			int len = 0;
			Set<String> set1 = new TreeSet<String>();
			Set<String> set2 = new TreeSet<String>();
			ArrayList<String> arrayList = new ArrayList<String>();
			for (Text text : value) {
				arrayList.add(text.toString());
				len++;
			}
			if(len != 2){
				return;
			}
			String [] sz = arrayList.get(0).split(" ");
			for (String s : sz) {
				set1.add(s);
			}
			sz = arrayList.get(1).trim().split(" ");
			for (String s : sz) {
				set2.add(s);
			}
			Set<String> res = intersect(set1, set2);
			if(res == null){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (String s : res) {
				sb.append(s + ", ");
			}
			sb.append("]");
			String substring = null;
			if(sb.length() > 1){
				substring = sb.substring(0, sb.length());
			}
			if(substring != null){
				this.outValue.set(substring);
				context.write(key, outValue);
			}
		}
	}
 
	public static void main(String[] args) 
			throws IOException, ClassNotFoundException, InterruptedException {
	Configuration conf = new Configuration();
	String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: wordcount <in> [<in>...] <out>");
      System.exit(2);
    }

    Job job=Job.getInstance(conf);
	job.setJarByClass(CommonFriend.class);
	job.setMapperClass(MyMapper.class);
	job.setReducerClass(MyReducer.class);
	job.setMapOutputKeyClass(Text.class);
	job.setMapOutputValueClass(Text.class);
	job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileSystem fs = FileSystem.get(conf);
    Path outPath=new Path(otherArgs[otherArgs.length - 1]);
    if (fs.exists(outPath)) {
        fs.delete(outPath, true);
    }
	for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,outPath);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
