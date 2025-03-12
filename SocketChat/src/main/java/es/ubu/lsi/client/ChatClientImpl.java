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
import java.util.Date;
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
	private static int port=DEFAULT_PORT;
	
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
	
	//Hora 
	private SimpleDateFormat hora = new SimpleDateFormat("HH:mm:ss");
	

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
		ChatClientImpl.port = port;
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
	 * 
	 * El puerto y el servidor se escogen con el valor por defecto
	 * 
	 * @param username
	 */
	public ChatClientImpl(String username) {
		this(username,DEFAULT_HOST,DEFAULT_PORT);
		
	}
	/*
	 * Metodo de inicio del chat por parte de un usuario
	 * 
	 * @return true si el arranque es correcto y false en caso contrario 
	 */
	@Override
	public boolean start() {
		try {
			//Apertura de la connexión
			socket = new Socket(server,port);
			
			//Mensaje de bienvenida si la conexión es correcta
			System.out.println("Fernando patrocina el mensaje: Son las: [" + hora.format(new Date() ) + 
					"]. Conexión establecida correctamente");
			System.out.println("Fernando patrocina el mensaje: Cliente nuevo en servidor: / " + server + 
					" Usuario: " + username);
			
			//Creación de las entradas y salidas
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			
			
			//Apertura de mensajes para el cliente
			out.writeObject(new ChatMessage(id,MessageType.MESSAGE,username));
			
			//Creación del oyente
			escuchaCliente = new ChatClientListener(in);
			escuchaCliente.setOwner(this);
			
			//Creación de un hilo nuevo del cliente
			new Thread(escuchaCliente).start();
			
			//Variable para recoger el post en el chat del cliente
			BufferedReader lectura = new BufferedReader(new InputStreamReader(System.in));
			
			
			//lectura de las entradas
			while (lectura.readLine()!= null && carryOn) {
				//Entrada logout para salir del chat
				if(lectura.readLine().equals("logout")) {
					carryOn = false;
					sendMessage(new ChatMessage(id,MessageType.LOGOUT,"Fernando patrocina el mensaje: "));
				}else if(lectura.readLine().equals("ban")){ //Entrada de baneo de un usuario
					//variable para almacenar el usuario baneado, tomando solamente el nombre
					String baneado = lectura.readLine().split("")[1];
					//Se almacena el usuario baneado en el mapa creado al efecto
					this.baneados.put(baneado.hashCode(), baneado);
					sendMessage(new ChatMessage(id,MessageType.MESSAGE,"Fernando patrocina el mensaje: " 
					+ username + "ha baneado al usuario: " + baneado));
				}else if(lectura.readLine().equals("unban")){ //Entrada de anulación de baneo de un usuario
					//variable para almacenar el usuario desbaneado, tomando solamente el nombre
					String desbaneado = lectura.readLine().split("")[1];
					//Se elimina el usuario desbaneado en el mapa creado para los baneados
					this.baneados.remove(desbaneado.hashCode(), desbaneado);
					sendMessage(new ChatMessage(id,MessageType.MESSAGE,"Fernando patrocina el mensaje: " 
					+ username + "ha desbaneado al usuario: " + desbaneado));
				}else {//En el resto de los casos se trata de un post del chat normal y se muestra
					sendMessage(new ChatMessage(id,MessageType.MESSAGE,"Fernando patrocina el mensaje: " 
							+ username + " dice: " + lectura.readLine()));
				}
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		//Al terminar el chat se desconecta al usuario
		disconnect();
		return true;
	}
	
	/*
	 * Gestión de mensajes del servidor relativos a cada usuario
	 * 
	 * @param mensaje a enviar
	 * 
	 */
	@Override
	public void sendMessage(ChatMessage message) {

		try {
			out.writeObject (message);
		}catch (IOException e){
			e.printStackTrace();
		}

	}
	
	/*
	 * Desconexion del cliente
	 */
	@Override
	public void disconnect() {
		// Se pasa el carriOn a false para indicar el final
		carryOn = false;
		
		// Se para el oyente
		escuchaCliente.pararChat();
		
		//Se cierran los streams y el socket
		try {
			out.close();
			in.close();
			socket.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Variables para gestión de los argumentos
		String server = ""; // identificación del servidor que se recibe por parámetro
		String username = ""; // identificación del usuario que se recibe por parámetro
		
		//En función de los parámetros recibidos se actualiza el valor de las variables
		switch (args.length) {
			case 1: //Cuando hay solamente un argumento, éste corresponde al usuario
				server = DEFAULT_HOST; //El servidor será el servidor por defecto
				username = args[0]; //El usuario será el que se pase por argumento
				break;
				
			case 2: // en este caso se reciben ambos datos
				server = args[0]; // el servidor en la primera posición
				username = args[1]; // el usuario en la segunda
				break;
				
			default: // En cualquier otro caso se envia mensaje de advertencia
				System.out.println("Fernando patrocina el mensaje:"
						+"Error. Pasar por parámetos [servidor] (opcional) <usuario> (obligatorio)");
				break;
		}
		
		// Una vez registrados los parámetros se lanza el chat
		new ChatClientImpl(server,username,port).start();

	}
	
	private static class ChatClientListener implements Runnable{
		//Variable de Entrada de datos
		private ObjectInputStream in;
		
		// Booleano para saber si el cliente está activo y actuar en consecuencia
		private boolean activo=true;
		
		// Propietario del chat
		private ChatClientImpl propietario;
		
		
		
		/*
		 * Arranca el oyente
		 * 
		 * @param in
		 * @param active
		 */
		public ChatClientListener(ObjectInputStream in) {
			this.in = in;
			activo = true;
		}

		/*
		 * Para el chat del cliente 
		 */
		public void pararChat() {
			activo= false;
		}
		
		/*
		 * Método set para crear el cliente propietario del post en el chat
		 * 
		 * @param propietario del post
		 */
		public void setOwner(ChatClientImpl propietario) {
			this.propietario = propietario;
			
		}


		/*
		 * Arranca el chat
		 */
		@Override
		public void run() {
			try {
				ChatMessage mensaje = (ChatMessage) in.readObject();
				System.out.println("Fernando patrocina el mensaje: " 
				+ mensaje.getId() + " " + mensaje.getMessage());
				
			}catch(IOException e) {
				e.printStackTrace();
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			propietario.disconnect();
			
		}
		
	}

}
