package br.cefetmg.lsi.robodeck.exceptions;

public class EmptyMessageException extends Exception {
	private static final long serialVersionUID = 3907912094136250356L;

	public EmptyMessageException(){
		super("A mensagem do pacote deve ter tamanho maior ou igual a 2. Primeiro, ajuste a mensagem do pacote antes de criar o cabeçalho.");
	}
	
}
