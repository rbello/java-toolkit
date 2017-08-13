package fr.evolya.javatoolkit.net.discover.win32;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.evolya.javatoolkit.code.Tokenizer;
import fr.evolya.javatoolkit.net.discover.IRouteProvider;

/**
 * Find out the local IP address and default gateway
 * @author Henry Zheng
 * @url http://www.ireasoning.com
 */
public class Win32RouteParser implements IRouteProvider
{

	@Override
    public RouteResult getResult()
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
                	return new IRouteProvider.RouteResult(tokens[2], tokens[3]);
                }
            }
            //pro.waitFor();      
        }
        catch(IOException e) { }
        return null;
    }

}


