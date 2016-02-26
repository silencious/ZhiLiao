package com.zhiliao.client;

public class Config {
	public static String host = "vultr.riaqn.com";
	public static String port = "8080";
	public static String scheme = "http";
	public static String path = "/zhiliao";
	
	public static String restURI = scheme + "://" + host + ":" + port + path;
	
	public static String endpoint = "/msg";
	
	public static String websocketURI = "ws://" + host + ":" + port + path + endpoint;
}