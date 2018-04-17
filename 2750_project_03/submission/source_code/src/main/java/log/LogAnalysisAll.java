/**
 * 
 */
package log;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Isolachine
 *
 */
public class LogAnalysisAll {

    public void pathHits() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder().addContactPoint("159.89.43.89").build();
            Session session = cluster.connect();
            
            ResultSet resultSet1 = session.execute("SELECT count(*) FROM project_03.log WHERE path='/assets/img/release-schedule-logo.png' ALLOW FILTERING;");
            System.out.println("/assets/img/release-schedule-logo.png " + resultSet1.one().getLong("count"));
            
            ResultSet resultSet2 = session.execute("SELECT count(*) FROM project_03.log WHERE ip='10.207.188.188' ALLOW FILTERING;");
            System.out.println("10.207.188.188 " + resultSet2.one().getLong("count"));
            
            ResultSet resultSet3 = session.execute("SELECT * FROM project_03.path;");
            String path = "";
            long count3 = 0;
            for (Row row : resultSet3) {
                long rowCount = row.getLong("count");
                if (rowCount > count3) {
                    count3 = rowCount;
                    path = row.getString("path");
                }
            }
            System.out.println(path + " " + count3);
            
            ResultSet resultSet4 = session.execute("SELECT * FROM project_03.ip;");
            String ip = "";
            long count4 = 0;
            for (Row row : resultSet4) {
                long rowCount = row.getLong("count");
                if (rowCount > count4) {
                    count4 = rowCount;
                    ip = row.getString("ip");
                }
            }
            System.out.println(ip + " " + count4);

        } finally {
            if (cluster != null)
                cluster.close();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new LogAnalysisAll().pathHits();
        long end = System.currentTimeMillis();
        double total = (end - start) / 1000.0;
        System.out.println("Total running time: " + total + " seconds");
    }

}
