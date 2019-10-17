package logica;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Channel {
	
	/**
	 * Archivo del video transmitido
	 */
	private File video;
	
	/**
	 * Socket del canal
	 */
	private DatagramSocket ss;
	
	/**
	 * Direccion del multicast
	 */
	private InetAddress multicastingGroup;
	
	/**
	 * Puerto donde funciona el canal
	 */
	private int puerto;
	
	/**
	 * Division en segmentos del archivo
	 */
	public static final Integer TAMANIO_SEGMENTO = 32768;
	
	
	public Channel(String host, int port, File arch) throws UnknownHostException 
	{
		multicastingGroup = InetAddress.getByName(host);
		puerto = port;
		video = arch;
	}
	

	public File getVideo() 
	{
		return video;
	}


	public void setVideo(File video) 
	{
		this.video = video;
	}


	public DatagramSocket getSs() 
	{
		return ss;
	}


	public void setSs(DatagramSocket ss) 
	{
		this.ss = ss;
	}


	public InetAddress getMulticastingGroup() 
	{
		return multicastingGroup;
	}


	public void setMulticastingGroup(InetAddress multicastingGroup) 
	{
		this.multicastingGroup = multicastingGroup;
	}


	public int getPuerto() 
	{
		return puerto;
	}


	public void setPuerto(int puerto) 
	{
		this.puerto = puerto;
	}
	
	public void enviar(DatagramPacket pp) throws IOException
	{
		ss.send(pp);
	}
	
	public void recibir(DatagramPacket pp) throws IOException
	{
		ss.receive(pp);
	}
	
	public void cerrar()
	{
		ss.close();
	}


	public void canal() throws IOException 
	{
			
		ss = new DatagramSocket(puerto);
		
		DatagramPacket dp;
				
		while(true)
		{
			File videoTemp = video;
			BufferedInputStream bif = new BufferedInputStream(new FileInputStream(videoTemp));
			int n;
			int sumaTam = 0;
			
			byte[] content = new byte[TAMANIO_SEGMENTO];
			
			while(sumaTam < video.length() &&  (n = bif.read(content)) != 1)
			{
				dp = new DatagramPacket(content, 0, content.length, multicastingGroup, puerto);
				enviar(dp);
				sumaTam += n;
			}
			bif.close();
			cerrar();
			
		}
		
	}
	

}
