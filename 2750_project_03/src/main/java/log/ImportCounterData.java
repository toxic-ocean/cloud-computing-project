package log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class ImportCounterData {

    public void importData() {
        File file = new File("/root/access_log");
        Cluster cluster = null;
        final Semaphore semaphore = new Semaphore(256);
        try {

            cluster = Cluster.builder().addContactPoint("master").addContactPoint("slave").build();
            Session session = cluster.connect();

            session.execute("DROP TABLE IF EXISTS project_03.ip;");
            session.execute("DROP TABLE IF EXISTS project_03.path;");
            session.execute("CREATE TABLE project_03.ip (ip text PRIMARY KEY, count counter);");
            session.execute("CREATE TABLE project_03.path (path text PRIMARY KEY, count counter);");

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            PreparedStatement ipStatement = session.prepare("UPDATE project_03.ip SET count = count + 1 WHERE ip = ?");
            PreparedStatement pathStatement = session.prepare("UPDATE project_03.path SET count = count + 1 WHERE path = ?");
            int count = 0;
            while (br.ready()) {
                semaphore.acquire();
                semaphore.acquire();
                String line = br.readLine();
                Pattern pattern = Pattern.compile("([^ ]*).*\"\\w+ ([^ ]*)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    String path = matcher.group(2);

                    BoundStatement ipBoundStatement = ipStatement.bind(ip);
                    BoundStatement pathBoundStatement = pathStatement.bind(path);
                    count++;

                    ResultSetFuture ipFuture = session.executeAsync(ipBoundStatement);
                    ResultSetFuture pathFuture = session.executeAsync(pathBoundStatement);

                    Futures.addCallback(ipFuture, new FutureCallback<ResultSet>() {
                        public void onSuccess(ResultSet resultSet) {
                            semaphore.release();
                        }

                        public void onFailure(Throwable throwable) {
                            semaphore.release();
                            System.out.println("Error: " + throwable.toString());
                        }
                    });

                    Futures.addCallback(pathFuture, new FutureCallback<ResultSet>() {
                        public void onSuccess(ResultSet resultSet) {
                            semaphore.release();
                        }

                        public void onFailure(Throwable throwable) {
                            semaphore.release();
                            System.out.println("Error: " + throwable.toString());
                        }
                    });
                }
                if (count % 20000 == 0) {
                    System.out.println("inserted " + count);
                }
            }
            System.out.println("inserted " + count);

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (cluster != null)
                cluster.close();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new ImportCounterData().importData();
        long end = System.currentTimeMillis();
        long total = (end - start) / 1000;
        System.out.println("Total running time: " + total + " seconds");
    }
}
