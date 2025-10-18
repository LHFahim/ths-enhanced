//package com.fahim.ths;
//
//import com.fahim.ths.repo.DatabaseInit;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.*;
//
//public class ServerMain {
//
//    public static final int PORT = 5050;
//    private static volatile boolean running = true;
//
//    public static void main(String[] args) throws Exception {
//        // make sure db exists / tables created
//        DatabaseInit.init();
//        DatabaseInit.seed();
//
//        ExecutorService pool = Executors.newFixedThreadPool(16);
//        System.out.println("ths server listening on " + PORT + " ...");
//
//        try (ServerSocket server = new ServerSocket(PORT)) {
//            while (running) {
//                Socket socket = server.accept();
//                pool.submit(new ClientHandler(socket));
//            }
//        } catch (IOException e){
//            e.printStackTrace();
//        } finally {
//            pool.shutdownNow();
//        }
//    }
//}



//package com.fahim.ths;
//
//import com.fahim.ths.repo.DatabaseInit;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.*;
//
//public class ServerMain {
//    public static final int PORT = 5050;
//    public static void main(String[] args) throws Exception {
//        DatabaseInit.init();
//        DatabaseInit.seed();
//        ExecutorService pool = Executors.newFixedThreadPool(16);
//        System.out.println("ths server listening on " + PORT + " ...");
//        try (ServerSocket server = new ServerSocket(PORT)) {
//            while (true) {
//                Socket socket = server.accept();
//                pool.submit(new ClientHandler(socket));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            pool.shutdownNow();
//        }
//    }
//}


package com.fahim.ths;

import com.fahim.ths.repo.DatabaseInit;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    public static final int PORT = 5050;

    /** Standalone entrypoint: run this class by itself. */
    public static void main(String[] args) {
        try {
            startBlocking(); // runs forever until you stop it
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Core server loop. */
    private static void startBlocking() throws Exception {
        // Ensure DB schema/seed users exist
        try { DatabaseInit.init(); } catch (Exception initEx) { initEx.printStackTrace(); }

        ExecutorService pool = Executors.newFixedThreadPool(16);
        System.out.println("THS server listening on " + PORT + " ...");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket s = server.accept();
                // Your existing request handler that serves doctor/patient commands
                pool.submit(new ClientHandler(s));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
        }
    }
}
