import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class HighlyConcurrentEnvironment implements Runnable {

    private Bank bank;
    private ConcurrentHashMap<String, Account> accounts;
    private static Logger log2file = LogManager.getRootLogger();

    public HighlyConcurrentEnvironment(Bank bank) {
        this.bank = bank;
        this.accounts = bank.getAccounts();
    }

    @Override
    public void run() {
        for (int i = 0; i < 1_000_000; i++) { //Сделаем 500_000 транзакций на поток
            //Получаем случайный номер аккаутна
            String randomAccNumber = String.valueOf((int) (Math.random() * accounts.values().size()));
            //Пробежимся по всем аккаунтам

            accounts.values().forEach(fromAccount -> {
                long amount = 100 + (long) (Math.random() * 80_000);
                bank.transfer(fromAccount.getAccNumber(), randomAccNumber, amount);
            });
            if (i % 10_000 == 0) { //Каждые 10 тысяч транзакций будем выводить логи в файл
                log2file.info("Current iteration: " + i);
                log2file.info("Sum of all balances is " + bank.getBankBalance());
                log2file.info("Locked accounts: " + bank.getLockedAccounts());
            }
        }
        log2file.info("Final sum of all balances is " + bank.getBankBalance());
        log2file.info("Locked accounts: " + bank.getLockedAccounts());
    }
}
