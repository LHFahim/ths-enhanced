package com.fahim.ths;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ThsClient {

    private final String host;
    private final int port;

    public ThsClient(String host, int port){
        this.host = host; this.port = port;
    }

    public Response send(String op, Map<String, Object> payload) throws IOException {
        try (Socket s = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            Request req = new Request();
            req.op = op;
            req.payload = payload;

            out.write(JsonUtil.toJson(req));
            out.write("\n");
            out.flush();

            String line = in.readLine();
            return JsonUtil.fromJson(line, Response.class);
        }
    }
}
