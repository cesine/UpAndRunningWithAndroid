package com.androidmontreal.commander;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class URLConnectionReader {
    public static String sendGET(String url) {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            byte buffer[] = new byte[1024] ;
			InputStream is = entity.getContent() ;
			int numBytes = is.read(buffer) ;
			is.close();
			
			return "";
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return "";
    }
}