import java.util.concurrent.atomic.AtomicLong;

public class Account {
    private volatile boolean locked;
    private volatile boolean onCheck; //Проверяется ли в данный момент
    private AtomicLong money;
    private String accNumber;
    private Bank bank;

    public Account(Bank bank, String accNumber) {
        this.bank = bank;
        this.accNumber = accNumber;
        locked = false;
        money = new AtomicLong();
        bank.addAccount(this);
        onCheck = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        this.locked = true;
    }

    public long checkBalance() {
        return money.longValue();
    }

    public void deposit(long money) {
        if (money > 0) {
            this.money.addAndGet(money);
        } else {
            System.out.println("Deposit must be positive!");
        }
    }

    public String getAccNumber() {
        return accNumber;
    }

    public void debit(long amount) {
        if (canDebit(amount)) {
            this.money.addAndGet(-amount);
        } else {
//            System.out.println("You can't debit negative number!");
        }
    }

    public boolean canDebit(long amount) {
        if (amount > 0 && amount <= this.checkBalance()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCheck() {
        return onCheck;
    }

    public void setOnCheck(boolean onCheck) {
        this.onCheck = onCheck;
    }
}
