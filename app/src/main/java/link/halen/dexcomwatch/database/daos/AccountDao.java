package link.halen.dexcomwatch.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import link.halen.dexcomwatch.database.enteties.Account;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM account")
    List<Account> getAll();

    @Query("SELECT COUNT(*) FROM account")
    int count();

    @Query("SELECT * FROM account WHERE uid IN (:userIds)")
    List<Account> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM account LIMIT 1")
    List<Account> getAccount();

    @Insert
    void insert(Account account);

    @Delete
    void delete(Account user);

    @Query("DELETE FROM account")
    void deleteAll();
}
