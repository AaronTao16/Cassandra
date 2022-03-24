import Model.Log;
import Services.KeyspaceService;
import Services.LogService;
import com.datastax.oss.driver.api.core.CqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MainApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) throws FileNotFoundException {
        new MainApplication().run();
    }

    public void run() throws FileNotFoundException {
        Connector connector = new Connector();
        // locally test
//        connector.connect("localhost", 9042, "datacenter1");
        connector.connect("10.254.2.176", 9042, "datacenter1");
        CqlSession session = connector.getSession();

        KeyspaceService keyspaceService = new KeyspaceService(session);

        // create keyspace and set 2 replica
        keyspaceService.createKeyspace("project2", 2);
        keyspaceService.useKeyspace("project2");

        LogService logService = new LogService(session);

        // create table and index
        logService.createLogTable("test");
        logService.createIndex("project2", "index_req_url", "req_url");
        logService.createIndex("project2", "index_ip", "ip");

        try (BufferedReader br = new BufferedReader(new FileReader("test.csv"))) {
            String line;
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

        connector.close();
    }

}
