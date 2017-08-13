package fr.evolya.javatoolkit.net.discover;

import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.net.discover.linux.LinuxRouteParser;
import fr.evolya.javatoolkit.net.discover.win32.Win32RouteParser;

public interface IRouteProvider {

	public static class RouteResult {
		public final String gateway;
		public final String localAddress;
		public RouteResult(String gateway, String address) {
			this.gateway = gateway;
			this.localAddress = address;
		}
	}
	
	RouteResult getResult();
	
	public static IRouteProvider getInstance() 
    {
        if (Utils.isWindows())
        {
            return new Win32RouteParser();
        }
        else if (Utils.isLinux())
        {
            return new LinuxRouteParser();
        }
        return null;
    }
	
}
