package com.ecomm.estore.util;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class HttpPurgeClient extends HttpRequestBase  {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HttpPurgeClient.class);
	
	private HttpPurgeClient() {
		super();
	}
	
	private HttpPurgeClient(String uri) {
		this();
		setURI(URI.create(uri));
	}

	@Override
	public String getMethod() {
		return Constants.HTTP_METHOD_PURGE;
	}
	
	public static void call(String url) {	
		LOG.info("Received request to pruge cache for url - "+url);
		
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPurgeClient httpPurge = new HttpPurgeClient(url.toString());
        Header header = new BasicHeader("Host", System.getProperty("endpoint"));
        httpPurge.setHeader(header);
        //header = new BasicHeader("Cache-Control", "no-cache");
        //httpPurge.setHeader(header);
        // do soft purge
        header = new BasicHeader(Constants.HTTP_HEADER_SOFT_PURGE_NAME, Constants.HTTP_HEADER_SOFT_PURGE_VALUE);
        httpPurge.setHeader(header);
       
        try {
            HttpResponse response = httpclient.execute(httpPurge);          
            HttpEntity entity = response.getEntity();
            LOG.info("Http response line <"+response.getStatusLine()+"> and response body <"+IOUtils.toString(entity.getContent())+">");
       } catch (Exception e) {
    	   LOG.error("Error while pruging url cache", e);
        } finally {
        	httpPurge.abort();
        	httpclient.close();
        }
	}
	
	public static void main(String[] args) {
		HttpPurgeClient.call("http://www.estore.com.global.prod.fastly.net/rest/order/1");
	}
	
}
