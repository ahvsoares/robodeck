package br.cefetmg.lsi.robodeck;

import java.io.IOException;

import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;
import br.cefetmg.lsi.robodeck.exceptions.ReadInfraredSensorsDepthException;

public class Consumidor1 implements Runnable {
	private Robot robot;
	private int totalConsumir;
	private boolean terminou;

	public boolean terminou() {
		return terminou;
	}

	public Consumidor1(Robot bot, int total) {
		robot = bot;
		totalConsumir = total;
		terminou = false;
	}

	public void run() {
		
		for (int i = 0; i < totalConsumir; i++) {
			
			try {
				robot.readInfraredSensorsDepth(this.getClass().getName());
			} catch (IOException | EmptyMessageException
					| ReadInfraredSensorsDepthException
					| InterruptedException e) {
				System.err.println("Falha no consumidor 1.");
				e.printStackTrace();
			}
				
		}
		
		System.out.println("Consumidor 1 concluido!");
		terminou = true;
	}
	
}
