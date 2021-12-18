import com.sun.net.httpserver.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class ManejadorHttp implements HttpHandler {
	String indexRoute = "front/index.html";
	String imgRoute = "front/img";
	
	public ManejadorHttp(){
	}
	
	@Override 
	public void handle(HttpExchange httpExchange) throws IOException {
		Map<String, String> requestParamValue=null; 
		if("GET".equals(httpExchange.getRequestMethod())) { 
			requestParamValue = handleGetRequest(httpExchange);
			handleGETResponse(httpExchange,requestParamValue); 
		}else if("POST".equals(httpExchange.getRequestMethod())) { 
			requestParamValue = handlePostRequest(httpExchange);  
			handlePOSTResponse(httpExchange,requestParamValue);       
		}
	}

	private Map<String, String> handleGetRequest(HttpExchange httpExchange) {
		System.out.println("\n\n=======\nParameter\n=======\n" + httpExchange.getRequestURI());
		//return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
		return reqGetParser(httpExchange.getRequestURI().toString());
		//return null;
	}

	private Map<String, String> handlePostRequest(HttpExchange httpExchange) {
		System.out.println("\n\n====\nPOST\n====");
		return null;
	}

	private void handleGETResponse(HttpExchange httpExchange, Map<String, String> requestParamValue)  throws  IOException {
		OutputStream outputStream;
		String filePath = httpExchange.getRequestURI().toString();
		boolean isImg = false;
		byte[] htmlResponse = null;
		
		if(filePath.contains("?")){
			int paramsIndex = filePath.indexOf("?");
			filePath = filePath.substring(0, paramsIndex);
			System.out.println(filePath);
		}
		
		if(filePath.equals("/")){
			filePath = indexRoute;
		}else if(filePath.equals("/favicon.ico")){
			isImg = true;
			filePath = imgRoute+"/favicon.ico";
		}else{
			isImg = filePath.startsWith("/img");
			filePath = imgRoute;
		}
		
		if((htmlResponse = ReadHTMLFile(filePath, isImg)) == null){
			String e404 = new String ("<html><meta charset=\"utf-8\"></meta><body><h1>404 Not Found</h1>No se encontró contexto para la solicitud.</body></html>".getBytes(), StandardCharsets.UTF_8);
            htmlResponse = e404.toString().getBytes(StandardCharsets.UTF_8);
			httpExchange.sendResponseHeaders(404, htmlResponse.length);
		}else{
			httpExchange.sendResponseHeaders(200, htmlResponse.length);
		}

		outputStream = httpExchange.getResponseBody();

		outputStream.write(htmlResponse);
		outputStream.flush();
		outputStream.close();
	}
	
	private void handlePOSTResponse(HttpExchange httpExchange, Map<String, String> requestParamValue)  throws  IOException {
		System.out.println("LE POS");
		OutputStream outputStream;
		Headers reqh, resh;
		byte[] htmlResponse = " ".getBytes();
		String uri = httpExchange.getRequestURI().toString();
		Coordinador c;
		
		//System.out.println(getBodyString(httpExchange));
		c = new Coordinador(getBodyString(httpExchange));
		
		c.sync();
		System.out.println("=== SYNC ===");
		System.out.println(c.getSyncType());
		System.out.println("=== MAESTRO ===");
		System.out.println(c.getMaestro());
		System.out.println("=== ESCLAVOS ===");
		System.out.println(c.getAllEsclavos());
		
		resh = httpExchange.getResponseHeaders();
		//resh.add("content-encoding", "identity");
		resh.add("content-type", "application/json");
		//resh.add("vary", "Accept-Encoding");
		//resh.add("x-frame-options", "SAMEORIGIN");
		
		htmlResponse = c.enviarLista();
		
		httpExchange.sendResponseHeaders(200, htmlResponse.length);

		outputStream = httpExchange.getResponseBody();
		outputStream.write(htmlResponse);
		outputStream.flush();
		outputStream.close();
	}
	
	private byte[] ReadHTMLFile(String filePath, boolean isImg){
		byte[] content; 
        try
        {
			if(isImg){
				content = Files.readAllBytes(Paths.get(filePath));
			}else{
				content = (new String (Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)).getBytes();
			}
        } 
        catch (IOException e) 
        {
			System.out.println("Error 404.");
			content = null;
        }
        return content;
	}
	
	public Map<String, String> reqGetParser(String uri){
		Map<String, String> data = null;
		try{
			if(uri.contains("?")){
				data = new HashMap<String, String>();
				System.out.println("Parser:");
				for(String s : uri.split("\\?")[1].split("\\&")){
					String[] aux = s.split("=");
					System.out.println("[" + aux[0] + ", " + aux[1] + "]");
					data.put(aux[0], aux[1]);
				}
			}
		}catch(IndexOutOfBoundsException ioobe){
			System.out.println("Error!");
			data = null;
		}
		return data;
	}
	
	public Map<String, String> reqPostParser(HttpExchange httpExchange) throws IOException {
		String boundary = "";
		int indexString;
		String body;
		String[] bodyLines;
		Map<String, String> data;
		String key = "";
		String value = "";
		
		boundary = httpExchange.getRequestHeaders().getFirst("Content-Type");
		indexString = boundary.indexOf("boundary=");
		boundary = boundary.substring(indexString + 9);
		
		body = getBodyString(httpExchange);
		bodyLines = body.split("(\r\n|\r|\n)", -1);
		System.out.println(", B: [" + boundary + "]");
		
		int i = -1;
		data = new HashMap<String, String>(); 
		for(String s : bodyLines){			
			if(s.equals("--" + boundary)){
				i++;
				if(i > 0)
					data.put(key, value);
			}else if(s.startsWith("Content-Disposition")){
				indexString = s.indexOf("name=");
				key = s.substring(indexString + 5).replace("\"", "");
				value = "";
			}else if(s.equals("--" + boundary + "--")){
				data.put(key, value);
				break;
			}else if(s.length() > 0){
				value+=s;
			}
		}
		
		return data;
	}
	
	public String getBodyString(HttpExchange httpExchange) throws IOException {
		InputStream inputStream;
		int reqBodyLength;
		byte[] reqBodyBytes;
		
		reqBodyLength = Integer.parseInt(httpExchange.getRequestHeaders().getFirst("Content-Length"));
		
		System.out.print("L: " + reqBodyLength);
		reqBodyBytes = new byte[reqBodyLength];
		
		inputStream = httpExchange.getRequestBody();
		inputStream.read(reqBodyBytes, 0, reqBodyLength);
		
		return new String (reqBodyBytes, StandardCharsets.UTF_8);
	}
}