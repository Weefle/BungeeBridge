package me.dommi2212.BungeeBridge;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.dommi2212.BungeeBridge.util.EncryptionUtil;
import me.dommi2212.BungeeBridge.util.SerializationUtil;

/**
 * Used to manage the Clients using Mutlithreading.
 */
public class ClientRunnable implements Runnable {
	
	private Socket client = null;
	
	/**
	 * Instantiates a Thread to manage a request.
	 *
	 * @param client client
	 */
	public ClientRunnable(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		if(BungeeBridgeS.getSecurityMode() == SecurityMode.OFF) {
		    try {
		    	ObjectInputStream objIN = new ObjectInputStream(client.getInputStream());
		    	ObjectOutputStream objOUT = new ObjectOutputStream(client.getOutputStream());
		    	Object obj = objIN.readObject();
		    	BungeePacket packet = (BungeePacket) obj;
		    	Object answer = PacketHandler.handlePacket(packet, client.getInetAddress());
		    	if(packet.shouldAnswer()) {  
		    		objOUT.writeObject(answer);
		    	}
			    objIN.close();
	    	 	objOUT.close();
		    } catch(ClassCastException e) {
		    	ConsolePrinter.err("�4Failed to read packet!");
			} catch(InvalidClassException e) {
				ConsolePrinter.err("�4Your version of BungeeBridgeS(Bungeecord) is incompatible to your version of BungeeBridgeC(Spigot)!\n�4You have to update immediately!");
		    } catch(IOException e) {
		    	e.printStackTrace();
		    } catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if(BungeeBridgeS.getSecurityMode() == SecurityMode.PASS) {
		    try {
		    	ObjectInputStream objIN = new ObjectInputStream(client.getInputStream());
		    	ObjectOutputStream objOUT = new ObjectOutputStream(client.getOutputStream());
		    	Object obj = objIN.readObject();
		    	BungeePacket packet = (BungeePacket) obj;
		    	String pass = packet.getPassword();
		    	if(pass != null) {
		    		if(pass.equals(BungeeBridgeS.getPass())) {
				    	Object answer = PacketHandler.handlePacket(packet, client.getInetAddress());
				    	if(packet.shouldAnswer()) {  
				    		objOUT.writeObject(answer);
				    	}
		    		} else {
		    			ConsolePrinter.err("�4Recieved packet with wrong password!");
		    			ConsolePrinter.err("�4Source-InetAddress: " + client.getInetAddress());
		    			ConsolePrinter.err("�4Used password: \"" + packet.getPassword() + "\"");
		    		}
		    	} else {
		    		ConsolePrinter.err("�4Recieved packet without password!");
		    		ConsolePrinter.err("�4Source-InetAddress: " + client.getInetAddress());
		    	}
			    objIN.close();
	    	 	objOUT.close();
		    } catch(ClassCastException e) {
		    	ConsolePrinter.err("�4Failed to read packet!");
			} catch(InvalidClassException e) {
				ConsolePrinter.err("�4Your version of BungeeBridgeS(Bungeecord) is incompatible to your version of BungeeBridgeC(Spigot)!\n�4You have to update immediately!");
		    } catch(IOException e) {
		    	e.printStackTrace();
		    } catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if(BungeeBridgeS.getSecurityMode() == SecurityMode.CIPHER) {
			try {
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				Object rawpacket = in.readObject();
				byte[] decoded = EncryptionUtil.decode((byte[]) rawpacket, BungeeBridgeS.getPass());
				Object obj = SerializationUtil.deserialize(decoded);
				BungeePacket packet = (BungeePacket) obj;
				Object answer = PacketHandler.handlePacket(packet, client.getInetAddress());
				if(packet.shouldAnswer()) {
					byte[] serialized = SerializationUtil.serialize(answer);
					byte[] encoded;
					try {
						encoded = EncryptionUtil.encode(serialized, BungeeBridgeS.getPass());
						out.writeObject((Object) encoded);
					} catch (BadPaddingException e) {
						e.printStackTrace();
					}
				}
			    in.close();
	    	 	out.close();
			} catch (InvalidClassException e) {
				ConsolePrinter.err("�4Your version of BungeeBridgeS(Bungeecord) is incompatible to your version of BungeeBridgeC(Spigot)!\n�4You have to update immediately!");
			} catch (IOException e) {
				e.printStackTrace();	
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				ConsolePrinter.err("�4Recieved wrong encoded packet!");
				ConsolePrinter.err("�4Source-InetAddress: " + client.getInetAddress());
			}

		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
