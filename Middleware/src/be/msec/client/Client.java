package be.msec.client;

import be.msec.client.connection.Connection;
import be.msec.client.connection.IConnection;
import be.msec.client.connection.SimulatedConnection;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.smartcardio.*;

public class Client {

	private final static byte IDENTITY_CARD_CLA =(byte)0x80;
	private final static short SW_VERIFICATION_FAILED = 0x6300;
	private final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
	
	private static final byte VALIDATE_PIN_INS = 0x22;
	private static final byte GET_NAME_INS = 0x24;
	private static final byte GET_SERIAL_INS = 0x26;


	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		IConnection c;

		//Simulation:
		c = new SimulatedConnection();

		//Real Card:

		/*c = new Connection();
		((Connection)c).setTerminal(0); //depending on which cardreader you use
		*/
		
		c.connect(); 
		
		try {

			/*
			 * For more info on the use of CommandAPDU and ResponseAPDU:
			 * See http://java.sun.com/javase/6/docs/jre/api/security/smartcardio/spec/index.html
			 */
			
			CommandAPDU a;
			ResponseAPDU r;
			

			
			//0. create applet (only for simulator!!!)
			a = new CommandAPDU(0x00, 0xa4, 0x04, 0x00,new byte[]{(byte) 0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x08, 0x01}, 0x7f);
			r = c.transmit(a);
			System.out.println(r);
			if (r.getSW()!=0x9000) throw new Exception("select installer applet failed");
			
			a = new CommandAPDU(0x80, 0xB8, 0x00, 0x00,new byte[]{0xb, 0x01,0x02,0x03,0x04, 0x05, 0x06, 0x07, 0x08, 0x09,0x00, 0x00, 0x00}, 0x7f);
			r = c.transmit(a);
			System.out.println(r);

			if (r.getSW()!=0x9000) throw new Exception("Applet creation failed");

			
			//1. Select applet  (not required on a real card, applet is selected by default)
			a = new CommandAPDU(0x00, 0xa4, 0x04, 0x00,new byte[]{0x01,0x02,0x03,0x04, 0x05, 0x06, 0x07, 0x08, 0x09,0x00, 0x00}, 0x7f);
			r = c.transmit(a);
			System.out.println(r);
			if (r.getSW()!=0x9000) throw new Exception("Applet selection failed");
			
			
			
			//2. Send PIN
			a = new CommandAPDU(IDENTITY_CARD_CLA, VALIDATE_PIN_INS, 0x00, 0x00,new byte[]{0x01,0x02,0x03,0x04});
			r = c.transmit(a);

			System.out.println(r);
			if (r.getSW()==SW_VERIFICATION_FAILED) throw new Exception("PIN INVALID");
			else if(r.getSW()!=0x9000) throw new Exception("Exception on the card: " + r.getSW());
			System.out.println("PIN Verified");
			
			//
			a = new CommandAPDU(IDENTITY_CARD_CLA, GET_SERIAL_INS, 0x00, 0x00);
			r = c.transmit(a);
			
			System.out.println(new String(r.getData()));
			
			//
			a = new CommandAPDU(IDENTITY_CARD_CLA, GET_NAME_INS, 0x00, 0x00);
			r = c.transmit(a);			
			
			System.out.println(new String(r.getData()));
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			c.close();  // close the connection with the card
		}


	}
	
	private byte[] getRandomBytes(){
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		return bytes;
	}

}
