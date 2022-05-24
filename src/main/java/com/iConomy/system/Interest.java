package com.iConomy.system;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import com.iConomy.util.Messaging;
import com.iConomy.util.Template;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;

public class Interest extends TimerTask {
    Template Template;
    Logger log = iConomy.instance.getLogger();

    public Interest(String directory) {
        this.Template = new Template(directory, "Template.yml");
    }

    public void run() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        DecimalFormat DecimalFormat = new DecimalFormat("#.##");
        List<String> players = new ArrayList<>();
        HashMap<String, Integer> bankPlayers = new HashMap<>();

        if (Constants.InterestOnline) {
            /*
             * Select all Online Players
             */
            Collection<? extends Player> player = iConomy.getBukkitServer().getOnlinePlayers();

            if (Constants.InterestType.equalsIgnoreCase("players") || !Constants.Banking) {
                for (Player p : player)
                    players.add(p.getName());
            } else {
                for (Player p : player) {
                    Account account = iConomy.getAccount(p.getName());

                    if (account != null)
                        for (BankAccount baccount : account.getBankAccounts())
                            bankPlayers.put(p.getName(), baccount.getBankId());
                }
            }
        } else {
            /*
             * Select ALL players.
             */
            conn = iConomy.getiCoDatabase().getConnection();
            try {
                if (Constants.InterestType.equalsIgnoreCase("players") || !Constants.Banking)
                    ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable);
                else {
                    ps = conn.prepareStatement("SELECT account_name,bank_id FROM " + Constants.SQLTable + "_BankRelations group by bank_id");
                }

                rs = ps.executeQuery();

                while (rs.next()) {
                    if (Constants.InterestType.equalsIgnoreCase("players") || !Constants.Banking) {
                        players.add(rs.getString("username"));
                        continue;
                    }
                    bankPlayers.put(rs.getString("account_name"), rs.getInt("bank_id"));
                }
            } catch (Exception E) {
                log.warning("Error executing query for interest: " + E.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ignored) {
                    }
                    conn = null;
                }
                if (ps != null) {
                    ps = null;
                }
            }
        }
        double cutoff = Constants.InterestCutoff;
        double amount = 0.0D;
        boolean percentage = false;

        if (Constants.InterestPercentage != 0.0D) {
            percentage = true;
        } else {
            double min = Constants.InterestMin;
            double max = Constants.InterestMax;
            try {
                if (min != max)
                    amount = Double.parseDouble(DecimalFormat.format(Math.random() * (max - min) + min));
                else
                    amount = max;
            } catch (NumberFormatException e) {
                log.warning("Invalid Interest: " + e);
            }
        }
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            conn.setAutoCommit(false);

            if (Constants.InterestType.equalsIgnoreCase("players") || !Constants.Banking) {
                String updateSQL = "UPDATE " + Constants.SQLTable + " SET balance = ? WHERE username = ?";
                ps = conn.prepareStatement(updateSQL);

                for (String name : players) {
                    Account account = iConomy.getAccount(name);

                    if (account != null) {
                        Holdings holdings = account.getHoldings();

                        if (holdings != null) {
                            double balance = holdings.balance();

                            if (cutoff > 0.0D ? balance >= cutoff : cutoff < 0.0D && balance <= cutoff) {
                                continue;
                            }

                            if (percentage) {
                                amount = Math.round(Constants.InterestPercentage * balance / 100.0D);
                            }

                            ps.setDouble(1, balance + amount);
                            ps.setString(2, name);
                            ps.addBatch();

                            if (Constants.InterestAnn) {
                                Player player = iConomy.getBukkitServer().getPlayer(name);
                                if (player != null)
                                    Messaging.send(player, this.Template.parse("interest.announcement", new String[]{"+amount,+money,+interest,+a,+m,+i"}, new Object[]{iConomy.format(amount)}));
                            }

                            if (amount < 0.0D)
                                iConomy.getTransactions().insert("[System Interest]", name, 0.0D, balance, 0.0D, 0.0D, amount);
                            else
                                iConomy.getTransactions().insert("[System Interest]", name, 0.0D, balance, 0.0D, amount, 0.0D);
                        }
                    }
                }
            } else {
                String updateSQL = "UPDATE " + Constants.SQLTable + "_BankRelations SET holdings = ? WHERE account_name = ? AND bank_id = ?";
                ps = conn.prepareStatement(updateSQL);

                for (String name : bankPlayers.keySet()) {
                    Account account = iConomy.getAccount(name);

                    if (account != null) {
                        Holdings holdings = account.getBankHoldings(bankPlayers.get(name));

                        if (holdings != null) {
                            double balance = holdings.balance();

                            if (cutoff > 0.0D ? balance >= cutoff : cutoff < 0.0D && balance <= cutoff) {
                                continue;
                            }

                            if (percentage) {
                                amount = Math.round(Constants.InterestPercentage * balance / 100.0D);
                            }

                            ps.setDouble(1, balance + amount);
                            ps.setString(2, name);
                            ps.setInt(3, bankPlayers.get(name));
                            ps.addBatch();

                            if (Constants.InterestAnn && Constants.InterestOnline) {
                                Player player = iConomy.getBukkitServer().getPlayer(name);
                                if (player != null)
                                    Messaging.send(player, this.Template.parse("interest.announcement", new String[]{"+amount,+money,+interest,+a,+m,+i"}, new Object[]{iConomy.format(amount)}));
                            }

                            if (amount < 0.0D)
                                iConomy.getTransactions().insert("[System Interest]", name, 0.0D, balance, 0.0D, 0.0D, amount);
                            else {
                                iConomy.getTransactions().insert("[System Interest]", name, 0.0D, balance, 0.0D, amount, 0.0D);
                            }
                        }
                    }
                }

            }

            ps.executeBatch();

            conn.commit();

            ps.clearBatch();
        } catch (SQLException ex) {
            log.warning(ex.getMessage());
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                iConomy.getiCoDatabase().close(conn);
        }
    }
}
