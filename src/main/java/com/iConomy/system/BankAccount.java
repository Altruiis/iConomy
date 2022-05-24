package com.iConomy.system;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BankAccount {
    private final String BankName;
    private final int BankId;
    private String AccountName;

    public BankAccount(String BankName, int BankId, String AccountName) {
        this.BankName = BankName;
        this.BankId = BankId;
        this.AccountName = AccountName;
    }

    public String getBankName() {
        return this.BankName;
    }

    public int getBankId() {
        return this.BankId;
    }

    public void getAccountName(String AccountName) {
        this.AccountName = AccountName;
    }

    public Holdings getHoldings() {
        return new Holdings(this.BankId, this.AccountName, true);
    }

    public void remove() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + "_BankRelations WHERE bank_id = ? AND account_name = ?");
            ps.setInt(1, this.BankId);
            ps.setString(2, this.AccountName);
            ps.executeUpdate();
        } catch (Exception ignored) {
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }
}
