package ru.goncharoff;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main7 {

    private static Pattern pattern = Pattern.compile("(.*?)(class|interface)(.*?)(extends|implements)(\\s\\w+)(.*?)");

    private static Map<String, List<String>> classToHisExtended = new HashMap<>();

    public static class MapperClass
            extends Mapper<LongWritable, Text, Text, Text> {

        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context
        ) throws IOException, InterruptedException {

            Matcher matcher = pattern.matcher(value.toString());

            while(matcher.find()) {
                String mainClass = matcher.group(5).trim();
                String childClass = matcher.group(3).trim();

                word.set(mainClass);
                context.write(word, new Text(childClass));

                System.out.println(mainClass + "  " + childClass);
            }
        }
    }

    public static class ReducerClass
            extends Reducer<Text,Text,Text, Text> {

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            StringBuilder stringBuilder = new StringBuilder();
            System.out.println("Reducer: " + key);
            System.out.println("Values: ");
            for (Text val : values) {
                System.out.println(val);
                stringBuilder.append(" " + val.toString());
            }
            context.write(key, new Text(stringBuilder.toString()));
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration configuration = new Configuration();
        configuration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true");
        Job job = Job.getInstance(configuration, "Search extends class");
        job.setJarByClass(Main7.class);

        job.setMapperClass(MapperClass.class);
        job.setReducerClass(ReducerClass.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);


        FileInputFormat.addInputPath(job, new Path(args[0]));

        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }
}
