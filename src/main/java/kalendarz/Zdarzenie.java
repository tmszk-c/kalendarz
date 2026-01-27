package kalendarz;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Klasa reprezentująca zdarzenie w kalendarzu.
 * Implementuje interfejs Comparable do sortowania chronologicznego.
 */
public class Zdarzenie implements Comparable<Zdarzenie> {
    /** Unikalny identyfikator zdarzenia. */
    private int id;
    /** Tytuł lub krótki opis wydarzenia. */
    private String tytul;
    /** Data i godzina zaplanowanego zdarzenia. */
    private LocalDateTime data;
    /** Opcjonalny odnośnik URL powiązany ze zdarzeniem. */
    private URL link;

    /**
     * Lista uczestników przypisanych do tego zdarzenia.
     * Adnotacja JsonIgnore zapobiega nieskończonej rekurencji podczas zapisu XML.
     */
    @JsonIgnore
    private ArrayList<Osoba> listaOsob;

    /**
     * Konstruktor bezparametrowy wymagany przez bibliotekę Jackson.
     */
    public Zdarzenie() {
        this.listaOsob = new ArrayList<>();
    }

    /**
     * Konstruktor tworzący nowe zdarzenie z podstawowymi parametrami.
     * @param tytul Nazwa wydarzenia.
     * @param data Data i godzina wydarzenia.
     * @param link Adres URL (może być null).
     */
    public Zdarzenie(String tytul, LocalDateTime data, URL link) {
        super();
        this.tytul = tytul;
        this.data = data;
        this.link = link;
        this.listaOsob = new ArrayList<>();
    }

    /** @return Tytuł zdarzenia. */
    public String getTytul() { return tytul; }
    /** @param tytul Nowy tytuł zdarzenia. */
    public void setTytul(String tytul) { this.tytul = tytul; }
    /** @return Obiekt LocalDateTime reprezentujący czas zdarzenia. */
    public LocalDateTime getData() { return data; }
    /** @param data Nowa data i czas zdarzenia. */
    public void setData(LocalDateTime data) { this.data = data; }
    /** @return Obiekt URL z linkiem do zdarzenia. */
    public URL getLink() { return link; }
    /** @param link Nowy obiekt URL. */
    public void setLink(URL link) { this.link = link; }
    /** @return Lista osób biorących udział w zdarzeniu. */
    public ArrayList<Osoba> getListaOsob() { return listaOsob; }

    /**
     * Dodaje osobę do listy uczestników zdarzenia.
     * @param nowaOsoba Obiekt osoby do dodania.
     */
    public void dodajOsobe(Osoba nowaOsoba) {
        this.listaOsob.add(nowaOsoba);
    }

    /**
     * Usuwa osobę z listy uczestników.
     * @param osobaDoUsuniecia Obiekt osoby do usunięcia.
     */
    public void usunOsobe(Osoba osobaDoUsuniecia) {
        this.listaOsob.remove(osobaDoUsuniecia);
    }

    /** @return ID zdarzenia z bazy danych. */
    public int getId() { return id; }
    /** @param id Nowe ID zdarzenia. */
    public void setId(int id) { this.id = id; }

    /**
     * Zwraca tekstową reprezentację zdarzenia wraz z sformatowaną datą.
     * @return Sformatowany ciąg znaków z opisem zdarzenia.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return "Zadanie [Nazwa: " + getTytul() +
                ", Data: " + getData().format(formatter) +
                ", Link: " + getLink() +
                ", Uczestnicy: " + listaOsob.size() + "]";
    }

    /**
     * Porównuje dwa zdarzenia chronologicznie.
     * @param inneZdarzenie Obiekt do porównania.
     * @return Wartość ujemna, zero lub dodatnia.
     */
    @Override
    public int compareTo(Zdarzenie inneZdarzenie) {
        return this.data.compareTo(inneZdarzenie.getData());
    }
}