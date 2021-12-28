package link.halen.dexcomwatch.database.enteties;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Account {

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "account_name")
    public String accountName;

    @ColumnInfo(name = "password")
    public String password;
}