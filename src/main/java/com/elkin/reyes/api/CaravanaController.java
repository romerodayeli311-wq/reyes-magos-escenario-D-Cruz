package com.elkin.reyes.api;

import com.elkin.reyes.model.Caravana;
import com.elkin.reyes.model.Parada;
import com.elkin.reyes.service.CaravanaService;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

/** Controlador HTTP: transforma solicitudes en llamadas al servicio de negocio. */
public class CaravanaController {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final CaravanaService service = new CaravanaService();

    public void create(HttpExchange x) throws IOException { ApiHandler.send(x, 201, service.create(body(x))); }
    public void list(HttpExchange x) throws IOException { ApiHandler.send(x, 200, service.all(queryEstado(x))); }
    public void get(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.get(parseId(id))); }
    public void update(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.update(parseId(id), body(x))); }
    public void changeState(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.changeState(parseId(id), upper(body(x).get("estado")))); }
    public void addStop(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 201, service.addStop(parseId(id), body(x))); }
    public void stops(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.stops(parseId(id))); }
    public void changeStop(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.changeStop(parseId(id), upper(body(x).get("estado")))); }
    public void deleteStop(HttpExchange x, String id) throws IOException { service.deleteStop(parseId(id)); ApiHandler.send(x, 200, Map.of("mensaje", "Parada eliminada")); }
    public void itinerary(HttpExchange x, String id) throws IOException { ApiHandler.send(x, 200, service.itinerary(parseId(id))); }

    @SuppressWarnings("unchecked")
    private Map<String,Object> body(HttpExchange x) throws IOException {
        try { return JSON.readValue(x.getRequestBody(), Map.class); }
        catch (Exception e) { throw new ApiException(400, "JSON_INVALIDO", "El cuerpo debe ser un JSON válido"); }
    }
    private long parseId(String value) {
        try { return Long.parseLong(value); }
        catch (Exception e) { throw new ApiException(400, "ID_INVALIDO", "El id debe ser numérico"); }
    }
    private String upper(Object value) { return value == null ? "null" : value.toString().toUpperCase(java.util.Locale.ROOT); }
    private String queryEstado(HttpExchange x) {
        String q=x.getRequestURI().getRawQuery(); if(q==null)return null;
        for(String part:q.split("&")){String[] kv=part.split("=",2);if(kv.length==2&&kv[0].equals("estado"))return java.net.URLDecoder.decode(kv[1],java.nio.charset.StandardCharsets.UTF_8).toUpperCase(java.util.Locale.ROOT);}
        return null;
    }
}
