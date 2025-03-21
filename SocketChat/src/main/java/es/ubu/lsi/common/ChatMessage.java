package es.ubu.lsi.common;

import java.io.*;

/**
 * Mensaje que aparece en el chat
 * 
 * @autor Raúl Marticorena
 * @author Joaquin P. Seco
 */

public class ChatMessage implements Serializable {

	/**
	 * UID por defecto
	 */
	private static final long serialVersionUID = 1L;

	public enum MessageType{
		/*Mensaje*/
		MESSAGE,
		/*Shutdom del servidor*/
		SHUTDOWN,
		/*Logout del Cliente*/
		LOGOUT;
	}
	
	// Tipo de mensaje
	private MessageType tipo;
	
	// Texto del mensaje
	private String mensaje;
	
	//Identificación del cliente
	private int id;
	
	/*
	 * Constructor
	 * 
	 * @return type
	 * @see type
	 */
	public ChatMessage(int id, MessageType tipo, String mensaje) {
		this.setId(id);
		this.setType(tipo);
		this.setMessage(mensaje);
	}

	/*
	 * Obtiene el tipo
	 * @return tipo
	 */
	public MessageType getType() {
		return tipo;
	}
	
	/*
	 * Establece el tipo
	 * @param tipo
	 */
	public void setType(MessageType tipo) {
		this.tipo = tipo;
	}
	
	/*
	 * Obtiene el mensaje
	 * @return mensaje
	 */
	public String getMessage() {
		return mensaje;
	}
	
	/*
	 * Establece el mensaje
	 * @param mensaje
	 */
	public void setMessage(String mensaje) {
		this.mensaje = mensaje;
	}
	
	/*
	 * Obtiene la identificación
	 * @return id
	 */
	public int getId() {
		return id;
	}
	
	/*
	 * Establece la identificación
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}	
}
