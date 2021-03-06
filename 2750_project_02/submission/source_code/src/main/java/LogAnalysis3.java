import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.spark_project.guava.base.Function;

import scala.Tuple2;

public class LogAnalysis3 {
    private static void analysis(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Log Analysis 03");
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

        List<Tuple2<String, Integer>> output = counts.collect();

        Tuple2<String, Integer> maxTuple = new Tuple2<String, Integer>("", 0);
        for (Tuple2<String, Integer> t : output) {
            if (maxTuple._2 < t._2()) {
                maxTuple = t;
            }
        }
        System.out.println("----------------OUTPUT START----------------");
        System.out.println(maxTuple._1() + "\t" + maxTuple._2());
        System.out.println("----------------OUTPUT END----------------");
        sc.close();

    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        analysis(args);
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        System.out.println("----------------RUNNING TIME START----------------");
        System.out.println("Total running time: " + time + "s");
        System.out.println("----------------RUNNING TIME END----------------");
    }
}
