package br.cefetmg.lsi.robodeck.exceptions;

public class DifferentRobotConnectionAlreadyExists extends Exception {
	private static final long serialVersionUID = 6695912442201127700L;

	public DifferentRobotConnectionAlreadyExists(Class<?> existingConnectionClass, Class<?> newConnectionClass){
		super("Falha ao criar conexão. Você está tentando criar uma conexão do tipo \"" + newConnectionClass
				+ "\" sendo que já existe uma conexão do tipo \"" + existingConnectionClass + "\".");
	}

	public DifferentRobotConnectionAlreadyExists(String text){
		super(text);
	}
}
