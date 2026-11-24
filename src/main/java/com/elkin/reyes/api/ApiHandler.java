package com.elkin.reyes.api;

import com.sun.net.httpserver.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/** Punto de entrada HTTP: aplica middleware, delega al enrutador y centraliza errores. */
public class ApiHandler implements HttpHandler {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final SecurityMiddleware security = new SecurityMiddleware();
    private final RouteDispatcher routes = new RouteDispatcher();
    private final GlobalExceptionHandler errors = new GlobalExceptionHandler();

    @Override public void handle(HttpExchange x) throws IOException {
        try { security.authorize(x); routes.dispatch(x); }
        catch (Exception e) { errors.handle(x,e); }
        finally { x.close(); }
    }
    static void send(HttpExchange x,int status,Object data)throws IOException{
        byte[] b=JSON.writeValueAsBytes(data);
        x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        x.getResponseHeaders().set("Access-Control-Allow-Origin","*");
        x.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type, Authorization");
        x.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, PUT, PATCH, DELETE, OPTIONS");
        x.sendResponseHeaders(status,b.length);x.getResponseBody().write(b);
    }
}
