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

    public void importData(String filePath) {
        File file = new File(filePath);
        Cluster cluster = null;

        // pool for inserting data
        final Semaphore semaphore = new Semaphore(256);
        try {
            // create connection to Cassandra cluster
            cluster = Cluster.builder().addContactPoint("master").build();
            Session session = cluster.connect();

            // set up the keyspace
            session.execute("DROP TABLE IF EXISTS project_03.log;");
            session.execute("DROP TABLE IF EXISTS project_03.ip;");
            session.execute("DROP TABLE IF EXISTS project_03.path;");
            session.execute("DROP KEYSPACE IF EXISTS project_03;");
            session.execute("CREATE KEYSPACE project_03 WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 2};");
            session.execute(
                    "CREATE TABLE project_03.log (id int, ip text, identity text, username text, time text, method text, path text, protocol text, status int, size int, PRIMARY KEY ((id), ip, path));");
            session.execute("CREATE INDEX ip_index ON project_03.log (ip);");
            session.execute("CREATE INDEX path_index ON project_03.log (path);");
            session.execute("CREATE TABLE project_03.ip (ip text PRIMARY KEY, count counter);");
            session.execute("CREATE TABLE project_03.path (path text PRIMARY KEY, count counter);");
            session.execute("CREATE OR REPLACE FUNCTION project_03.state_group_and_count( state map<text, int>, type text )\n" + "CALLED ON NULL INPUT\n" + "RETURNS map<text, int>\n"
                    + "LANGUAGE java AS '\n" + "    Integer count = (Integer) state.get(type);\n" + "    if (count == null)\n" + "        count = 1;\n" + "    else\n" + "        count++; \n"
                    + "    state.put(type, count);\n" + "    return state; ' ;");
            session.execute("CREATE OR REPLACE FUNCTION project_03.ccMapMax(input map<text, int>)\n" + "RETURNS NULL ON NULL INPUT\n" + "RETURNS map<text, int>\n" + "LANGUAGE java AS '\n"
                    + "    Integer max = Integer.MIN_VALUE;\n" + "    String data=\"\";\n" + "    for (String k : input.keySet()) {\n" + "        Integer tmp = input.get(k);\n"
                    + "        if (tmp > max) { max = tmp; data = k; }\n" + "    }\n" + "    Map<String,Integer> mm = new HashMap<String,Integer>();\n" + "    mm.put(data,max);\n" + "    return mm;\n"
                    + "';");
            session.execute("CREATE OR REPLACE AGGREGATE project_03.group_and_count_q34(text)\n" + "    SFUNC state_group_and_count\n" + "    STYPE map<text, int>\n" + "    FINALFUNC ccMapMax\n"
                    + "    INITCOND {};");

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            PreparedStatement preparedStatement = session
                    .prepare("INSERT INTO project_03.log (id, ip, identity, username, time, method, path, protocol, status, size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement ipStatement = session.prepare("UPDATE project_03.ip SET count = count + 1 WHERE ip = ?");
            PreparedStatement pathStatement = session.prepare("UPDATE project_03.path SET count = count + 1 WHERE path = ?");
            int count = 1;
            while (br.ready()) {
                semaphore.acquire();
                semaphore.acquire();
                semaphore.acquire();
                String line = br.readLine();
                Pattern pattern = Pattern.compile("([^ ]*) ([^ ]*) (.*) \\[(.*)\\] \"([^ ]*) ([^ ]*) *([^ ]*)\" ([^ ]*) ([^ ]*)");
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
                    BoundStatement ipBoundStatement = ipStatement.bind(ip);
                    BoundStatement pathBoundStatement = pathStatement.bind(path);
                    count++;

                    ResultSetFuture resultSetFuture = session.executeAsync(boundStatement);
                    ResultSetFuture ipFuture = session.executeAsync(ipBoundStatement);
                    ResultSetFuture pathFuture = session.executeAsync(pathBoundStatement);

                    Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
                        public void onSuccess(ResultSet resultSet) {
                            semaphore.release();
                        }

                        public void onFailure(Throwable throwable) {
                            semaphore.release();
                            System.out.println("Error: " + throwable.toString());
                        }
                    });

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
                if ((count - 1) % 20000 == 0) {
                    System.out.println("inserted " + (count - 1));
                }
            }
            System.out.println("inserted " + (count - 1));

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
        if (args.length == 0) {
            new ImportData().importData("/root/access_log");
        } else {
            new ImportData().importData(args[0]);
        }
        long end = System.currentTimeMillis();
        long total = (end - start) / 1000;
        System.out.println("Total running time: " + total + " seconds");
    }
}
