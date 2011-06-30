/*
 * This is a façade template so that other components can register the player
 * class independently of the used implementation.
 *
 * How does this work?
 *
 * You start by defining an interface file (player_interface.java)
 * Then you implement the interface using your desired technology (player_hsql.java)
 * And at last you create a new class that extends the implementation using a
 * generic name (player.java)
 *
 * The big advantage is that anyone using player.java will have access to the
 * underlying methods defined on the interface regardless of the specific
 * implementation.
 *
 * So, if we decide to use something else besides HSQL for the player, we can do
 * this without affecting other parts of our system.
 */

package players;

/**
 *
 * @author Nuno Brito
 */
public class player extends player_hsql implements player_interface{

}
