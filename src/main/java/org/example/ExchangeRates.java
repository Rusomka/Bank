package org.example;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ExchangeRates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double usd;
    @Column(nullable = false)
    private Double eur;
    private Date date = new Date(System.currentTimeMillis());

    public ExchangeRates() {
    }

    public ExchangeRates(Double usd, Double eur) {
        this.usd = usd;
        this.eur = eur;
    }

    public Double get(String str) {
        if (str.equalsIgnoreCase("uah"))
            return 1.0;
        return str.equalsIgnoreCase("usd") ? usd : eur;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getUsd() {
        return usd;
    }

    public void setUsd(Double usd) {
        this.usd = usd;
    }

    public Double getEur() {
        return eur;
    }

    public void setEur(Double eur) {
        this.eur = eur;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "ExchangeRates{" +
                "id=" + id +
                ", usd=" + usd +
                ", eur=" + eur +
                ", date=" + date +
                '}';
    }
}
