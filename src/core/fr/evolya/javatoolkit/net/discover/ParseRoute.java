package fr.evolya.javatoolkit.net.discover;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import fr.evolya.javatoolkit.code.utils.StringUtils;
import fr.evolya.javatoolkit.code.utils.Utils;

/**
 * Find out the local IP address and default gateway
 * @author Henry Zheng
 * @url http://www.ireasoning.com
 */
public class ParseRoute 
{
    private String _gateway;
    private String _ip;
    private static ParseRoute _instance;

    public static void main(String[] args)
    {
        try
        {
            ParseRoute pr = ParseRoute.getInstance();
            System.out.println( "Gateway: " + pr.getGateway() );
            System.out.println( "IP: " + pr.getLocalIPAddress() );
        }
        catch(Exception e)
        {
            System.out.println( e);
            e.printStackTrace();
        }
    }

    public ParseRoute ()
    {
        parse();
    }

    public String getLocalIPAddress()
    {
        return _ip;
    }

    public String getGateway()
    {
        return _gateway;
    }

    public static ParseRoute getInstance()
    {
        if(_instance == null)
        {
            _instance = new ParseRoute();
        }
        return _instance;
    }

    private void parse() 
    {
        if(Utils.isWindows())
        {
            parseWindows();
        }
        else if(Utils.isLinux())
        {
            parseLinux();
        }
    }

    private void parseWindows()
    {
        try
        {
            Process pro = Runtime.getRuntime().exec("cmd.exe /c route print");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pro.getInputStream())); 

            String line;
            while((line = bufferedReader.readLine())!=null)
            {
                line = line.trim();
                String [] tokens = Tokenizer.parse(line, ' ', true , true);// line.split(" ");
                if(tokens.length == 5 && tokens[0].equals("0.0.0.0"))
                {
                    _gateway = tokens[2];
                    _ip = tokens[3];
                    return;
                }
            }
            //pro.waitFor();      
        }
        catch(IOException e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    private void parseLinux()
    {
    	BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader("/proc/net/route"));
            String line;
            while((line = reader.readLine())!=null)
            {
                line = line.trim();
                String [] tokens = Tokenizer.parse(line, '\t', true , true);// line.split(" ");
                if(tokens.length > 1 && tokens[1].equals("00000000"))
                {
                    String gateway = tokens[2]; //0102A8C0
                    if(gateway.length() == 8)
                    {
                        String[] s4 = new String[4];
                        s4[3] = String.valueOf(Integer.parseInt(gateway.substring(0, 2), 16));
                        s4[2] = String.valueOf(Integer.parseInt(gateway.substring(2, 4), 16));
                        s4[1] = String.valueOf(Integer.parseInt(gateway.substring(4, 6), 16));
                        s4[0] = String.valueOf(Integer.parseInt(gateway.substring(6, 8), 16));
                        _gateway = s4[0] + "." + s4[1] + "." + s4[2] + "." + s4[3];
                    }
                    String iface = tokens[0];
                    NetworkInterface nif = NetworkInterface.getByName(iface);
                    Enumeration<InetAddress> addrs = nif.getInetAddresses();
                    while(addrs.hasMoreElements())
                    {
                        Object obj = addrs.nextElement();
                        if(obj instanceof Inet4Address)
                        {
                            _ip =  obj.toString();
                            if(_ip.startsWith(StringUtils.SLASH)) _ip = _ip.substring(1);
                            return;
                        }
                    }
                    return;
                }
            }
        }
        catch (IOException e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
        finally {
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e) {
					System.err.println(e);
		            e.printStackTrace();
				}
        	}
        }
    }



}


