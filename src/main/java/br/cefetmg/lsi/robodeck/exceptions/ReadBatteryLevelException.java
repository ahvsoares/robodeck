package br.cefetmg.lsi.robodeck.exceptions;

public class ReadBatteryLevelException extends Exception {
	private static final long serialVersionUID = 1167735745202978332L;

	public ReadBatteryLevelException(String text) {
		super(text);
	}	
}
