/**
 * 
 */
package log;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * @author Isolachine
 *
 */
public class LogAnalysis2 {

    public void pathHits() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder().addContactPoint("master").addContactPoint("slave").build();
            Session session = cluster.connect();

            ResultSet resultSet = session.execute("select count(*) from project_03.log where ip='10.207.188.188' ALLOW FILTERING;");

            System.out.println("10.207.188.188 " + resultSet.one().getLong("count"));

        } finally {
            if (cluster != null)
                cluster.close();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new LogAnalysis2().pathHits();
        long end = System.currentTimeMillis();
        double total = (end - start) / 1000.0;
        System.out.println("Total running time: " + total + " seconds");
    }

}
