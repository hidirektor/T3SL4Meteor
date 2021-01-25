package me.t3sl4.meteor.util;

import java.util.List;

public class MessageUtil {
    public static String PREFIX;
    public static String NOPERM;
    public static List<String> INFO;
    public static String UNKNOWN;
    public static String TOOMANY;
    public static String CHECK;
    public static String RELOAD;
    public static String MUSTINGAME;
    public static String VERIFYMETEOR;
    public static String AVAILABLEMETEORS;
    public static String SHOOTING;
    public static String RANDOMMETEORS;
    public static String ALREADYRANDOM;
    public static String DISABLEDRANDOM;
    public static String RANDOMNOTFALLING;
    public static String STOPRANDOM;
    public static String FAILEDMETEOR;
    public static String INVALID;
    public static String TREASURECHEST;

    static SettingsManager manager = SettingsManager.getInstance();

    public static void loadMessages() {
        PREFIX = colorize(manager.getConfig().getString("Messages.Prefix"));
        NOPERM = PREFIX + colorize(manager.getConfig().getString("Messages.Prefix"));
        INFO = colorizeList(manager.getConfig().getStringList("CmdInfo"));
        UNKNOWN = PREFIX + colorize(manager.getConfig().getString("Messages.Unknown"));
        TOOMANY = PREFIX + colorize(manager.getConfig().getString("Messages.TooMany"));
        CHECK = PREFIX + colorize(manager.getConfig().getString("Messages.Check"));
        RELOAD = PREFIX + colorize(manager.getConfig().getString("Messages.Reload"));
        MUSTINGAME = PREFIX + colorize(manager.getConfig().getString("Messages.MustinGame"));
        VERIFYMETEOR = PREFIX + colorize(manager.getConfig().getString("Messages.VerifyMeteor"));
        AVAILABLEMETEORS = PREFIX + colorize(manager.getConfig().getString("Messages.AvailableMeteors"));
        SHOOTING = PREFIX + colorize(manager.getConfig().getString("Messages.Shooting"));
        RANDOMMETEORS = PREFIX + colorize(manager.getConfig().getString("Messages.RandomMeteors"));
        ALREADYRANDOM = PREFIX + colorize(manager.getConfig().getString("Messages.AlreadyRandom"));
        DISABLEDRANDOM = PREFIX + colorize(manager.getConfig().getString("Messages.DisabledRandom"));
        RANDOMNOTFALLING = PREFIX + colorize(manager.getConfig().getString("Messages.RandomNotFalling"));
        STOPRANDOM = PREFIX + colorize(manager.getConfig().getString("Messages.StopRandom"));
        FAILEDMETEOR = PREFIX + colorize(manager.getConfig().getString("Messages.FailedMeteor"));
        INVALID = PREFIX + colorize(manager.getConfig().getString("Messages.Invalid"));
        TREASURECHEST = colorize(manager.getConfig().getString("Messages.TreasureChest"));
    }

    public static String colorize(String str) {
        return str.replace("&", "§");
    }

    public static List<String> colorizeList(List<String> str) {
        for(int x=0; x<str.size(); x++) {
            str.set(x, str.get(x).replace("&", "§"));
        }
        return str;
    }
}