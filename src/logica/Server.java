package logica;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class Server 
{
	public static final int PORT = 8080;
	
	private DatagramSocket ss;
	private  String actualHost;
	private ArrayList<Channel> canales;
	private static final int PUERTO_SERVIDOR = 8787;
	private static final int MAX_CHANNELS = 3825;
	private static final int PUERTO = 7777;
	
	public Server() 
	{
		actualHost = "224.0.0.0";
	}

	public void asignacionDeCanales() throws IOException
	{
			File directorio = new File("./data");
			File [] archivosMultimedia = directorio.listFiles();
			
 			for (int i = 0; i < archivosMultimedia.length && canales.size() < MAX_CHANNELS ; i++) 
 			{
 				String[] splitted = actualHost.split(".");
  				String actualCh = splitted[1];
 				String actualNet =  splitted[0];
 				
 				if(Integer.parseInt(actualCh) == 255)
 				{
 					actualNet = (Integer.parseInt(actualNet) + 1 ) + "";
 					actualCh = "1";
 				}
 				else 
 				{
 					actualCh = (Integer.parseInt(actualCh) + 1) + "";
 				}
 				String nextHost = actualNet + "." + actualCh + ".0.0";
 				
 				//
 				actualHost = nextHost;
 			
 				Channel canalNuevo = new Channel(nextHost, PUERTO, archivosMultimedia[i]);
 				canalNuevo.canal();
 				canales.add(canalNuevo);
				
			}
		
	}

	public void servidor() throws Exception
	{

		System.out.println("Empezando servidor maestro en puerto " + PUERTO_SERVIDOR);

		// Crea el socket que escucha en el puerto seleccionado.
		ss = new DatagramSocket(PUERTO_SERVIDOR);
		System.out.println("Socket creado.");
		
		System.out.println("Esperando solicitudes.");
		
		//creamos los canales
		asignacionDeCanales();

		while (true) 
		{
			try 
			{ 
	
					// Recibe el paquete de listo del cliente
					byte[] buf = new byte[9];
					DatagramPacket p = new DatagramPacket(buf, buf.length);
					ss.receive(p);
					String recibida = new String(p.getData(), 0, p.getLength());

						if(recibida.equals(Protocol.PREPARADO)) 
						{
							//para cada cliente ejecuta un protocolo
							Protocol pro = new Protocol();
							pro.run();
						}
						else 
						{
							System.err.println("Algo ocurrió y llegó un paquete que no decía PREPARADO (ya existe una referencia al cliente de donde llegó)");
						}

			} 
			catch (IOException e) 
			{
				System.err.println("Error creando el socket cliente.");
				ss.close();
				e.printStackTrace();
			}
		}
	}
	
	



	
	public void enviar(DatagramPacket paq) throws IOException {
		ss.send(paq);
	}
	
	public void recibir(DatagramPacket paq) throws IOException {
		ss.receive(paq);
	}
	public ArrayList<Channel> obtenerCanales(){
		return canales;
	}
	public static void main(String ... args){
		try {
			Server pool = new Server();
			pool.servidor();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}