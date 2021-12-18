import java.io.*;

import java.util.Iterator;
import java.util.Map;
  
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class Coordinador{
	Reloj maestro;
	Reloj[] esclavos;
	enum Sync {cristian, berkeley}
	Sync tipoSync;
	int diferenciaRazonable = 60;
	
	public Coordinador(){
		this.maestro = new Reloj();
		this.esclavos = new Reloj[5];
		for(int i = 0; i < 5; i++){
			this.esclavos[i] = new Reloj();
		}
	}
	
	public Coordinador(String datosJSON){
		this();
		parsearLista(datosJSON);
	}
	
	public void parsearLista(String datosJSON){
		// parsing and typecasting datosJSON to JSONObject
		JSONParser parser = new JSONParser();
		JSONObject jo = null;
		try{
			jo = (JSONObject) parser.parse(datosJSON);
		}catch (ParseException pe){
			System.out.println("ERROR AL PARSEAR JSON.");
		}
          
        // getting syncType
        String syncType = (String) jo.get("sync");
        
		if(syncType.equals("cristian")){
			tipoSync = Sync.cristian;
		}else if(syncType.equals("berkeley")){
			tipoSync = Sync.berkeley;
		}
		System.out.println("Tipo de sync: " + tipoSync);
          
        // getting mapMaestro
        Map mapMaestro = ((Map)jo.get("maestro"));
		
		// iterating mapMaestro Map
		Iterator<Map.Entry> itr1 = mapMaestro.entrySet().iterator();
		while (itr1.hasNext()) {
			Map.Entry pair = itr1.next();
			System.out.print(pair.getKey() + " = ");
			
			Map relojMaestro = ((Map)pair.getValue());
			// iterating relojMaestro Map
			Iterator<Map.Entry> itr1Reloj = relojMaestro.entrySet().iterator();
			while (itr1Reloj.hasNext()) {
				Map.Entry pairReloj = itr1Reloj.next();
				System.out.print(pairReloj.getKey() + " : " + pairReloj.getValue() + ", ");
				if(pairReloj.getKey().equals("horas")){
					maestro.setHoras(Integer.parseInt(pairReloj.getValue().toString()));
				}else if(pairReloj.getKey().equals("minutos")){
					maestro.setMinutos(Integer.parseInt(pairReloj.getValue().toString()));
				}
			}
			System.out.println("");
		}
		  
		// getting esclavos
		JSONArray ja = (JSONArray) jo.get("esclavos");
		  
		// iterating esclavos
		Iterator itr2 = ja.iterator();
		  
		int index = 0;
		while (itr2.hasNext()) 
		{
			itr1 = ((Map) itr2.next()).entrySet().iterator();
			while (itr1.hasNext()) {
				Map.Entry pair = itr1.next();
				System.out.print(pair.getKey() + " = ");
				
				Map relojEsclavo = ((Map)pair.getValue());
				// iterating relojEsclavo Map
				Iterator<Map.Entry> itr1Reloj = relojEsclavo.entrySet().iterator();
				while (itr1Reloj.hasNext()) {
					Map.Entry pairReloj = itr1Reloj.next();
					System.out.print(pairReloj.getKey() + " : " + pairReloj.getValue() + ", ");
					if(pairReloj.getKey().equals("horas")){
						esclavos[index].setHoras(Integer.parseInt(pairReloj.getValue().toString()));
					}else if(pairReloj.getKey().equals("minutos")){
						esclavos[index].setMinutos(Integer.parseInt(pairReloj.getValue().toString()));
					}
				}
				index++;
				System.out.println("");
			}
		}
	}
	
	public byte[] enviarLista(){
        // creating JSONObject
        JSONObject jo = new JSONObject();
		
        // putting data to JSONObject
        jo.put("sync", tipoSync.toString());
		
		JSONObject obj2 = new JSONObject();
		JSONObject obj3 = new JSONObject();
		
		obj3.put("horas", maestro.getHoras());
		obj3.put("minutos", maestro.getMinutos());
		obj2.put("reloj", obj3);
		
		jo.put("maestro", obj2);
		
		JSONArray list = new JSONArray();
		JSONObject obj4;
		JSONObject obj5;
		
		for(int i = 0; i < 5; i++){
			obj4 = new JSONObject();
			obj4.put("horas", esclavos[i].getHoras());
			obj4.put("minutos", esclavos[i].getMinutos());
			
			obj5 = new JSONObject();
			obj5.put("reloj", obj4);
			
			list.add(obj5);
		}
		
		jo.put("esclavos", list);
          
        return jo.toJSONString().getBytes();
	}
	
	public String getMaestro(){
		return maestro.toString();
	}
	
	public String getEsclavo(int index){
		return esclavos[index].toString();
	}
	
	public String getAllEsclavos(){
		String salida = "";
		for(int i = 0; i < 5; i++){
			salida += getEsclavo(i) + "\n";
		}
		return salida;
	}
	
	public String getSyncType(){
		return tipoSync.toString();
	}
	
	public void sync(){
		if(tipoSync == Sync.cristian){
			syncCristian();
		}else{
			syncBerkeley();
		}
	}
	
	void syncBerkeley(){
		int[] diferenciaTiempos = new int[5];
		int suma = 0;
		int diferenciaMedia;
		int tiempoSicronizacionMaestro;
		int errores = 0;
		//Obtenemos el tiempo de los esclavos y calculamos la diferencia con el tiempo del maestro
		for(int i = 0; i < 5; i++){
			diferenciaTiempos[i] = esclavos[i].getTiempoMinutos() - maestro.getTiempoMinutos();
			if(Math.abs(diferenciaTiempos[i]) <= diferenciaRazonable){
				suma += diferenciaTiempos[i];
			}else{
				diferenciaTiempos[i] = 0;
				errores++;
			}
		}
		//Calculamos la diferencia media
		diferenciaMedia = suma/(5+1-errores);
		tiempoSicronizacionMaestro = maestro.getTiempoMinutos() + diferenciaMedia;
		maestro.setTiempoMinutos(tiempoSicronizacionMaestro);
		
		for(int i = 0; i < 5; i++){
			Reloj r = new Reloj();
			r.setTiempoMinutos(tiempoSicronizacionMaestro);
			this.esclavos[i] = r;
		}
	}
	
	void syncCristian(){
		for(int i = 0; i < 5; i++){
			this.esclavos[i] = new Reloj(maestro);
		}
	}
}