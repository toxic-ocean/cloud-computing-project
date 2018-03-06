import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * W3C log analyzer
 */
public class LogAnalyze {

    public static final String LOGTYPE_CONF = "log_prob_type";
    public static final String UNMATCHED = "unmatched";

    public static class LogMapper extends Mapper<Object, Text, Text, LongWritable> {

        // Reference: https://databricks.gitbooks.io/databricks-spark-reference-applications/content/logs_analyzer/chapter1/java8/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
        private Pattern logPattern = Pattern.compile("^(\\S+).*\"(\\S+) (\\S+).*");
        private final static LongWritable oneLong = new LongWritable(1);
        private Text interestText = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            int pType = context.getConfiguration().getInt(LOGTYPE_CONF, 1);

            String aLine = value.toString();
            Matcher matcher = logPattern.matcher(aLine);
            if (!matcher.matches()) {
                interestText.set(UNMATCHED);
                context.write(interestText, oneLong);
                return;
            }
            String interest = "Other";

            // See comments in TestRegex.java
            switch (pType) {
                case 1:
                    // 'URL' = '/assets/img/home-logo.png'
                    String target1 = "/assets/img/home-logo.png";
                    if (matcher.group(3).trim().equals(target1))
                        interest = target1;
                    break;
                case 2:
                    // 'IP' = 10.153.239.5
                    String target2 = "10.153.239.5";
                    if (matcher.group(1).trim().equals(target2))
                        interest = target2;
                    break;
                case 3:
                case 5:
                    // GROUP BY 'URL'
                    interest = matcher.group(3).trim();
                    break;
                case 4:
                case 6:
                    // GROUP BY 'IP'
                    interest = matcher.group(1).trim();
                    break;
                default:
                    interest = "Illegal P_val!";
                    break;
            }

            interestText.set(interest);
            context.write(interestText, oneLong);
        }
    }

    public static class LogReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

        private int pType;
        private long max = 0;
        private Text textMax = new Text();

        @Override
        protected void setup(Context context) {
            pType = context.getConfiguration().getInt(LOGTYPE_CONF, 1);
        }

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long alSum = 0;
            for (LongWritable val : values) {
                alSum += val.get();
            }

            switch (pType) {
                case 1:
                case 2:
                case 5:
                case 6:
                    // Reducer aggregation
                    context.write(key, new LongWritable(alSum));
                    break;
                case 3:
                case 4:
                    // Reducer find max
                    if (max < alSum) {
                        max = alSum;
                        textMax.set(key);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            switch (pType) {
                case 3:
                case 4:
                    // Reducer aggregation
                    context.write(textMax, new LongWritable(max));
                    break;
                default:
                    break;
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String helpText = "Usage: LogAnalyze <input_file> <output_folder> <Problem type id (1,2,3,4)>";
        GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
        String[] remainingArgs = optionParser.getRemainingArgs();
        if (remainingArgs.length != 3) {
            System.err.println(helpText);
            System.exit(2);
        }
        int pType = Integer.parseInt(remainingArgs[2]);
        if (pType > 6 || pType < 1) {
            System.err.println(helpText);
            System.err.println("   Problem 1: How many hits were made to the website item “/assets/img/home-logo.png”?");
            System.err.println("   Problem 2: How many hits were made from the IP: 10.153.239.5");
            System.err.println("   Problem 3: Which path in the website has been hit most? How many hits were made to the path?");
            System.err.println("   Problem 4: Which IP accesses the website most? How many accesses were made by it?");
            System.err.println("   Problem 5&6: Special debug operation!");
            System.exit(2);
        }

        conf.setInt(LOGTYPE_CONF, pType);
        Job job = Job.getInstance(conf, "LogAnalyze problem type: " + pType);

        job.setJarByClass(LogAnalyze.class);
        job.setMapperClass(LogMapper.class);
        // *>&>&*!!!!!!!!
        // job.setCombinerClass(LogReducer.class);
        job.setReducerClass(LogReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path(remainingArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(remainingArgs[1]));

        System.exit(job.waitForCompletion(false) ? 0 : 1);
    }
}
