/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.old;

import java.io.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 *
 * @author rahmanj
 */
public class RegisterService {
    
    public final static String serviceName = "_beatbox._tcp.local";
    
    public static void registerService() throws IOException {
        JmDNS dns = JmDNS.create();
            
        ServiceInfo info = ServiceInfo.create(RegisterService.serviceName, "BeatBox Server", 5050, "BeatBox Central Server");
            
        dns.registerService(info); 
    }
    
}
