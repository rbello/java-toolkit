package fr.evolya.javatoolkit.net.discover.linux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import fr.evolya.javatoolkit.code.Tokenizer;
import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.net.discover.IRouteProvider;

/**
 * Find out the local IP address and default gateway
 * @author Henry Zheng
 * @url http://www.ireasoning.com
 */
public class LinuxRouteParser implements IRouteProvider {
	
	@Override
    public RouteResult getResult()
    {
    	BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader("/proc/net/route"));
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                String [] tokens = Tokenizer.parse(line, '\t', true , true);
                if(tokens.length > 1 && tokens[1].equals("00000000"))
                {
                    String gateway = tokens[2]; //0102A8C0
                    if (gateway.length() == 8)
                    {
                        String[] s4 = new String[4];
                        s4[3] = String.valueOf(Integer.parseInt(gateway.substring(0, 2), 16));
                        s4[2] = String.valueOf(Integer.parseInt(gateway.substring(2, 4), 16));
                        s4[1] = String.valueOf(Integer.parseInt(gateway.substring(4, 6), 16));
                        s4[0] = String.valueOf(Integer.parseInt(gateway.substring(6, 8), 16));
                        gateway = s4[0] + "." + s4[1] + "." + s4[2] + "." + s4[3];
                    }
                    else gateway = null;
                    String iface = tokens[0];
                    NetworkInterface nif = NetworkInterface.getByName(iface);
                    Enumeration<InetAddress> addrs = nif.getInetAddresses();
                    while (addrs.hasMoreElements())
                    {
                        Object obj = addrs.nextElement();
                        if (obj instanceof Inet4Address)
                        {
                            String ip = obj.toString();
                            if (ip.startsWith(StringUtils.SLASH)) ip = ip.substring(1);
                            return new IRouteProvider.RouteResult(gateway, ip);
                        }
                    }
                    return new IRouteProvider.RouteResult(gateway, null);
                }
            }
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
        finally {
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e) { }
        	}
        }
    }

}


