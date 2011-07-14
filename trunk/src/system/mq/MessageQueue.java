/*
 * This is the reference class that is used. It isolates other classes from
 * different implementations of our message queue.
 */

package system.mq;

import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 11th of July 2011 in Darmstadt, Germany
 */
public class MessageQueue extends MessageQueueFlatFile{

    public MessageQueue(Remedium instance){
        super(instance);
    }

}
