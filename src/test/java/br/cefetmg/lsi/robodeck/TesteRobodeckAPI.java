package br.cefetmg.lsi.robodeck;

import java.io.IOException;

import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;
import br.cefetmg.lsi.robodeck.exceptions.MoveRobotException;
import br.cefetmg.lsi.robodeck.exceptions.SpinRobotException;
import br.cefetmg.lsi.robodeck.exceptions.StrafeRobotException;
import br.cefetmg.lsi.robodeck.exceptions.TurnRobotException;

public class TesteRobodeckAPI {
	private static Robot robot;

	public static void main(String[] args) {
//		testeSequencial();
		testeConcorrente();
	}
	
	private static void testeConcorrente(){
		boolean canDisconect = false;

		try {
			robot = new Robot();
			robot.connect();
			
			Consumidor1 consumidor1 = new Consumidor1(robot, 20);
			Consumidor2 consumidor2 = new Consumidor2(robot, 20);
			Consumidor3 consumidor3 = new Consumidor3(robot, 20);
			Consumidor4 consumidor4 = new Consumidor4(robot, 20);
	
			Thread t1 = new Thread(consumidor1);
			t1.start();
	
			Thread t2 = new Thread(consumidor2);
			t2.start();
	
			Thread t3 = new Thread(consumidor3);
			t3.start();
	
			Thread t4 = new Thread(consumidor4);
			t4.start();
	
			while (!canDisconect){
				Thread.sleep(1 * 1000);
				canDisconect = consumidor1.terminou() && consumidor2.terminou() && consumidor3.terminou() && consumidor4.terminou();
			}
			
			robot.disconnect();			
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				robot.disconnect();
			} catch (Exception ex) {
				System.out.println("Falha ao fechar conexão com o robô.");
				ex.printStackTrace();
			}
			
		}
	}
	
	private static void testeSequencial(){

		try {
			robot = new Robot();
			robot.connect();
			
			/*ReaderInfraredSensorsDepth readerInfraredSensorsDepth = new ReaderInfraredSensorsDepth(robot);
			Thread threadReaderInfraredSensorsDepth = new Thread(readerInfraredSensorsDepth);
			threadReaderInfraredSensorsDepth.start();*/
			
//			// Obtém a versão do protocolo de comunicação.
//			robot.getCommunicationProtocolVersion();
//			
//			// Comando para os atuadores do robô.
//			moveTest(TesteRobodeckAPI.class.getName());
//			turnTest(TesteRobodeckAPI.class.getName());
//			strafeTest(TesteRobodeckAPI.class.getName());
//			spinTest(TesteRobodeckAPI.class.getName());
			
//			// Comandos para os sensores de infravermelho do robô.
//			robot.readInfraredSensorsDepth(TesteRobodeckAPI.class.getName());
//			robot.readInfraredSensorsDistance(TesteRobodeckAPI.class.getName());
//
//			// Comandos para os sensores de ultrassom do robô.
//			robot.readUltrassonicSensorsDistance(TesteRobodeckAPI.class.getName());
//			robot.readUltrassonicSensorsLuminosity(TesteRobodeckAPI.class.getName());
//
//			// Comandos para a bússola do robô.
//			robot.readCompassSensor(TesteRobodeckAPI.class.getName());
//
//			// Comandos para o acelerômetro do robô.
//			robot.readAccelerometerSensor(TesteRobodeckAPI.class.getName());
//
//			// Comandos para os sensores de temperatura e umidade do robô.
//			robot.readTemperatureAndHumiditySensors(TesteRobodeckAPI.class.getName());
//
//			// Comandos para os sensores ópticos do robô.
//			robot.readCollisionDetection(TesteRobodeckAPI.class.getName()); // TODO: ver como funciona.

			// Comandos para o GPS do robô.
//			robot.readGPSInfo(TesteRobodeckAPI.class.getName()); // TODO: verificar se existe mesmo
//			robot.readGPSGet(TesteRobodeckAPI.class.getName());
//			robot.readGPSValidate(TesteRobodeckAPI.class.getName());
//			robot.readGPSSatellite(TesteRobodeckAPI.class.getName());
//			robot.readGPSTime(TesteRobodeckAPI.class.getName()); // TODO: ver como funciona.
//			robot.readGPSDate(TesteRobodeckAPI.class.getName()); // TODO: ver como funciona.
//			robot.readGPSLatitude(TesteRobodeckAPI.class.getName()); // TODO: ver como funciona.
//			robot.readGPSLongitude(TesteRobodeckAPI.class.getName()); // TODO: ver como funciona.
//			robot.readGPSAltitude(TesteRobodeckAPI.class.getName());
//			robot.readGPSSpeed(TesteRobodeckAPI.class.getName());
//			robot.readGPSHead(TesteRobodeckAPI.class.getName());

			// Comandos para o robô.
//			robot.readBatteryLevel(TesteRobodeckAPI.class.getName()); // TODO: rever este para pegar do Arduino
			
			// Comandos para o módulo de alta performance (MAP) do robô.
//			robot.readMAPVersion(TesteRobodeckAPI.class.getName());

			// Comandos para a câmera do robô.
			robot.cameraStart(TesteRobodeckAPI.class.getName());
			Thread.sleep(8000);
			robot.cameraStop(TesteRobodeckAPI.class.getName());
//			robot.cameraTakePhoto(TesteRobodeckAPI.class.getName());
//			robot.rtspServerStart(TesteRobodeckAPI.class.getName());
//			robot.rtspServerStop(TesteRobodeckAPI.class.getName());
			
			robot.disconnect();
//		} catch (OpenSessionException ose){
//			System.out.println("Falha ao conectar com o robô.");
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				robot.disconnect();
			} catch (Exception ex) {
				System.out.println("Falha ao fechar conexão com o robô.");
				ex.printStackTrace();
			}
			
		}
		
	}
	
	private static void moveTest() throws IOException, EmptyMessageException, MoveRobotException, InterruptedException{
		robot.move(1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
		
		robot.move(-1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
	}
	
	private static void turnTest() throws IOException, EmptyMessageException, TurnRobotException, InterruptedException, MoveRobotException{
		robot.turn((byte)10, 1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
		
		robot.turn((byte)10, -1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
	}
	
	private static void strafeTest() throws IOException, EmptyMessageException, InterruptedException, MoveRobotException, StrafeRobotException{
		robot.strafe((byte)10, 1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
		
		robot.strafe((byte)10, -1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
	}
	
	private static void spinTest() throws IOException, EmptyMessageException, InterruptedException, MoveRobotException, SpinRobotException{
		robot.spin(Robot.SPIN_CLOCKWISE, 1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
		
		robot.spin(Robot.SPIN_COUNTERCLOCKWISE, 1000, TesteRobodeckAPI.class.getName());
		Thread.sleep(5000);
		robot.brake(TesteRobodeckAPI.class.getName());
	}

}
