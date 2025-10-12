package com.fahim.ths;

import com.fahim.ths.repo.DatabaseInit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ServerMain {

    public static final int PORT = 5050;
    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        // make sure db exists / tables created
        DatabaseInit.init();
        DatabaseInit.seed();

        ExecutorService pool = Executors.newFixedThreadPool(16);
        System.out.println("ths server listening on " + PORT + " ...");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (running) {
                Socket socket = server.accept();
                pool.submit(new ClientHandler(socket));
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
        }
    }
}
