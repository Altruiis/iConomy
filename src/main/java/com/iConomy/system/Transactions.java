package com.iConomy.system;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transactions {

    public void insert(String from, String to, double from_balance, double to_balance, double set, double gain, double loss) {
        if (!Constants.Logging) {
            return;
        }
        int i = 1;
        long timestamp = System.currentTimeMillis() / 1000L;

        Object[] data = {from, to, from_balance, to_balance, timestamp, set, gain, loss};

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_Transactions(account_from, account_to, account_from_balance, account_to_balance, `timestamp`, `set`, gain, loss) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            for (Object obj : data) {
                ps.setObject(i, obj);
                i++;
            }

            ps.executeUpdate();
        } catch (SQLException ignored) {
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ignored) {
                }

            iConomy.getiCoDatabase().close(conn);
        }
    }
}
