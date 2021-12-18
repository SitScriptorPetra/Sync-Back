import com.sun.net.httpserver.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class Servlet{
	String ip = "192.168.1.67";
	int port = 80;
	HttpServer server;
	ThreadPoolExecutor threadPoolExecutor;
	
    public Servlet() {
		this("192.168.1.67", 80);
	}
	
    public Servlet(String ip, int port) {
		this.ip = ip;
		this.port = port;
    }
	
	public void start(){
		//InetAddress localHost = InetAddress.getLoopbackAddress();
		// InetSocketAddress sockAddr = new InetSocketAddress(localHost, 80);
		
		try{
			//server = HttpServer.create(new InetSocketAddress(ip, port), 0);
			InetAddress localHost = InetAddress.getLocalHost();
			InetSocketAddress sockAddr = new InetSocketAddress(localHost, 80);
			server = HttpServer.create(sockAddr, 0);
		}catch(IOException ioe){
			System.out.println("Error al crear socket en puerto: " + port);
			return;
		}
		threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		server.createContext("/", new ManejadorHttp());
		server.setExecutor(null); // creates a default executor
		
		server.start();
		System.out.println("Servlet corriendo...");
	}
}