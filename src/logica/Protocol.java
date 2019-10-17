package logica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Protocol implements Runnable 
{
	
	public static final String PREPARADO = "PREPARADO";
	
	private static Server servidor;
	
	private DatagramPacket dp;

	
	public void procesar() throws IOException 
	{
		ArrayList<Channel> canales = servidor.obtenerCanales();
		
		String mensajeTotal = "";
		//envio al cliente de los canales prestados por el servidor
		for (Channel channel : canales) 
		{
			String hostName = channel.getMulticastingGroup().getHostName();
			String videoName = channel.getVideo().getName();
			int puerto =  Server.PORT;
			//le decimos en que host esta y el video que se muestra alli(separado por comas)
			System.out.println("Canal " + hostName + " " + " Video : " + videoName + " Puerto "  + puerto );
			//formato : 224.X.0.0:7070/mivideo.mp4
			String message = hostName + ":" +puerto +"/"+ videoName + ";";
			
			mensajeTotal += message;
			
		}
		byte[] bytemessage = mensajeTotal.getBytes();
		dp = new DatagramPacket(bytemessage, bytemessage.length);
		servidor.enviar(dp);
	}
	@Override
	public void run() 
	{
		try 
		{
			procesar();
			
		} catch (IOException e) 
		{
			System.out.println("Ocurrio un error " + e.getMessage());
		}		
	}


}
