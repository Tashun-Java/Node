package com.tsc.node.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    @JsonProperty("bootstrapServer")
    private Neighbour bootstrapServer;
    @JsonProperty("myNode")
    private Neighbour myNode;

    public Neighbour getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(Neighbour bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public Neighbour getMyNode() {
        return myNode;
    }

    public void setMyNode(Neighbour myNode) {
        this.myNode = myNode;
    }
}
