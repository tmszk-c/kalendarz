package org.kalendarz;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Zdarzenie implements Comparable<Zdarzenie> {

    private String tytul;
    private LocalDateTime data;
    private URL link;

    // @JsonIgnore - KLUCZOWE DLA XML
    // Blokuje zapisywanie tej listy do pliku.
    // Zapobiega błędowi StackOverflow (nieskończona pętla: Zdarzenie ma Osobę, Osoba ma Zdarzenie...)
    @JsonIgnore
    private ArrayList<Osoba> listaOsob;

    // Konstruktor bezparametrowy wymagany przez Jackson (XML)
    public Zdarzenie() {
        this.listaOsob = new ArrayList<>();
    }

    // Konstruktor główny
    public Zdarzenie(String tytul, LocalDateTime data, URL link) {
        super();
        this.tytul = tytul;
        this.data = data;
        this.link = link;
        this.listaOsob = new ArrayList<>();
    }

    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public URL getLink() { return link; }
    public void setLink(URL link) { this.link = link; }
    public ArrayList<Osoba> getListaOsob() { return listaOsob; }

    public void dodajOsobe(Osoba nowaOsoba) {
        this.listaOsob.add(nowaOsoba);
    }

    public void usunOsobe(Osoba osobaDoUsuniecia) {
        this.listaOsob.remove(osobaDoUsuniecia);
    }

    @Override
    public String toString() {
        return "Zadanie [nazwa=" + getTytul() + ", data=" + getData() + ", link=" + getLink() + ", uczestnicy="
                + listaOsob.size() + "]";
    }

    // Implementacja Comparable
    @Override
    public int compareTo(Zdarzenie inneZdarzenie) {
        return this.data.compareTo(inneZdarzenie.getData());
    }
}