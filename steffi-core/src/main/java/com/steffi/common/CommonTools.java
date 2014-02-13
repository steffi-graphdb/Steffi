/*******************************************************************************
 * Copyright (c) 2014 EURA NOVA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Aldemar Reynaga - initial API and implementation
 *     Salim Jouili - initial API and implementation
 ******************************************************************************/
package com.steffi.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Aldemar Reynaga
 * Class containing common functions
 */
public class CommonTools {
	
	
	public static String getLocalIP()
    {

        String ipOnly = "";
        try
        {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            if (nifs == null) return "";
            while (nifs.hasMoreElements())
            {
                NetworkInterface nif = nifs.nextElement();
                // We ignore subinterfaces - as not yet needed.

                if (!nif.isLoopback() && nif.isUp() && !nif.isVirtual())
                {
                    Enumeration<InetAddress> adrs = nif.getInetAddresses();
                    while (adrs.hasMoreElements())
                    {
                        InetAddress adr = adrs.nextElement();
                        if (adr != null && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress()))
                        {
                            String adrIP = adr.getHostAddress();
                            String adrName;
                            if (nif.isPointToPoint()) // Performance issues getting hostname for mobile internet sticks
                                adrName = adrIP;
                            else
                                adrName = adr.getCanonicalHostName();

                            if (!adrName.equals(adrIP))
                                return adrIP;
                            else
                                ipOnly = adrIP;
                        }
                    }
                }
            }
            return ipOnly;
        }
        catch (SocketException ex)
        {
            return "";
        }
    }
	
	
	
	
	public static byte[] convertObjectToBytes(Object object) throws IOException {
		byte [] msg = null;
		ByteArrayOutputStream bos;
		ObjectOutput out = null;
		bos = new ByteArrayOutputStream();
		out = new ObjectOutputStream(bos);
		out.writeObject(object);
		out.flush();
		
		bos.flush();
		
		
		msg = bos.toByteArray();
		out.close();
		bos.close();
		return msg;
	}
	
	
	
	
	public static Object readFromBytes(byte [] msg) throws IOException {
		ByteArrayInputStream bis;
		ObjectInput in = null;
		bis = new ByteArrayInputStream(msg);
		in = new ObjectInputStream(bis);
		try {
			return in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
		
	public static byte[] serialize(Object object) throws IOException {
		
		
		return convertObjectToBytes(object);
	}
	

}
