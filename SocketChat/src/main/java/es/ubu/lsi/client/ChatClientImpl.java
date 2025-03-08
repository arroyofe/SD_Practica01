/**
 * 
 */
package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 */
public class ChatClientImpl implements ChatClient {
	
	//Servidor que va a usar el cliente
	private String server;
	
	//Código de usuario del cliente
	private String username;
	
	//Puerto por el que se comunica el cliente
	private int port;
	
	//
	private boolean carryOn = true;
	
	//identificación numérica del cliente
	private int id;
	
	

	/*
	 * Constructor Crea los datos del cliente
	 * 
	 * @param server
	 * @param username
	 * @param port
	 */
	public ChatClientImpl(String server, String username, int port) {
		super();
		this.server = server;
		this.username = username;
		this.port = port;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendMessage(ChatMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	class CatClientListener implements Runnable{
		
		@Override
		public void run() {
			
		}
		
	}

}
