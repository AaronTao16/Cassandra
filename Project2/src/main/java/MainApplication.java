import Model.Log;
import Services.KeyspaceService;
import Services.LogService;
import com.datastax.oss.driver.api.core.CqlSession;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MainApplication {

    private static final Map<String, String> argsMap = new HashMap<>();
    private static final String IP = "localhost";
    private static final int PORT = 9042;
    private static final String DATA_CENTER = "datacenter1";
    private static final String KEYSPACE = "project2";
    private static final String TABLE = "access_log";
    private static final String DATA_FILE = "access_log.csv";
    private static final List<String> TABLES = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        initialize(args);
        new MainApplication().run();
    }

    private static void initialize(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if(args[i].charAt(0) == '-'){
                if(i+1 == args.length)
                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
                switch (args[i]){
                    case "-":
                        throw new IllegalArgumentException("Not a valid argument: "+args[i]);
                    case "-init":
                        argsMap.put("CREATE", "TRUE");
                        break;
                    case "-ip":
                        argsMap.put("IP", args[i+1]);
                        break;
                    case "-port":
                        argsMap.put("PORT", args[i+1]);
                        break;
                    case "-group":
                        argsMap.put("COLUMN", args[i+1]);
                        break;
                    case "-keyspace":
                        argsMap.put("KEYSPACE", args[i+1]);
                        break;
                    case "-table":
                        argsMap.put("TABLE", args[i+1]);
                        break;
                    case "-tables":
                        if(i+1 < args.length) TABLES.add(args[i+1]);
                        if(i+2 < args.length) TABLES.add(args[i+2]);
                        if(i+3 < args.length) TABLES.add(args[i+3]);
                        break;
                    case "-file":
                        argsMap.put("DATA_FILE", args[i+1]);
                        break;
                    case "-replica":
                        argsMap.put("REPLICA", args[i+1]);
                        break;
                    default:
                        throw new IllegalArgumentException("Not a valid argument: "+args[i] + "\n   -init <keyspace,table> \n -group <column> -keyspace <keyspace> -table <table>");
                }
            }
        }
    }

    public void run() {
        Connector connector = new Connector();
        // locally test
        connector.connect(argsMap.getOrDefault("IP", IP), PORT, DATA_CENTER);
//        connector.connect("10.254.2.176", 9042, "datacenter1");
        CqlSession session = connector.getSession();

        KeyspaceService keyspaceService = new KeyspaceService(session);

        // create keyspace and set 2 replica by default
        keyspaceService.createKeyspace(argsMap.getOrDefault("KEYSPACE", KEYSPACE), Integer.parseInt(argsMap.getOrDefault("REPLICA", "2")));
        keyspaceService.useKeyspace(argsMap.getOrDefault("KEYSPACE", KEYSPACE));

        LogService logService = new LogService(session);

        // create table and index
        if(argsMap.containsKey("CREATE") && argsMap.get("CREATE").equals("TRUE")){
            logService.createLogTable(argsMap.getOrDefault("TABLE", TABLE));
            logService.createIndex(argsMap.getOrDefault("KEYSPACE", KEYSPACE), argsMap.getOrDefault("TABLE", TABLE), "req_url");
            logService.createIndex(argsMap.getOrDefault("KEYSPACE", KEYSPACE), argsMap.getOrDefault("TABLE", TABLE), "ip");

            try (BufferedReader br = new BufferedReader(new FileReader(argsMap.getOrDefault("DATA_FILE", DATA_FILE)))) {
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(" ");
                    // access_log.csv
//                logService.insertLogBatch(new Log(values[0],values[1]));
                    // access_log_ip_url.csv or test.csv
                    logService.insertLogBatch(new Log(Long.parseLong(values[0]),values[1],values[2], values[3],values[4], values[5],values[6], values[7]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(argsMap.containsKey("COLUMN") && argsMap.get("COLUMN").equals("ip")){
            Map<String, Long> ip_res = new HashMap<>();
            for(String table: TABLES){
                ip_res = logService.SelectAllLogGroupBy(table, "ip", ip_res);
            }
            ip_res.entrySet().stream().sorted((a,b) -> b.getValue().compareTo(a.getValue())).limit(10).forEach(System.out::println);
        }

        if(argsMap.containsKey("COLUMN") && argsMap.get("COLUMN").equals("req_url")){
            Map<String, Long> url_res = new HashMap<>();
            for(String table: TABLES){
                url_res = logService.SelectAllLogGroupBy(table, "req_url", url_res);
            }
            url_res.entrySet().stream().sorted((a,b) -> b.getValue().compareTo(a.getValue())).limit(10).forEach(System.out::println);
        }

        connector.close();
    }

}
