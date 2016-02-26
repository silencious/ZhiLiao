package com.zhiliao.client.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class MyConnector {
	Socket socket = null; // socket object
	DataInputStream din = null; // data stream of input
	DataOutputStream dout = null; // data stream of output

	public MyConnector(String address, int port) {
		try {
			socket = new Socket(address, port);
			din = new DataInputStream(socket.getInputStream()); // get input stream
			dout = new DataOutputStream(socket.getOutputStream()); // get output stream
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// disconnect and free resource
	public void disconnect() {
		try {
			dout.writeUTF("<#USER_LOGOUT#>"); // send logout msg
			din.close();
			dout.close();
			socket.close();
			socket = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
