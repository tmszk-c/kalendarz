package org.kalendarz;

import java.util.ArrayList;

import org.apache.commons.validator.routines.EmailValidator;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class Osoba implements Comparable<Osoba> {

    private String nazwa;
    private String numer;
    private String email;
    private ArrayList<Zdarzenie> listaZadan;

    // Konstruktor bezparametrowy
    // WYMAGANY przez bibliotekę Jackson do wczytywania XML (tworzy pusty obiekt przed wypełnieniem)
    public Osoba() {
        this.listaZadan = new ArrayList<>();
    }

    // Konstruktor z logiką walidacji
    public Osoba(String nazwa, String numer, String email) {
        super();
        this.nazwa = nazwa;

        // Walidacja telefonu (biblioteka Google libphonenumber)
        // Sprawdza format międzynarodowy i zamienia na standard E164 (+48...)
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber numerTestowy = phoneUtil.parse(numer, "ZZ");
            if (phoneUtil.isValidNumber(numerTestowy)) {
                this.numer = phoneUtil.format(numerTestowy, PhoneNumberUtil.PhoneNumberFormat.E164);
            } else {
                throw new IllegalArgumentException("Numer jest nieprawidłowy");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Błąd formatu numeru", e);
        }

        // Walidacja Email (biblioteka Apache Commons)
        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Niepoprawny format email: " + email);
        }

        this.listaZadan = new ArrayList<>();
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getNumer() {
        return numer;
    }

    public void setNumer(String numer) {
        this.numer = numer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Zdarzenie> getListaZadan() {
        return listaZadan;
    }

    public void dodajZadanie(Zdarzenie noweZdarzenie) {
        this.listaZadan.add(noweZdarzenie);
    }

    public void usunZadanie(Zdarzenie zadanieDoUsuniecia) {
        this.listaZadan.remove(zadanieDoUsuniecia);
    }

    @Override
    public String toString() {
        return "Osoba [nazwa=" + getNazwa() + ", numer=" + getNumer() + ", email=" + getEmail() + ", liczbaZadan="
                + listaZadan.size() + "]";
    }

    // Implementacja Comparable (sortowanie domyślne)
    // Logika: wycina nazwisko po spacji i porównuje alfabetycznie
    @Override
    public int compareTo(Osoba innaOsoba) {
        int spacja = this.nazwa.indexOf(" ");
        String nazwisko = (spacja == -1) ? this.nazwa : this.nazwa.substring(spacja + 1);

        String innaNazwa = innaOsoba.getNazwa();
        int innaSpacja = innaNazwa.indexOf(" ");
        String inneNazwisko = (innaSpacja == -1) ? innaNazwa : innaNazwa.substring(innaSpacja + 1);

        return nazwisko.compareTo(inneNazwisko);
    }
}