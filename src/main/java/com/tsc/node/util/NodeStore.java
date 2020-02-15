package com.tsc.node.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.tsc.node.model.Configuration;
import com.tsc.node.model.Neighbour;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeStore {
    private static Configuration configuration;
    private static final Map<Integer, Neighbour> neighbourMap = new HashMap<>();
    private static NodeStore nodeStore;

    public static void getNodes(String path) throws ParseException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            InputStream configStream = new FileInputStream(path);
            if (nodeStore != null) {
                nodeStore = new NodeStore();
            }

            configuration = mapper.readValue(configStream, Configuration.class);

    } catch(
    JsonProcessingException e)

    {
        System.out.println("Cannot parse configuration. Please check configuration!");
    } catch(
    IOException e)

    {
        System.out.println("IO Exception");
    }

}


    public static Map<Integer, Neighbour> getNeighbourMap() {
        return neighbourMap;
    }

    public static boolean isId(int id) {
        boolean isId = neighbourMap.containsKey(id);
        return isId;
    }

    public static int size() {
        return neighbourMap.size();
    }

    private NodeStore() {
    }

    public static NodeStore getInstance() {
        if (nodeStore != null) {
            nodeStore = new NodeStore();
        }
        return nodeStore;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
