/**
 * This class is used to record all the details that pertain centrum
 * clients connecting onto our instance.
 *
 * Available features of this class:
 *
    add - add a new client
    remove - remove an existent client
    get - get a given client by URL
    getAll - get all registered clients
    checkExpiration - check validity of all registered clients
    exportMessage - Export all registered clients to a message format
    importMessage - Import clients from a received message
 */

package app.centrum;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import system.msg;

/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA.
 */
public class CentrumManager implements msg {

    private HashMap<String, CentrumClient> clients = new HashMap();


    /** Places the new client on the list */
    public void add(CentrumClient client){
        // pre flight checks
        if (client.getUrl().isEmpty()){
            System.out.println("Can't add an empty client");
            return;
        }
        // place the client on the queue
        clients.put(client.getUrl(), client);
    }

    /** Removes a given client from the list */
    public synchronized void remove(String URL){
        // remove the client from our list
        clients.remove(URL);
    }

    /** Get a client from the list */
    public CentrumClient get(String URL){
        // remove the client from our list
        return clients.get(URL);
    }

    /** Get a collection of registered clients */
    public Collection<CentrumClient> getAll(){
        // remove the client from our list
        return clients.values();
    }


    /** If we have expired clients, remove them from the list */
    public synchronized void CheckExpiration(){
    // do the iteration
    Collection c = this.getAll();
    //obtain an Iterator for Collection
    Iterator<CentrumClient> itr = c.iterator();
    //iterate through HashMap values iterator
    while(itr.hasNext()){
       CentrumClient client = itr.next();
       if (client.isAlive()==false){
            //System.out.println("Removing "+client.getUrl());
            itr.remove();
       }
    }
  }

    /** return the number of registered clients*/
    public long size(){
        return this.clients.size();
    }

    /** Export all registered clients to a message object */
    public Properties exportMessage(Properties message){
        Collection<CentrumClient> myClients = this.getAll();

        String list = "";

        // iterate all registered clients
        for(CentrumClient client : myClients){
            // create a string that lists them all
            list = list.concat(client.getUrl()) + ";";
            // create a property for each, to hold individual data
// disabled for the moment
//            message.setProperty("client_" + client.getUrl(),
//                    client.doExport() // get client data as a string
//                    );
        }
        // add a list of clients that are included on this message
        message.setProperty(FIELD_MESSAGE, list);
        //System.out.println("-------------" + list);
        return message;
    }

    /** Import clients from a message object */
    public void importMessage(Properties message){
    }

 }
