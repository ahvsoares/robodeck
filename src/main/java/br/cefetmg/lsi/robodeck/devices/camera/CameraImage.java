package br.cefetmg.lsi.robodeck.devices.camera;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import br.cefetmg.lsi.robodeck.exceptions.CameraImageFormatLenghtException;

public class CameraImage {
	/**
	 * Largura da imagem.
	 */
	private int width;
	
	/**
	 * Altura da imagem.
	 */
	private int height;
	
	/**
	 * Tamanho da imagem, em número de bytes.
	 */
	private int size;

	/**
	 * Formato da imagem.
	 */
	private String format;

	/**
	 * Conteúdo da imagem.
	 */
	private byte[] data;
	
	/**
	 * Nome da imagem, gerado automaticamente e sequencialmente.
	 */
	private String name;
	
	public CameraImage(String imgName){
		name = imgName;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(byte[] format) throws CameraImageFormatLenghtException, UnsupportedEncodingException {
		
		if (format.length != 4){
			throw new CameraImageFormatLenghtException("O formato deve possuir 4 bytes de tamanho");
		}
		
		this.format = new String(format, "US-ASCII");
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public BufferedImage getImage() throws IOException{		
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(this.data));
        
		return image;
	}
}
