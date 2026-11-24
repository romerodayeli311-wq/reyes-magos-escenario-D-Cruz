package com.elkin.reyes.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;

/** Enrutador: asocia método + ruta REST con el controlador correspondiente. */
public class RouteDispatcher {
    private final CaravanaController controller = new CaravanaController();

    public void dispatch(HttpExchange x) throws IOException {
        String path=x.getRequestURI().getPath(), method=x.getRequestMethod();
        if(path.equals("/") && method.equals("GET")) { ApiHandler.send(x,200,Map.of("servicio","Reyes Magos API","estado","activo","tecnologia","Java puro + HttpServer + H2")); return; }
        String[] a=path.split("/");
        if(path.equals("/api/caravanas")&&method.equals("POST")){controller.create(x);return;}
        if(path.equals("/api/caravanas")&&method.equals("GET")){controller.list(x);return;}
        if(a.length==4&&a[1].equals("api")&&a[2].equals("caravanas")&&method.equals("GET")){controller.get(x,a[3]);return;}
        if(a.length==5&&a[1].equals("api")&&a[2].equals("caravanas")&&a[4].equals("itinerario")&&method.equals("GET")){controller.itinerary(x,a[3]);return;}
        if(a.length==5&&a[1].equals("api")&&a[2].equals("caravanas")&&a[4].equals("estado")&&method.equals("PATCH")){controller.changeState(x,a[3]);return;}
        if(a.length==4&&a[1].equals("api")&&a[2].equals("caravanas")&&method.equals("PUT")){controller.update(x,a[3]);return;}
        if(a.length==5&&a[1].equals("api")&&a[2].equals("caravanas")&&a[4].equals("paradas")&&method.equals("POST")){controller.addStop(x,a[3]);return;}
        if(a.length==5&&a[1].equals("api")&&a[2].equals("caravanas")&&a[4].equals("paradas")&&method.equals("GET")){controller.stops(x,a[3]);return;}
        if(a.length==5&&a[1].equals("api")&&a[2].equals("paradas")&&a[4].equals("estado")&&method.equals("PATCH")){controller.changeStop(x,a[3]);return;}
        if(a.length==4&&a[1].equals("api")&&a[2].equals("paradas")&&a[3].matches("\\d+")&&method.equals("DELETE")){controller.deleteStop(x,a[3]);return;}
        throw new ApiException(404,"RUTA_NO_ENCONTRADA","No existe esa ruta");
    }
}
