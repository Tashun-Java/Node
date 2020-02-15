package com.tsc.node.sevice;

import com.tsc.node.model.Neighbour;
import com.tsc.node.util.MessageCreate;
import com.tsc.node.util.NodeStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Service {
    private Neighbour btServer = NodeStore.getConfiguration().getBootstrapServer();
    private MessageCreate messageCreate;

    public Service() {
        messageCreate = new MessageCreate();
    }

    public MessageCreate getMessageCreate() {
        return messageCreate;
    }



}
