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

public class ImportData {

    public void importData() {
        File file = new File("/root/access_log");
        Cluster cluster = null;
        final Semaphore semaphore = new Semaphore(256);
        try {

            cluster = Cluster.builder().addContactPoint("master").addContactPoint("slave").build();
            Session session = cluster.connect();

            session.execute("DROP TABLE IF EXISTS project_03.log;");
            session.execute("DROP KEYSPACE IF EXISTS project_03;");
            session.execute("CREATE KEYSPACE project_03 WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 2};");
            session.execute("CREATE TABLE project_03.log (id int, ip text, identity text, username text, time text, method text, path text, protocol text, status int, size int, PRIMARY KEY ((id), ip, path));");
            session.execute("CREATE INDEX ip_index ON log (ip);");
            session.execute("CREATE INDEX path_index ON log (path);");

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            PreparedStatement preparedStatement = session.prepare("INSERT INTO project_03.log (id, ip, identity, username, time, method, path, protocol, status, size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            int count = 1;
            while (br.ready()) {
                semaphore.acquire();
                String line = br.readLine();
                Pattern pattern = Pattern.compile("(.*) (.*) (.*) \\[(.*)\\] \"(.*) (.*) (.*)\" (.*) (.*)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    String identity = matcher.group(2).equals("-") ? null : matcher.group(2);
                    String username = matcher.group(3).equals("-") ? null : matcher.group(3);
                    String time = matcher.group(4);
                    String method = matcher.group(5);
                    String path = matcher.group(6);
                    String protocol = matcher.group(7);
                    Integer status = Integer.valueOf(matcher.group(8));
                    Integer size = matcher.group(9).equals("-") ? null : Integer.valueOf(matcher.group(9));

                    BoundStatement boundStatement = preparedStatement.bind(count, ip, identity, username, time, method, path, protocol, status, size);
                    count++;

                    ResultSetFuture resultSetFuture = session.executeAsync(boundStatement);
                    Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
                        public void onSuccess(ResultSet resultSet) {
                            semaphore.release();
                        }

                        public void onFailure(Throwable throwable) {
                            semaphore.release();
                            System.out.println("Error: " + throwable.toString());
                        }
                    });
                }
                if ((count - 1) % 1000 == 0) {
                    System.out.println("inserted " + (count - 1));
                }
            }

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
        new ImportData().importData();
        long end = System.currentTimeMillis();
        long total = (end - start) / 1000;
        System.out.println("Total running time: " + total + " seconds");
    }
}
