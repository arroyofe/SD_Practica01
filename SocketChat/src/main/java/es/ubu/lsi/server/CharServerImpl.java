/**
 * 
 */
package es.ubu.lsi.server;

import java.text.SimpleDateFormat;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 */
public class CharServerImpl implements ChatServer {
	
	// Puerto por defecto
	private static int DEFAULT_PORT =1500;
	
	//Identificación del cliente
	private int  clientId;
	
	//Fecha
	private SimpleDateFormat sdf;
	
	//Puerto que se usa
	private int port;
	
	//Booleano que indica si la sesión está activa
	private boolean alive;
	
		

	/*
	 * Constructor 
	 * @param port
	 */
	public CharServerImpl(int port) {
		super();
		this.port = port;
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void broadcast(ChatMessage mensaje) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(int id) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	class ServerThreadForClient extends Thread{
		
		//Identificación del usuario
		private int id;
		
		//Nombre del usuario
		private String username;
		
		/*
		 * run
		 * 
		 * espera los mensajes de los clientes
		 */
		
		public void run() {
			
		}
		
	}

}
