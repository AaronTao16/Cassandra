package Model;

public class Log {
    private long id;
    private String ip;
    private String time_stamp;
    private String req_method;
    private String req_url;
    private String req_protocol;
    private String res_status_code;
    private String res_size;

    public Log(long id, String ip, String time_stamp, String req_method, String req_url, String req_protocol, String res_status_code, String res_size) {
        this.id = id;
        this.ip = ip;
        this.time_stamp = time_stamp;
        this.req_method = req_method;
        this.req_url = req_url;
        this.req_protocol = req_protocol;
        this.res_status_code = res_status_code;
        this.res_size = res_size;
    }

    public Log(long id, String ip, String req_url) {
        this.id = id;
        this.ip = ip;
        this.req_url = req_url;
    }

    public Log(String ip, String req_url) {
        this.ip = ip;
        this.req_url = req_url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getReq_method() {
        return req_method;
    }

    public void setReq_method(String req_method) {
        this.req_method = req_method;
    }

    public String getReq_url() {
        return req_url;
    }

    public void setReq_url(String req_url) {
        this.req_url = req_url;
    }

    public String getReq_protocol() {
        return req_protocol;
    }

    public void setReq_protocol(String req_protocol) {
        this.req_protocol = req_protocol;
    }

    public String getRes_status_code() {
        return res_status_code;
    }

    public void setRes_status_code(String res_status_code) {
        this.res_status_code = res_status_code;
    }

    public String getRes_size() {
        return res_size;
    }

    public void setRes_size(String res_size) {
        this.res_size = res_size;
    }
}
