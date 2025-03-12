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
 * Implementación del servidor del chat
 * 
 */
public class ChatServerImpl implements ChatServer {
	
	// Puerto por defecto
	private static int DEFAULT_PORT =1500;
	
	//Identificación del cliente
	private int  clientId=0;
	
	//Hora 
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	//Puerto que se usa
	private int port;
	
	//Booleano que indica si la sesión está activa
	private boolean alive=true;
	
	//Socket del usuario
	private Socket socket;
	
	//Socket del servidor
	private ServerSocket socketServ ;
	
	//instancia del servidor
	private static ChatServerImpl instance;
	
	//Diccionario con los datos de los usuarios
	private Map<Integer,ServerThreadForClient> cjtoHilosCliente = new HashMap<Integer,ServerThreadForClient>();
		

	/*
	 * Constructor por defecto
	 * 
	 */
/*	public ChatServerImpl() {

	}*/

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
				
				// Id para el usuario recien creado, añadimos una unidad
				clientId++;
				ServerThreadForClient hiloCliente = new ServerThreadForClient(clientId,socket);
				
				// Se almacena el cliente y el hilo en el mapa creado al efecto
				cjtoHilosCliente.put(clientId,hiloCliente);
				
				//Se arranca el hilo recién creado
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
	 *  Envia los mensajes recibidos a todos los clientes presentes en el chat
	 *  
	 *  @param mensaje
	 */
	@Override
	public void broadcast(ChatMessage mensaje) {
		// Envío de los mensajes a todos los clientes
		for (int hiloCliente :cjtoHilosCliente.keySet()) {
			if(hiloCliente !=mensaje.getId()) {
				cjtoHilosCliente.get(hiloCliente).sendMessage(mensaje);
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
	
	/*
	 * Singleton que asegura la creación de una única instancia del servidor
	 */
	public static ChatServerImpl getInstance() {
		if (instance == null) {
			instance = new ChatServerImpl();
		}
		
		return instance;
	}
	
	/*
	 * Hace que el hash del cliente sea su id para la sesión,borrando el valor precedente
	 * 
	 * @param nuevoId
	 * @param id
	 */
	public void setClientId(int nuevoId, int id) {
		cjtoHilosCliente.put(nuevoId, cjtoHilosCliente.get(id));
		cjtoHilosCliente.remove(id);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Se lanza el servidor
		ChatServerImpl.getInstance().startup();

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
		
		private boolean activo = true;
		

		/*
		 * Constructor
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
			
			try {
				this.username = ((ChatMessage) in.readObject()).getMessage();
				// Se crea un id a usando el hash
				int nuevo = this.username.hashCode();
				// Se cambia el id antiguo por el valor del hash
				ChatServerImpl.getInstance().setClientId(nuevo,id);
				id = nuevo;
				
				//Se informa de la conexión del usuario
				System.out.println("Fernando patrocina el mensaje : Bienvenido " + this.username +"con id:" 
				+ this.id + " te has conectado a las: " + ChatServerImpl.getInstance().sdf.format(new Date( )));				
				
				//Bucle degestión de los mensajes
				while(activo) {
					//Se lee el mensaje del cliente
					ChatMessage mensaje = (ChatMessage) in.readObject();
					
					//Si el cliente envía el mensaje de logout se le desactiv
					if(mensaje.getTipo()==MessageType.LOGOUT) {
						activo = false;
						System.out.println("Fernando patrocina el mensaje : " + this.username +"con id:" 
								+ this.id + " se ha desconectado a las: " + ChatServerImpl.getInstance().sdf.format(new Date( )));						
					}else { //En caso contrario se publica el mensaje SHUTDOWN no se requiere en la práctica
						System.out.println("Fernando patrocina el mensaje : " + this.username +"con id:" 
								+ this.id + " ha publicado a las: " + ChatServerImpl.getInstance().sdf.format(new Date( )));
						ChatServerImpl.getInstance().broadcast(mensaje);
						
					}
					
				}
				
			}catch(IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
			}finally {// Se cierra el chat y sus componentes cuanto finaliza el cliente
				try {
					in.close();
					out.close();
					socket.close();
					ChatServerImpl.getInstance().remove(id);
				} catch (IOException e) {
					e.printStackTrace();
				}		
			}
			
		}
		
	}

}
