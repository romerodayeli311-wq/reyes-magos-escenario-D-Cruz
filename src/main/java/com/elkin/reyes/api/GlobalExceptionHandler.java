package com.elkin.reyes.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Convierte excepciones en el formato JSON uniforme requerido por la consigna. */
public class GlobalExceptionHandler {
    public void handle(HttpExchange x, Exception e) throws IOException {
        String path=x.getRequestURI().getPath();
        if(e instanceof ApiException api) ApiHandler.send(x,api.status,error(api.codigo,api.getMessage(),path));
        else ApiHandler.send(x,500,error("ERROR_INTERNO","Error interno del servidor",path));
    }
    private Map<String,Object> error(String c,String m,String p){return Map.of("codigo",c,"mensaje",m,"detalles",List.of(),"timestamp",Instant.now().toString(),"ruta",p);}
}
