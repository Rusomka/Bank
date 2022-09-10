package org.example;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "fromAccount_id")
    private Account from;
    @JoinColumn(name = "toAccount_id")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Account to;
    @Column(nullable = false)
    private Double sum;
    private Date date = new Date(System.currentTimeMillis());

    public Transaction() {
    }

    public Transaction(Account from, Account to, Double sum) {
        this.from = from;
        this.to = to;
        this.sum = sum;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getFrom() {
        return from;
    }

    public void setFrom(Account from) {
        this.from = from;
        from.addTransaction(this);
    }

    public Account getTo() {
        return to;
    }

    public void setTo(Account to) {
        this.to = to;
        to.addTransaction(this);
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                (from != null ? (from.getAccountNumber() != null ? "from=" + from.getAccountNumber().toString() : "") : "") +
                (to != null ? (to.getAccountNumber() != null ? ", to=" + to.getAccountNumber().toString() : "") : "") +
                ", sum=" + sum +
                ", date=" + date +
                '}' + "\n";

    }
}
