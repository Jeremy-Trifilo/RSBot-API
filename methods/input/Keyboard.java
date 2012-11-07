package org.powerbot.game.api.methods.input;

import java.awt.Canvas;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.powerbot.core.script.job.Task;

import org.powerbot.game.bot.Context;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.methods.Tabs;

public class Keyboard
{
	private static Canvas target = null;
	private static KeyListener listener = null;
	
	public static int START = -1, END = -1;

	public static final int ENTER_AFTER_EACH_WORD = 0x1;
	public static final int ENTER_WHEN_DONE = 0x2;

	private static int getDelay()
	{
		if (END - START >= 0 && START >= 0)
			return Random.nextInt(START, END);
		return Random.nextInt(120, 160);
	}

	private static boolean grabInformation()
	{
		if (target != null && listener != null)
			return true;
		try {
			target = Context.client().getCanvas();
			listener = target.getKeyListeners()[0];
		} catch (Exception e) {
			System.out.println("Error: Obtaining Key Information.");
			return false;
		}
		return true;
	}

	public static boolean keyPressed(char key)
	{
		return keyPressed(key, 0x0, 0x0);
	}

	public static boolean keyPressed(char key, int code)
	{
		return keyPressed(key, code, 0x0);
	}

	public static boolean keyPressed(char key, int code, int modifiers)
	{
		if (!grabInformation())
			return false;
		KeyEvent event = new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, code, key, 0x1);
		System.out.println(toString("[PRESSED]: ", event));
		listener.keyPressed(event);
		return true;
	}

	public static boolean keyTyped(char key)
	{
		return keyTyped(key, 0x0, 0x0);
	}

	public static boolean keyTyped(char key, int code)
	{
		return keyTyped(key, code, 0x0);
	}

	public static boolean keyTyped(char key, int code, int modifiers)
	{
		if (!grabInformation())
			return false;
		KeyEvent event = new KeyEvent(target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), modifiers, code, key, 0x0);
		listener.keyTyped(event);
		return true;
	}

	public static boolean keyReleased(char key)
	{
		return keyReleased(key, 0x0, 0x0);
	}

	public static boolean keyReleased(char key, int code)
	{
		return keyReleased(key, code, 0x0);
	}

	public static boolean keyReleased(char key, int code, int modifiers)
	{
		if (!grabInformation())
			return false;
		KeyEvent event = new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, code, key, 0x1);
		listener.keyReleased(event);
		return true;
	}

	public static boolean sendTab(int tab)
	{
		if (!grabInformation())
			return false;
		listener.keyPressed(
			new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0x0, 0x6F + tab, (char)0xFFFF, 0x1)
		);
		Task.sleep(getDelay());
		listener.keyReleased(
			new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0x0, 0x6F + tab, (char)0xFFFF, 0x1)
		);
		return true;
	}

	public static boolean sendTab(Tabs tab)
	{
		if (!grabInformation())
			return false;
		listener.keyPressed(
			new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis() + getDelay(), 0x0, tab.getFunctionKey() & 0xFF, (char)0xFFFF, 0x1)
		);
		Task.sleep(getDelay());
		listener.keyReleased(
			new KeyEvent(target, KeyEvent.KEY_RELEASED, System.currentTimeMillis() + getDelay(), 0x0, tab.getFunctionKey() & 0xFF, (char)0xFFFF, 0x1)
		);
		return true;	
	}

	public static void type(String text, int settings)
	{
		type(text.toCharArray(), settings);
	}

	public static void type(char[] text, int settings)
	{
		int modifiers = 0x0;
		int code = 0x0;
		int previous = '\0';
		boolean status = true;
		for (char c : text)
		{
			if (c == ' ' && (settings & ENTER_AFTER_EACH_WORD) == ENTER_AFTER_EACH_WORD)
			{
				hitSpecialKey(KeyEvent.VK_ENTER, 0x0);				
				continue;
			}
			modifiers = c >= 'A' && c <= 'Z' ? 0x10 : 0x0;
			if (c != (previous & 0xFF))
			{
				if (modifiers == 0x10)
					hitSpecialKey(KeyEvent.VK_SPACE, modifiers);
				previous = c;
				keyPressed(c, code, modifiers);
				keyTyped(c, code, modifiers);
				Task.sleep(getDelay());
				keyReleased(c, code, modifiers);
			} else {
				keyTyped(c, code, modifiers);
				if (previous < 0xFF00)
				{
					if (modifiers == 0x10)
						hitSpecialKey(KeyEvent.VK_SPACE, modifiers);
					Task.sleep(500); //FIRST TIME HOLD DELAY.
					previous |= 0xFF00;
					if (modifiers == 0x10)
						hitSpecialKey(KeyEvent.VK_SPACE, modifiers);
				} else {
					Task.sleep(getDelay());
					if (modifiers == 0x10)
						hitSpecialKey(KeyEvent.VK_SPACE, modifiers);
				}
				keyReleased(c, code, modifiers);
			}
		}
		if ((settings & ENTER_WHEN_DONE) == ENTER_WHEN_DONE)
			hitSpecialKey(KeyEvent.VK_ENTER, 0x0);
	}

	public static void hitSpecialKey(int code, int modifiers)
	{
		char c = (char)0xFFFF;
		keyPressed(c, code, modifiers);
		Task.sleep(getDelay());
		keyReleased(c, code, modifiers);
	}

	public static String toString(String name, KeyEvent e)
	{
		StringBuilder sb = new StringBuilder();
		char key = e.getKeyChar();
		sb.append(name);
		sb.append(", Key(INT): ");
		sb.append((int)key);
		sb.append(", Key(CHAR): ");
		sb.append(key >= ' ' ? key : "N/A");
		sb.append(", Code: ");
		sb.append(e.getKeyCode());
		sb.append(", Location: ");
		sb.append(e.getKeyLocation());
		sb.append(", Modifiers: ");
		sb.append(e.getModifiers());
		return sb.toString();
	}
}