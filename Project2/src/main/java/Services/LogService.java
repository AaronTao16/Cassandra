package Services;

import Model.Log;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class LogService {
    private String TABLE_NAME;

    private final CqlSession session;

    public LogService(CqlSession session) {
        this.session = session;
    }

    public void createLogTable(String keyspace) {
        this.TABLE_NAME = keyspace;
        deleteTable(keyspace);
//        CreateTable createTable = SchemaBuilder.createTable(TABLE_NAME).ifNotExists()
////                .withPartitionKey("id", DataTypes.UUID)
//                .withPartitionKey("ip", DataTypes.TEXT)
//                .withPartitionKey("req_url", DataTypes.TEXT);

//        id ip time_stamp req_method req_url req_protocol res_status_code res_size
        String query = "CREATE TABLE " +
                TABLE_NAME + "(" +
                "id bigint," +
                "ip text," +
                "time_stamp text," +
                "req_method text," +
                "req_url text," +
                "req_protocol text," +
                "res_status_code text," +
                "res_size text," +
                "PRIMARY KEY ((ip),req_url));";
        SimpleStatement simpleStatement = SimpleStatement.newInstance(query);
        executeStatement(simpleStatement, keyspace);
    }

    public void createIndex(String keySpace, String table, String index) {
        String query = "CREATE INDEX  IF NOT EXISTS " +
                "index_" + index + " ON " +
                keySpace + "." + table + " (" +
                index + ");";
        SimpleStatement simpleStatement = SimpleStatement.newInstance(query);
        executeStatement(simpleStatement, table);
    }

    /**
     * Insert logs into related tables using a batch query.
     *
     * @param log
     */
    public void insertLogBatch(Log log) {

        BoundStatement logBoundStatement = this.getLogInsertStatement(log);

        BatchStatement batch = BatchStatement.newInstance(DefaultBatchType.LOGGED, logBoundStatement);

        session.execute(batch);
    }

    public List<Log> selectAllLog(String keyspace) {
        Select select = QueryBuilder.selectFrom(TABLE_NAME).all();

        ResultSet resultSet = executeStatement(select.build(), keyspace);

        List<Log> result = new ArrayList<>();

        resultSet.forEach(x -> result.add(new Log(x.getInt("id"),x.getString("ip"), x.getString("req_url"))));

        return result;
    }

    public List<String> selectAllLogByURL(String keyspace) {
        Term term = new Term() {
            @Override
            public boolean isIdempotent() {
                return false;
            }

            @Override
            public void appendTo(@NonNull StringBuilder stringBuilder) {
                stringBuilder.append("/assets/img/release-schedule-logo.png");
            }
        };
        Select select = QueryBuilder.selectFrom(TABLE_NAME).countAll().whereColumn("req_url").isEqualTo(term);

        ResultSet resultSet = executeStatement(select.build(), keyspace);

        List<String> res = new ArrayList<>();

        resultSet.forEach(m -> res.add(m.toString()));

        return res;
    }

    /**
     * Delete table.
     *
     * @param tableName the name of the table to delete.
     */
    public void deleteTable(String tableName) {
        StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS ").append(tableName);

        final String query = sb.toString();
        session.execute(query);
    }

    private ResultSet executeStatement(SimpleStatement statement, String keyspace) {
        if (keyspace != null) {
            statement.setKeyspace(CqlIdentifier.fromCql(keyspace));
        }

        return session.execute(statement);
    }

    private BoundStatement getLogInsertStatement(Log log) {
        String insertQuery = new StringBuilder("").append("INSERT INTO ").append(TABLE_NAME)
                .append("(id, ip, time_stamp, req_method, req_url, req_protocol, res_status_code, res_size) ").append("VALUES (")
                .append(":id").append(", ")
                .append(":ip").append(", ")
                .append(":time_stamp").append(", ")
                .append(":req_method").append(", ")
                .append(":req_url").append(", ")
                .append(":req_protocol").append(", ")
                .append(":res_status_code").append(", ")
                .append(":res_size")
                .append(");").toString();

        PreparedStatement preparedStatement = session.prepare(insertQuery);

        return preparedStatement.bind(log.getId(),log.getIp(),log.getTime_stamp(),log.getReq_method(),log.getReq_url(),log.getReq_protocol(),log.getRes_status_code(),log.getRes_size());
    }
}
