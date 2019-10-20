package logica;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class RecieveVideo extends Thread 
{
	
	private static final String NUEVO_VIDEO =  "VIDEO;";
	
	private static Server servidor;
	private Socket cliente;
	
	private BufferedReader lectorClienteTCP;
	
	private PrintWriter escritorClienteTCP;
	
	public RecieveVideo(Server s,Socket cliente, BufferedReader buf, PrintWriter pw) 
	{
		this.cliente = cliente;
		this.lectorClienteTCP = buf;
		this.escritorClienteTCP = pw;
		RecieveVideo.servidor = s;
	}
	
	@Override
	public void run() 
	{
		//ciclo infinito debido a que siempre puede subir videos
		while(true) 
		{
			try 
			{
				String nuevoVideo = lectorClienteTCP.readLine();
				if(nuevoVideo.contains(NUEVO_VIDEO))
				{
					String nombreNuevoVideo = nuevoVideo.split(";")[1];
					
					File newVideo = new File("./data/"+nombreNuevoVideo);
					FileOutputStream fos = new FileOutputStream(newVideo);
					
					
					int r ;
					byte[] buffer = new byte[8192];
					DataInputStream dis = new DataInputStream(cliente.getInputStream());

					while((r = dis.read(buffer)) != -1)
					{
						fos.write(buffer, 0, r);
					}
					fos.flush();
					fos.close();
					
					servidor.aniadirCanal(newVideo);
					
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
					//envio del nuevo canal
					escritorClienteTCP.write(mensajeTotal);	
				}
				else
				{
					escritorClienteTCP.write(Protocol.ERROR);
					cliente.close();
					System.out.println("El cliente no desarrollo el protocolo");
				}
			} catch (IOException e) 
			{
				try 
				{
					cliente.close();
					e.printStackTrace();
				} catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			
		}
		
	}

}
