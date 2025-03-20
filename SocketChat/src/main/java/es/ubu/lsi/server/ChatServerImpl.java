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
	private boolean alive = true;
	
	//Socket del usuario
	private Socket socket;
	
	//Socket del servidor
	private ServerSocket socketServ ;
	
	//instancia del servidor
	private static ChatServerImpl instance;
	
	//Diccionario con los datos de los usuarios
	private Map<Integer,ServerThreadForClient> cjtoClientes = new HashMap<Integer,ServerThreadForClient>();
	
	//Diccionario de clientes baneaso
	private Map<Integer,ServerThreadForClient> cjtoClientesBaneados = new HashMap<Integer,ServerThreadForClient>();
	

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
			System.out.println("Escuchando al puerto: " + port);
			
			//Bucle de gestión de los mensajes de los clientes
			while (alive) {
				// El socket abierto para el servidor se usa para el cliente
				socket = socketServ.accept();
				
				
				// Id para el usuario recien creado, añadimos una unidad
				clientId++;
				
				//Recepción de mensajes de los clientes
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				//Impresión en pantalla de los mensajes recibidos
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				
				//Extracción de los datos del usuario
				//out.println(clientId);
				//String usuario = in.readLine();
				
				if (cjtoClientes.containsKey(clientId)){
					
					System.out.println("Usuario actualmente en uso.");
					socket.close();
				}else {
				
				ServerThreadForClient hiloCliente = new ServerThreadForClient(clientId,socket);
				
				// Se almacena el cliente y el hilo en el mapa creado al efecto
				cjtoClientes.put(clientId,hiloCliente);
				
				//Se arranca el hilo recién creado
				hiloCliente.start();
				
				System.out.println("Conexión establecida con usuario con id: "+socket.getInetAddress().getHostName());
				}
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
		for (ServerThreadForClient chat :cjtoClientes.values()) {
			// Se informa del cierre
			System.out.println("Se va a cerrar el chat.");
			chat.stopChat();
		}
		
		//Una vez parados los chats, se cierra el resto
		try {
			
			// Se informa del cierre
			System.out.println("Se va a apagar el servidor.");
			
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
		for (int hiloCliente :cjtoClientes.keySet()) {
			if(hiloCliente != mensaje.getId()) {
				cjtoClientes.get(hiloCliente).sendMessage(mensaje);
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
		cjtoClientes.get(id).stopChat();
		
		//Se elimina el hilo del diccionario
		cjtoClientes.remove(id);

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
		cjtoClientes.put(nuevoId, cjtoClientes.get(id));
		cjtoClientes.remove(id);
		
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
		
		//Socket del servidor
		private ServerSocket socketS;
		
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
		public ServerThreadForClient(int id,  Socket socket) {
			this.id = id;
		//	this.username = username; 
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
			
				socketS = new ServerSocket (DEFAULT_PORT);
				socket = socketS.accept();
				
				this.username = ((ChatMessage) in.readObject()).getMessage();
				// Se crea un id a usando el hash
				int nuevo = this.username.hashCode();
				// Se cambia el id antiguo por el valor del hash
				ChatServerImpl.getInstance().setClientId(nuevo,id);
				id = nuevo;
				
				
				
				//Se informa de la conexión del usuario
				System.out.println("Fernando patrocina el mensaje : Bienvenido " + this.socket.getInetAddress().getHostName() +"con id:" 
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
					}else if(mensaje.getTipo()==MessageType.SHUTDOWN) {
						activo = false;//SHUTDOWN no se requiere en la práctica
						System.out.println("Fernando patrocina el mensaje : " + this.username +"con id:" 
								+ this.id + " se ha cerrado el servidor a las: " + ChatServerImpl.getInstance().sdf.format(new Date( )));	
						System.exit(1);
					}else { //En caso contrario se publica el mensaje 
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
