package org.deri.nettopo.util;

public class DuplicateIDException extends Exception {
	public DuplicateIDException(){
	}
	public DuplicateIDException(String message){
		super(message);
	}
}