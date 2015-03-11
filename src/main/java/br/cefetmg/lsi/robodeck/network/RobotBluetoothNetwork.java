package br.cefetmg.lsi.robodeck.network;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Extende a classe {@link RobotNetwork} para realizar comunicação através da
 * rede Bluetooth.
 * 
 * @see RobotNetwork
 * @see RobotNetworkComm
 */
public class RobotBluetoothNetwork extends RobotConnection {

    /**
     * Construtor padrão.
     * 
     * @param uuid UUID do servidor do Robô
     */
	protected RobotBluetoothNetwork(String uuid) {
		// TODO Auto-generated method stub
    }

    /**
     * Conecta-se com o robô através de uma conexão bluetooth.
     * 
     * @throws IOException 
     * @throws UnknownHostException 
     */
	public void connect() throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
	}

    /**
     * Desconecta-se com o robô através de uma conexão bluetooth.
     * 
     * @throws IOException 
     */
	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
	}
    
} 