package org.example;


import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static EntityManagerFactory emf;
    private static EntityManager em;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            emf = Persistence.createEntityManagerFactory("JPAConfig");
            em = emf.createEntityManager();
            try {
                ExchangeRates exchangeRates = new ExchangeRates(36.97, 37.13);
                Client client = new Client("A", "AA");
                Client client1 = new Client("B", "BB");
                Account account1 = new Account(generateNumber(), Currency.USD, 1000.0);
                Account account2 = new Account(generateNumber(), Currency.UAH, 1000.0);
                Account account3 = new Account(generateNumber(), Currency.USD, 500.0);
                Account account4 = new Account(generateNumber(), Currency.UAH, 500.0);
                client.addAccount(account1, account2);
                client1.addAccount(account3, account4);

                em.getTransaction().begin();
                em.persist(exchangeRates);
                em.persist(client);
                em.persist(client1);
                em.getTransaction().commit();
                while (true) {
                    System.out.println("1: add Client");
                    System.out.println("2: add Account");
                    System.out.println("3: replenish the balance");
                    System.out.println("4: account-to-account transfer");
                    System.out.println("5: amount of client money in UAH");
                    System.out.println("6: update course");
                    System.out.println("7: get transactions by account number");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            addAccount(sc);
                            break;
                        case "3":
                            replenishBalance(sc);
                            break;
                        case "4":
                            transaction(sc);
                            break;
                        case "5":
                            calculateUAH(sc);
                            break;
                        case "6":
                            updateCourse(sc);
                            break;
                        case "7":
                            getTransactions(sc);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public static void addClient(Scanner sc) {
        System.out.println("Enter name client");
        String name = sc.nextLine();
        System.out.println("Enter firstName client");
        String firstName = sc.nextLine();
        Client client = new Client(name, firstName);
        em.getTransaction().begin();
        try {
            em.persist(client);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    public static void addAccount(Scanner sc) {
        Client client = findClient(sc);
        if (client == null) {
            System.out.println("no such client");
            return;
        }
        System.out.print("Select currencies: ");
        System.out.println("\n 1=USD \n 2=EUR \n 3=UAH \n 0=Cancel");
        String currency = "";
        do {
            String selection = sc.nextLine();
            switch (selection) {
                case "1":
                    currency = "USD";
                    break;
                case "2":
                    currency = "EUR";
                    break;
                case "3":
                    currency = "UAH";
                    break;
                case "0":
                    return;
                default:
                    System.out.println("invalid input, please try again");
            }
        } while (!currency.matches("USD|EUR|UAH"));

        Account account = new Account(generateNumber(), Currency.valueOf(currency));
        client.addAccount(account);
        em.getTransaction().begin();
        try {
            em.persist(client);
            em.getTransaction().commit();
            System.out.println("done, your account number - " + account.getAccountNumber());
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    public static void replenishBalance(Scanner sc) {
        System.out.println("Enter the account number for replenishment");
        Account account = findAccount(sc);
        if (account == null) {
            System.out.println("no such account");
            return;
        }
        System.out.println("enter amount");
        String strBalance = sc.nextLine();

        Double balance = Double.parseDouble(strBalance);
        account.setBalance(account.getBalance() + balance);

        Transaction transaction = new Transaction(null, account, balance);
        transaction.setTo(account);


        em.getTransaction().begin();
        try {
            em.merge(account);
            em.getTransaction().commit();
            System.out.println("done");
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    public synchronized static void transaction(Scanner sc) {
        System.out.println("Enter sender's account");
        Account from = findAccount(sc);
        System.out.println("Enter beneficiary's account");
        Account to = findAccount(sc);
        System.out.println("enter amount");
        String sumStr = sc.nextLine();
        Double sum = Double.parseDouble(sumStr);

        if (from.getBalance() < sum) {
            System.out.println("ERROR: insufficient funds");
            return;
        }

        Transaction transaction = new Transaction(from, to, sum);
        transfer(from, to, sum);

        transaction.setFrom(from);
        transaction.setTo(to);

        em.getTransaction().begin();
        try {
            em.persist(transaction);
            em.getTransaction().commit();
            System.out.println("translation successful");
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    public static void calculateUAH(Scanner sc) {
        Client client = findClient(sc);
        if (client == null) {
            System.out.println("no such client");
            return;
        } else if (client.getAccounts().size() == 0) {
            System.out.println("the client has no accounts");
            return;
        }
        ExchangeRates er = getExchangeRates();
        Double sumUAH = 0.0;
        for (Account ac : client.getAccounts()) {
            switch (ac.getCurrency()) {
                case USD:
                    sumUAH += ac.getBalance() * er.getUsd();
                    break;
                case EUR:
                    sumUAH += ac.getBalance() * er.getEur();
                    break;
                case UAH:
                    sumUAH += ac.getBalance();
                    break;
            }
        }
        System.out.println("total amount of money in UAH: " + sumUAH);
    }

    public static void updateCourse(Scanner sc) {
        System.out.println("Enter a new course USD");
        String usdStr = sc.nextLine();
        Double usd = Double.parseDouble(usdStr);
        System.out.println("Enter a new course EUR");
        String eurStr = sc.nextLine();
        Double eur = Double.parseDouble(eurStr);
        ExchangeRates exchangeRates = new ExchangeRates(usd, eur);
        em.getTransaction().begin();
        try {
            em.persist(exchangeRates);
            em.getTransaction().commit();
            System.out.println("done");
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    public static void getTransactions(Scanner sc) {
        Account account = findAccount(sc);
        if (account == null) {
            System.out.println("no such account");
            return;
        } else if (account.getTransactions().size()==0) {
            System.out.println("No transactions \n");
            return;
        }
        System.out.println(account.getTransactions());
    }

    private static void transfer(Account from, Account to, Double sum) {
        from.setBalance(from.getBalance() - sum);

        if (from.getCurrency() == to.getCurrency()) {
            to.setBalance(to.getBalance() + sum);
        } else {
            ExchangeRates er = getExchangeRates();
            Double convertFrom = sum * er.get(from.getCurrency().name());
            Double convertTo = convertFrom / er.get(to.getCurrency().name());
            to.setBalance(to.getBalance() + convertTo);
        }
    }

    private static ExchangeRates getExchangeRates() {
        Long id = em.createQuery("SELECT MAX(x.id) FROM ExchangeRates x  ", Long.class).getSingleResult();
        ExchangeRates er = em.find(ExchangeRates.class, id);
        return er;
    }

    private static Account findAccount(Scanner sc) {
        System.out.print("Enter number: ");
        String accNumStr = sc.nextLine();
        Long accNumber = Long.parseLong(accNumStr);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> rot = cq.from(Account.class);
        cq.select(rot).where(cb.equal(rot.get("accountNumber"), accNumber));
        Account account = em.createQuery(cq).getSingleResult();
        return account;
    }

    private static Client findClient(Scanner sc) {
        System.out.println("Enter id client");
        String strId = sc.nextLine();
        Long id = Long.parseLong(strId);
        Client client = em.find(Client.class, id);
        return client;
    }

    private static Long generateNumber() {
        String str = "";
        for (int i = 0; i < 16; i++) {
            int temp = new Random().nextInt(10);
            str += String.valueOf(temp);
        }
        return Long.parseLong(str);
    }
}
