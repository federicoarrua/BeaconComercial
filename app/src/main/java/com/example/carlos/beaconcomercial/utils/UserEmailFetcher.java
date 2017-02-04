package com.example.carlos.beaconcomercial.utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.RequiresPermission;

/**
 * Esta clase usa el AccountManager para obtener la dirección de emal primaria del dispositivo

 */
public class UserEmailFetcher {

    @RequiresPermission(Manifest.permission.ACCOUNT_MANAGER)
    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }

    @RequiresPermission(Manifest.permission.ACCOUNT_MANAGER)
    private static Account getAccount(AccountManager accountManager) throws SecurityException {

        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }
}
