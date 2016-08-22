package org.deri.nettopo.util;

public class ArgumentNotValidException extends Exception {
	public ArgumentNotValidException(){
		
	}
	
	public ArgumentNotValidException(String message){
		super(message);
	}
}
