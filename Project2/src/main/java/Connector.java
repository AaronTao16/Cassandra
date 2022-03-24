import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

public class Connector {
    private CqlSession session;

    public void connect(final String node, final Integer port, final String dataCenter) {
        CqlSessionBuilder builder = CqlSession.builder();
        builder.addContactPoint(new InetSocketAddress(node, port));
//        builder.withAuthCredentials("ubuntu", "6da33c05aea6");
        if (StringUtils.isNotBlank(dataCenter)) {
            builder.withLocalDatacenter(dataCenter);
        }

        session = builder.build();
    }

    public CqlSession getSession() {
        return this.session;
    }

    public void close() {
        session.close();
    }
}
