package com.example.tecaji;

//class za posamezni tečaj
public class CurrencyRate {
    private String oznaka;
    private String sifra;
    private String value;


    public CurrencyRate(String oznaka, String sifra, String value) {
        this.oznaka = oznaka;
        this.sifra = sifra;
        this.value = value;
    }

    public String getOznaka() {
        return oznaka;
    }

    public String getSifra() {
        return sifra;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Oznaka: " + oznaka + ", Sifra: " + sifra + ", Rate: " + value;
    }
}
