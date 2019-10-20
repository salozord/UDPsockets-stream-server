package logica;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Server 
{
	public static final int PORT = 8080;

	private ServerSocket serverSocket;
	private  String actualHost;
	private ArrayList<Channel> canales;
	private static final int PUERTO_SERVIDOR = 8787;
	private static final int MAX_CHANNELS = 3825;
	private static final int PUERTO = 7777;
	private static final String PASSWORD = "1234";


	public Server() 
	{
		actualHost = "224.0.0.0";
		canales = new ArrayList<Channel>();
	}

	public void asignacionDeCanales() throws IOException
	{
		File directorio = new File("./data");
		File [] archivosMultimedia = directorio.listFiles();

		for (int i = 0; i < archivosMultimedia.length && canales.size() < MAX_CHANNELS ; i++) 
		{
			obtenerSiguienteCanal();
			System.out.println("Next host for multicasting : "+ actualHost);

			Channel canalNuevo = new Channel(actualHost, PUERTO, archivosMultimedia[i]);
			canales.add(canalNuevo);
			canalNuevo.start();

		}

	}
	public void obtenerSiguienteCanal()
	{
		String[] splitted = actualHost.split("\\.");
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
	}
	public Channel aniadirCanal(File nuevoVideo) throws UnknownHostException
	{
		obtenerSiguienteCanal();
		Channel canalNuevo = new Channel(actualHost, PUERTO, nuevoVideo);
		canalNuevo.start();
		canales.add(canalNuevo);
		return canalNuevo;
	}

	public void servidor() throws Exception
	{

		System.out.println("Empezando servidor maestro en puerto " + PUERTO_SERVIDOR);

		System.out.println("Esperando solicitudes.");

		//creamos los canales
		asignacionDeCanales();
		
		Socket client = null;
		BufferedReader bf;
		PrintWriter pw = null;

		while (true) 
		{
			try 
			{ 

				serverSocket = new ServerSocket(PUERTO_SERVIDOR);
				System.out.println("Socket creado.");

				// Recibe el paquete de listo del cliente
				client = serverSocket.accept();

				bf = new BufferedReader(new InputStreamReader(client.getInputStream()));
				pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
				String recibida = bf.readLine();

				String[] userPss = recibida.split(";");
				String contra = userPss[1];
				if(contra.equals(PASSWORD)) 
				{
					//para cada cliente ejecuta un protocolo
					Protocol pro = new Protocol(client, bf, pw);
					pro.start();
				}
				else 
				{
					pw.write(Protocol.ERROR);
					System.err.println("Algo ocurrió y llegó un paquete que no decía PREPARADO (ya existe una referencia al cliente de donde llegó)");
				}
				
				

			} 
			catch (IOException e) 
			{
				pw.write(Protocol.ERROR);
				System.err.println("Error creando el socket cliente.");
				client.close();
				e.printStackTrace();
			}
		}
	}


	public ArrayList<Channel> obtenerCanales()
	{
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