package br.cefetmg.lsi.robodeck.devices.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import br.cefetmg.lsi.robodeck.exceptions.CameraException;
import br.cefetmg.lsi.robodeck.exceptions.CameraImageFormatLenghtException;
import br.cefetmg.lsi.robodeck.utils.PrimitiveDataTypesManipulation;
import br.cefetmg.lsi.robodeck.utils.PropertiesLoaderImpl;

public class Camera implements Runnable {
	protected final boolean debug = new Boolean(PropertiesLoaderImpl.getValor("robodeckapi.debugmode")); // Para ativar/desativar saidas "System.out.println();" de depuracao.
	
	/**
	 * Para saber se deve ou não continuar a capturar imagens da câmera.
	 */
	private boolean capture;
	
	/**
	 * Para saber se pode interromper a captura de imagens.
	 */
	private boolean canStopCapture;

	/**
	 * Instância de Camera.
	 */
	private static Camera instance;
	
    /**
     * Fluxo de entrada de dados da comunicação do servidor de imagens da câmera.
     */
	private InputStream inputStream;
	
	/**
	 * Número de tentativas para ler os dados da imagem da câmera.
	 */
	private final int NBR_OF_TRIES = 15;
	/**
	 * Quanto tempo dorme antes de iniciar nova tentativa de obter os dados da imagem da câmera.
	 */
	private final int SLEEP_TIME = 1000;

	/**
	 * Cria uma instância de Camera, ajustando seu fluxo de dados.
	 */
	private Camera(){
		capture = false;
		canStopCapture = true;
		inputStream = null;
	}

	/**
	 * Retorna uma instância de Camera, com seu fluxo de dados.
	 * 
	 * @return uma instância de Camera, com seu fluxo de dados.
	 */
	public static Camera getInstance(){

		if (instance == null){
			instance = new Camera();
			
			return instance;
		} else {
			return instance;
		}
		
	}
	
	/**
	 * Recebe, continuamente, o fluxo de dados vindo da câmera.
	 */
	public void run() {
		int i = 0;
		
		while (capture){
			CameraImage cameraImage = null;
			canStopCapture = false; // Não se pode parar uma captura tendo ela já iniciado.
			
			try {
				cameraImage = acquireImage(i);
			} catch (IOException e) {
				
				if (capture){
					System.err.println("!!!!!!!!!!!!!");
					System.err.println("Falha ao ler fluxo de dados da câmera. Erro: " + e.getMessage());
					System.err.println("!!!!!!!!!!!!!");
					canStopCapture = true;
					capture = false;
				}
				
			} catch (CameraImageFormatLenghtException e) {
				
				if (capture){
					System.err.println("!!!!!!!!!!!!!");
					System.err.println("Falha ao ler formato de imagem recebida pela câmera. Erro: " + e.getMessage());
					System.err.println("!!!!!!!!!!!!!");
					canStopCapture = true;
					capture = false;
				}
				
			} catch (CameraException e) {
				
				if (capture){
					System.err.println("!!!!!!!!!!!!!");
					System.err.println("Falha ao ler imagem recebida pela câmera. Erro: " + e.getMessage());
					System.err.println("!!!!!!!!!!!!!");
					canStopCapture = true;
					capture = false;
				}
				
			} catch (InterruptedException e){
				
				if (capture){
					System.err.println("!!!!!!!!!!!!!");
					System.err.println("Falha ao ler imagem recebida pela câmera. Erro: " + e.getMessage());
					System.err.println("!!!!!!!!!!!!!");
					canStopCapture = true;
					capture = false;
				}
				
			}
			
			try {
				
				if (cameraImage != null){
					
					if (debug) {
						ImageIO.write(cameraImage.getImage(), cameraImage.getFormat(),
								new File(PropertiesLoaderImpl.getValor("robot.camera.imageDestinationFolder")
										+ "/imgRecebidas", "recebido" + i + "." + cameraImage.getFormat()));
					}
					
				}
				
			} catch (IOException e) {
				System.err.println("!!!!!!!!!!!!!");
				System.err.println("Falha ao gravar imagem recebida pela câmera. Erro:" + e.getMessage());
				System.err.println("!!!!!!!!!!!!!");
				canStopCapture = true;
				capture = false;
			}
			
			i++;
			canStopCapture = true; // Quando uma captura já foi feita, pode-se cancelar as demais.
		}
		
        StringBuffer debugStr = new StringBuffer();
		
		if (debug){
	        debugStr.append("\n===========\n");
	        debugStr.append("Camera.run():");
	        debugStr.append("\nCaptura de imagens da câmera finalizada.");
	        debugStr.append("\n===========\n");
			System.out.println(debugStr);
		}
		
	}
	
	/**
	 * Lê uma imagem enviada pela câmera.
	 * 
	 * @return CameraImage da imagem enviada pela câmera.
	 * 
	 * @throws IOException
	 * @throws CameraImageFormatLenghtException
	 * @throws CameraException 
	 * @throws InterruptedException 
	 */
	private CameraImage acquireImage(int imgNbr) throws IOException, CameraImageFormatLenghtException, CameraException, InterruptedException{
		
		if (inputStream != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final int imgHeaderLength = 12;
			CameraImage cameraImage = new CameraImage();
			
			byte[] header = new byte[imgHeaderLength];
	        
	        // Lê o cabeçalho da imagem.
			inputStream.read(header, 0 , imgHeaderLength);
	        baos.write(header);
			
	        StringBuffer debugStr = new StringBuffer();
	        
			if (debug){
		        debugStr.append("\n===========\n");
		        debugStr.append("Camera.acquireImage(" + imgNbr + "):");
		        debugStr.append("\nCabeçalho da imagem: " + dataToString(header));
				System.out.println(debugStr);
			}
	
			// Ajusta os atributos da imagem.
	        cameraImage.setWidth(PrimitiveDataTypesManipulation.twoBytesToInt(header[0], header[1]));
	        cameraImage.setHeight(PrimitiveDataTypesManipulation.twoBytesToInt(header[2], header[3]));
	        cameraImage.setSize(calculateImageSize(new byte[]{header[4], header[5], header[6], header[7]}));
	        cameraImage.setFormat(new byte[]{header[8], header[9], header[10], header[11]});
	
	        // Lê os dados da imagem. Tenta várias leituras, até atingir o número de bytes esperado ou o número máximo de tentativas.
	        byte[] imgData = new byte[cameraImage.getSize()];	        
	        int bytesRead = 0;
	        
			for (int i = 0; i < NBR_OF_TRIES; i ++) {
		        Thread.sleep(SLEEP_TIME);
				bytesRead += inputStream.read(imgData, bytesRead, cameraImage.getSize() - bytesRead);
		        
				if (debug){
					debugStr = new StringBuffer();
			        debugStr.append("\nLeu " + bytesRead + " na tentiva " + i + ". Esperado: " + cameraImage.getSize() + ".");
					System.out.println(debugStr);
				}
				
				if (bytesRead >= cameraImage.getSize()) {
					break;
				}
				
			}
	        
			if (debug){
				debugStr = new StringBuffer();
		        debugStr.append("\nLeu, no total, " + bytesRead + ". Esperado: " + cameraImage.getSize() + ".");
				System.out.println(debugStr);
		        debugStr.append("\n===========\n");
			}
			
			if (bytesRead != cameraImage.getSize()){
				throw new CameraException("Leu menos bytes da câmera que deveria. Esperado: " + cameraImage.getSize() + ". Lido: " + bytesRead + ".");
			}
	        
			// Ajusta os dados da imagem.
	        baos.write(imgData);        
	        cameraImage.setData(imgData);
	
	        baos.flush();
	        baos.close();
	        
	        return cameraImage;
		} else {
			throw new CameraException("Fluxo de dados da câmera é \"null\"");
		}
		
	}
	
	/**
	 * Ajusta os atributos para o robô começar a capturar imagens da câmera.
	 * 
	 * @param cameraInputStream Fluxo de dados da câmera.
	 */
	public void setStartCaptureAttributes(InputStream cameraInputStream) {
		capture = true;
		inputStream = cameraInputStream;
	}
	
	/**
	 * Ajusta os atributos para o robô parar de capturar imagens da câmera.
	 */
	public void setStopCaptureAttributes() {
		capture = false;
	}
	
	/**
	 * Calcula o tamanho da imagem.
	 * 
	 * @param sizeArray Array com os bytes referentes ao tamanho da imagem.
	 * 
	 * @return o tamanho da imagem, em número de bytes.
	 * 
	 * @throws CameraException
	 */
	private int calculateImageSize(byte[] sizeArray) throws CameraException{
		
		if (sizeArray.length == 4){
			int int1 = (int)((sizeArray[0] << 24) & 0xFFFFFFFF);
			int int2 = (int)((sizeArray[1] << 16) & 0x00FFFFFF);
			int int3 = (int)((sizeArray[2] << 8) & 0x0000FFFF);
			int int4 = (int)((sizeArray[3]) & 0x000000FF);
			
			return int1 + int2 + int3 + int4;
		} else {
			throw new CameraException("O tamanho do array de bytes para cálculo do tamanho da imagem deve ser 4.");
		}
	}
	
	/**
	 * Retorna uma string que representa o conteúdo do dos dados recevidos da câmera.
	 * 
	 * @param data array com os dados que serão convertido para string.
	 * 
	 * @return string que representando o conteúdo dos dados, em valores hexadecimais.
	 */
	private String dataToString(byte[] data){
		StringBuffer returnStr = new StringBuffer();
		
		if (data != null){
			
			for (int i = 0; i < data.length; i++) {
								
				if (i != (data.length - 1)) {
					returnStr.append(String.format("%02x", data[i]) + " ");
					
				} else {
					returnStr.append(String.format("%02x", data[i]));
				}

			}
 
		}
		
		return returnStr.toString();		
	}
	
	/**
	 * Retorna se uma captura de imagens pode ou não ser parada.
	 * 
	 * @return true se puder a captura de imagens puder ser parada e false caso contrário.
	 */
	public boolean canStopCapture(){
		return canStopCapture;
	}
	
}
