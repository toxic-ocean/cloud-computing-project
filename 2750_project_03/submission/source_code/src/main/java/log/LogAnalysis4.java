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
public class LogAnalysis4 {

    public void pathHits() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder().addContactPoint("159.89.43.89").build();
            Session session = cluster.connect();

            ResultSet resultSet = session.execute("SELECT * from project_03.ip;");
            String ip = "";
            long count = 0;
            for (Row row : resultSet) {
                long rowCount = row.getLong("count");
                if (rowCount > count) {
                    count = rowCount;
                    ip = row.getString("ip");
                }
            }
            System.out.println(ip + " " + count);

        } finally {
            if (cluster != null)
                cluster.close();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new LogAnalysis4().pathHits();
        long end = System.currentTimeMillis();
        double total = (end - start) / 1000.0;
        System.out.println("Total running time: " + total + " seconds");
    }

}
