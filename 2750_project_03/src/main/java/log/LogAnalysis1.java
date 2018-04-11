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
public class LogAnalysis1 {

    public void pathHits() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder().addContactPoint("master").addContactPoint("slave").build();
            Session session = cluster.connect();

            ResultSet resultSet = session.execute("select count(*) from project_03.log where path='/assets/img/release-schedule-logo.png' ALLOW FILTERING;");

            System.out.println("/assets/img/release-schedule-logo.png " + resultSet.one().getLong("count"));

        } finally {
            if (cluster != null)
                cluster.close();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new LogAnalysis1().pathHits();
        long end = System.currentTimeMillis();
        double total = (end - start) / 1000.0;
        System.out.println("Total running time: " + total + " seconds");
    }

}
