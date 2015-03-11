package br.cefetmg.lsi.robodeck.network;

public class PackageHeader {
	private final int PID = 0;
	private final int REF = 1;
	private final int SRC1 = 2;
	private final int SRC2 = 3;
	private final int DST1 = 4;
	private final int DST2 = 5;
	private final int SID1 = 6;
	private final int SID2 = 7;
	private final int ACT = 8;
	private final int LEN = 9;
	
	private byte[] header;
	
	/*public PackageHeader(){
		header[PID] = (byte)1;
		header[REF] = (byte)0;
		header[SRC1] = (byte)0;
		header[SRC2] = (byte)2;
		header[DST1] = (byte)0;
		header[DST2] = (byte)1;
		header[SID1] = (byte)0;
		header[SID2] = (byte)0;
		header[ACT] = (byte)1;
		header[LEN] = (byte)2;
	}*/
	
	public PackageHeader(byte pid, byte ref, int src, int dst, int sid, byte act, byte len){
		header = new byte[10];
		
		header[PID] = pid;
		header[REF] = ref;
		header[SRC1] = (byte)((src & 0xF0) >> 4);
		header[SRC2] = (byte)(src & 0x0F);
		header[DST1] = (byte)((dst & 0xF0) >> 4);
		header[DST2] = (byte)(dst & 0x0F);
		header[SID1] = (byte)((sid & 0xF0) >> 4);
		header[SID2] = (byte)(sid & 0x0F);
		header[ACT] = act;
		header[LEN] = len;
	}
	
	/**
	 * Retorna o tamanho do cabeçalho.
	 * 
	 * @return tamanho do cabeçalho.
	 */
	public int getLength(){
		return header.length;
	}
	
	/**
	 * Retorna o i-ésimo elemento do cabeçalho.
	 * 
	 * @param i i-ésimo elemento do cabeçalho.
	 * 
	 * @return i-ésimo elemento do cabeçalho.
	 */
	public byte get(int i){
		return header[i];
	}
	
	/*public void setPid(byte value){
		header[PID] = value; 
	}
	
	public byte getPid(){
		return header[PID]; 
	}
	
	public void setRef(byte value){
		header[REF] = value; 
	}
	
	public byte getRef(){
		return header[REF]; 
	}
	
	public void setSrc(int value){
		header[SRC1] = (byte)((value & 0xF0) >> 4);
		header[SRC2] = (byte)(value & 0x0F);
	}
	
	public int getSrc(){
		return ((header[SRC1] << 4) | (header[SRC2] & 0x0F));
	}
	
	public void setDst(int value){
		header[DST1] = (byte)((value & 0xF0) >> 4);
		header[DST2] = (byte)(value & 0x0F);
	}
	
	public int getDst(){
		return ((header[DST1] << 4) | (header[DST2] & 0x0F));
	}
	
	public void setSid(int value){
		header[SID1] = (byte)((value & 0xF0) >> 4);
		header[SID2] = (byte)(value & 0x0F);
	}
	
	public int getSid(){
		return ((header[SID1] << 4) | (header[SID2] & 0x0F));
	}
	
	public void setAct(byte value){
		header[ACT] = value; 
	}
	
	public byte getAct(){
		return header[ACT]; 
	}
	
	public void setLen(byte value){
		header[LEN] = value; 
	}
	
	public byte getLen(){
		return header[LEN]; 
	}
	
	public byte[] getHeader(){
		return header;
	}*/
	
}
