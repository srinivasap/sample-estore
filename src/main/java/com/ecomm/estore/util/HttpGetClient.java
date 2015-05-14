package com.ecomm.estore.util;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class HttpGetClient extends HttpRequestBase  {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HttpGetClient.class);
	
	private HttpGetClient() {
		super();
	}
	
	private HttpGetClient(String uri) {
		this();
		setURI(URI.create(uri));
	}

	@Override
	public String getMethod() {
		return Constants.HTTP_METHOD_GET;
	}
	
	public static void call(String url) {
		LOG.info("Received request to update cache for url - "+url);
		
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGetClient httpGet = new HttpGetClient(url.toString());
        
        try {
            HttpResponse response = httpclient.execute(httpGet);          
            HttpEntity entity = response.getEntity();
            LOG.info("Http response line <"+response.getStatusLine()+"> and response body <"+IOUtils.toString(entity.getContent())+">");
       } catch (Exception e) {
    	   LOG.error("Error while update url cache", e);
        } finally {
        	httpGet.abort();
        	httpclient.close();
        }
	}
	
	public static void main(String[] args) {
		HttpGetClient.call("http://www.estore.com.global.prod.fastly.net/rest/order/1");
	}
	
}
