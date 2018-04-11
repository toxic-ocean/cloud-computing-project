package log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class is implemented to test the regular expression for reading the access_log file 
 */
public class Test {

    public void importData() {
        File file = new File("/access_log");
        try {

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            int ipCount = 0;
            int pathCount = 0;
            while (br.ready()) {
                String line = br.readLine();
                // Pattern pattern = Pattern.compile("([^ ]*) ([^ ]*) ([^ ]*) \\[(.*)\\] \"([^ ]*) ([^ ]*) ([^ ]*)\" ([^ ]*) ([^ ]*)");
                Pattern pattern = Pattern.compile("([^ ]*).*\"\\w+ ([^ ]*)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    if (ip.equals("")) {
                        System.out.println(line);
                    }
                    String path = matcher.group(2);
                    if (path.equals("")) {
                        System.out.println(line);
                    }
                    if (ip.equals("10.216.113.172")) {
                        ipCount++;
                    }
                    if (path.equals("/assets/css/combined.css")) {
                        pathCount++;
                    }
                }
            }
            System.out.println("10.216.113.172 " + ipCount);
            System.out.println("/assets/css/combined.css " + pathCount);

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new Test().importData();
        long end = System.currentTimeMillis();
        long total = (end - start) / 1000;
        System.out.println("Total running time: " + total + " seconds");
    }
}
