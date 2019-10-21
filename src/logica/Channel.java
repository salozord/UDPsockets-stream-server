package logica;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class Channel extends Thread{

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

	private static String formatRtpStream(String serverAddress, int serverPort) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#rtp{dst=");
        sb.append(serverAddress);
        sb.append(",port=");
        sb.append(serverPort);
        sb.append(",mux=ts}");
        return sb.toString();
    }
	
	@Override
	public void run() 
	{

		String mrl = video.getAbsolutePath();
		String opciones = formatRtpStream(multicastingGroup.getHostAddress(), puerto);
		
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(new String[] {video.getAbsolutePath()});
		MediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();
		
		boolean ans = mediaPlayer.media().play(mrl, opciones, ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");
		System.out.println(ans);
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
