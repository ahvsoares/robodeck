package br.cefetmg.lsi.robodeck;

import java.io.IOException;

import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;
import br.cefetmg.lsi.robodeck.exceptions.ReadUltrassonicSensorsDistanceException;

public class Consumidor3 implements Runnable {
	private Robot robot;
	private int totalConsumir;
	private boolean terminou;

	public boolean terminou() {
		return terminou;
	}

	public Consumidor3(Robot bot, int total) {
		robot = bot;
		totalConsumir = total;
		terminou = false;
	}

	public void run() {
		
		for (int i = 0; i < totalConsumir; i++) {
			
			try {
				robot.readUltrassonicSensorsDistance(this.getClass().getName());
			} catch (IOException | EmptyMessageException
					| ReadUltrassonicSensorsDistanceException
					| InterruptedException e) {
				System.err.println("Falha no consumidor 3.");
				e.printStackTrace();
			}
				
		}
		
		System.out.println("Consumidor 3 concluido!");
		terminou = true;
	}
	
}
