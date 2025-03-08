/**
 * 
 */
package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Implementa el chat del cliente
 * 
 * @author Fernando Arroyo
 */
public class ChatClientImpl implements ChatClient {
	
	//Puerto por defecto
	private static int DEFAULT_PORT = 1500;
	
	//Puerto por defecto
	private static String DEFAULT_HOST= "localhost";
	
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
	
	//Flujo de entrada
	private ObjectInputStream in;
	
	//Flujo de salida
	private ObjectOutputStream out;
	
	//Variable Listener
	private ChatClientListener escuchaCliente;
	
	//Diccionario para almacenar los usuarios baneados
	private Map<Integer,String> baneados = new HashMap<Integer,String>();
	
	//Variable socket para la gestión de las conexiones
	private Socket socket;
	

	/*
	 * Constructor Crea los datos del chat con todos los datos por parámetro
	 * 
	 * @param server
	 * @param username
	 * @param port
	 */
	public ChatClientImpl(String server, String username, int port) {
		this.server = server;
		this.username = username;
		this.port = port;
		this.id = username.hashCode();
		this.carryOn= false;
	}
	
	/*
	 * Constructor Crea los datos del chat con servidor y cliente.
	 * El puerto se escoge con el valor por defecto
	 * 
	 * @param server
	 * @param username
	 */
	public ChatClientImpl(String server, String username) {
		this(server,username,DEFAULT_PORT);
		
	}
	
	/*
	 * Constructor Crea los datos del chat con el cliente solamente.
	 * El puerto y el servidor se escogen con el valor por defecto
	 * 
	 * @param username
	 */
	public ChatClientImpl(String username) {
		this(username,DEFAULT_HOST,DEFAULT_PORT);
		
	}

	@Override
	public boolean start() {
		try {
			//Apertura de la connexión
			socket = new Socket(server,port);
			//Creación de las entradas y salidas
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			//Mensaje de bienvenida si la conexión es correcta
			SimpleDateFormat hora = new SimpleDateFormat("HH:mm;ss");
			System.out.println("Fernando patrocina el mensaje: "
					+ "Conexión establecida correctamente");
			System.out.println("Fernando patrocina el mensaje: "
					+ "Son las: [" + hora + 
					"]. Conexión establecida correctamente");
			
			//Apertura de mensajes para el cliente
			out.writeObject(new ChatMessage(id,MessageType.MESSAGE,username));
			
			//Creación del oyente
			escuchaCliente = new ChatClientListener(in);
			escuchaCliente.setOwner(this);
			
			//Creación de un hilo nuevo del cliente
			new Thread(escuchaCliente).start();
			
			//Variable para recoger el post en el chat del cliente
			BufferedReader lectura = new BufferedReader(new InputStreamReader(System.in));
			
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		disconnect();
		return true;
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
	
	class ChatClientListener implements Runnable{
		//Variable de Entrada de datos
		private ObjectInputStream in;
		
		// Booleano para saber si el cliente está activo y actuar en consecuencia
		private boolean active;
		
		// Propietario del chat
		private ChatClientImpl propietario;
		
		
		
		/**
		 * @param in
		 * @param active
		 */
		public ChatClientListener(ObjectInputStream in) {
			this.in = in;
			active = true;
		}



		public void setOwner(ChatClientImpl propietario) {
			this.propietario = propietario;
			
		}



		@Override
		public void run() {
			
		}
		
	}

}
