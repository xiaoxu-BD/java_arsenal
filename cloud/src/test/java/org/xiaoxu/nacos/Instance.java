package org.xiaoxu.nacos;

class Instance {
    private String ip;
    private int port;

    public Instance(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Instance{ip='" + ip + "', port=" + port + "}";
    }
}