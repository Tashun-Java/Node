package com.tsc.node.util;

import com.tsc.node.model.Neighbour;

public class MessageCreate {
    private String delimiter = " ";

    public String serverRegMessage() {
        String regMessage = "REG";
        Neighbour myNode = NodeStore.getConfiguration().getMyNode();
        regMessage = regMessage + delimiter +
                String.valueOf(myNode.getIp()) + delimiter +
                String.valueOf(myNode.getPort() + delimiter +
                        String.valueOf(myNode.getUsername()));
        regMessage = String.format("%04d", regMessage.length() + 5) + " " + regMessage;
        return regMessage;
    }
}
