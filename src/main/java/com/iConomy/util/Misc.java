package com.iConomy.util;

import com.iConomy.iConomy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class Misc {

    /**
     * Checks text against two variables, if it equals at least one returns true.
     *
     * @param text The text that we were provided with.
     * @param is   The first variable that needs to be checked against
     * @return <code>Boolean</code> - True or false based on text.
     */
    public static boolean is(String text, String[] is) {
        for (String s : is) {
            if (text.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSelf(CommandSender sender, String name) {
        return sender.getName().equalsIgnoreCase(name);
    }

    public static int plural(Double amount) {
        return 1;
    }

    public static int plural(Integer amount) {
        return 1;
    }

    public static String BankCurrency(int which, String denom) {
        String[] denoms = denom.split(",");

        return denoms[which];
    }

    public static String formatted(String amount, List<String> maj, List<String> min) {
        String formatted;
        String famount = amount.replace(",", "");

        if (Constants.FormatMinor) {
            String[] pieces;
            String[] fpieces;

            if (amount.contains(".")) {
                pieces = amount.split("\\.");
                fpieces = new String[]{pieces[0].replace(",", ""), pieces[1]};
            } else {
                pieces = new String[]{amount, "0"};
                fpieces = new String[]{amount.replace(",", ""), "0"};
            }

            if (Constants.FormatSeperated) {
                String major = maj.get(plural(Integer.valueOf(fpieces[0])));
                String minor = min.get(plural(Integer.valueOf(fpieces[1])));

                if (pieces[1].startsWith("0") && !pieces[1].equals("0"))
                    pieces[1] = pieces[1].substring(1);
                if (pieces[0].startsWith("0") && !pieces[0].equals("0"))
                    pieces[0] = pieces[0].substring(1);

                if (Integer.parseInt(fpieces[1]) != 0 && Integer.parseInt(fpieces[0]) != 0)
                    formatted = pieces[0] + " " + major + ", " + pieces[1] + " " + minor;
                else if (Integer.parseInt(fpieces[0]) != 0)
                    formatted = pieces[0] + " " + major;
                else
                    formatted = pieces[1] + " " + minor;
            } else {
                String currency;

                if (Double.parseDouble(famount) < 1.0D || Double.parseDouble(famount) > -1.0D)
                    currency = min.get(plural(Integer.valueOf(fpieces[1])));
                else {
                    currency = maj.get(1);
                }

                formatted = amount + " " + currency;
            }
        } else {
            int plural = plural(Double.valueOf(famount));
            String currency = maj.get(plural);

            formatted = amount + " " + currency;
        }

        return formatted;
    }

    /**
     * Get the player from the server (matched)
     */
    public static Player playerMatch(String name) {
        Collection<? extends Player> online = Bukkit.getServer().getOnlinePlayers();
        Player lastPlayer = null;

        for (Player player : online) {
            String playerName = player.getName();

            if (playerName.equalsIgnoreCase(name)) {
                lastPlayer = player;
                break;
            }

            if (playerName.toLowerCase().contains(name.toLowerCase())) {
                if (lastPlayer != null) {
                    return null;
                }

                lastPlayer = player;
            }
        }

        return lastPlayer;
    }
}
