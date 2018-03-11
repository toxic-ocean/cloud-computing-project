import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.spark_project.guava.base.Function;

import scala.Tuple2;

public class LogAnalysisNoCache {

    private static void analysis(String[] args) {

        long start = System.currentTimeMillis();

        SparkConf conf = new SparkConf().setAppName("Log Analysis No Cache");
        JavaSparkContext sc = new JavaSparkContext(conf);

        String file = "hdfs:///user/root/data/access_log";
        if (args.length > 0) {
            file = args[0];
        }

        JavaRDD<String> lines = sc.textFile(file);

        class Count implements Function<String, Tuple2<String, Integer>> {
            public Tuple2<String, Integer> apply(String s) {
                Pattern pattern = Pattern.compile("\"\\w+ ([^ ]*) ");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    return new Tuple2<String, Integer>(matcher.group(1), 1);
                }
                return new Tuple2<String, Integer>(null, null);
            }
        }

        JavaPairRDD<String, Integer> counts = lines.mapToPair(s -> new Count().apply(s)).reduceByKey((a, b) -> a + b);

        Map<String, Integer> output = counts.collectAsMap();

        System.out.println("----------------OUTPUT START----------------");
        System.out.println("/assets/img/loading.gif" + "\t" + output.get("/assets/img/loading.gif"));
        System.out.println("----------------OUTPUT END----------------");
        
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        System.out.println("----------------RUNNING TIME START----------------");
        System.out.println("Total running time: " + time + "s");
        System.out.println("----------------RUNNING TIME END----------------");
        
        lines.unpersist();
        counts.unpersist();
        output.clear();
        output = null;


        JavaRDD<String> newLines = sc.textFile(file);
        JavaPairRDD<String, Integer> newCounts = newLines.mapToPair(s -> new Count().apply(s)).reduceByKey((a, b) -> a + b);
        Map<String, Integer> newOutput = newCounts.collectAsMap();

        System.out.println("----------------OUTPUT START----------------");
        System.out.println("/assets/js/lightbox.js" + "\t" + newOutput.get("/assets/js/lightbox.js"));
        System.out.println("----------------OUTPUT END----------------");

        sc.close();

        end = System.currentTimeMillis();
        time = (end - start) / 1000.0;
        System.out.println("----------------RUNNING TIME START----------------");
        System.out.println("Total running time: " + time + "s");
        System.out.println("----------------RUNNING TIME END----------------");
    }

    public static void main(String[] args) {
        // long start = System.currentTimeMillis();
        analysis(args);
        // long end = System.currentTimeMillis();
        // double time = (end - start) / 1000.0;
        // System.out.println("----------------RUNNING TIME START----------------");
        // System.out.println("Total running time: " + time + "s");
        // System.out.println("----------------RUNNING TIME END----------------");
    }
}
