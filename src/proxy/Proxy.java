package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

	public static void main(String[] args) {
		
		Proxy proxy = new Proxy();
		proxy.httpProxy(10240);
		
	}
	
	public void httpProxy(int port) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("The proxy has start on port:" + port + "\n");
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					new HttpProxyThread(socket).start();
				} catch (Exception e) {
					System.out.println("start thread fail\n");
				}
				
			}
		} catch (IOException e) {
			System.out.println("create serverSocket fail\n");
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("serverSocket close fail\n");
			}
		}
	}
	
	
}
