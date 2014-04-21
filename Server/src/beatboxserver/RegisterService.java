/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author rahmanj
 */
public class RegisterService {
    
    /**
     * Fully qualified service name
     */
    public final static String serviceName = "_beatbox._tcp.local.";
    
    /**
     * Service port
     */
    public final static int servicePort = 5050;
    
    public RegisterService() throws IOException {
        dns = JmDNS.create();
        serviceInfo = null;
    }
    
    /**
     * Register this instance
     * @param serverName {@link String} Human readable server name
     * @param serverPort Port over which the server is operating
     * @throws IOException
     */
    public void registerService(String serverName, int serverPort) throws IOException {  
        
        if (serviceInfo != null) {
            throw new IllegalStateException();
        }
        
        String instanceName = RegisterService.normalizeServerName(serverName);

        logger.info("Registering mDNS name \"%s.%s:%d\"", instanceName, serviceName, serverPort);
        
        serviceInfo = ServiceInfo.create(serviceName, instanceName, serverPort, serverName);
        
        dns.registerService(serviceInfo);
    }
    
    /**
     * 
     */
    public void DeregisterService() {
        if (serviceInfo != null) {
            dns.unregisterService(serviceInfo);
            dns.unregisterAllServices();
            serviceInfo = null;
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Convert a human friendly server name into a safe mDNS service name
     * @param serverName {@link String} human readable server name
     * @return Returns the normalized server name usable for mDNS
     */
    private static String normalizeServerName(String serverName) {
       
        // Remove whitespace
        serverName = serverName.replaceAll("[ \t\n]", "");
        
        // TODO Filter more from the string
        
        return serverName;
    }
    
    private ServiceInfo serviceInfo;
    private JmDNS dns;
    
    private final static Logger logger = LogManager.getFormatterLogger(RegisterService.class.getName());
}
