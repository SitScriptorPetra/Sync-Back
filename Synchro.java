import com.sun.net.httpserver.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class Synchro {

    public static void main(String[] args) {
		Servlet s;
		
		if(args.length == 1){
			int colonLocation = args[0].indexOf(":");
			s = new Servlet(args[0].substring(0, colonLocation), Integer.parseInt(args[0].substring(colonLocation + 1)));
		}else if(args.length == 2){
			s = new Servlet(args[0], Integer.parseInt(args[1]));
		}else{
			s = new Servlet("192.168.1.67", 80);
		}
		s.start();
    }

}