package com.lyyq;

import java.io.IOException;
import java.util.*; 

import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.fs.FileSystem; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser;


public class CommonFriend {
	public static class MyMapper1 extends Mapper <LongWritable,Text,Text,Text> {
		
		public void map(LongWritable key, Text value, Context context) 
				throws IOException, InterruptedException {
			String[] PrsnAndFrnd=value.toString().split(", ");   
			String person=PrsnAndFrnd[0];
			String[] friends=PrsnAndFrnd[1].split(" ");  
			for (String f:friends){
				context.write(new Text(f),new Text(person));
			}
		}
	}//out 200/300... 100

    public static class MyReduce1 extends Reducer <Text,Text,Text,Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context) 
				throws IOException, InterruptedException {
			StringBuffer res1= new StringBuffer();
			for (Text val:values){
				res1.append(val.toString()).append(",");
			}
			String outvalue=String.valueOf(res1);
			outvalue=outvalue.substring(0, outvalue.length()-1);
			context.write(key,new Text(outvalue));
		}
	}

	
	public static class MyMapper2 extends Mapper <LongWritable,Text,Text,Text> {
		
		public void map(LongWritable key, Text value, Context context) 
				throws IOException, InterruptedException {
			String[] PrsnAndFrnd = value.toString().split("\t");//...tried " "&"  "&"   " :(
			String friend = PrsnAndFrnd[0];
			String[] person = PrsnAndFrnd[1].split(",");
			Arrays.sort(person);//sort
			for (int i=0;i<person.length-1;i++){
				for (int j=i+1;j<person.length;j++){
					context.write(new Text("(["+person[i]+","+person[j]+"],"),new Text(friend));   //2.0 改输出格式
				}
			}    
		}
	}
 
	public static class MyReduce2 extends Reducer <Text,Text,Text,Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context) 
				throws IOException, InterruptedException {
			StringBuffer res=new StringBuffer();
			Set<String> p=new HashSet<>();
			for (Text v:values){
				if (!p.contains(v.toString())){
					p.add(v.toString());
				}
			}
			for (String s:p){
				res.append(s).append(",");
			}
			//res = res.deleteCharAt(res.length()-1);
			String outvalue=String.valueOf(res);
			outvalue=outvalue.substring(0, outvalue.length()-1);
			context.write(key,new Text("["+outvalue+"])")); 
		}
	}

		
    public static void main(String[] args) throws Exception{
		
        Configuration conf=new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
		  System.err.println("Usage: commmon <in> [<in>...] <out>");
		  System.exit(2);
		}
		
		Path TempPath = new Path("hdfs://lyyq181850099-master:9000/sharedfriend/FriendsTempOutput");
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
		job2.setJarByClass(CommonFriend.class);
        job2.setMapperClass(MyMapper2.class);
        job2.setReducerClass(MyReduce2.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
		job2.setInputFormatClass(TextInputFormat.class);
		FileInputFormat.addInputPath(job2,TempPath);
        FileOutputFormat.setOutputPath(job2,outPath);
		job2.waitForCompletion(true); 
		
		FileSystem.get(conf).delete(TempPath, true);  
		System.exit(job2.waitForCompletion(true)?0:1);
    }
}
