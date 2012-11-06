package org.powerbot.game.api.methods;

import java.awt.Canvas;
import java.awt.Dimension;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.api.util.internal.Constants;
import org.powerbot.game.api.util.internal.Multipliers;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import org.powerbot.game.client.BaseInfo;
import org.powerbot.game.client.Client;
import org.powerbot.game.client.RSInfo;

/**
 * A utility for the manipulation of the game.
 *
 * @author Timer
 */
public class Game {
        private static short world = -1;

	public static final int LOGOUT_SCREEN = -834768765;
	public static final int LOGIN_CHECK_ONE = -638352585;
	public static final int LOBBY_SCREEN = -932976855;
	public static final int LOGIN_CHECK_TWO = -196416180;
	public static final int LOGGING_IN_TO_GAME = -441936405; //Is this map loading? Never checked.
	public static final int LOGGED_IN = -245520225;

	/**
	 * @return The current state of the game client.
	 */
	public static int getClientState()
	{
		return Context.client().getLoginIndex();
	}

	/**
	 * @return The current world you are. -1 means you can't obtain current world yet.
         * @parameter "recheck" if true it will force a check for current world regardless of state.
         *             Otherwise no checks are performed unless the current world is -1.
	 */
	public static short getWorld(boolean recheck)
	{
		if (recheck || world == -1)
		{
			int state = getClientState();
			char[] message;
			int length = -1;
			if (state == LOBBY_SCREEN)
			{
				String text = Widgets.get(910).getChild(11).getText();
				if (text == null)
					return world;

				message = text.toCharArray();
				length = message.length - 6;
			} 
			else if (state == LOGGED_IN) 
			{
				String text = Widgets.get(550).getChild(18).getText();
				if (text == null)
					return world;

				message = text.toCharArray();
				length = message.length - 26;
			} else {
				return world;
			}

			StringBuilder sb = new StringBuilder(length);
			for (int i = message.length - length; i < message.length; ++i)
			{
				if (message[i] < '0' || message[i] > '9')
					break;
				sb.append(message[i]);
			}
			world = (short)Integer.parseInt(sb.toString());
		}	
		return world;
	}

	/**
	 * @return The floor level, or plane, you are currently located on.
	 */
	public static int getPlane() {
		final Client client = Context.client();
		final Multipliers multipliers = Context.multipliers();
		return client.getPlane() * multipliers.GLOBAL_PLANE;
	}

	/**
	 * @return The x location of the currently loaded map base.
	 */
	public static int getBaseX() {
		final Client client = Context.client();
		final Multipliers multipliers = Context.multipliers();
		return ((BaseInfo) ((RSInfo) client.getRSGroundInfo()).getBaseInfo()).getX() * multipliers.BASEDATA_X >> 8;
	}

	/**
	 * @return The y location of the currently loaded map base.
	 */
	public static int getBaseY() {
		final Client client = Context.client();
		final Multipliers multipliers = Context.multipliers();
		return ((BaseInfo) ((RSInfo) client.getRSGroundInfo()).getBaseInfo()).getY() * multipliers.BASEDATA_Y >> 8;
	}

	public static Tile getMapBase() {
		final Client client = Context.client();
		final Multipliers multipliers = Context.multipliers();
		final BaseInfo infoInts = (BaseInfo) ((RSInfo) client.getRSGroundInfo()).getBaseInfo();
		return new Tile(
				(infoInts.getX() * multipliers.BASEDATA_X) >> 8,
				(infoInts.getY() * multipliers.BASEDATA_Y) >> 8,
				Game.getPlane()
		);
	}

	public static int getLoopCycle() {
		final Client client = Context.client();
		final Multipliers multipliers = Context.multipliers();
		return client.getLoopCycle() * multipliers.GLOBAL_LOOPCYCLE;
	}

	public static Dimension getDimensions() {
		final Canvas canvas = Context.client().getCanvas();
		return new Dimension(canvas.getWidth(), canvas.getHeight());
	}

	/**
	 * @param lobby <tt>true</tt> if the method should log out to the lobby, <tt>false</tt> if the method should fully log out.
	 * @return <tt>true</tt> if and only if the client's state equals the state you want it to be in.
	 */
	public static boolean logout(final boolean lobby) {
		if (Game.getClientState() == Game.INDEX_LOBBY_SCREEN && lobby || Game.getClientState() == Game.INDEX_LOGIN_SCREEN && !lobby) {
			return true;
		}
		if (Tabs.LOGOUT.open()) {
			final WidgetChild w = Widgets.get(182, lobby ? 6 : 13);
			if (w != null && w.validate() && w.interact("Exit to " + (lobby ? "Lobby" : "Login"))) {
				for (int i = 0; i < 10; i++, Task.sleep(100, 200)) {
					if (Game.getClientState() == Game.INDEX_LOBBY_SCREEN && lobby || Game.getClientState() == Game.INDEX_LOGIN_SCREEN && !lobby) {
						return true;
					}
				}
			}
		}
		return false;
	}
}