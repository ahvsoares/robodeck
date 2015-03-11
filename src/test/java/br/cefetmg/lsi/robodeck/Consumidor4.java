package br.cefetmg.lsi.robodeck;

import java.io.IOException;

import br.cefetmg.lsi.robodeck.exceptions.CameraImageFormatLenghtException;
import br.cefetmg.lsi.robodeck.exceptions.CameraStartException;
import br.cefetmg.lsi.robodeck.exceptions.CameraStopException;
import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;

public class Consumidor4 implements Runnable {
	private Robot robot;
	private boolean terminou;

	public boolean terminou() {
		return terminou;
	}

	public Consumidor4(Robot bot, int total) {
		robot = bot;
		terminou = false;
	}

	public void run() {
			
		try {
			robot.cameraStart(TesteRobodeckAPI.class.getName());
			Thread.sleep(8000);
			robot.cameraStop(TesteRobodeckAPI.class.getName());
		} catch (IOException | EmptyMessageException | CameraStartException
				| CameraStopException | CameraImageFormatLenghtException
				| InterruptedException e) {
			System.err.println("Falha no consumidor 4.");
			e.printStackTrace();
		}
		
		System.out.println("Consumidor 4 concluido!");
		terminou = true;
	}
	
}
