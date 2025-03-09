/**
 * 
 */
package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * 
 */
public class ChatServerImpl implements ChatServer {
	
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
	
	//Socket del usuario
	private Socket socket;
	
	//Socket del servidor
	private ServerSocket socketServ ;
	
	//Diccionario con los datos de los usuarios
	private Map<Integer,ServerThreadForClient> cjtoHilosCliente = new HashMap<Integer,ServerThreadForClient>();
		

	/*
	 * Constructor 
	 * @param port
	 */
	public ChatServerImpl(int port) {

		this.port = port;
	}

	@Override
	public void startup() {
		// Determinación del puerto
		this.port = DEFAULT_PORT;
		
		try {
			//Creación del socket para el server
			socketServ = new ServerSocket(this.port);
			//Comunicación de apertura del socket
			System.out.println("Fernando patrocina el mensaje: Servidor iniciado a las "
					+ sdf.format(new Date()));
			
			//Bucle de gestión de los mensajes de los clientes
			while (alive) {
				// El socket abierto para el servidor se acepta
				socket = socketServ.accept();
				
				//Id para el usuario reicen creado
				clientId--;
				ServerThreadForClient hiloCliente = new ServerThreadForClient(clientId,socket);
				cjtoHilosCliente.put(clientId,hiloCliente);
				hiloCliente.start();
			}
			
		}catch(IOException e) {
			e.getStackTrace();
		}

	}
	
	/*
	 * para el sistema 
	 */
	@Override
	public void shutdown() {
		// Primer paso: parada de los chats de los usuarios
		for (ServerThreadForClient chat :cjtoHilosCliente.values()) {
			chat.stopChat();
		}
		
		//Una vez parados los chats, se cierra el resto
		try {
			//Cierre del socket
			socket.close();
			socketServ.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		//Una vez cerrado todo se sale del sistema
		System.exit(0);

	}
	
	/*
	 *  Envia los mensajes recibidos a todos los clientes
	 *  
	 *  @param mensaje
	 */
	@Override
	public void broadcast(ChatMessage mensaje) {
		// Envío de los mensajes a todos los clientes
		for (int hilo :cjtoHilosCliente.keySet()) {
			if(hilo !=mensaje.getId()) {
				cjtoHilosCliente.get(hilo).sendMessage(mensaje);
			}
		}

	}
	
	/*
	 * Elimina un cliente de la lista usando su id
	 * 
	 * @param id
	 */
	@Override
	public void remove(int id) {
		// Se informa en primer lugar al usuario que se va a cerrar 
		broadcast(new ChatMessage(id,MessageType.LOGOUT,"Fernando patrocina el mensaje: se va a eliminar su usuario"));
		
		//Se para el chat del usuario
		cjtoHilosCliente.get(id).stopChat();
		
		//Se elimina el hilo del diccionario
		cjtoHilosCliente.remove(id);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	private static class ServerThreadForClient extends Thread{
		
		//Identificación del usuario
		private int id;
		
		//Nombre del usuario
		private String username;
		
		// Socket que usa el servidor
		private Socket socket;
		
		//Flujo de entrada
		private ObjectInputStream in;
		
		//Flujo de salida
		private ObjectOutputStream out;
		
		private boolean activo = false;
		

		/*
		 * Constructor*
		 * @param id
		 * @param socket
		 */
		public ServerThreadForClient(int id, Socket socket) {
			this.id = id;
			this.socket = socket;
			this.activo = true;
			
			//Creación de los streams I/O
			try {
				this.in = new ObjectInputStream(socket.getInputStream());
				this.out = new ObjectOutputStream(socket.getOutputStream());
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		public void sendMessage(ChatMessage mensaje) {
			// Envio de un mensaje
			try {
				out.writeObject(mensaje);
				
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		}

		/*
		 * Para un chat
		 */
		public void stopChat() {
			//Para parar el chat se pone el booleano a false
			activo=false;
			
		}

		/*
		 * run tramita los mensajes que vayan llegando
		 * 
		 * espera los mensajes de los clientes y realiza las operaciones correspondientes
		 */
		public void run() {
			
			// TODO Auto-generated method stub
			
		}
		
	}

}
