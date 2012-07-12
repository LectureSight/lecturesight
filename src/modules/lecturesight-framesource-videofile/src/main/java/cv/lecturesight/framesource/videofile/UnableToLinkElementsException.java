/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.lecturesight.framesource.videofile;

import org.gstreamer.Element;

/**
 *
 * @author wsmirnow
 */
public class UnableToLinkElementsException extends Exception {
    
    public UnableToLinkElementsException(Element src, Element sink) {
        super("Can not link " + src.getName() + " to " + sink.getName());
    }
}
