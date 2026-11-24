package com.elkin.reyes;
import com.elkin.reyes.api.ApiHandler; import com.elkin.reyes.db.Database; import com.sun.net.httpserver.HttpServer; import java.net.InetSocketAddress;
public class Main {
 public static void main(String[] args)throws Exception {Database.init();int port=Integer.parseInt(System.getenv().getOrDefault("PORT","8080"));HttpServer server=HttpServer.create(new InetSocketAddress(port),0);server.createContext("/",new ApiHandler());server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());server.start();System.out.println("Reyes Magos API activa en http://localhost:"+port);System.out.println("Java puro + HttpServer + JDBC + H2. Ctrl+C para detener.");}
}
