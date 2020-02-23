import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class Test extends TestCase {
    //Будем отслеживать баланс банка до начала транзакций и после
    public void testBank() throws InterruptedException {
        Bank bank = new Bank();
        List<Account> accounts = new ArrayList<>();
        for (int i = 1; i < 800; i++) {
            Account account = new Account(bank, String.valueOf(i));
            accounts.add(account);
            account.deposit(1000 + (long) (Math.random() * 350_000));
        }
        long sumAtAccountsBeforeTransactions = bank.getBankBalance(); //Сумма на счетах банка до начала транзакций
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            threadList.add(new Thread(new HighlyConcurrentEnvironment(bank)));
        }
        threadList.forEach(Thread::start);

        long sumAtAccountsAfterTransactions = bank.getBankBalance();

        for (Thread t : threadList) {
            while (t.getState() != Thread.State.TERMINATED) {
                Thread.sleep(10000); //Каждые 10 секунд будем проверять состояние потоков
            }
        }


        System.out.println("Bank balance before start: " + sumAtAccountsBeforeTransactions);
        System.out.println("Bank balance after start: " + sumAtAccountsAfterTransactions);
        assertEquals(sumAtAccountsBeforeTransactions, sumAtAccountsAfterTransactions);

    }


}
