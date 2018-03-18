import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class ListeningCount {

    private static void count(String[] args) throws AnalysisException {
        String file = "hdfs:///user/root/data/user_artists.dat";
        Integer numOfRows = 20;
        if (args.length >= 1) {
            file = args[0];
            if (args.length >= 2) {
                numOfRows = Integer.valueOf(args[1]);
            }
        }

        SparkSession spark = SparkSession.builder().appName("Total Listening Count").getOrCreate();

        Dataset<Row> userArtistTable = spark.read().format("csv").option("sep", "\t").option("inferSchema", "true").option("header", "true").load(file);
        userArtistTable.createGlobalTempView("UserArtistTable");

        // SQL processing
        Dataset<Row> result = spark.sql("SELECT artistID, sum(weight) AS count FROM global_temp.UserArtistTable GROUP BY artistID ORDER BY count DESC");

        // Object processing
        // Dataset<Row> unsortedResult = userArtistTable.groupBy("artistID").sum("weight").sort("sum(weight)").orderBy();
        // Dataset<Row> result = unsortedResult.sort(unsortedResult.col("sum(weight)").desc());

        System.out.println("----------------OUTPUT START----------------");
        result.show(numOfRows);
        System.out.println("----------------OUTPUT END----------------");
        spark.stop();
    }

    public static void main(String[] args) throws AnalysisException {
        long start = System.currentTimeMillis();
        count(args);
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        System.out.println("----------------RUNNING TIME START----------------");
        System.out.println("Total running time: " + time + "s");
        System.out.println("----------------RUNNING TIME END----------------");
    }

}
