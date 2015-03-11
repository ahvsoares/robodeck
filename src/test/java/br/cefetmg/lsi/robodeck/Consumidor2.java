package br.cefetmg.lsi.robodeck;

import java.io.IOException;

import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;
import br.cefetmg.lsi.robodeck.exceptions.ReadInfraredSensorsDistanceException;

public class Consumidor2 implements Runnable {
	private Robot robot;
	private int totalConsumir;
	private boolean terminou;

	public boolean terminou() {
		return terminou;
	}

	public Consumidor2(Robot bot, int total) {
		robot = bot;
		totalConsumir = total;
		terminou = false;
	}

	public void run() {
		
		for (int i = 0; i < totalConsumir; i++) {
			
			try {
				robot.readInfraredSensorsDistance(this.getClass().getName());
			} catch (IOException | EmptyMessageException
					| ReadInfraredSensorsDistanceException
					| InterruptedException e) {
				System.err.println("Falha no consumidor 2.");
				e.printStackTrace();
			}
				
		}
		
		System.out.println("Consumidor 2 concluido!");
		terminou = true;
	}
	
}
