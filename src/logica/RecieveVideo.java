package logica;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class RecieveVideo extends Thread 
{
	
	private static final String NUEVO_VIDEO =  "VIDEO;";
	
	private static Server servidor;
	private Socket cliente;
	
	private BufferedReader lectorClienteTCP;
	
	private PrintWriter escritorClienteTCP;
	
	public RecieveVideo(Socket cliente, BufferedReader buf, PrintWriter pw) 
	{
		this.cliente = cliente;
		this.lectorClienteTCP = buf;
		this.escritorClienteTCP = pw;
	}
	
	@Override
	public void run() 
	{
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
					
					Channel canal = servidor.aniadirCanal(newVideo);
					
					//envio del nuevo canal
					escritorClienteTCP.write(canal.getMulticastingGroup().getHostName()+":"+ Server.PORT + "/" + nombreNuevoVideo);
					
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
