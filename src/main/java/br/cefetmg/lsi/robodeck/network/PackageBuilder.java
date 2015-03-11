package br.cefetmg.lsi.robodeck.network;

import br.cefetmg.lsi.robodeck.exceptions.EmptyMessageException;

public class PackageBuilder {
	/**
	 * Tamanho do cabeçalho.
	 */
	public static final int HEADER_LENGTH = 10;
	
	/**
	 * Indica que o pacote não se refere a outro.
	 */
	private final byte NO_REF = (byte)0;
	
	/**
	 * Informa que o pacote é um comando. O remetente está solicitando ao destinatário a execução de um comando. 
	 */
	private final byte ACT_CMD = (byte)0x01;
	
	/**
	 * Informa que o pacote é uma resposta parcial. O remetente está enviando ao destinatário uma parte da resposta.
	 * Ou seja, para o mesmo comando ainda seguirão outras respostas. Esta resposta refere-se ao comando enviado no
	 * pacote com o campo Pid igual ao campo Ref deste pacote.
	 */
//	private final byte ACT_PART_RESP = (byte)0x02;
	
	/**
	 * Informa que o pacote é uma resposta final. O remetente está enviando ao destinatário a última parte da resposta.
	 * Esta resposta refere-se ao comando enviado no pacote com o campo PID igual ao campo REF deste pacote. 
	 */
//	private final byte ACT_FINAL_RESP = (byte)0x03;
	
	/**
	 * Informa que o pacote é um comando com erro. O remetente está avisando que recebeu a mensagem enviada no pacote
	 * indicado por REF, mas que não conseguiu interpretá-la corretamente. A mensagem pode conter um texto
	 * (string ISO-8859-1) descrevendo o erro ou pode ser nula. Por exemplo: o comando não existe ou algum parâmetro
	 * continha um valor inválido.
	 */
//	private final byte ACT_CMD_WITH_ERROR = (byte)0x04;

	/**
	 * Para ler os sensores de infravermelho de distância.
	 */
	public static final byte INFRAREDS_DISTANCE = (byte)0x00;

	/**
	 * Para ler os sensores de infravermelho de profundidade.
	 */
	public static final byte INFRAREDS_DEPTH = (byte)0x01;

	/**
	 * Para ler os sensores dianteiros de ultrassom.
	 */
	public static final byte ULTRASONIC_FRONT = (byte)0x00;

	/**
	 * Para ler os sensores esquerdos de ultrassom.
	 */
	public static final byte ULTRASONIC_LEFT = (byte)0x02;

	/**
	 * Para ler os sensores direitos de ultrassom.
	 */
	public static final byte ULTRASONIC_RIGHT = 0x01;

	/**
	 * Para ler os sensores traseiros de ultrassom.
	 */
	public static final byte ULTRASONIC_REAR = (byte)0x03;
	
	/**
	 * Para ler o nível da bateria.
	 */
	public static enum BatteryLevelPackage {FIRST, SECOND};
	
    /**
     * Identificador do pacote.
     */
    private byte pid;
    
    /**
     * Identificador da sessão.
     */
    private int sid;
    
    /**
     * Identidade do usuário.
     */
    private int src;
    
    /**
     * Identidade do Robô.
     */
    private int dst;
	
	/**
	 * Instância da conexao com o robo.
	 */
	private static PackageBuilder instance;
	
	/**
	 * Cabeçalho do pacote.
	 */
	private PackageHeader header;
	
	/**
	 * Comando do pacote.
	 */
	private byte[] message;
	
	/**
	 * Construtor
	 *
	 * @param source Remetente do pacote.
	 * @param destination Destinatário do pacote.
	 */
	private PackageBuilder(int source, int destination){
		header = null;
		message = null;
	    pid = (byte)1;
	    sid = 0;
	    src = source;
	    dst = destination;
	}
	
	/**
	 * Retorna uma instância de PackageBuilder.
	 * 
	 * @param source Remetente do pacote.
	 * @param destination Destinatário do pacote.
	 * 
	 * @return uma instância de PackageBuilder.
	 */
	public static PackageBuilder getInstance(int source, int destination){
		
		if (instance == null){
			return new PackageBuilder(source, destination);
		} else {
			return instance;
		}
		
	}
	
	/**
	 * Cria um pacote que será enviado ao robô.
	 * 
	 * @param cmd Array de bytes referente ao comando que será enviado.
	 * @param ref Remetente.
	 * @param act Destinatário.
	 * 
	 * @return pacote que será enviado ao robô.
	 * 
	 * @throws EmptyMessageException 
	 */
	@SuppressWarnings("null")
	private byte[] createPackage(byte[] cmd, byte ref, byte act) throws EmptyMessageException{
    	
    	if ((cmd != null) || (cmd.length > 1)){
    		createPackageHeader(ref, act, (byte)cmd.length);
    		message = cmd;
        	
    		return mountPackage();
    	} else {
    		throw new EmptyMessageException();
    	}
    	
	}
	
	/**
	 * Cria o cabeçalho do pacote.
	 * 
	 * @param ref Referência a outro pacote.
	 * @param act Ação à qual o pacote se refere a saber:
	 * 			- ACT_CMD: comando;
	 * 			- ACT_PART_RESP: resposta parcial;
	 * 			- ACT_FINAL_RESP: resposta final;
	 * 			- ACT_CMD_WITH_ERROR: comando com erro.
	 * @param len Tamanho da mensagem, em bytes.
	 */
    private void createPackageHeader(byte ref, byte act, byte len) {    	
		header = new PackageHeader(pid, ref, src, dst, sid, act, len);
    }
    
    private byte[] mountPackage(){
    	int packCounter = 0;
    	byte[] pack = new byte[header.getLength() + message.length];
    	
    	for (int i = 0; i < header.getLength(); i++){
    		pack[i] = header.get(i);
    		packCounter++;
    	}
    	
    	for (int i = 0; i < message.length; i++){
    		pack[packCounter] = message[i];
    		packCounter++;
    	}
    	
    	return pack;
    }

    /**
     * Incrementa o identificador do pacote.
     */
	public void incrementPid(){
		pid++;
	}

    /**
     * Ajusta o identificador da sessão.
     * 
     * @param sessionId Identificador da sessão.
     */
	public void setSid(int sessionId){
		sid = sessionId;
	}

    /**
     * Retorna o identificador da sessão.
     * 
     * @return O identificador da sessão.
     */
	public int getSid(){
		return sid;
	}
	
	/**
	 * Retorna uma string que representa o conteúdo do pacote, em valores hexadecimais.
	 * 
	 * @param pack pacote que será convertido para string.
	 * 
	 * @return string que representando o conteúdo do pacote, em valores hexadecimais.
	 */
	public String packageToString(byte[] pack){
		StringBuffer returnStr = new StringBuffer();
		
		if (pack != null){
			
			for (int i = 0; i < pack.length; i++) {
								
				if (i != (pack.length - 1)) {

					if ((i == 2) | (i == 4)| (i == 6)){
						returnStr.append(String.format("%02x", pack[i]) + String.format("%02x", pack[i+1]) + " ");
						i++;
					} else {
						
						if (i == 9){
							returnStr.append(String.format("%02x", pack[i]) + " | ");
						} else {
							returnStr.append(String.format("%02x", pack[i]) + " ");
						}
						
					}
					
				} else {
					returnStr.append(String.format("%02x", pack[i]));
				}

			}
 
		}
		
		return returnStr.toString();		
	}
	
	/**
	 * Cria um pacote de abertura de sessão.
	 * 
	 * @return pacote de abertura de sessão.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createOpenSessionPackage() throws EmptyMessageException {
        byte[] cmd = new byte[2];

        // Monta os bytes do comando de abertura de sessao.
        cmd[0] = 0x01;
        cmd[1] = 0x01;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}
	
	/**
	 * Cria um pacote de fechamento de sessão.
	 * 
	 * @return pacote de fechamento de sessão.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCloseSessionPackage() throws EmptyMessageException {
        byte[] cmd = new byte[2];

        // Monta os bytes do comando de fechamento de sessao.
        cmd[0] = 0x01;
        cmd[1] = 0x02;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}
	
	/**
	 * Cria um pacote de solicitação da versão do protocolo de comunicação.
	 * 
	 * @return pacote de solicitação da versão do protocolo de comunicação.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCommunicationProtocolVersionPackage() throws EmptyMessageException {
        byte[] cmd = new byte[2];

        // Monta os bytes do comando de fechamento de sessao.
        cmd[0] = 0x00;
        cmd[1] = 0x00;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que diz para o robô se mover.
	 * 
	 * @param intensity Nova intensidade de locomoção.
	 * 
	 * @return pacote que diz para o robô se mover.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createMovePackage(int intensity) throws EmptyMessageException {	
		byte[] cmd = new byte[4];

        // Monta os bytes do comando de movimento.
		cmd[0] = 0x03;
		cmd[1] = 0x01;
        cmd[2] = (byte)(intensity >> 8);
        cmd[3] = (byte)intensity;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que diz para o robô se virar.
	 * 
     * @param ângulo da curva a ser realizada, em graus. Valor entre -32 e +32.
     * @param intensity Nova intensidade de locomoção.
	 * 
	 * @return pacote que diz para o robô se virar.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createTurnPackage(byte angle, int intensity) throws EmptyMessageException {	
		byte[] cmd = new byte[5];

        // Monta os bytes do comando de virar.
		cmd[0] = 0x03;
		cmd[1] = 0x02;
		cmd[2] = angle;
        cmd[3] = (byte)(intensity >> 8);
        cmd[4] = (byte)intensity;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

    /**
     * Cria um pacote que diz para o robô se virar andar se mantendo o alinhamento de sua carroceria com a mesma
     * direção na qual se encontrava antes de iniciar o movimento.
     * 
     * @param ângulo final das rodas em relação à frente do robô, em graus. Valor entre -32 e +32.
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws EmptyMessageException
     */
	public byte[] createStrafePackage(byte angle, int intensity) throws EmptyMessageException {	
		byte[] cmd = new byte[5];

        // Monta os bytes do comando de movimento alinhado com a carroceria.
		cmd[0] = 0x03;
		cmd[1] = 0x03;
		cmd[2] = angle;
        cmd[3] = (byte)(intensity >> 8);
        cmd[4] = (byte)intensity;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

    /**
     * Cria um pacote que diz para o robô girar em torno do próprio eixo.
     * 
     * @param side sentido do giro. Anti-horário Robot.SPIN_COUNTERCLOCKWISE ou horário Robot.SPIN_CLOCKWISE.
     * @param intensity Nova intensidade de locomoção. Se o valor for negativo, o robô anda para trás.
     * 
     * @throws EmptyMessageException
     */
	public byte[] createSpinPackage(byte side, int intensity) throws EmptyMessageException {
		byte[] cmd = new byte[5];

        // Monta os bytes do comando de giro.
		cmd[0] = 0x03;
		cmd[1] = 0x04;
		cmd[2] = side;
        cmd[3] = (byte)(intensity >> 8);
        cmd[4] = (byte)intensity;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seus sensores de infravermelho.
	 * 
	 * @param sensorPair PackageBuilder.INFRAREDS_DISTANCE para os sensores de distância
	 * 			ou PackageBuilder.INFRAREDS_DEPTH para os sensores de profundidade.
	 * 
	 * @return pacote que pede para o robô informar os valores de seus sensores de infravermelho.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createInfraredSensorsPackage(byte sensorPair) throws EmptyMessageException {
		byte[] cmd = new byte[3];

        // Monta os bytes para leitura dos sensores de infravermelho.
		cmd[0] = 0x03;
		cmd[1] = 0x05;
		cmd[2] = sensorPair;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seus sensores de ultrassom de distância.
	 * 
	 * @param sensorPair PackageBuilder.ULTRASONIC_FRONT para ler os sensores frontais de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_LEFT para ler os sensores esquerdos de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_RIGHT para ler os sensores direitos de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_REAR para ler os sensores traseiros de ultrassom.
	 * 
	 * @return pacote que pede para o robô informar os valores de seus sensores de ultrassomde distância.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createUltrassonicSensorsDistancePackage(byte sensorPair) throws EmptyMessageException {
		byte[] cmd = new byte[3];

        // Monta os bytes para leitura dos sensores de utlrassom.
		cmd[0] = 0x03;
		cmd[1] = 0x06;
		cmd[2] = sensorPair;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seus sensores de ultrassom de luminosidade.
	 * 
	 * @param sensorPair PackageBuilder.ULTRASONIC_FRONT para ler os sensores frontais de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_LEFT para ler os sensores esquerdos de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_RIGHT para ler os sensores direitos de ultrassom,
	 * 					 PackageBuilder.ULTRASONIC_REAR para ler os sensores traseiros de ultrassom.
	 * 
	 * @return pacote que pede para o robô informar os valores de seus sensores de ultrassom de luminosidade.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createUltrassonicSensorsLuminosityPackage(byte sensorPair) throws EmptyMessageException {
		byte[] cmd = new byte[3];

        // Monta os bytes para leitura dos sensores de utlrassom.
		cmd[0] = 0x03;
		cmd[1] = 0x07;
		cmd[2] = sensorPair;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de sua bússola.
	 * 
	 * @return pacote que pede para o robô informar os valores de sua bússola.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCompassSensorPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da bússola.
		cmd[0] = 0x03;
		cmd[1] = 0x08;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seu acelerômetro.
	 * 
	 * @return pacote que pede para o robô informar os valores de seu acelerômetro.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createAccelerometerSensorPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura do acelerômetro.
		cmd[0] = 0x03;
		cmd[1] = 0x09;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seus sensores de temperatura e umidade.
	 * 
	 * @return pacote que pede para o robô informar os valores de seus sensores de temperatura e umidade.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createTemperatureAndHumiditySensorsPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura dos sensores de temperatura e umidade.
		cmd[0] = 0x03;
		cmd[1] = 0x0A;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar os valores de seus sensores ópticos.
	 * 
	 * @return pacote que pede para o robô informar os valores de seus sensores ópticos.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCollisionDetectionPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura dos sensores ópticos.
		cmd[0] = 0x03;
		cmd[1] = 0x0B;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar as versões do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar as versões do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSInfoPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura das versões do módulo receptor GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x0C;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar todos os dados do GPS.
	 * 
	 * @return pacote que pede para o robô informar todos os dados do GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSGetPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para informação sobre a validade da string do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x16;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar se a string do GPS é válida.
	 * 
	 * @return pacote que pede para o robô informar se a string do GPS é válida.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSValidatePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para informação sobre a validade da string do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x0D;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar o número de satélites utilizados pelo GPS.
	 * 
	 * @return pacote que pede para o robô informar o número de satélites utilizados pelo GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSSatellitePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura do número de satélites utilizados pelo GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x0E;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar o horário do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar o horário do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSTimePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura do horário do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x0F;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a data do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar a data do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSDatePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da data do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x10;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a latitude do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar a latitude do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSLatitudePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da latitude do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x11;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a longitude do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar a longitude do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSLongitudePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da longitude do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x12;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a altitude do módulo receptor GPS.
	 * 
	 * @return pacote que pede para o robô informar a altitude do módulo receptor GPS.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSAltitudePackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da altitude do GPS.
		cmd[0] = 0x03;
		cmd[1] = 0x13;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a velocidade do robô.
	 * 
	 * @return pacote que pede para o robô informar a velocidade do robô.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSSpeedPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da velocidade do robô.
		cmd[0] = 0x03;
		cmd[1] = 0x14;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a direção do movimento do robô.
	 * 
	 * @return pacote que pede para o robô informar a direção do movimento do robô.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createGPSHeadPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da direção do movimento do robô.
		cmd[0] = 0x03;
		cmd[1] = 0x15;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar o nível da bateria do robô.
	 * 
	 * @return pacote que pede para o robô informar o nível da bateria do robô.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createBatteryLevelPackage(BatteryLevelPackage packOrder) throws EmptyMessageException {
		/*byte[] cmd = new byte[2];

        // Monta os bytes para leitura do nível da bateria do robô.
		cmd[0] = 0x03;
		cmd[1] = 0x17;*/
		
		byte[] cmd;
		switch (packOrder) {
		case FIRST:
			cmd = new byte[4];
	
	        // Monta os bytes para leitura do nível da bateria do robô.
			cmd[0] = 0x05;
			cmd[1] = 0x01;
			cmd[2] = 0x00;
			cmd[3] = 0x09;
			
			break;
			
		case SECOND:
			cmd = new byte[4];
	
	        // Monta os bytes para leitura do nível da bateria do robô.
			cmd[0] = 0x05;
			cmd[1] = 0x02;
			cmd[2] = 0x00;
			cmd[3] = 0x00;
			
			break;

		default:
			return null;
		}
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô informar a versão do Módulo de Alta Performance (MAP).
	 * 
	 * @return pacote que pede para o robô informar a versão do MAP.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createMAPVersionPackage() throws EmptyMessageException {
		byte[] cmd = new byte[2];

        // Monta os bytes para leitura da versão do MAP.
		cmd[0] = 0x04;
		cmd[1] = 0x01;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô iniciar a captura de imagens da câmera.
	 * 
	 * @return pacote que pede para o robô iniciar a captura de imagens da câmera.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCameraStartPackage() throws EmptyMessageException {
		byte[] cmd = new byte[3];

        // Monta os bytes para iniciar a captura de imagens da câmera.
		cmd[0] = 0x04;
		cmd[1] = 0x04;
		cmd[2] = 0x01;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}

	/**
	 * Cria um pacote que pede para o robô encerrar a captura de imagens da câmera.
	 * 
	 * @return pacote que pede para o robô encerrar a captura de imagens da câmera.
	 * 
	 * @throws EmptyMessageException
	 */
	public byte[] createCameraStopPackage() throws EmptyMessageException {
		byte[] cmd = new byte[3];

        // Monta os bytes para iniciar a captura de imagens da câmera.
		cmd[0] = 0x04;
		cmd[1] = 0x04;
		cmd[2] = 0x02;
        
		return createPackage(cmd, NO_REF, ACT_CMD);
	}
	
}
