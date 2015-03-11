package br.cefetmg.lsi.robodeck.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Vector;

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
import br.cefetmg.lsi.robodeck.utils.PrimitiveDataTypesManipulation;
import br.cefetmg.lsi.robodeck.utils.PropertiesLoaderImpl;


/**
 * Esta classe oferece o básico para realizar uma conexão e troca de mensagens
 * com o Robô.
 * Outras classes devem ser especializadas a partir desta para implementarem
 * os protocolos de comunicação desejados.
 * 
 * @see RobotBluetoothNetwork
 * @see RobotWifiNetwork
 */
public abstract class RobotConnection {
	protected final boolean debug = new Boolean(PropertiesLoaderImpl.getValor("robodeckapi.debugmode")); // Para ativar/desativar saidas "System.out.println();" de depuracao.
	
	private final byte CMD_DONE = (byte)0xFF;
	
    private static String connectionHost;
	
	/**
	 * Instância da conexao com o robo.
	 */
	private static RobotConnection instance;
	
    /**
     * Fluxo de entrada de dados da comunicação com o robô.
     */
	protected InputStream inputStream;
	
    /**
     * Fluxo de saída de dados da comunicação com o robô.
     */
	protected OutputStream outputStream;
    
    /**
     * Soquete de conexão com a câmera do robô.
     */
    private static Socket cameraSocketConnection;
    
    /**
     * Criador de pacotes de comunicação com o robô.
     */
    private static PackageBuilder packageBuilder;
    
    /**
     * Para saber se deve esperar para enviar outro comando. Sempre que enviar um comando, deve esperar sua resposta.
     */
    private boolean waitReceive = false;
    
    /**
     * Para saber se deve esperar para ler outro comando. Só se pode ler a resposta de um comando já enviado.
     */
    private boolean waitSend = true;

    /**
     * Cria uma conexão do tipo WiFi.
     * 
     * @param port Porta a ser utilizada para a conexão.
     * 
     * @return Uma conexão WiFi.
     * 
     * @throws DifferentRobotConnectionAlreadyExists 
     * @throws UnknownHostException 
     * @throws PortUnreachableException 
     */
    public static RobotConnection makeConnection() throws DifferentRobotConnectionAlreadyExists
    		, PortUnreachableException, UnknownHostException {

    	int src = Integer.parseInt(PropertiesLoaderImpl.getValor("robot.network.src"));
    	int dst = Integer.parseInt(PropertiesLoaderImpl.getValor("robot.network.dst"));	
    	
    	connectionHost = PropertiesLoaderImpl.getValor("robot.network.host");
    	
    	initialize(src, dst);
    	
		if (instance == null){
    		return new RobotWifiNetwork(connectionHost, Integer.parseInt(PropertiesLoaderImpl.getValor("robot.network.port")));
    	} else {
    		
    		if (instance instanceof RobotBluetoothNetwork){
        		return instance;
    		} else {
    			throw new DifferentRobotConnectionAlreadyExists(instance.getClass(), RobotBluetoothNetwork.class);
    		}
    		
    	}
    	
    }

    /**
     * Cria uma conexão do tipo Bluetooth.
     * 
     * @param uuid UUID do servidor do Robô.
     * 
     * @return Uma conexão Bluetooth.
     * 
     * @throws DifferentRobotConnectionAlreadyExists 
     */
    public static RobotConnection makeConnection(String uuid) throws DifferentRobotConnectionAlreadyExists {
    	
    	// ******
    	// TODO: Estes parâmetros deveriam ser dinâmicos, não hardcoded.
    	int src = 2;
    	int dst = 1;
    	// ******
    	
    	initialize(src, dst);
    	
    	if (instance == null){
    		return new RobotBluetoothNetwork(uuid);
    	} else {
    		
    		if (instance instanceof RobotBluetoothNetwork){
        		return instance;
    		} else {
    			throw new DifferentRobotConnectionAlreadyExists(instance.getClass(), RobotBluetoothNetwork.class);
    		}
    		
    	}

    }
	
	/**
	 * Inicia demais componentes do robô.
	 * 
	 * @param src Remetente do pacote.
	 * @param dst Destinatário do pacote.
	 */
	private static void initialize(int src, int dst){
		packageBuilder = PackageBuilder.getInstance(src, dst);
	}

    /**
     * Conecta-se ao Robô de acordo com o tipo de conexão (wifi, bluethooth etc.).
     * 
     * @throws IOException 
     * @throws UnknownHostException
     */
    abstract public void connect() throws IOException;

    /**
     * Desconecta-se com o robô através de uma conexão wifi.
     * 
     * @throws IOException 
     */
    abstract public void disconnect() throws IOException;
	
	/**
	 * Envia um pacote ao robô e incrementa o id do pacote.
	 * 
	 * @param bytes Pacote a ser enviado.
	 * 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private synchronized void send(byte[] bytes) throws IOException, InterruptedException {
		
		while (waitReceive == true) {
			
	        StringBuffer debugStr = new StringBuffer();
			
			if (debug){
		        debugStr.append("\n===========\n");
		        debugStr.append("RobotConnection.send():");
		        debugStr.append("\nAguardando resposta...");
		        debugStr.append("\n===========\n");
				System.out.println(debugStr);
			}
			
//			try {
				wait();
//			} catch (InterruptedException e) {
//				throw new SendPackageException("Falha ao bloquear thread no método RobotConnection.send().");
//			}
			
		}

//		try{
			waitReceive = true;
			
			outputStream.write(bytes);
			outputStream.flush();
			packageBuilder.incrementPid();
//		} catch (IOException e) {
//			throw new SendPackageException("Falha ao enviar pacote, método RobotConnection.send().");
//		}

		waitSend = false;

		notifyAll();
	}

	/**
	 * Recebe um pacote do robô.
	 * 
	 * @return Pacote recebido
	 * 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private synchronized byte[] receive() throws InterruptedException, IOException {
		
		while (waitSend == true) {
			
	        StringBuffer debugStr = new StringBuffer();
			
			if (debug){
		        debugStr.append("\n===========\n");
		        debugStr.append("RobotConnection.receive():");
		        debugStr.append("\nAguardando envio de comando...");
		        debugStr.append("\n===========\n");
				System.out.println(debugStr);
			}
			
//			try {
				wait();
//			} catch (InterruptedException e) {
//				throw new ReceivePackageException("Falha ao bloquear thread no método RobotConnection.receive().");
//			}
			
		}

		waitSend = true;
		
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        try {

            byte[] header = new byte[PackageBuilder.HEADER_LENGTH];
            byte[] msgRcvd;
            int msgLength = 0;
            
			inputStream.read(header, 0, PackageBuilder.HEADER_LENGTH);
	        baos.write(header);

	        msgLength = (header[9] & 0xFF);

	        msgRcvd = new byte[msgLength];

	        inputStream.read(msgRcvd, 0, msgLength);
	        baos.write(msgRcvd);
	        baos.flush();
	        baos.close();
//		} catch (IOException e) {
//			throw new ReceivePackageException("Falha ao receber pacote, método RobotConnection.receive().");
//		}
		
		waitReceive = false;

		notifyAll();

        return baos.toByteArray();
    }

    /**
     * Abre uma sessão com o robô.
     * 
     * @return O ID da sessão.
     * 
     * @throws IOException
     * @throws OpenSessionException 
     * @throws EmptyMessageException 
     * @throws InterruptedException 
     */
    public void openSession() throws IOException, OpenSessionException, EmptyMessageException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.openSession():");
	        debugStr.append("\nAbrindo sessao...");
			System.out.println(debugStr);
		}
		
		// Cria o pacote de abertura de sessao e o envia.
        byte[] pack = packageBuilder.createOpenSessionPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo e ajusta o id da sessao.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}
		
        packageBuilder.setSid(PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]));
        
		// Checa se a sessao foi realmente aberta. 
        if (packageBuilder.getSid() == 0x0000){
        	throw new OpenSessionException("Falha ao abrir a sessão."); 
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nSessao aberta.");
	        debugStr.append("\nSID: " + packageBuilder.getSid());
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
    }
    	
    /**
     * Fecha uma sessão com o robô.
     * 
     * @throws IOException 
     * @throws CloseSessionException 
     * @throws EmptyMessageException 
     * @throws InterruptedException 
     */
    public void closeSession() throws IOException, CloseSessionException, EmptyMessageException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.closeSession():");
	        debugStr.append("\nFechando a sessão " + packageBuilder.getSid() + "...");
			System.out.println(debugStr);
		}
		
		// Cria o pacote de fechamento de sessao e o envia.
        byte[] pack = packageBuilder.createCloseSessionPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se a sessao foi realmente fechada.
        if (!((ans[10] == (byte)0x81) && (ans[11] == (byte)0x02) && (ans[12] == CMD_DONE))){
        	throw new CloseSessionException("Falha ao fechar a sessão.");
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nSessao fechada.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
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
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[3];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.getCommunicationProtocolVersion():");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a versão do protocolo de comunicação.
        byte[] pack = packageBuilder.createCommunicationProtocolVersionPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x80) && (ans[11] == (byte)0x00))){
        	throw new GetCommunicationProtocolVersionException("Falha ao receber informação da versão do protocolo de comunicação.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.byteToInt(ans[14]);
        
        String communicationProtocolVersion = sensorValues[0] + "." + sensorValues[1] + "." + sensorValues[2];
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nVersão do protocolo de comunicação: " + communicationProtocolVersion);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return communicationProtocolVersion;
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
    public void sendMoveCommand(int intensity, String source) throws IOException, EmptyMessageException, MoveRobotException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendMoveCommand(" + intensity + ", " + source + "):");
			System.out.println(debugStr);
		}
		
		// Cria o pacote dizendo para o robô se mover e o envia.
        byte[] pack = packageBuilder.createMovePackage(intensity);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x01) && (ans[12] == CMD_DONE))){
        	throw new MoveRobotException("Falha ao mover robô.");
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nComando aceito.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
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
    public void sendTurnCommand(byte angle, int intensity, String source) throws IOException, EmptyMessageException, TurnRobotException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendTurnCommand(" + angle + ", " + intensity + ", " + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô se virar e o envia.
        byte[] pack = packageBuilder.createTurnPackage(angle, intensity);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x02) && (ans[12] == CMD_DONE))){
        	throw new TurnRobotException("Falha ao girar robô.");
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nComando aceito.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
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
    public void sendStrafeCommand(byte angle, int intensity, String source) throws IOException, EmptyMessageException, StrafeRobotException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendStrafeCommand(" + angle + ", " + intensity + ", " + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô se mover alinhado com sua carroceria.
        byte[] pack = packageBuilder.createStrafePackage(angle, intensity);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x03) && (ans[12] == CMD_DONE))){
        	throw new StrafeRobotException("Falha ao mover robô alinhado com sua carroceria.");
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nComando aceito.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
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
	public void sendSpinCommand(byte side, int intensity, String source) throws IOException, EmptyMessageException, SpinRobotException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendSpinCommand(" + side + ", " + intensity + ", " + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô girar.
        byte[] pack = packageBuilder.createSpinPackage(side, intensity);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x04) && (ans[12] == CMD_DONE))){
        	throw new SpinRobotException("Falha ao girar o robô.");
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nComando aceito.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
        
    }
	
	/**
	 * Lê os valores dos sensores de infravermelho de profundidade.
	 * 
	 * @return um array de 2 bytes, sendo o primeiro o valor do sensor dianteiro e o segundo o valor do sensor traseiro.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws InterruptedException 
	 * @throws SpinRobotException
	 */
	public int[] sendReadInfraredSensorsDepthCommand(String source) throws IOException, EmptyMessageException, ReadInfraredSensorsDepthException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadInfraredSensorsDepthCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de seus sensores de infravermelho.
        byte[] pack = packageBuilder.createInfraredSensorsPackage(PackageBuilder.INFRAREDS_DEPTH);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x05))){
        	throw new ReadInfraredSensorsDepthException("Falha ao receber dados dos sensores de infravermelho de profundidade do robô.");
        }
		
        int[] sensorValues = new int[2];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nDianteiro: " + sensorValues[0]);
			debugStr.append("\nTraseiro: " + sensorValues[1]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Lê os valores dos sensores de infravermelho de distância.
	 * 
	 * @return um array de 2 bytes, sendo o primeiro o valor do sensor esquerdo e o segundo o valor do sensor direito.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws InterruptedException 
	 * @throws SpinRobotException
	 */
	public int[] sendReadInfraredSensorsDistanceCommand(String source) throws IOException, EmptyMessageException, ReadInfraredSensorsDistanceException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadInfraredSensorsDistanceCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de seus sensores de infravermelho.
        byte[] pack = packageBuilder.createInfraredSensorsPackage(PackageBuilder.INFRAREDS_DISTANCE);
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x05))){
        	throw new ReadInfraredSensorsDistanceException("Falha ao receber dados dos sensores de infravermelho de distância do robô.");
        }
		
        int[] sensorValues = new int[2];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nEsquerdo: " + sensorValues[0]);
			debugStr.append("\nDireito: " + sensorValues[1]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadUltrassonicSensorsDistanceCommand(String source) throws IOException, EmptyMessageException, ReadUltrassonicSensorsDistanceException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[4];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadUltrassonicSensorsDistanceCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria os pacotes dizendo para o robô informar os valores de seus sensores de utlrassom de distância.
        byte[] packFront = packageBuilder.createUltrassonicSensorsDistancePackage(PackageBuilder.ULTRASONIC_FRONT);
        byte[] packRear = packageBuilder.createUltrassonicSensorsDistancePackage(PackageBuilder.ULTRASONIC_REAR);
        byte[] packLeft = packageBuilder.createUltrassonicSensorsDistancePackage(PackageBuilder.ULTRASONIC_LEFT);
        byte[] packRight = packageBuilder.createUltrassonicSensorsDistancePackage(PackageBuilder.ULTRASONIC_RIGHT);
        
        Vector <byte[]> packs = new Vector<byte[]>();
        packs.add(packFront);
        packs.add(packRear);
        packs.add(packLeft);
        packs.add(packRight);
        
        // Envia os pacotes e recebe as respostas.
        int i = 0;
        for (byte[] pack : packs){
	        // Envia o pacote.
        	send(pack);
			
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
				System.out.println(debugStr);
			}
	
	        // Recebe a resposta do robo.
	        byte[] ans = receive();
			
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
				System.out.println(debugStr);
			}
	
			// Checa se o comando foi aceito.
	        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x06))){
	        	throw new ReadUltrassonicSensorsDistanceException("Falha ao receber dados dos sensores de ultrassom do robô.");
	        }
			
	        // Armazena os valores no array de resposta.	        
	        sensorValues[i] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
	        i++;
        }
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nFrontal: " + sensorValues[0]);
			debugStr.append("\nTraseiro: " + sensorValues[1]);
			debugStr.append("\nEsquerdo: " + sensorValues[2]);
			debugStr.append("\nDireito: " + sensorValues[3]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadUltrassonicSensorsLuminosityCommand(String source) throws IOException, EmptyMessageException, ReadUltrassonicSensorsLuminosityException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[4];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadUltrassonicSensorsLuminosityCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria os pacotes dizendo para o robô informar os valores de seus sensores de utlrassom de luminosidade.
        byte[] packFront = packageBuilder.createUltrassonicSensorsLuminosityPackage(PackageBuilder.ULTRASONIC_FRONT);
        byte[] packRear = packageBuilder.createUltrassonicSensorsLuminosityPackage(PackageBuilder.ULTRASONIC_REAR);
        byte[] packLeft = packageBuilder.createUltrassonicSensorsLuminosityPackage(PackageBuilder.ULTRASONIC_LEFT);
        byte[] packRight = packageBuilder.createUltrassonicSensorsLuminosityPackage(PackageBuilder.ULTRASONIC_RIGHT);
        
        Vector <byte[]> packs = new Vector<byte[]>();
        packs.add(packFront);
        packs.add(packRear);
        packs.add(packLeft);
        packs.add(packRight);
        
        // Envia os pacotes e recebe as respostas.
        int i = 0;
        for (byte[] pack : packs){
	        // Envia o pacote.
        	send(pack);
			
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
				System.out.println(debugStr);
			}
	
	        // Recebe a resposta do robo.
	        byte[] ans = receive();
			
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
				System.out.println(debugStr);
			}
	
			// Checa se o comando foi aceito.
	        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x07))){
	        	throw new ReadUltrassonicSensorsLuminosityException("Falha ao receber dados dos sensores de ultrassom do robô.");
	        }
			
	        // Armazena os valores no array de resposta.	        
	        sensorValues[i] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
	        i++;
        }
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nFrontal: " + sensorValues[0]);
			debugStr.append("\nTraseiro: " + sensorValues[1]);
			debugStr.append("\nEsquerdo: " + sensorValues[2]);
			debugStr.append("\nDireito: " + sensorValues[3]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int sendReadCompassSensorCommand(String source) throws IOException, EmptyMessageException, ReadCompassSensorException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValue;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadCompassSensorCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de sua bússola.
        byte[] pack = packageBuilder.createCompassSensorPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x08))){
        	throw new ReadCompassSensorException("Falha ao receber dados da bússola.");
        }
		
        // Armazena o valor no array de resposta.
        sensorValue = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValor do sensor: " + sensorValue);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValue;
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
	public int[] sendReadAccelerometerSensorCommand(String source) throws IOException, EmptyMessageException, ReadAccelerometerSensorException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[3];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadAccelerometerSensorCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de seu acelerômetro.
        byte[] pack = packageBuilder.createAccelerometerSensorPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x09))){
        	throw new ReadAccelerometerSensorException("Falha ao receber dados do acelerômetro.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues[0] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        sensorValues[1] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[14], ans[15]);
        sensorValues[2] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[16], ans[17]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores do sensor:");
			debugStr.append("\nEixo X: " + sensorValues[0]);
			debugStr.append("\nEixo Y: " + sensorValues[1]);
			debugStr.append("\nEixo Z: " + sensorValues[2]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Lê os valores dos sensores de temperatura e de umidade.
	 * 
	 * @return um array de 2 ints, sendo o primeiro o valor do sensor de temperatura
	 * 		   (em centésimos de graus celsius - ex.: 3289, que é 32,89oC) e o segundo do sensor de umidade
	 *         (em porcentagem - ex.: 44, que é 44%).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws InterruptedException 
	 * @throws ReadUltrassonicSensorsLuminosityException
	 */
	public int[] sendReadTemperatureAndHumiditySensorsCommand(String source) throws IOException, EmptyMessageException, ReadTemperatureAndHumiditySensorsException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[2];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadTemperatureAndHumiditySensorsCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de seus sensores de temperatura e umidade.
        byte[] pack = packageBuilder.createTemperatureAndHumiditySensorsPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0A))){
        	throw new ReadTemperatureAndHumiditySensorsException("Falha ao receber dados dos sensores de temperatura e umidade.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues[0] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        sensorValues[1] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[14], ans[15]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nTemperatura: " + sensorValues[0]);
			debugStr.append("\nUmidade: " + sensorValues[1]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Lê os valores dos sensores ópticos do robô utilizados para detectar colisão.
	 * 
	 * @return "false" na ausência ou "true" na presença de objetos à frente de cada um dos 8 sensores ópticos
	 * 		   (somente se houver algum objeto a menos de 5 milêmetros de distância de algum dos sensores).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadOpticalSensorsException
	 * @throws InterruptedException 
	 */
	public boolean sendReadCollisionDetectionCommand(String source) throws IOException, EmptyMessageException, ReadOpticalSensorsException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        byte sensorValues;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadCollisionDetectionCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar os valores de seus sensores ópticos.
        byte[] pack = packageBuilder.createCollisionDetectionPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0B))){
        	throw new ReadOpticalSensorsException("Falha ao receber dados dos sensores ópticos.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues = ans[12];
        
        boolean hasObject = (sensorValues == (byte)0xFF);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores: " + String.format("%02x", sensorValues));
			debugStr.append("\nhasObject: " + hasObject);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return hasObject;
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
	public int[] sendReadGPSInfoCommand(String source) throws IOException, EmptyMessageException, ReadGPSInfoException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSInfoCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar as versões do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSInfoPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0C))){
        	throw new ReadGPSInfoException("Falha ao receber as versões do módulo receptor GPS do robô.");
        }
		
        int[] sensorValues = new int[2];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nVersões do módulo receptor GPS:");
			debugStr.append("\nHardware: " + sensorValues[0]);
			debugStr.append("\nFirmware: " + sensorValues[1]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Lê todos os dados do GPS.
	 * 
	 * @return um array de byte contendo todos os dados do GPS.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSGetException
	 * @throws InterruptedException 
	 */
	public int[] sendReadGPSGetCommand(String source) throws IOException, EmptyMessageException, ReadGPSGetException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int[] sensorValues = new int[21];
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSGetCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar todos os dados do GPS.
        byte[] pack = packageBuilder.createGPSGetPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x16))){
        	throw new ReadGPSGetException("Falha ao receber todas as informações do GPS ao mesmo tempo.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]); // hard: número da versão do hardware do GPS
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]); // firm: número da versão do firmware do GPS
        sensorValues[2] = PrimitiveDataTypesManipulation.byteToInt(ans[14]); // validity: indica se a string é válida (0), ou não é válida (1)
        sensorValues[3] = PrimitiveDataTypesManipulation.byteToInt(ans[15]); // sats: número de satélites utilizados
        sensorValues[4] = PrimitiveDataTypesManipulation.byteToInt(ans[16]); // hour: horas no meridiano de Greenwich
        sensorValues[5] = PrimitiveDataTypesManipulation.byteToInt(ans[17]); // min: minutos no meridiano de Greenwich
        sensorValues[6] = PrimitiveDataTypesManipulation.byteToInt(ans[18]); // sec: segundos no meridiano de Greenwich
        sensorValues[7] = PrimitiveDataTypesManipulation.byteToInt(ans[19]); // year: ano no meridiano de Greenwich após o ano de 2000
        sensorValues[8] = PrimitiveDataTypesManipulation.byteToInt(ans[20]); // month: mêsno meridiano de Greenwich
        sensorValues[9] = PrimitiveDataTypesManipulation.byteToInt(ans[21]); // day: dia no meridiano de Greenwich
        sensorValues[10] = PrimitiveDataTypesManipulation.byteToInt(ans[22]); // deg: graus da latitude
        sensorValues[11] = PrimitiveDataTypesManipulation.byteToInt(ans[23]); // min: minutos da latitude
        sensorValues[12] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[24], ans[25]); // frac: fração de minuto da latitute entre [0; 65535]
        sensorValues[13] = PrimitiveDataTypesManipulation.byteToInt(ans[26]); // dir: indica se a latitude é norte (0) ou sul (1)
        sensorValues[14] = PrimitiveDataTypesManipulation.byteToInt(ans[27]); // deg: graus da longitude
        sensorValues[15] = PrimitiveDataTypesManipulation.byteToInt(ans[28]); // min: minutos da longitude
        sensorValues[16] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[29], ans[30]); // frac: fração de minuto da latitute [0; 65535]
        sensorValues[17] = PrimitiveDataTypesManipulation.byteToInt(ans[31]); // dir: indica se a longitude é leste (0) ou oeste (1)
        sensorValues[18] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[32], ans[33]); // dm: altitude do robô em decímetros entre [0; 65535]
        sensorValues[19] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[34], ans[35]); // speed: velocidade do robô, em décimos de nós
        sensorValues[20] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[36], ans[37]); // dir: direção do movimento do robô, dada em décimos de graus
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores:");
			debugStr.append("\nNúmero da versão do hardware: " + sensorValues[0]);
			debugStr.append("\nNúmero da versão do firmware: " + sensorValues[1]);
			debugStr.append("\nString válida?: " + (sensorValues[2] == (byte)0xFF));
			debugStr.append("\nNúmero de satélites: " + sensorValues[3]);
			debugStr.append("\nHoras: " + sensorValues[4]);
			debugStr.append("\nMinutos: " + sensorValues[5]);
			debugStr.append("\nSegundos: " + sensorValues[6]);
			debugStr.append("\nAno: " + sensorValues[7]);
			debugStr.append("\nMês: " + sensorValues[8]);
			debugStr.append("\nDia: " + sensorValues[9]);
			debugStr.append("\nGraus da latitude: " + sensorValues[10]);
			debugStr.append("\nMinutos da latitude: " + sensorValues[11]);
			debugStr.append("\nFração de minuto da latitute: " + sensorValues[12]);
			debugStr.append("\nA latitude é norte (0) ou sul (1)?: " + sensorValues[13]);
			debugStr.append("\nGraus da longitude: " + sensorValues[14]);
			debugStr.append("\nMinutos da longitude: " + sensorValues[15]);
			debugStr.append("\nFração de minuto da latitute: " + sensorValues[16]);
			debugStr.append("\nA longitude é leste (0) ou oeste (1)?: " + sensorValues[17]);
			debugStr.append("\nAltitude do robô em decímetros: " + sensorValues[18]);
			debugStr.append("\nVelocidade do robô, em décimos de nós: " + sensorValues[19]);
			debugStr.append("\nDireção do movimento do robô, dada em décimos de graus: " + sensorValues[20]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public boolean sendReadGPSValidateCommand(String source) throws IOException, EmptyMessageException, ReadGPSValidateException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        byte sensorValues;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSValidateCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a validade da string do GPS.
        byte[] pack = packageBuilder.createGPSValidatePackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0D))){
        	throw new ReadGPSValidateException("Falha ao receber informação sobre a validade da string do GPS.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues = ans[12];
        
        boolean gpsValid = (sensorValues == (byte)0xFF);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nValores dos sensores: " + String.format("%02x", sensorValues));
			debugStr.append("\nA string do GPS é " + (gpsValid ? "válida." : "inválida."));
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return gpsValid;
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
	public int sendReadGPSSatelliteCommand(String source) throws IOException, EmptyMessageException, ReadGPSSatelliteException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValues;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSSatelliteCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar o número de satélites utilizados pelo GPS.
        byte[] pack = packageBuilder.createGPSSatellitePackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0E))){
        	throw new ReadGPSSatelliteException("Falha ao receber informação sobre o número de satélites utilizados pelo GPS.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nNúmero de satélites utilizados pelo GPS: " + sensorValues);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadGPSTimeCommand(String source) throws IOException, EmptyMessageException, ReadGPSTimeException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSTimeCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar o horário do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSTimePackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x0F))){
        	throw new ReadGPSTimeException("Falha ao receber o horário do módulo receptor GPS do robô.");
        }
		
        int[] sensorValues = new int[3];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.byteToInt(ans[14]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nHorário do módulo receptor GPS:");
			debugStr.append("\nHoras: " + sensorValues[0]);
			debugStr.append("\nMinutos: " + sensorValues[1]);
			debugStr.append("\nSegundos: " + sensorValues[2]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadGPSDateCommand(String source) throws IOException, EmptyMessageException, ReadGPSDateException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSDateCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a data do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSDatePackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x10))){
        	throw new ReadGPSDateException("Falha ao receber a data do módulo receptor GPS do robô.");
        }
		
        int[] sensorValues = new int[3];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.byteToInt(ans[14]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nData do módulo receptor GPS:");
			debugStr.append("\nAno: " + sensorValues[0]);
			debugStr.append("\nMês: " + sensorValues[1]);
			debugStr.append("\nDia: " + sensorValues[2]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Lê a latitude do robô em relação à linha do equador.
	 * 
	 * @return um array de 4 ints, sendo o primeiro correspondente aos graus da latitude, o segundo aos minutos da latitude,
	 * 		   	o terceiro correspondente à fração de minuto da latitute entre [0; 65535]
	 * 			e o quarto indica se a latitude é norte (0) ou sul (1).
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws ReadGPSLatitudeException
	 * @throws InterruptedException 
	 */
	public int[] sendReadGPSLatitudeCommand(String source) throws IOException, EmptyMessageException, ReadGPSLatitudeException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSLatitudeCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a latitude do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSLatitudePackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x11))){
        	throw new ReadGPSLatitudeException("Falha ao receber a latitude do módulo receptor GPS do robô.");
        }
		
        int[] sensorValues = new int[4];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[14], ans[15]);
        sensorValues[3] = PrimitiveDataTypesManipulation.byteToInt(ans[16]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nLatitude do módulo receptor GPS:");
			debugStr.append("\nGraus: " + sensorValues[0]);
			debugStr.append("\nMinutos: " + sensorValues[1]);
			debugStr.append("\nFração de minuto: " + sensorValues[2]);
			debugStr.append("\nNorte (0) ou sul (1): " + sensorValues[3]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadGPSLongitudeCommand(String source) throws IOException, EmptyMessageException, ReadGPSLongitudeException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSLongitudeCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a longitude do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSLongitudePackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x12))){
        	throw new ReadGPSLongitudeException("Falha ao receber a longitude do módulo receptor GPS do robô.");
        }
		
        int[] sensorValues = new int[4];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.twoBytesToInt(ans[14], ans[15]);
        sensorValues[3] = PrimitiveDataTypesManipulation.byteToInt(ans[16]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nLongitude do módulo receptor GPS:");
			debugStr.append("\nGraus: " + sensorValues[0]);
			debugStr.append("\nMinutos: " + sensorValues[1]);
			debugStr.append("\nFração de minuto: " + sensorValues[2]);
			debugStr.append("\nLeste (0) ou oeste (1): " + sensorValues[3]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int sendReadGPSAltitudeCommand(String source) throws IOException, EmptyMessageException, ReadGPSAltitudeException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValue;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSAltitudeCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a altitude do módulo receptor GPS.
        byte[] pack = packageBuilder.createGPSAltitudePackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x13))){
        	throw new ReadGPSAltitudeException("Falha ao receber a altitude do módulo receptor GPS do robô.");
        }
		
        // Armazena o valor no array de resposta.
        sensorValue = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nLongitude do módulo receptor GPS: " + sensorValue);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValue;
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
	public int sendReadGPSSpeedCommand(String source) throws IOException, EmptyMessageException, ReadGPSSpeedException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValue;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSSpeedCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar velocidade do robô.
        byte[] pack = packageBuilder.createGPSSpeedPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x14))){
        	throw new ReadGPSSpeedException("Falha ao receber a velocidade do robô.");
        }
		
        // Armazena o valor no array de resposta.
        sensorValue = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nVelocidade do robô: " + sensorValue);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValue;
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
	public int sendReadGPSHeadCommand(String source) throws IOException, EmptyMessageException, ReadGPSHeadException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValue;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadGPSHeadCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a direção do movimento do robô.
        byte[] pack = packageBuilder.createGPSHeadPackage();

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x83) && (ans[11] == (byte)0x15))){
        	throw new ReadGPSHeadException("Falha ao receber a direção do movimento do robô.");
        }
		
        // Armazena o valor no array de resposta.
        sensorValue = PrimitiveDataTypesManipulation.twoBytesToInt(ans[12], ans[13]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nDireção do movimento do robô: " + sensorValue);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValue;
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
	public int sendReadBatteryLevelCommand(String source) throws IOException, EmptyMessageException, ReadBatteryLevelException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
        int sensorValues;
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadBatteryLevelCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria um primeiro pacote solicitando ao robô informação sobre o nível de sua bateria.
        byte[] pack = packageBuilder.createBatteryLevelPackage(PackageBuilder.BatteryLevelPackage.FIRST);

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x85) && (ans[11] == (byte)0x01) && (ans[12] == (byte)0x00) && (ans[13] == (byte)0xFF))){
        	throw new ReadBatteryLevelException("Falha ao receber informação do nível de bateria do robô.");
        }

		// Cria um segundo pacote solicitando ao robô informação sobre o nível de sua bateria.
        pack = packageBuilder.createBatteryLevelPackage(PackageBuilder.BatteryLevelPackage.SECOND);

        // Envia o pacote.
    	send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x85) && (ans[11] == (byte)0x02) && (ans[12] == (byte)0x00))){
        	throw new ReadBatteryLevelException("Falha ao receber informação do nível de bateria do robô.");
        }
		
        // Armazena os valores no array de resposta.
        sensorValues = PrimitiveDataTypesManipulation.twoBytesToInt(ans[13], ans[14]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nNível da bateria do robô: " + sensorValues);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
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
	public int[] sendReadMAPVersionCommand(String source) throws IOException, EmptyMessageException, ReadMAPVersionException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendReadMAPVersionCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô informar a versão do módulo MAP.
        byte[] pack = packageBuilder.createMAPVersionPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if (!((ans[10] == (byte)0x84) && (ans[11] == (byte)0x01))){
        	
        	if ((ans[10] == (byte)0x84) && (ans[11] == (byte)0x00)){
        		byte[] subArray = Arrays.copyOfRange(ans, 12, ans.length - 1);
        		String errorMsg = new String(subArray);
        		
        		throw new ReadMAPVersionException("Falha ao receber a versão do MAP. Erro: " + errorMsg);
        	} else {
        		throw new ReadMAPVersionException("Falha ao receber a versão do MAP.");
        	}
        	
        }
		
        int[] sensorValues = new int[3];
        sensorValues[0] = PrimitiveDataTypesManipulation.byteToInt(ans[12]);
        sensorValues[1] = PrimitiveDataTypesManipulation.byteToInt(ans[13]);
        sensorValues[2] = PrimitiveDataTypesManipulation.byteToInt(ans[14]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nVersão do Módulo de Alta Performance (MAP): " + sensorValues[0] + "." + sensorValues[1] + "." + sensorValues[2]);
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
		return sensorValues;
    }
	
	/**
	 * Inicia a captura continua das imagens da câmera.
	 * 
	 * Quando este comando é recebido, o aplicativo inicia um servidor de imagens que passa a ouvir em um canal Wifi. Em seguida,
	 * a resposta é enviada ao controlador passando o número do port no qual o servidor está esperando por conexões.
	 * 
	 * O aplicativo espera por uma conexão do controlador durante 4 segundos. Caso nenhuma conexão seja estabelecida, o servidor
	 * é baixado e uma mensagem de erro é enviada. Após a primeira conexão ser estabelecida outras conexões são rejeitadas,
	 * ou seja, o servidor de imagens é exclusivo do controlador que o solicitou.
	 * 
	 * As imagens são enviadas sequencialmente e ininterruptamente até que o comando "sendCameraStopCommand" seja recebido pelo aplicativo.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws CameraStartException
	 * @throws InterruptedException 
	 * @throws CameraImageFormatLenghtException 
	 */
	public InputStream sendCameraStartCommand(String source) throws IOException, EmptyMessageException, CameraStartException, InterruptedException, CameraImageFormatLenghtException{		
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendCameraStartCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô iniciar a captura de imagens da câmera.
        byte[] pack = packageBuilder.createCameraStartPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if ((ans[10] == (byte)0x84) && (ans[11] == (byte)0x04) && (ans[12] == (byte)0x00)){
        	//TODO: verificar como pegar a string com o erro.
        	String cameraError = Byte.toString(ans[13]); 
        	throw new CameraStartException("Falha ao iniciar captura de imagens da câmera. Erro: " + cameraError);
        } else {
        
	        if (!((ans[10] == (byte)0x84) && (ans[11] == (byte)0x04) && (ans[12] == (byte)0x01))){
	        	throw new CameraStartException("Falha ao iniciar captura de imagens da câmera.");
	        }
	        
        }
		
        // Obtém a porta na qual o servidor de imagens está aguardando conexões.
        int port = PrimitiveDataTypesManipulation.twoBytesToInt(ans[13], ans[14]);
        
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("\nPorta para conexão no servidor de  imagens da câmera: " + port);
			System.out.println(debugStr);
		}
		
		return cameraConnect(port);
    }

    /**
     * Conecta-se com o servidor de imagens da câmera do robô através de uma conexão wifi.
     * 
     * @throws IOException 
     * @throws UnknownHostException 
     */
	private InputStream cameraConnect(int connectionPort) throws IOException {
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\nRobotConnection.cameraConnect():");
	        debugStr.append("\nConectando ao servidor de imagens da câmera do robô no IP " + connectionHost + " e porta " + connectionPort + "...");
			System.out.println(debugStr);
		}
		
    	cameraSocketConnection = new Socket(connectionHost, connectionPort);
    	cameraSocketConnection.setTcpNoDelay(true);
    	cameraSocketConnection.setKeepAlive(true);
    	InputStream cameraInputStream = cameraSocketConnection.getInputStream();
        
    	if (cameraInputStream == null) {
        	cameraSocketConnection.close();
            throw new IOException("O stream é nulo!! A conexão foi com o servidor de imagens da câmera foi cancelada."); 
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("Conectado ao servidor de imagens da câmera.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}

		return cameraInputStream;
	}
	
	/**
	 * Finaliza a captura de imagens da câmera.
	 * 
	 * Interrompe a captura e o envio de imagens ao controlador. A resposta relativa a este comando é sempre enviada,
	 * independentemente do processo de captura e envio de imagens não estar sendo executado quando do recebimento
	 * deste comando.
	 * 
	 * @throws IOException
	 * @throws EmptyMessageException
	 * @throws CameraStopException
	 * @throws InterruptedException 
	 */
	public void sendCameraStopCommand(String source) throws IOException, EmptyMessageException, CameraStopException, InterruptedException{
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotConnection.sendCameraStopCommand(" + source + "):");
			System.out.println(debugStr);
		}

		// Cria o pacote dizendo para o robô iniciar a captura de imagens da câmera.
        byte[] pack = packageBuilder.createCameraStopPackage();
        send(pack);
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("\nEnviou:  " + packageBuilder.packageToString(pack));
			System.out.println(debugStr);
		}

        // Recebe a resposta do robo.
        byte[] ans = receive();
		
		if (debug){
			debugStr = new StringBuffer();
	        debugStr.append("Recebeu: " + packageBuilder.packageToString(ans));
			System.out.println(debugStr);
		}

		// Checa se o comando foi aceito.
        if ((ans[10] == (byte)0x84) && (ans[11] == (byte)0x04) && (ans[12] == (byte)0x00)){
        	//TODO: verificar como pegar a string com o erro.
        	String cameraError = Byte.toString(ans[13]); 
        	throw new CameraStopException("Falha ao encerrar captura de imagens da câmera. Erro: " + cameraError);
        } else {
        
	        if (!((ans[10] == (byte)0x84) && (ans[11] == (byte)0x04) && (ans[12] == (byte)0x02))){
	        	throw new CameraStopException("Falha ao encerrar captura de imagens da câmera.");
	        }
	        
        }
		
		cameraDisconnect();
    }

    /**
     * Desconecta-se com o servidor de imagens da câmera do robô através de uma conexão wifi.
     * 
     * @throws IOException 
     */
	private void cameraDisconnect() throws IOException {
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\nRobotConnection.cameraDisconnect():");
	        debugStr.append("\nDesconectando do servidor de imagens da câmera do robô...");
			System.out.println(debugStr);
		}
		
    	cameraSocketConnection.close();
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("Desconectado do servidor de imagens da câmera do robô.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
	}
    
}
