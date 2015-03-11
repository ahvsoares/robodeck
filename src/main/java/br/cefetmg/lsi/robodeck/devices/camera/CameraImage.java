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
		// http://www.programcreek.com/2009/02/java-convert-image-to-byte-array-convert-byte-array-to-image/
		
		/*ByteArrayInputStream bis = new ByteArrayInputStream(this.data);
        Iterator<?> readers = ImageIO.getImageReadersByFormatName(this.format); 
 
        ImageReader reader = (ImageReader) readers.next();
        Object source = bis; 
        ImageInputStream iis = ImageIO.createImageInputStream(source); 
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();

        Image image = reader.read(0, param);*/
		
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(this.data));
        
		return image;
	}
}
