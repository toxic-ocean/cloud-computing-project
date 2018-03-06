import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    private static void SOP(String x) {
        System.out.println(x);
    }

    /*
    Group 1: 10.223.157.186
    Group 2: -
    Group 3: -
    Group 4: 15/Jul/2009:15:50:35 -0700
    Group 5: GET
    Group 6: /assets/css/the-associates.css
    Group 7: HTTP/1.1
    Group 8: 200
    Group 9: 15779 (Optional)
    */

    public static void main(String[] args) {
        String apache_log = "10.223.157.186 - - [15/Jul/2009:15:50:35 -0700] \"GET /assets/css/the-associates.css HTTP/1.1\" 200 15779";
        String a1 = "10.211.47.159 - - [03/Jan/2010:19:13:59 -0800] \"GET /crm/publications.php?_search=false&nd=1262574839254&rows=50&page=1&sidx=media_outlet&sord=asc HTTP/1.1\" 200 1554";
        String a2 = "10.211.47.159 - - [03/Jan/2010:19:18:05 -0700] \"GET /assets/css/combined.css HTTP/1.1\" 200 5911";
        String a3 = "10.6.73.193 - - [04/Jan/2010:04:10:10 -0800] \"GET /assets/css/combined.css HTTP/1.1\" 200 6373";
        String a4 = "10.223.157.186 - - [15/Jul/2009:14:58:59 -0700] \"GET / HTTP/1.1\" 403 202";

        StringTokenizer itr = new StringTokenizer(apache_log);
        while (itr.hasMoreTokens()) {
            SOP(itr.nextToken());
        }
        SOP("------------------");
        // https://regex101.com/r/Vg6rhA/1
        //Pattern logPattern = Pattern.compile("^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)");
        Pattern logPattern = Pattern.compile("^(\\S+).*\"(\\S+) (\\S+).*");
        /*
        Group 1: 10.223.157.186
        Group 2: GET
        Group 3: /assets/css/the-associates.css
        */
        Matcher matcher = logPattern.matcher(a1);
        if (matcher.matches()) {
            for (int j = 0; j < 1; j++) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    SOP("Group " + i + ": " + matcher.group(i));
                }
            }
        }
    }
}
