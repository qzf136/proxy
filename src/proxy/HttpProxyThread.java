package proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HttpProxyThread extends Thread {
	
	private Socket cSocket;
	private Socket sSocket;
	
	private String host = "";
	private int port = 80;
	
	private Set<String> guideHost = new HashSet<String>() {{add("jwc.hit.edu.cn");}};		// µˆ”„Õ¯’æ
	private Set<String> forbidHost = new HashSet<String>(){{add("jwc.hit.edu.cn");}};		// Ω˚÷π∑√Œ µƒÕ¯’æ
	private Set<String> forbidUser = new HashSet<String>() {{add("/127.0.0.1");}};		// Ω˚÷π∑√Œ µƒ”√ªß
	public HttpProxyThread(Socket cSocket) {
		this.cSocket = cSocket;
	}
	
	@Override
	public void run() {
		try {
			 // Ω˚÷π”√ªß∑√Œ 
//			if(forbidUser.contains(cSocket.getInetAddress().toString())) {
//				System.err.println("Current user is forbidden to access! " +  cSocket.getInetAddress());
//				return;
//			}
			
			InputStream cis = cSocket.getInputStream();
			OutputStream cos = cSocket.getOutputStream();
			
			BufferedReader cisReader = new BufferedReader(new InputStreamReader(cis));
			String line = cisReader.readLine();
			System.out.println("firstline: " + line);
			if(line.contains("443")) {
				return;
			}
			String urlStr = getUrl(line);
			System.out.println("url: " + urlStr);
			parseUrl(urlStr);
			
			// Ω˚÷π∑√Œ ƒ≥–©Õ¯’æ
//			if(forbidHost.contains(host)) {
//				System.err.println("forbid to visit " + urlStr + "\n");
//				return;
//			}
//			
			File file = createfile(urlStr);
			long lastmodify = file.lastModified();
			String modify = formatTime(lastmodify);
			
			if (guideHost.contains(host)) {
				sSocket = guide();
				InputStream sis = sSocket.getInputStream();
				byte[] bytes = new byte[1024];
	            int len = -1;
	            while((len=sis.read(bytes))!=-1) {
	                cos.write(bytes, 0, len);
	                cos.flush();
	            }
	            sis.close();
			} else {
				sSocket = new Socket(host, port);
				if (file.length() == 0) {
					StringBuffer buffer = new StringBuffer();
					while (!line.isEmpty()) {
						buffer.append(line + "\r\n");
						line = cisReader.readLine();
					}
					buffer.append("\r\n");
					OutputStream sos = sSocket.getOutputStream();
					sos.write(buffer.toString().getBytes());
					sos.flush();
					System.err.println(buffer);
					InputStream sis = sSocket.getInputStream();
					OutputStream stream = new FileOutputStream(file);
					byte[] bytes = new byte[1024];
		            int len = -1;
		            while((len=sis.read(bytes))!=-1) {
		                cos.write(bytes, 0, len);
		                stream.write(bytes, 0, len);
		                cos.flush();
		            }
		            stream.close();
				} else {
					StringBuffer buffer = new StringBuffer();
					buffer.append(line + "\r\n");
					buffer.append("If-modified-since: " + modify + "\r\n");
					while (!(line = cisReader.readLine()).isEmpty()) {
						buffer.append(line + "\r\n");
					}
					buffer.append("\r\n");
					OutputStream sos = sSocket.getOutputStream();
					sos.write(buffer.toString().getBytes());
					sos.flush();
					System.err.println(buffer.toString());
					InputStream sis = sSocket.getInputStream();
					byte[] bytes = new byte[1024];
					sis.read(bytes);
					if (bytes.toString().contains("Not Modified")) {
						InputStream fStream = new FileInputStream(file);
						byte[] bs = new byte[1024];
						while ((fStream.read(bs)) != -1) {
							cos.write(bs);
							cos.flush();
						}
						fStream.close();
					} else {
						InputStream fStream = new FileInputStream(file);
						byte[] filebytes = new byte[1024];
						while ((fStream.read(filebytes)) != -1) {
							cos.write(filebytes);
							cos.flush();
						} 
					}
				}	
			}
			
            sSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUrl(String firstline) {
		String[] strings = firstline.split(" ");
		return strings[1];
	}
	
	public void parseUrl(String urlStr) throws Exception {
		URL url = new URL(urlStr);
		host = url.getHost();
	}
	
	public File createfile(String urlStr) throws Exception {
		int index = urlStr.indexOf("//");
		String p = urlStr.substring(index+2);
		String path = "src/" + p + "0.dat";
		if (path.contains("?"))	
			path = path.replace("?", "1234");
		System.out.println("path: " + path);
		File file = new File(path);
		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
		}
		return file;
	}
	
	public Socket guide() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GET http://today.hit.edu.cn/ HTTP/1.1\r\n");
		buffer.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134\r\n");
		buffer.append("Accept-Language: zh-Hans-CN,zh-Hans;q=0.8,en-US;q=0.5,en;q=0.3\r\n");
		buffer.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
		buffer.append("Upgrade-Insecure-Requests: 1\r\n");
		buffer.append("Accept-Encoding: gzip, deflate\r\n");
		buffer.append("Host: today.hit.edu.cn\r\n");
		buffer.append("Proxy-Connection: Keep-Alive\r\n");
		buffer.append("Cookie: SESS0b78b3575298f2ed94ea5549d866ad3c=jkMbQG0vgl-tzr861UwJf7pYE76MheY3o3YKUKB6G_M; __utma=161430584.235118956.1507124793.1507124793.1507124793.1; bdshare_firstime=1507124764369; _ga=GA1.3.235118956.1507124793\r\n");
		buffer.append("\r\n");
		Socket socket = new Socket("today.hit.edu.cn", 80);
		OutputStream sos = socket.getOutputStream();
		sos.write(buffer.toString().getBytes());
		sos.flush();
		return socket;
	}
	
	public String formatTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
		String lastModify = format.format(time);
		return lastModify;
	}
	
}
