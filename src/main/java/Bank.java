import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {
    private ConcurrentHashMap<String, Account> accounts;
    private final Random random = new Random();

    public Bank() {
        this.accounts = new ConcurrentHashMap<>();
    }

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        if (amount < 50_000) return false; //Если сумма менее 50к, но кто-то вызвал этот метод - вернём false
        Account from = accounts.get(fromAccountNum);
        Account to = accounts.get(toAccountNum);
        if (fromAccountNum.compareTo(toAccountNum) < 0) { //Пытаемся сделать synchronized
            synchronized (from) {
                synchronized (to) {
                    from.setOnCheck(true);
                    from.setOnCheck(true);
                }
            }
        } else {
            synchronized (to) {
                synchronized (from) {
                    from.setOnCheck(true);
                    from.setOnCheck(true);
                }
            }
        }

        //Закомментим задержку, а то очень долго ждать, пока программа завершится
//        Thread.sleep(1000);
        boolean result = random.nextBoolean();
        if (fromAccountNum.compareTo(toAccountNum) < 0) { //И тут пытаемся избежать взаимной блокировки
            synchronized (from) {
                synchronized (to) {
                    from.setOnCheck(false);
                    to.setOnCheck(false);
                }
            }
        } else {
            synchronized (to) {
                synchronized (from) {
                    to.setOnCheck(false);
                    from.setOnCheck(false);
                }
            }
        }
        return result;
    }

    /**
     * TODO: реализовать метод. Метод переводит деньги между счетами.
     * Если сумма транзакции > 50000, то после совершения транзакции,
     * она отправляется на проверку Службе Безопасности – вызывается
     * метод isFraud. Если возвращается true, то делается блокировка
     * счетов (как – на ваше усмотрение)
     */
    public void transfer(String fromAccountNum, String toAccountNum, long amount) {
        if (accounts.containsKey(fromAccountNum)
                && accounts.containsKey(toAccountNum)) {

            Account fromAccount = accounts.get(fromAccountNum);
            Account toAccount = accounts.get(toAccountNum);
            if (fromAccount.isCheck() || toAccount.isCheck()) {
//                System.out.println("You can't transfer now! Try again later");
            } else {
                if (fromAccountNum.equals(toAccountNum)) {
                    /**
                     * Тут и далее закомментируем подразумеваемый вывод в консоль, чтобы
                     * приложение кушало меньше памяти, иначе мой ноутбук взлетит на воздух
                     */
//                System.out.println("You can't transfer money to this account");
                } else {
                    if (!fromAccount.isLocked() && !toAccount.isLocked()) {

                        if (fromAccount.canDebit(amount)) { //Тут пробуем сделать synchronized

                            if (fromAccountNum.compareTo(toAccountNum) < 0) {
                                synchronized (fromAccount) {
                                    synchronized (toAccount) {
                                        fromAccount.debit(amount);
                                        toAccount.deposit(amount);
                                    }
                                }

                            } else {
                                synchronized (toAccount) {
                                    synchronized (fromAccount) {
                                        toAccount.deposit(amount);
                                        fromAccount.debit(amount);
                                    }
                                }

                            }
                            if (amount > 50_000) {
                                try {
                                    if (isFraud(fromAccountNum, toAccountNum, amount)) { //Тут тоже пробуем сделать synchronized
                                        if (fromAccountNum.compareTo(toAccountNum) < 0) {
                                            synchronized (fromAccount) {
                                                synchronized (toAccount) {
                                                    fromAccount.lock();
                                                    toAccount.lock();
                                                }
                                            }
                                        } else {
                                            synchronized (toAccount) {
                                                synchronized (fromAccount) {
                                                    toAccount.lock();
                                                    fromAccount.lock();
                                                }
                                            }
                                        }

                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
//                        System.out.println("You can't debit this sum");
                        }
                    } else {
//                    System.out.println("Wrong operation. One of (or all) accounts was blocked");
                    }
                }
            }
        } else {
//            System.out.println("Wrong operation. Check account numbers and try again");
        }
    }

    /**
     * TODO: реализовать метод. Возвращает остаток на счёте.
     */
    public long getBalance(String accountNum) {
        if (accounts.keySet().contains(accountNum)) {
            return accounts.get(accountNum).checkBalance();
        } else {
//            System.out.println("Can't find account with number " + accountNum);
            return 0;
        }
    }

    protected int getLockedAccounts() {
        int lockedAccounts = 0;
        for (Account account : accounts.values()) {
            if (account.isLocked()) lockedAccounts++;
        }
        return lockedAccounts;
    }

    protected void addAccount(Account acc) {
        accounts.put(acc.getAccNumber(), acc);
    }

    private long calculateBankBalance() {
        return accounts.values().stream().mapToLong(Account::checkBalance).sum();
    }

    public ConcurrentHashMap<String, Account> getAccounts() {
        return accounts;
    }

    public long getBankBalance() { //Общий баланс банка (баланс всех счетов)
        return calculateBankBalance();
    }

}
