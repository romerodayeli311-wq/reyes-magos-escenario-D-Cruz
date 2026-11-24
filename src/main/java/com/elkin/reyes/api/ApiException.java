package com.elkin.reyes.api;
public class ApiException extends RuntimeException {
  public final int status; public final String codigo;
  public ApiException(int status,String codigo,String mensaje){super(mensaje);this.status=status;this.codigo=codigo;}
}
