package com.elkin.reyes.repository;
import com.elkin.reyes.db.Database; import com.elkin.reyes.model.Parada; import java.sql.*; import java.util.*;
public class ParadaRepository {
  private Parada map(ResultSet r)throws SQLException{return new Parada(r.getLong("id"),r.getLong("caravana_id"),r.getString("ciudad"),r.getInt("orden"),r.getInt("dias_estancia"),r.getString("estado"));}
  public Parada create(long cid,String ciudad,int orden,int dias)throws SQLException{try(Connection c=Database.get();PreparedStatement p=c.prepareStatement("INSERT INTO paradas(caravana_id,ciudad,orden,dias_estancia,estado) VALUES(?,?,?,?, 'PLANIFICADA')",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,cid);p.setString(2,ciudad);p.setInt(3,orden);p.setInt(4,dias);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();return find(k.getLong(1));}}}
  public Parada find(long id)throws SQLException{try(Connection c=Database.get();PreparedStatement p=c.prepareStatement("SELECT * FROM paradas WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}}
  public List<Parada> byCaravana(long cid)throws SQLException{List<Parada>a=new ArrayList<>();try(Connection c=Database.get();PreparedStatement p=c.prepareStatement("SELECT * FROM paradas WHERE caravana_id=? ORDER BY orden")){p.setLong(1,cid);try(ResultSet r=p.executeQuery()){while(r.next())a.add(map(r));}}return a;}
  public void state(long id,String estado)throws SQLException{try(Connection c=Database.get();PreparedStatement p=c.prepareStatement("UPDATE paradas SET estado=? WHERE id=?")){p.setString(1,estado);p.setLong(2,id);p.executeUpdate();}}
  public void delete(long id)throws SQLException{try(Connection c=Database.get();PreparedStatement p=c.prepareStatement("DELETE FROM paradas WHERE id=?")){p.setLong(1,id);p.executeUpdate();}}
}
