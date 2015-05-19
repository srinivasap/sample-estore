package com.ecomm.estore.util;

public class Constants {

	public static final String HTTP_HEADER_CACHE_CONTROL_NAME = "Cache-Control";
	public static final String HTTP_HEADER_SURROGATE_CONTROL_NAME = "Surrogate-Control";
	public static final String HTTP_HEADER_SURROGATE_KEY_NAME = "Surrogate-Key";
	public static final String HTTP_HEADER_SOFT_PURGE_NAME = "Fastly-Soft-Purge";	
	public static final String HTTP_HEADER_HOST = "Host";
	
	public static final String HTTP_HEADER_CACHE_CONTROL_NO_CACHE = "no-cache";
	public static final String HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE = "max-age=3600, stale-while-revalidate=30, stale-if-error=86400";
	public static final String HTTP_HEADER_SOFT_PURGE_VALUE = "1";
	
	public static final String HTTP_METHOD_PURGE = "PURGE";
	public static final String HTTP_METHOD_GET = "GET";
	
}
