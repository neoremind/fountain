package net.neoremind.fountain.exception;

/**
 * @author hanxu03
 *
 * 2013-7-9
 */
public class EOFException extends RuntimeException {
	private static final long serialVersionUID = -5756919868588957107L;

	public EOFException(){
		super();
	}
	
	public EOFException(String message){
		super(message);
	}
	
	public EOFException(String message, Throwable throwable){
		super(message, throwable);
	}
}
