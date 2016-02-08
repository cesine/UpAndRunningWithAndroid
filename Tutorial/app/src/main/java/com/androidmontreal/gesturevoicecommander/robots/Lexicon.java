package com.androidmontreal.gesturevoicecommander.robots;

import android.content.Intent;

import java.util.ArrayList;
import java.util.Locale;

public class Lexicon {
    private int language;
    private int timer = 5;

    /*
     * Languages
     */
    public static final int EN = 1000;
    public static final int FR = 1001;
    public static final String EN_CARRIER_PHRASE = "I will tell the robot:  ";
    public static final String FR_CARRIER_PHRASE = "Je vais dire au robo: ";

    /*
     * Commands, use the order of these constants for the precedence order of the
     * commands (STOP has the highest precedence)
     */
    public static final int STOP = 0;
    public static final int EXPLORE = 1;
    public static final int FORWARD = 2;
    public static final int REVERSE = 3;
    public static final int ROTATERIGHT = 4;
    public static final int ROTATELEFT = 5;
    public static final int TURNRIGHT = 6;
    public static final int TURNLEFT = 7;
    public static final int CONNECT = 8;

    private ArrayList<String> en;
    private ArrayList<String> fr;

    private int mCommandToExecute;

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

    public String connect() {
        return "CONNECT";
    }

    public void defineLanguages() {
        en = new ArrayList<String>();
        en.add(STOP, "stop:wait:don't:no:damn");
        en.add(EXPLORE, "explore:try");
        en.add(FORWARD, "forward:ahead");
        en.add(REVERSE, "reverse:back");
        en.add(ROTATERIGHT, "rotate&right");
        en.add(ROTATELEFT, "rotate&left");
        en.add(TURNRIGHT, "right");
        en.add(TURNLEFT, "left");
        en.add(CONNECT, "connect:body:robot:bluetooth");

        fr = new ArrayList<String>();
        fr.add(STOP, "arrête:arrêter:arrêté:arrêter:arrêtez:pas:voyons:voyant:m****:merde:zut:non:stop");
        fr.add(EXPLORE, "explore:explorer");
        fr.add(FORWARD, "avance:tu dois:tu dois:te doi:tournoi:tout droit:avanc:forward");
        fr.add(REVERSE, "recule:recul:reverse:arrière");
        fr.add(ROTATERIGHT, "pivoter vers la droite:vers la droite:pilot:pivot:rotate&droit");
        fr.add(ROTATELEFT, "pivoter vers la gauche:vers la gauche:rotate&gauche");
        fr.add(TURNRIGHT, "à la droite:droite:droit:right");
        fr.add(TURNLEFT, "à la gauche:gauche:left");
        fr.add(CONNECT, "connecter:mon corps:robot:bluetooth");
    }

    public String execute(int commandInteger) {
        switch (commandInteger) {
            case CONNECT:
                return connect();
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
        command = command.toLowerCase();
        int commandToExecute = STOP;
        String commandForHumans = command;

        ArrayList<String> humancommands = en;
        if (language == FR) {
            humancommands = fr;
        }
        for (int i = 0; i < humancommands.size(); i++) {
            String[] andwords = humancommands.get(i).split("&");
            String[] orwords = humancommands.get(i).split(":");
            /*
             * If there are AND words, then check first to see if it matches all words
             */
            if (andwords.length > 1) {
                int wordsfound = 0;
                commandForHumans = andwords[0];
                for (int k = 0; k < andwords.length; k++) {
                    if (command.contains(andwords[k])) {
                        wordsfound++;
                    }
                }
                if (wordsfound >= andwords.length) {
                    commandToExecute = i;
                    mCommandToExecute = commandToExecute;
                    return commandForHumans;
                }
            }
            /*
             * Then if a command hasn't been issued, check for the OR words.
             */
            if (orwords.length > 0) {
                commandForHumans = orwords[0];
                for (int k = 0; k < orwords.length; k++) {
                    if (command.contains(orwords[k])) {
                        commandToExecute = i;
                        mCommandToExecute = commandToExecute;
                        return commandForHumans;
                    }
                }
            }

        }
        mCommandToExecute = commandToExecute;
        return commandForHumans;
    }

    public String executeGuess() {
        if (mCommandToExecute >= 0) {
            return execute(mCommandToExecute);
        }
        return "";
    }

    public Lexicon(int language, int timer) {
        super();
        defineLanguages();
        this.language = language;
        this.timer = timer;
    }

    public Lexicon(int language) {
        super();
        defineLanguages();
        this.language = language;
        this.timer = 5;
    }

    public Lexicon() {
        super();
        defineLanguages();
        if (Locale.getDefault().getLanguage().contains("fr")) {
            this.language = FR;
        } else {
            this.language = EN;
        }
        this.timer = 5;
    }

}
