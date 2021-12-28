package link.halen.dexcomwatch.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import link.halen.dexcomwatch.database.daos.AccountDao;
import link.halen.dexcomwatch.database.enteties.Account;

@Database(entities = {Account.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
}

