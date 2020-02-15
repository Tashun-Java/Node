package com.tsc.node;

import com.tsc.node.model.Neighbour;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StartNode {
    public static void main(String[] args) {
        DatagramSocket sock = null;
        String s;
        Neighbour bootstrapServer = new Neighbour("127.0.0.1", 55555, "bs");
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        List<String> storedFiles = new ArrayList<>();
        ArrayList<String> metaData = new ArrayList<>(Arrays.asList("Adventures of Tintin",
                "Jack and Jill",
                "Glee",
                "The Vampire Diarie",
                "King Arthur",
                "Windows XP",
                "Harry Potter",
                "Kung Fu Panda",
                "Lady Gaga",
                "Twilight",
                "Windows 8",
                "Mission Impossible",
                "Turn Up The Music",
                "Super Mario",
                "American Pickers",
                "Microsoft Office 2010",
                "Happy Feet",
                "Modern Family",
                "American Idol",
                "Hacking for Dummies"));
        try {
            InetAddress bsIP = InetAddress.getByName(bootstrapServer.getIp());
            InetAddress myIP = InetAddress.getByName(bootstrapServer.getIp());

            int port = Integer.parseInt(args[0]);
            ServerSocket server = new ServerSocket(port);
            String username = args[1];

            sock = new DatagramSocket(port);

            Random rand = new Random();
            int numFiles = 3 + rand.nextInt(3);
            metaData = shuffleArray(metaData);
            for (int j = 0; j < numFiles; j++) {
                storedFiles.add(metaData.get(j));
                echo(metaData.get(j) + " " + new Date().toString());
            }
            String reg = String.format(" REG %s %d %s", myIP, port, username);
            int regLength = reg.length() + 4;
            reg = String.format("%04d", regLength) + reg;
            byte[] regData = reg.getBytes();

            DatagramPacket request = new DatagramPacket(regData, regData.length, bsIP, 55555);
            sock.send(request);
//            String msg = String.format(" SIZE ");
//            int msgLength = msg.length() + 4;
//            msg = String.format("%04d", msgLength) + msg;
//            byte[] data = msg.getBytes();
//
//            DatagramPacket sizeRequest = new DatagramPacket(data, data.length, bsIP
//                    , 55555);
//            sock.send(sizeRequest);

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data_recieve = incoming.getData();
                s = new String(data_recieve, 0, incoming.getLength());
                echo("Incoming String " + s + " " + new Date().getTime());
                s.replace("\n", "");
                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (Integer.parseInt(length) != s.length()) {
                    echo("Expected length : " + s.length() + ", Found: " + length);
                    String reply = "0015 REGOK 9999";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                } else {
                    switch (command) {
//                        case ("REPSIZE"): {
//                            int numFile;
//                            double size = Integer.parseInt(st.nextToken());
//                            if (size > 0) {
//                                numFile = (int) Math.round(metaData.size() / size);
//                            } else {
//                                numFile = metaData.size();
//                            }
//                            storedFiles = getFiles(metaData, numFile);
//                            String reg = String.format(" REG %s %d %s", myIP, port, username);
//                            int regLength = msg.length() + 4;
//                            msg = String.format("%04d", regLength) + reg;
//                            byte[] regData = msg.getBytes();
//
//                            DatagramPacket request = new DatagramPacket(regData, regData.length, bsIP, 55555);
//                            sock.send(request);
//                        break;
//                        }
                        case "REGOK": {
                            int statusCode = Integer.parseInt(st.nextToken());
                            echo(String.format("REGOK Status %d", statusCode));
                            if (statusCode < 900) {
                                for (int n = 0; n < statusCode; n++) {
                                    String peerIP = st.nextToken();
                                    if (peerIP.contains("/")) {
                                        peerIP = peerIP.substring(1);
                                    }
                                    int peerPort = Integer.parseInt(st.nextToken());
                                    nodes.add(new Neighbour(peerIP, peerPort));
                                    String joinMsg = "JOIN " + InetAddress.getByName(bootstrapServer.getIp()) + " " + port;
                                    joinMsg = String.format("%04d", joinMsg.length() + 5) + " " + joinMsg;
                                    sock.send(new DatagramPacket(joinMsg.getBytes(), joinMsg.getBytes().length, InetAddress.getByName(peerIP), peerPort));
                                }
                            }
                            echo(String.format("Have %d peer nodes", nodes.size()));
                            break;
                        }
                        case "ECHO": {
                            for (Neighbour node : nodes) {
                                echo(node.getIp() + " " + node.getPort() + " " + node.getUsername());
                            }
                            String reply = "0012 ECHOK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                            sock.send(dpReply);
                            break;
                        }
                        case "JOIN": {
                            String peerIP = st.nextToken();
                            if (peerIP.contains("/")) {
                                peerIP = peerIP.substring(1);
                            }
                            int peerPort = Integer.parseInt(st.nextToken());
                            nodes.add(new Neighbour(peerIP, peerPort));
                            String reply = "0013 JOINOK 0";
                            echo(reply + " " + new Date().getTime());
                            sock.send(new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort()));
                            break;
                        }
                        case "LEAVE_SEND": {
                            // unregister the peer itself from the bootstrap server
                            String unregMsg = "UNREG " + myIP + " " + port;
                            unregMsg = String.format("%04d", unregMsg.length() + 5) + " " + unregMsg;
                            echo("Going to shut down Node");
                            echo(unregMsg + " " + new Date().getTime());
                            sock.send(new DatagramPacket(unregMsg.getBytes(), unregMsg.getBytes().length, bsIP, 55555));

                            break;
                        }
                        case "LEAVE": {
                            echo("Shutting down Node");
                            // unregister the peer itself from the bootstrap server
                            String unregMsg = "UNREG " + myIP + " " + port;
                            unregMsg = String.format("%04d", unregMsg.length() + 5) + " " + unregMsg;
                            echo(unregMsg + " " + new Date().getTime());
                            break;
                        }

                        case "SER": {
                            String clientIP = st.nextToken();
                            if (clientIP.contains("/")) {
                                clientIP = clientIP.substring(1);
                            }
                            String clientPort = st.nextToken();
                            String query = st.nextToken();
                            String hops_temp = st.nextToken();
                            while (st.hasMoreTokens()) {
                                query += " " + hops_temp;
                                hops_temp = st.nextToken();
                            }
                            int hops = Integer.parseInt(hops_temp);
//                            StringTokenizer stringTokenizer = new StringTokenizer(s, "\"");
//                            stringTokenizer.nextToken();
//                            String query = "\"" + stringTokenizer.nextToken() + "\"";

                            List<String> search = searchStore(query, storedFiles);//reg ex
                            if (!search.isEmpty()) {
                                String serReply = "SEROK " + search.size() + " " + myIP.getHostAddress() + " " + port +
                                        " " + hops;
                                for (String s1 : search) {
                                    serReply += " " + s1;
                                }
                                serReply = String.format("%04d", serReply.length() + 5) + " " + serReply;
                                echo(serReply + " " + new Date().getTime());
                                sock.send(new DatagramPacket(serReply.getBytes(), serReply.getBytes().length, InetAddress.
                                        getByName(clientIP), Integer.parseInt(clientPort)));
                            }
                            if (hops > 0) {
                                hops--;
                                String newSearch = "SER " + clientIP + " " + clientPort + " " + query +
                                        " " + hops;
                                newSearch = String.format("%04d", newSearch.length() + 5) + " " + newSearch;

                                for (Neighbour neighbour : nodes) {
                                    sock.send(new DatagramPacket(newSearch.getBytes(), newSearch.getBytes().length,
                                            InetAddress.getByName(neighbour.getIp()), neighbour.getPort()));
                                }
                            } else if (hops == 0) {
                                String newSearch = "SEROK 0 " + myIP + " " + port + " " + hops;
                                newSearch = String.format("%04d", newSearch.length() + 5) + " " + newSearch;
                                echo(newSearch);
                                sock.send(new DatagramPacket(newSearch.getBytes(), newSearch.getBytes().length,
                                        InetAddress.getByName(clientIP), Integer.parseInt(clientPort)));
                            }

                            break;
                        }
                        case "DOWNLOAD": {
                            //length download ip port "filename"
                            String clientIP = st.nextToken();
                            System.out.println(clientIP);
                            if (clientIP.contains("/")) {
                                clientIP = clientIP.substring(1);
                            }
                            String clientPort = st.nextToken();
                            String fileName = null;
                            if (st.hasMoreTokens()) {
                                StringTokenizer stringTokenizer = new StringTokenizer(s, "\"");
                                stringTokenizer.nextToken();
                                fileName = stringTokenizer.nextToken();
                            } else {
                                echo("No File Name Found");
                                break;
                            }

                            if (storedFiles.contains(fileName)) {
                                echo(fileName + " File Found");

                                Random r = new Random();
                                int x = 1 + r.nextInt(10);
                                byte[] bytes = new byte[x * 1024 * 1024]; // x MB size byte array
                                r.nextBytes(bytes);
                                String digest;
                                int fileSize = 2;
//                                byte[] bytes = new byte[fileSize * 1024 * 1024];
                                try {
                                    MessageDigest md = MessageDigest.getInstance("MD5");
                                    byte[] hash = md.digest(bytes);

                                    //converting byte array to Hexadecimal String
                                    StringBuilder sb = new StringBuilder(2 * hash.length);
                                    for (byte b : hash) {
                                        sb.append(String.format("%02x", b & 0xff));
                                    }
                                    digest = sb.toString();


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                FileOutputStream fr = new FileOutputStream(fileName + "_" + new Date().getTime() + ".mp3");
                                fr.write(bytes, 0, bytes.length);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (true) {
                                            Thread t = new Thread() {
                                                public void run() {
                                                    echo("BEFORE FILE SEND " + new Date().toString());
                                                    OutputStream os = null;
                                                    try {
                                                        Socket sr = server.accept();
                                                        os = sr.getOutputStream();
//                                                        os.write(bytes, 0, bytes.length);
                                                        if (!sr.isClosed()) {
                                                            sr.close();
                                                        }

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    echo("FILE SENT " + new Date().toString());

                                                    // close the socket


                                                }
                                            };
                                            t.start();
                                        }
                                    }
                                }).start();


                                // send udp ack to accept the tcp sent file
                                String download_ok_msg = "DOWNLOADOK " + clientIP + " " + port + " " + fileName;
                                download_ok_msg = String.format("%04d", download_ok_msg.length() + 5) + " " + download_ok_msg;
                                sock.send(new DatagramPacket(download_ok_msg.getBytes(), download_ok_msg.getBytes().length,
                                        InetAddress.getByName(clientIP), Integer.parseInt(clientPort)));

                            }
                            break;
                        }
                    }

                }
            }

        } catch (IOException e) {
            System.err.println("IOException " + e.getStackTrace());
        }

    }

    public static List<String> searchStore(String query, List<String> storedArray) {
        List<String> tempArray = new ArrayList<>();
        List<String> tmpstoredArray = new ArrayList<>();
        storedArray.forEach(f -> {
            tmpstoredArray.add(f.toLowerCase());
        });
        tmpstoredArray.forEach(f -> {
            if (f.contains(query.toLowerCase())) {
                tempArray.add(f);
            }
        });
        return tempArray;
    }

    public static List<String> getFiles(List<String> metaData, int size) {
        ArrayList<String> tempArray = new ArrayList<>();
        ArrayList<Integer> positionArray = new ArrayList<Integer>();
        Random random = new Random();
        while (tempArray.size() == size) {
            int position = random.nextInt(metaData.size());
            while (positionArray.contains(position)) {
                position = random.nextInt(metaData.size());
            }
            positionArray.add(position);
            tempArray.add(metaData.get(position));
        }
        return tempArray;
    }

    public static ArrayList<String> shuffleArray(ArrayList<String> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            Random rand = new Random();
            int j = i + rand.nextInt(arrayList.size() - i);
            String temp = arrayList.get(i);
            arrayList.set(i, arrayList.get(j));
            arrayList.set(j, temp);
        }
        return arrayList;
    }

    //simple function to echo data to terminal
    public static void echo(String msg) {
        System.out.println(msg);
    }
}
