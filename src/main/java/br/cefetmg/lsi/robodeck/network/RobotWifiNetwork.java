package br.cefetmg.lsi.robodeck.network;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Extende a classe {@link RobotConnection} para realizar comunicação através da
 * rede WiFi.
 * 
 * @see RobotConnection
 * @see RobotNetworkComm
 */
public class RobotWifiNetwork extends RobotConnection {

	/**
	 * Porta de conexão com o robô.
	 */
    private int connectionPort;
    
    /**
     * Host para conexão com o robô, via wifi.
     */
    private String connectionHost;
    
    /**
     * Soquete de conexão com o robô.
     */
    private Socket socketConnection;

    /**
     * Construtor padrão.
     * 
     * @param host Endereço do Robô a ser utilizado, não pode ser nula.
     * @param port Porta de Conexão a ser utilizada, deve ser um valor positivo.
     * 
     * @throws UnknownHostException 
     * @throws PortUnreachableException 
     */
    protected RobotWifiNetwork(String host, int port) throws UnknownHostException, PortUnreachableException {
        connectionHost = host;
        connectionPort = port;
    }

    /**
     * Conecta-se com o robô através de uma conexão wifi.
     * 
     * @throws IOException 
     * @throws UnknownHostException 
     */
	public void connect() throws IOException {
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotWifiNetwork.connect():");
	        debugStr.append("\nConectando ao robô...");
			System.out.println(debugStr);
		}
		
    	socketConnection = new Socket(connectionHost, connectionPort);
    	socketConnection.setTcpNoDelay(true);
    	socketConnection.setKeepAlive(true);
    	outputStream = socketConnection.getOutputStream();
		inputStream = socketConnection.getInputStream();
        
        if (inputStream == null || outputStream == null) {
        	socketConnection.close();
            throw new IOException("Um dos streams é nulo!! A conexão foi cancelada."); 
        }
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("Conectado.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}

	}

    /**
     * Desconecta-se com o robô através de uma conexão wifi.
     * 
     * @throws IOException 
     */
	public void disconnect() throws IOException {
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("RobotWifiNetwork.disconnect():");
	        debugStr.append("\nDesconectando do robo...");
			System.out.println(debugStr);
		}
		
    	socketConnection.close();
		
		if (debug){
			debugStr = new StringBuffer();
			debugStr.append("Desconectado.");
	        debugStr.append("\n===========");
			System.out.println(debugStr);
		}
		
	}

}
