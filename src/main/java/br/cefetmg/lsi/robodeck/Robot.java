package br.cefetmg.lsi.robodeck;

import java.io.IOException;
import java.io.InputStream;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;

import br.cefetmg.lsi.robodeck.devices.camera.Camera;
import br.cefetmg.lsi.robodeck.devices.camera.CameraImage;
import br.cefetmg.lsi.robodeck.exceptions.CameraException;
import br.cefetmg.lsi.robodeck.exceptions.CameraImageFormatLenghtException;
import br.cefetmg.lsi.robodeck.exceptions.CameraStartException;
import br.cefetmg.lsi.robodeck.exceptions.CameraStopException;
import br.cefetmg.lsi.robodeck.exceptions.CloseSessionException;
import br.cefetmg.lsi.robodeck.exceptions.DifferentRobotConnectionAlreadyExists;
import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;
import br.cefetmg.lsi.robodeck.exceptions.GetCommunicationProtocolVersionException;
import br.cefetmg.lsi.robodeck.exceptions.MoveRobotException;
import br.cefetmg.lsi.robodeck.exceptions.OpenSessionException;
import br.cefetmg.lsi.robodeck.exceptions.ReadAccelerometerSensorException;
import br.cefetmg.lsi.robodeck.exceptions.ReadBatteryLevelException;
import br.cefetmg.lsi.robodeck.exceptions.ReadCompassSensorException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSAltitudeException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSDateException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSGetException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSHeadException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSInfoException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSLatitudeException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSLongitudeException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSSatelliteException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSSpeedException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSTimeException;
import br.cefetmg.lsi.robodeck.exceptions.ReadGPSValidateException;
import br.cefetmg.lsi.robodeck.exceptions.ReadInfraredSensorsDepthException;
import br.cefetmg.lsi.robodeck.exceptions.ReadInfraredSensorsDistanceException;
import br.cefetmg.lsi.robodeck.exceptions.ReadMAPVersionException;
import br.cefetmg.lsi.robodeck.exceptions.ReadOpticalSensorsException;
import br.cefetmg.lsi.robodeck.exceptions.ReadTemperatureAndHumiditySensorsException;
import br.cefetmg.lsi.robodeck.exceptions.ReadUltrassonicSensorsDistanceException;
import br.cefetmg.lsi.robodeck.exceptions.ReadUltrassonicSensorsLuminosityException;
import br.cefetmg.lsi.robodeck.exceptions.SpinRobotException;
import br.cefetmg.lsi.robodeck.exceptions.StrafeRobotException;
import br.cefetmg.lsi.robodeck.exceptions.TurnRobotException;
import br.cefetmg.lsi.robodeck.network.RobotConnection;
import br.cefetmg.lsi.robodeck.utils.PropertiesLoaderImpl;

public class Robot {
	protected final boolean debug = new Boolean(PropertiesLoaderImpl.getValor("robodeckapi.debugmode")); // Para ativar/desativar saidas "System.out.println();" de depuracao.
	
	/**
	 * Para girar o robô em sentido horário.
	 */
	public static final byte SPIN_CLOCKWISE = (byte)0xFF;
	
	/**
	 * Para girar o robô em sentido anti-horário.
	 */
	public static final byte SPIN_COUNTERCLOCKWISE = (byte)0x00;
	
	/**
	 * Conexão com o robô.
	 */
	private RobotConnection robotConnection;
	
	/**
	 * Thread da câmera.
	 */
//	private Thread cameraThread;
	
	/**
	 * Câmera do robô.
	 */
	private Camera camera;

    /**
     * Cria uma conexão do tipo WiFi com o robô.
     * 
     * @throws DifferentRobotConnectionAlreadyExists 
     * @throws UnknownHostException 
     * @throws PortUnreachableException 
     */
    public Robot() throws PortUnreachableException, UnknownHostException, DifferentRobotConnectionAlreadyExists {
    	robotConnection = RobotConnection.makeConnection();
    }

    /**
     * Cria uma conexão do tipo Bluetooth com o robô.
     * 
     * @param uuid UUID do servidor do Robô.
     * 
     * @return Uma conexão Bluetooth.
     * 
     * @throws DifferentRobotConnectionAlreadyExists 
     */
    public Robot(String uuid) throws DifferentRobotConnectionAlreadyExists {
    	robotConnection = RobotConnection.makeConnection(uuid);
    }

    /**
     * Conecta-se com o robô e abre uma sessão.
     * 
     * @throws UnknownHostException
     * @throws IOException
     * @throws OpenSessionException
     * @throws EmptyMessageException 
     * @throws InterruptedException 
     */
	public void connect() throws IOException, OpenSessionException, EmptyMessageException, InterruptedException {
		robotConnection.connect();
		robotConnection.openSession();
	}

	/**
	 * Fecha a sessão existente e se desconecta com o robô.
	 * 
	 * @throws IOException
	 * @throws CloseSessionException
	 * @throws EmptyMessageException 
	 * @throws InterruptedException 
	 */
	public void disconnect() throws IOException, CloseSessionException, EmptyMessageException, InterruptedException {
		robotConnection.closeSession();
		robotConnection.disconnect();
	}
	
	/**
	 * Retorna a versão do protocolo de comunicação.
	 * 
	 * @return versão do protocolo de comunicação.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws InterruptedException 
	 * @throws ReadInfraredSensorsDepthException
	 */
	public String getCommunicationProtocolVersion() throws IOException, EmptyMessageException, GetCommunicationProtocolVersionException, InterruptedException{
		return robotConnection.getCommunicationProtocolVersion();
	}

    /**
     * Movimenta o robô para frente ou para trás, de forma que não haja mudança da curvatura atual.
     * Por exemplo, se o robô está andando em linha reta e este comando for executado com um valor
     * de intensidade maior do que o atual, o robô continuará se locomovendo em linha reta, porém
     * a uma velocidade maior.
     * 
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws IOException
     * @throws EmptyMessageException
     * @throws MoveRobotException
     * @throws InterruptedException 
     */
	public void move(int intensity, String source) throws IOException, EmptyMessageException, MoveRobotException, InterruptedException{
		robotConnection.sendMoveCommand(intensity, source);
	}

    /**
     * Para o robô.
     *  
     * @throws IOException
     * @throws EmptyMessageException
     * @throws MoveRobotException
     * @throws InterruptedException 
     */
	public void brake(String source) throws IOException, EmptyMessageException, MoveRobotException, InterruptedException{
		move((byte)0, source);
	}

    /**
     * Faz com que o robô realize uma curva seguindo o ângulo e a intensidade dados. O ângulo
     * fornecido diz respeito ao ângulo que o robô fará com relação à sua frente, ao se deslocar. A
     * intensidade é um valor proporcional a velocidade resultante do robô.
     * 
     * @param ângulo da curva a ser realizada, em graus. Valor entre -32 e +32.
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws IOException
     * @throws EmptyMessageException
     * @throws TurnRobotException
     * @throws InterruptedException 
     */
	public void turn(byte angle, int intensity, String source) throws IOException, EmptyMessageException, TurnRobotException, InterruptedException{
		robotConnection.sendTurnCommand(angle, intensity, source);
	}

    /**
     * Faz com que o robô ande se desloque mantendo o alinhamento de sua carroceria com a mesma
     * direção na qual se encontrava antes de iniciar o movimento.
     * 
     * @param ângulo final das rodas em relação à frente do robô, em graus. Valor entre -32 e +32.
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws IOException
     * @throws EmptyMessageException
     * @throws StrafeRobotException
     * @throws InterruptedException 
     */
	public void strafe(byte angle, int intensity, String source) throws IOException, EmptyMessageException, StrafeRobotException, InterruptedException{
		robotConnection.sendStrafeCommand(angle, intensity, source);
	}

    /**
     * Faz o robô girar em torno do próprio eixo.
     * 
     * @param side sentido do giro. Anti-horário Robot.SPIN_COUNTERCLOCKWISE ou horário Robot.SPIN_CLOCKWISE.
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws IOException
     * @throws EmptyMessageException
     * @throws SpinRobotException
     * @throws InterruptedException 
     */
	public void spin(byte side, int intensity, String source) throws IOException, EmptyMessageException, SpinRobotException, InterruptedException{
		robotConnection.sendSpinCommand(side, intensity, source);
	}
	
	/**
	 * Lê os valores dos sensores de infravermelho de profundidade.
	 * 
	 * @return um array de 2 bytes, sendo o primeiro o valor do sensor dianteiro e o segundo o valor do sensor traseiro.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadInfraredSensorsDepthException
	 * @throws InterruptedException 
	 */
	public int[] readInfraredSensorsDepth(String source) throws IOException, EmptyMessageException, ReadInfraredSensorsDepthException, InterruptedException{
		return robotConnection.sendReadInfraredSensorsDepthCommand(source);
	}
	
	/**
	 * Lê os valores dos sensores de infravermelho de distância.
	 * 
	 * @return um array de 2 bytes, sendo o primeiro o valor do sensor esquerdo e o segundo o valor do sensor direito.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadInfraredSensorsDistanceException
	 * @throws InterruptedException 
	 */
	public int[] readInfraredSensorsDistance(String source) throws IOException, EmptyMessageException, ReadInfraredSensorsDistanceException, InterruptedException{
		return robotConnection.sendReadInfraredSensorsDistanceCommand(source);
	}
	
	/**
	 * Lê os valores dos sensores de ultrassom de distância.
	 * 
	 * @return um array de 4 ints, sendo o primeiro o valor do sensor frontal, o segundo do sensor traseiro,
	 * 			o terceito do sensor esquerdo e o quarto do sensor direito.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadUltrassonicSensorsDistanceException
	 * @throws InterruptedException 
	 */
	public int[] readUltrassonicSensorsDistance(String source) throws IOException, EmptyMessageException, ReadUltrassonicSensorsDistanceException, InterruptedException{
		return robotConnection.sendReadUltrassonicSensorsDistanceCommand(source);
	}
	
	/**
	 * Lê os valores dos sensores de ultrassom de luminosidade.
	 * 
	 * @return um array de 4 bytes, sendo o primeiro o valor do sensor frontal, o segundo do sensor traseiro,
	 * 			o terceito do sensor esquerdo e o quarto do sensor direito.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadUltrassonicSensorsLuminosityException
	 * @throws InterruptedException 
	 */
	public int[] readUltrassonicSensorsLuminosity(String source) throws IOException, EmptyMessageException, ReadUltrassonicSensorsLuminosityException, InterruptedException{
		return robotConnection.sendReadUltrassonicSensorsLuminosityCommand(source);
	}
	
	/**
	 * Lê os valores da bússola.
	 * 
	 * @return um int representando o valor do ângulo da bússola, em décimos de graus (de 0 até 3599) ou seja, ângulo = (valor / 10) graus.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadCompassSensorException
	 * @throws InterruptedException 
	 */
	public int readCompassSensor(String source) throws IOException, EmptyMessageException, ReadCompassSensorException, InterruptedException{
		return robotConnection.sendReadCompassSensorCommand(source);
	}
	
	/**
	 * Lê os valores do acelerômetro.
	 * 
	 * @return um array de 3 ints representando os valores em décimos de milésimos de força "g" ou seja,
	 * 		   força = (valor / 10000) "g", sendo o primeiro o valor do eixo X, o segundo do eixo Y e o terceiro do eixo Z.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadAccelerometerSensorException
	 * @throws InterruptedException 
	 */
	public int[] readAccelerometerSensor(String source) throws IOException, EmptyMessageException, ReadAccelerometerSensorException, InterruptedException{
		return robotConnection.sendReadAccelerometerSensorCommand(source);
	}
	
	/**
	 * Lê os valores dos sensores de temperatura e de umidade.
	 * 
	 * @return um array de 2 ints, sendo o primeiro o valor do sensor de temperatura e o segundo do sensor de umidade.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadTemperatureAndHumiditySensorsException
	 * @throws InterruptedException 
	 */
	public int[] readTemperatureAndHumiditySensors(String source) throws IOException, EmptyMessageException, ReadTemperatureAndHumiditySensorsException, InterruptedException{
		return robotConnection.sendReadTemperatureAndHumiditySensorsCommand(source);
	}
	
	/**
	 * Lê os valores dos sensores ópticos do robô utilizados para detectar colisão.
	 * 
	 * @return um byte indicando a ausência (0) ou presença (1) de objetos à frente de cada um dos 8 sensores.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadOpticalSensorsException
	 * @throws InterruptedException 
	 */
	public boolean readCollisionDetection(String source) throws IOException, EmptyMessageException, ReadOpticalSensorsException, InterruptedException{
		return robotConnection.sendReadCollisionDetectionCommand(source);
	}
	
	/**
	 * Lê as versões do módulo receptor GPS.
	 * 
	 * @return um array de 2 bytes, sendo o primeiro correspondente ao número da versão do hardware e o segundo ao número da versão do firmware do GPS.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSInfoException
	 * @throws InterruptedException 
	 */
	public int[] readGPSInfo(String source) throws IOException, EmptyMessageException, ReadGPSInfoException, InterruptedException{
		return robotConnection.sendReadGPSInfoCommand(source);
	}
	
	/**
	 * Lê todos os dados do GPS.
	 * 
	 * @return um array de int contendo todos os dados do GPS.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSGetException
	 * @throws InterruptedException 
	 */
	public int[] readGPSGet(String source) throws IOException, EmptyMessageException, ReadGPSGetException, InterruptedException{
		return robotConnection.sendReadGPSGetCommand(source);
	}
	
	/**
	 * Verifica a validade da string de dados recebida pelo GPS.
	 * 
	 * @return um byte indicando se a string é válida (0), ou não é válida (1).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSValidateException
	 * @throws InterruptedException 
	 */
	public boolean readGPSValidate(String source) throws IOException, EmptyMessageException, ReadGPSValidateException, InterruptedException{
		return robotConnection.sendReadGPSValidateCommand(source);
	}
	
	/**
	 * Lê o número de satélites utilizados pelo GPS.
	 * 
	 * @return um byte indicando número de satélites utilizados pelo GPS.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSSatelliteException
	 * @throws InterruptedException 
	 */
	public int readGPSSatellite(String source) throws IOException, EmptyMessageException, ReadGPSSatelliteException, InterruptedException{
		return robotConnection.sendReadGPSSatelliteCommand(source);
	}
	
	/**
	 * Retorna o horário no meridiano de Greenwich.
	 * 
	 * @return um array de 3 bytes, sendo o primeiro correspondente à hora, o segundo aos minutos e o terceiro aos segundos.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSTimeException
	 * @throws InterruptedException 
	 */
	public int[] readGPSTime(String source) throws IOException, EmptyMessageException, ReadGPSTimeException, InterruptedException{
		return robotConnection.sendReadGPSTimeCommand(source);
	}
	
	/**
	 * Retorna a data no meridiano de Greenwich.
	 * 
	 * @return um array de 3 bytes, sendo o primeiro correspondente ao ano, o segundo ao mês e o terceiro ao dia.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSDateException
	 * @throws InterruptedException 
	 */
	public int[] readGPSDate(String source) throws IOException, EmptyMessageException, ReadGPSDateException, InterruptedException{
		return robotConnection.sendReadGPSDateCommand(source);
	}
	
	/**
	 * Lê a latitude do robô em relação à linha do equador.
	 * 
	 * @return um array de 4 ints, sendo o primeiro correspondente aos graus da latitude, o segundo aos minutos da latitude,
	 * 		   	o terceiro correspondente à fração de minuto da latitude entre [0; 65535]
	 * 			e o quarto indica se a latitude é norte (0) ou sul (1).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSLatitudeException
	 * @throws InterruptedException 
	 */
	public int[] readGPSLatitude(String source) throws IOException, EmptyMessageException, ReadGPSLatitudeException, InterruptedException{
		return robotConnection.sendReadGPSLatitudeCommand(source);
	}
	
	/**
	 * Lê a longitude do robô em relação à linha do equador.
	 * 
	 * @return um array de 4 ints, sendo o primeiro correspondente aos graus da longitude, o segundo aos minutos da longitude,
	 * 		   	o terceiro correspondente à fração de minuto da longitude entre [0; 65535]
	 * 			e o quarto indica se a longitude é leste (0) ou oeste (1).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSLongitudeException
	 * @throws InterruptedException 
	 */
	public int[] readGPSLongitude(String source) throws IOException, EmptyMessageException, ReadGPSLongitudeException, InterruptedException{
		return robotConnection.sendReadGPSLongitudeCommand(source);
	}
	
	/**
	 * Lê a altitute do robô em relação ao nível do mar.
	 * 
	 * @return um int representando a altitude do robô, em decímetros entre [0; 65535].
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSAltitudeException
	 * @throws InterruptedException 
	 */
	public int readGPSAltitude(String source) throws IOException, EmptyMessageException, ReadGPSAltitudeException, InterruptedException{
		return robotConnection.sendReadGPSAltitudeCommand(source);
	}
	
	/**
	 * Lê a velocidade linear do robô independentemente de sua direção.
	 * 
	 * @return um int representando velocidade do robô, em décimos de nós.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSSpeedException
	 * @throws InterruptedException 
	 */
	public int readGPSSpeed(String source) throws IOException, EmptyMessageException, ReadGPSSpeedException, InterruptedException{
		return robotConnection.sendReadGPSSpeedCommand(source);
	}
	
	/**
	 * Lê o ângulo entre a direção do deslocamento do robô ao norte magnético da terra.
	 * 
	 * @return um int representando direção do movimento do robô, dada em décimos de graus.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSHeadException
	 * @throws InterruptedException 
	 */
	public int readGPSHead(String source) throws IOException, EmptyMessageException, ReadGPSHeadException, InterruptedException{
		return robotConnection.sendReadGPSHeadCommand(source);
	}
	
	/**
	 * Lê o nível da carga da bateria do robô.
	 * 
	 * @return um byte indicando nível da bateria do robô, variando de 0 a 100.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadBatteryLevelException
	 * @throws InterruptedException 
	 */
	public int readBatteryLevel(String source) throws IOException, EmptyMessageException, ReadBatteryLevelException, InterruptedException{
		return robotConnection.sendReadBatteryLevelCommand(source);
	}
	
	/**
	 * Lê a versão corrente do Módulo de Alta Performance (MAP).
	 * 
	 * @return um array de 3 bytes indicando a versão do MAP.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadMAPVersionException
	 * @throws InterruptedException 
	 */
	public int[] readMAPVersion(String source) throws IOException, EmptyMessageException, ReadMAPVersionException, InterruptedException{
		return robotConnection.sendReadMAPVersionCommand(source);
	}
	
	/**
	 * Inicia a captura contínua das imagens da câmera.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws CameraStartException
	 * @throws InterruptedException 
	 * @throws CameraImageFormatLenghtException 
	 */
	public void cameraStart(String source) throws IOException, EmptyMessageException, CameraStartException, InterruptedException, CameraImageFormatLenghtException{
		InputStream cameraInputStream = robotConnection.sendCameraStartCommand(source);		
//		startCameraThread(cameraInputStream);
		camera = Camera.getInstance();
		camera.setStartCaptureAttributes(cameraInputStream);
	}
	
	/**
	 * Inicia a thread da câmera.
	 * 
	 * @param cameraInputStream Fluxo de dados vindos da câmera.
	 */
	/*private void startCameraThread(InputStream cameraInputStream){		
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("Robot.startCameraThread():");
	        debugStr.append("\nIniciando captura de imagens da câmera...");
	        debugStr.append("\n===========\n");
			System.out.println(debugStr);
		}
		
		camera = Camera.getInstance();
		camera.setStartCaptureAttributes(cameraInputStream);
		
		cameraThread = new Thread(camera);
		cameraThread.start();
	}*/
	
	/**
	 * Finaliza a captura de imagens da câmera.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws CameraStopException
	 * @throws InterruptedException 
	 */
	public void cameraStop(String source) throws IOException, EmptyMessageException, CameraStopException, InterruptedException{
//		stopCameraThread();
		robotConnection.sendCameraStopCommand(source);
	}
	
	/**
	 * Finaliza a thread da câmera.
	 * 
	 * @throws InterruptedException 
	 */
	/*private void stopCameraThread() throws InterruptedException{	
        StringBuffer debugStr = new StringBuffer();
		
		camera = Camera.getInstance();		
		camera.setStopCaptureAttributes();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("Robot.stopCameraThread():");
	        debugStr.append("\nChamando 'camera.setStopCaptureAttributes()'...");
			System.out.println(debugStr);
		}
		
		while (!camera.canStopCapture()){
			
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("\nAguardando para finalizar captura da câmera...");
				System.out.println(debugStr);
			}
			
			Thread.sleep(2000);
		}
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nFinalizando captura de imagens da câmera...");
	        debugStr.append("\n===========\n");
			System.out.println(debugStr);
		}
		cameraThread.join(1000);		
	}*/
	
	/**
	 * 
	 * @return
	 * 
	 * @throws InterruptedException 
	 * @throws CameraException 
	 * @throws CameraImageFormatLenghtException 
	 * @throws IOException 
	 */
	public CameraImage acquireCameraImage() throws IOException, CameraImageFormatLenghtException, CameraException, InterruptedException{
		return camera.acquireImage();
	}
	
}
