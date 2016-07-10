package com.mottmac.ntis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class NtisHttpProxy {

	public static void main(String... args) throws IOException {

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");
		NtisHttpProxy proxy = applicationContext.getBean(NtisHttpProxy.class);
		proxy.listen();

	}

	private ServerSocket proxySocket;

	private int ntisPort;
	private String ntisHost;

	@Autowired
	private NtisHttpProxy(@Value("${ntis.proxy.port}") int proxyPort, @Value("${ntis.server.port}") int ntisPort,
			@Value("${ntis.server.host}") String ntisHost) throws IOException {

		this.proxySocket=new ServerSocket(proxyPort);
		
		this.ntisPort = ntisPort;
		this.ntisHost = ntisHost;
	}

	public void listen() {
		while (true) {
			try {
				Socket clientSocket = proxySocket.accept();
				new ProxyThread(clientSocket, ntisHost + ":" + ntisPort).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
