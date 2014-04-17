/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

/**
 * Giant class to hold all our logic to handle messages
 * @author rahmanj
 */
public class MessageHandler {
    
    public MessageHandler() {
        
    }
    
    /**
     * Converts a URI path into a usable method name for reflection
     * @param path Path from the request with leading '/' removed if present
     * @return Returns the path converted into camel-case appropriate for reflection
     */
    public String normalizeMethod(String path) {
        StringBuilder sb = new StringBuilder();
        
        // Split on underscores
        String[] components = path.split("_");
        
        String original;
        String modified;
        
        for (int i = 0; i < components.length; i++) {
            if (i != 0) {
                original = String.valueOf(components[i].charAt(0));
                modified = original.toUpperCase();
            } else {
                original = String.valueOf(components[i].charAt(0));
                modified = original.toLowerCase();
            }
            components[i] = components[i].replaceFirst(original, modified);
        }
        
        // Stich these together
        for (String s : components) {
            sb.append(s);
        }
        
        return sb.toString();
    }
}
