/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package players;

import java.util.ArrayList;
import java.util.Properties;
import players.ranking;
import players.ranking_interface;

/**
 *
 * @author Nuno Brito
 */
public class ranking_hsql implements ranking_interface{

         String
    // table name inside our databasde
            TABLE_PLAYER = "player";


    public boolean register(Properties parameters) {
        System.out.println("Hello "+parameters.get(ranking.FIELD_NAME));
        return true;
    }

    public boolean register(String playerName) {
        Properties data = new Properties();
        data.put(ranking.FIELD_NAME, playerName);
        return register(data);
    }

    public boolean remove(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(String playerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean update(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean update(String playerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean exists(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean exists(String playerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Properties get(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Properties get(String playerName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<Properties> getAll(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<Properties> getAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean start(Properties parameters) {
        // check if database is running
        // check if message queue is running
        // register on the process manager
        // ...
        return true;
    }

    public boolean start(String uniqueName) {
        Properties data = new Properties();
        data.put(ranking.FIELD_ID, uniqueName);
        return start(data);
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
