package br.cefetmg.lsi.robodeck.utils;

/**
 * Esta classe é um conjunto de funções para manipulação de tipos de dados primitivos. 
 */
public class PrimitiveDataTypesManipulation {

	/**
	 * Converte um byte em um int.
	 * 
	 * @param b Byte a ser convertido.
	 * 
	 * @return Int correspondente ao byte convertido.
	 */
	public static int byteToInt(byte b){
		return b & 0x000000ff;
	}

	/**
	 * Converte um short em um int.
	 * 
	 * @param s Short a ser convertido.
	 * 
	 * @return Int correspondente ao short convertido.
	 */
	public static int shortToInt(short s){
		return s & 0x000ffff;
	}
	
	/**
	 * Converte dois bytes em um short sendo o byte "high" o de maior significância e o "low" de menor.
	 * 
	 * @param high conjunto de 8 bits de maior significância
	 * @param low conjunto de 8 bits de menor significância
	 * 
	 * @return short formado pelos bits de high e de low
	 */
	public static short twoBytesToShort(byte high, byte low){
		return (short)((high << 8) | (low & 0x00ff));
	}
	
	/**
	 * Converte dois bytes em um int sendo o byte "high" o de maior significância e o "low" de menor.
	 * 
	 * @param high conjunto de bits de maior significância
	 * @param low conjunto de bits de menor significância
	 * 
	 * @return int formado pelos bits de high e de low
	 */
	public static int twoBytesToInt(byte high, byte low){
		short aux = twoBytesToShort(high, low);
		
		return shortToInt(aux);
	}
	
}
