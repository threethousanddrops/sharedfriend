package com.lyyq;

import java.io.IOException;
import java.util.*; 

import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.fs.FileSystem; 


public class CommonFriend {
	public static class MyMapper1 extends Mapper <LongWritable,Text,Text,Text> {
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line=value.toString();
			String[] person_friends=line.split(", ");     //3.0
			String person=person_friends[0];
			String[] friends=person_friends[1].split(" ");     //3.0
			for (String friend:friends){
				context.write(new Text(friend),new Text(person));
			}
		}
	}

    public static class MyReduce1 extends Reducer <Text,Text,Text,Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			StringBuffer bf= new StringBuffer();
			for (Text friend:values){
				bf.append(friend.toString()).append(",");
			}
			bf=bf.deleteCharAt(bf.length()-1);
			context.write(key,new Text(String.valueOf(bf)));
		}
	}

	
	public static class MyMapper2 extends Mapper <LongWritable,Text,Text,Text> {
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] friend_persons = line.split("\t");
			String friend = friend_persons[0];
			String[] persons = friend_persons[1].split(",");
			Arrays.sort(persons); //排序
			//两两配对
			for (int i=0;i<persons.length-1;i++){
				for (int j=i+1;j<persons.length;j++){
					context.write(new Text("(["+persons[i]+","+persons[j]+"],"),new Text(friend));   //2.0 改输出格式
				}
			}    
		}
	}
 
	public static class MyReduce2 extends Reducer <Text,Text,Text,Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			StringBuffer bf=new StringBuffer();
			Set<String> set=new HashSet<>();
			for (Text s:values){
				if (!set.contains(s.toString())){
					set.add(s.toString());
				}
			}
			for (String s:set){
				bf.append(s).append(",");
			}
			bf = bf.deleteCharAt(bf.length()-1);
			context.write(key,new Text("["+bf.toString()+"])"));    //2.0
		}
	}

		
    public static void main(String[] args) throws Exception{
		
        Configuration conf=new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
		  System.err.println("Usage: commmon <in> [<in>...] <out>");
		  System.exit(2);
		}
		
		Path TempPath = new Path("hdfs://lyyq181850099-master:9000/wordcount/FriendsTempOutput");
		FileSystem fs = FileSystem.get(conf);
		Path outPath=new Path(otherArgs[otherArgs.length - 1]);
		if (fs.exists(outPath)) {
			fs.delete(outPath, true);
		}      
		
		Job job1=Job.getInstance(conf);
        job1.setJarByClass(CommonFriend.class);
        job1.setMapperClass(MyMapper1.class);
        job1.setReducerClass(MyReduce1.class);
        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(Text.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        job1.setOutputFormatClass(TextOutputFormat.class);
		job1.setInputFormatClass(TextInputFormat.class);
		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job1, new Path(otherArgs[i]));
		  }
        FileOutputFormat.setOutputPath(job1,TempPath);
		job1.waitForCompletion(true); 

		Job job2=Job.getInstance(conf);
        //Job job2=new Job(conf,"job2");
		FileInputFormat.addInputPath(job2,TempPath);
        job2.setMapperClass(MyMapper2.class);
        job2.setReducerClass(MyReduce2.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        job2.setInputFormatClass(TextInputFormat.class);
        FileOutputFormat.setOutputPath(job2,outPath);
		job2.waitForCompletion(true); 
		
		FileSystem.get(conf).delete(TempPath, true);  
		System.exit(job1.waitForCompletion(true)?0:1);
    }
}
