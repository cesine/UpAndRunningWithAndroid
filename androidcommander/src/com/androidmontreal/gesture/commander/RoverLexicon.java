package com.androidmontreal.gesture.commander;

import java.util.ArrayList;


public class RoverLexicon {
	private int language;
	private int timer = 5;

	/*
	 * Languages
	 */
	public static final int EN = 1000;
	public static final int FR = 1001;

	/*
	 * Commands, use the order of these constants for
	 * the precedence order of the commands 
	 * (STOP has the highest precedence)
	 */
	public static final int STOP = 0;
	public static final int EXPLORE = 1;
	public static final int FORWARD = 2;
	public static final int REVERSE = 3;
	public static final int TURNRIGHT = 4;
	public static final int TURNLEFT = 5;
	public static final int ROTATERIGHT = 6;
	public static final int ROTATELEFT = 7;

	private ArrayList<String> en;
	private ArrayList<String> fr;

	public String stop() {
		return "S";
	}

	public String explore() {
		return "E";
	}

	public String forward() {
		return "1F2F";
	}

	public String reverse() {
		return "1R2R";
	}

	public String turnRight() {
		return "1F";
	}

	public String turnLeft() {
		return "2F";
	}

	public String rotateRight() {
		return "1F2R";
	}

	public String rotateLeft() {
		return "1R2F";
	}

	public void defineLanguages() {
		en = new ArrayList<String>();
		en.add(STOP, "stop|don't|no|damn");
		en.add(EXPLORE, "explore|try");
		en.add(FORWARD, "forward|ahead");
		en.add(REVERSE, "reverse|back");
		en.add(TURNRIGHT, "right");
		en.add(TURNLEFT, "left");
		en.add(ROTATELEFT, "rotate&left");
		en.add(ROTATERIGHT, "rotate&right");

		fr = new ArrayList<String>();
		fr.add(STOP, "arret|pas|voyons|merde");
		fr.add(EXPLORE, "explore");
		fr.add(FORWARD, "avanc");
		fr.add(REVERSE, "recul");
		fr.add(TURNRIGHT, "droit");
		fr.add(TURNLEFT, "gauche");
		fr.add(ROTATELEFT, "rotat&gauche");
		fr.add(ROTATERIGHT, "rotat&droit");
	}

	public String execute(int commandInteger) {
		switch (commandInteger) {
		case STOP:
			return stop();
		case EXPLORE:
			return explore();
		case FORWARD:
			return forward();
		case REVERSE:
			return reverse();
		case TURNRIGHT:
			return turnRight();
		case TURNLEFT:
			return turnLeft();
		case ROTATERIGHT:
			return rotateRight();
		case ROTATELEFT:
			return rotateLeft();
		default:
			return stop();
		}
	}

	public String guessWhatToDo(String command) {
		int commandToExecute = STOP;

		ArrayList<String> humancommands = en;
		if (language == FR) {
			humancommands = fr;
		}
		for (int i = 0; i < humancommands.size(); i++) {
			String[] andwords = humancommands.get(i).split("&");
			String[] orwords = humancommands.get(i).split("|");
			/*
			 * If there are AND words, then check first to see if it matches all
			 * words
			 */
			if (andwords.length > 0) {
				int wordsfound = 0;
				for (int k = 0; k < andwords.length; k++) {
					if (command.contains(andwords[k])) {
						wordsfound++;
					}
				}
				if (wordsfound >= andwords.length) {
					commandToExecute = i;
					break;
				}
			}
			/*
			 * Then if a command hasn't been issued, check for the or words.
			 */
			for (int k = 0; k < orwords.length; k++) {
				if (command.contains(orwords[k])) {
					commandToExecute = i;
					break;
				}
			}

		}
		return execute(commandToExecute);
	}

	public RoverLexicon(int language, int timer) {
		super();
		this.language = language;
		this.timer = timer;
	}

	public RoverLexicon() {
		super();
		this.language = EN;
		this.timer = 5;
	}

}
