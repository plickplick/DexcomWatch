package link.halen.dexcomwatch;

import android.widget.EditText;
import android.widget.TextView;

import androidx.room.Room;

import java.util.List;

import link.halen.dexcomwatch.database.AppDatabase;
import link.halen.dexcomwatch.database.enteties.Account;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    Thread applicationThread = null;
    private static Application applicationInstance = null;
    private final MainActivity activity;
    private GlucoseRetreiver glucoseRetreiver;
    private AppDatabase db;

    private Application(MainActivity activity) {
        this.activity = activity;
        db = Room.databaseBuilder(activity.getApplicationContext(), AppDatabase.class, "dexcom-database").build();
        Thread startThread = new Thread(() -> {
            log.info("Inside start thread");
            List<Account> accounts = db.accountDao().getAccount();
            log.info("Retreived saved accounts: " + accounts.size());
            glucoseRetreiver = new GlucoseRetreiver(activity.findViewById(R.id.textview_first));
            if (!accounts.isEmpty() && !accounts.get(0).password.isEmpty() && !accounts.get(0).accountName.isEmpty()) {
                glucoseRetreiver.setAccountName(accounts.get(0).accountName);
                glucoseRetreiver.setPassword(accounts.get(0).password);
                EditText accountN = (EditText) activity.findViewById(R.id.editTextTextAcccName);
                EditText pass = (EditText) activity.findViewById(R.id.editTextTextPassword);
                accountN.post(() -> accountN.setText(accounts.get(0).accountName));
                pass.post(() -> pass.setText(accounts.get(0).password));
                applicationThread = new Thread(glucoseRetreiver);
                applicationThread.start();
            }
        });
        startThread.start();
    }

    public static Application getApplicationInstance(MainActivity activity) {
        if (applicationInstance == null) {
            applicationInstance = new Application(activity);
        }
        return applicationInstance;
    }

    public void saveAccount(String accountName, String password) {
        TextView textView = (TextView) activity.findViewById(R.id.textview_first);
        textView.post(() -> textView.setText("--.--"));
        if (applicationThread != null && applicationThread.isAlive()) {
            log.info("interrups application: ");
            applicationThread.interrupt();
        }
        Thread dbTrhead = new Thread(() -> {
            int count = db.accountDao().count();
            if (count > 0) {
                db.accountDao().deleteAll();
                log.info("Deleted all accounts, number of entries: " + count);
            }
            Account account = new Account();
            account.accountName = accountName;
            account.password = password;
            db.accountDao().insert(account);
            log.info("Saved account in db.");
            glucoseRetreiver.setAccountName(accountName);
            glucoseRetreiver.setPassword(password);
            applicationThread = new Thread(glucoseRetreiver);
            applicationThread.start();
            log.info("Started thread");
        });
        dbTrhead.start();
    }
}
