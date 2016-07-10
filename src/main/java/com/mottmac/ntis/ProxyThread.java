package com.mottmac.ntis;

import java.io.*;
import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class ProxyThread extends Thread{

    public static final String CONTENT_LENGTH = "Content-Length:";
    public static final int CONTENT_LENGTH_VALUE_BEGIN_INDEX = 16;
    private Socket proxySocket;

    private String method;
    private String path;
    private String payload;
    private int contentLength;

    private String forwardHost;
    private String protocol = "http://";

	public ProxyThread(Socket proxySocket, String forwardHost) {
        this.proxySocket = proxySocket;
        this.forwardHost = forwardHost;
    }
	
	public void run(){
		try(
			InputStream fromClient = proxySocket.getInputStream();
            BufferedReader fromClientReader = new BufferedReader(new InputStreamReader(fromClient));
			OutputStream toClient = proxySocket.getOutputStream();
            OutputStreamWriter toClientWriter = new OutputStreamWriter(toClient)
        ){

            readRequestData(fromClientReader);

			HttpResponse response = forwardRequest(buildForwardUrl(), payload);

            respondWhenStatusOK(toClientWriter, response);

		} catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

    private void respondWhenStatusOK(OutputStreamWriter toClientWriter, HttpResponse response) throws IOException{
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode > 199 || statusCode < 300){
            toClientWriter.write(response.getStatusLine().toString().toCharArray());
            toClientWriter.flush();
        }
    }

    private void readRequestData(BufferedReader fromClientReader) throws IOException{
        readHttpHeaders(fromClientReader);
        readPayload(fromClientReader);
    }

    private void readPayload(BufferedReader fromServiceReader) throws IOException {
        char[] payload = new char[contentLength];
        fromServiceReader.read(payload);
        this.payload = new String(payload);
    }

    /**
     * Reads http headers using the reader. After the operation the reader marker is after the http headers and at the beginning of the payload.
     * @param fromServiceReader
     * @return
     * @throws IOException
     */
    private void readHttpHeaders(BufferedReader fromServiceReader) throws IOException {
        String line = fromServiceReader.readLine();
        String[] httpHeader = line.split(" ");
        method =  httpHeader[0];
        path = httpHeader[1];
        while((line = fromServiceReader.readLine()) != null){
            if (line.startsWith(CONTENT_LENGTH)){
                contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_VALUE_BEGIN_INDEX));
            }
            if (line.length() == 0)
                break;
        }
    }

    private String buildForwardUrl(){
        return protocol + forwardHost + path;
    }

    private HttpResponse forwardRequest(String url, String body) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(body));
		return client.execute(post);
	}

}
