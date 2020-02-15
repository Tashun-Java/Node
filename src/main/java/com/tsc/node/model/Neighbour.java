package com.tsc.node.model;

public class Neighbour {
    private String ip;
    private int port;
    private String username;

    public Neighbour(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Neighbour() {
    }

    public Neighbour(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;
    }

    public String getIp() {
        return this.ip;
    }

    public String getUsername() {
        return this.username;
    }

    public int getPort() {
        return this.port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
