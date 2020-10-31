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

  public static Set<String> FindCommon(Set<String> list1, Set<String> list2){
		if(list1==null || list2 == null){
			return null;
		}//no common friend
		Set<String> result = new TreeSet<String>();
		Set<String> l1 = null;Set<String> l2 = null;
		if(list1.size() < list2.size()){
			l1 = list1;
			l2 = list2;
		}
		else {
			l1 = list2;
			l2 = list1;
		}
		for (String frnd : l1) {
			if(l2.contains(frnd)){
				result.add(frnd);
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
			String [] friend = input[1].split(" ");
			for (String tofnd : friend) {
				if(input[0].compareTo(tofnd) < 0){
					outKey.set("([" + input[0] + ", " + tofnd + "],");
				}
				else {
					outKey.set("([" + tofnd + ", " + input[0] + "],");
				}
				context.write(outKey, outValue);
			}
		}
	}//[100,200] 200 300 400 500
	//[100,300] 200 300 400 500
	//[200,100] 300 400
	
	static class MyReducer extends Reducer<Text, Text, Text, Text>{

		private Text outValue = new Text();
		
		@Override
		protected void reduce(Text key, Iterable<Text> value, Context context)
				throws IOException, InterruptedException {
			int valnum = 0;
			Set<String> list1 = new TreeSet<String>();
			Set<String> list2 = new TreeSet<String>();
			ArrayList<String> friendlist = new ArrayList<String>();
			for (Text val : value) {
				friendlist.add(val.toString());
				valnum++;
			}
			if(valnum != 2){
				return;//one is the friendlist of 0,another one is the friendlist of 1;
			}
			String [] frndlst1 = friendlist.get(0).trim().split(" ");
			for (String s : frndlst1) {
				list1.add(s);
			}
			String [] frndlst2 = friendlist.get(1).trim().split(" ");
			for (String s : frndlst2) {
				list2.add(s);
			}
			Set<String> frndlst = FindCommon(list1, list2);

			StringBuilder shared = new StringBuilder();
			shared.append("[");
			for (String s : frndlst) {
				shared.append(s + ", ");
			}
			String res = null;
			if(shared.length() > 1){
				res = shared.substring(0, shared.length()-2);//cut ouy "," & " "  
			}
			res+="])";
			if(res != null){
				this.outValue.set(res);
				context.write(key, outValue);
			}
		}
	}
 
	public static void main(String[] args) 
			throws IOException, ClassNotFoundException, InterruptedException {
	Configuration conf = new Configuration();
	String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: commmon <in> [<in>...] <out>");
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
