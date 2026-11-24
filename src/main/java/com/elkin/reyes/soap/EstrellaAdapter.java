package com.elkin.reyes.soap;
import java.net.URI; import java.net.http.*; import java.time.Duration; import javax.xml.parsers.*; import org.w3c.dom.*; import java.io.*;
public class EstrellaAdapter {
 public record Ruta(String visibilidad,int diasRestantes){}
 private final String endpoint=System.getenv().getOrDefault("SOAP_ESTRELLA_URL","http://localhost:8081/soap/estrella");
 public Ruta consultar(String origen){
   String xml="""
   <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:eb="http://belen.beatlech/estrella"><soapenv:Header/><soapenv:Body><eb:ConsultarRutaRequest><eb:origen>%s</eb:origen><eb:destino>Belen</eb:destino></eb:ConsultarRutaRequest></soapenv:Body></soapenv:Envelope>
   """.formatted(escape(origen));
   for(int intento=0;intento<2;intento++) try {
     HttpRequest req=HttpRequest.newBuilder(URI.create(endpoint)).timeout(Duration.ofSeconds(3)).header("Content-Type","text/xml; charset=utf-8").header("SOAPAction","ConsultarRuta").POST(HttpRequest.BodyPublishers.ofString(xml)).build();
     HttpResponse<String> res=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build().send(req,HttpResponse.BodyHandlers.ofString());
     if(res.statusCode()>=500)throw new IOException("SOAP HTTP "+res.statusCode());
     DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);Document d=f.newDocumentBuilder().parse(new ByteArrayInputStream(res.body().getBytes()));
     if(d.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/","Fault").getLength()>0)throw new IOException("SOAP Fault");
     String v=find(d,"visibilidad");String dias=find(d,"diasRestantes"); if(v==null)throw new IOException("Respuesta SOAP sin visibilidad");return new Ruta(v.trim().toUpperCase(),dias==null?0:Integer.parseInt(dias.trim()));
   }catch(Exception e){if(intento==1)throw new RuntimeException("Servicio de la Estrella no disponible",e);try{Thread.sleep(250);}catch(InterruptedException ie){Thread.currentThread().interrupt();}}
   throw new RuntimeException("Servicio de la Estrella no disponible");
 }
 private String find(Document d,String local){NodeList n=d.getElementsByTagNameNS("*",local);return n.getLength()==0?null:n.item(0).getTextContent();}
 private String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
