package com.elkin.reyes.api;

import com.sun.net.httpserver.HttpExchange;

/** Middleware de autenticación y autorización mediante API Key Bearer. */
public class SecurityMiddleware {
    public void authorize(HttpExchange x) {
        String method=x.getRequestMethod(), path=x.getRequestURI().getPath();
        if(!requiresAuth(method,path)) return;
        String token=x.getRequestHeaders().getFirst("Authorization");
        if(token==null||!token.startsWith("Bearer ")) throw new ApiException(401,"NO_AUTORIZADO","Falta el token Bearer");
        String value=token.substring(7);
        String jefe=System.getenv().getOrDefault("TOKEN_JEFE_CARAVANA","dev-jefe-caravana");
        String ayudante=System.getenv().getOrDefault("TOKEN_AYUDANTE","dev-ayudante");
        if(value.equals(ayudante)) throw new ApiException(403,"ROL_INSUFICIENTE","El rol AYUDANTE no puede realizar esta operación");
        if(!value.equals(jefe)) throw new ApiException(401,"TOKEN_INVALIDO","El token no es válido");
    }
    private boolean requiresAuth(String m,String p){
        if(m.equals("GET")&&(p.equals("/api/caravanas")||p.matches("/api/caravanas/\\d+")||p.matches("/api/caravanas/\\d+/paradas")||p.matches("/api/caravanas/\\d+/itinerario")))return false;
        if(m.equals("PATCH")&&p.matches("/api/paradas/\\d+/estado"))return false;
        return p.startsWith("/api/");
    }
}
