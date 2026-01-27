package kalendarz;

import java.util.ArrayList;
import org.apache.commons.validator.routines.EmailValidator;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Klasa reprezentująca osobę (kontakt) w systemie.
 * Zawiera mechanizmy walidacji numeru telefonu oraz adresu e-mail.
 */
public class Osoba implements Comparable<Osoba> {
    /** ID osoby. */
    private int id;
    /** Pełne imię i nazwisko. */
    private String nazwa;
    /** Zweryfikowany numer telefonu. */
    private String numer;
    /** Zweryfikowany adres e-mail. */
    private String email;
    /** Lista zadań przypisanych do tej osoby. */
    private ArrayList<Zdarzenie> listaZadan;

    /** Konstruktor bezparametrowy wymagany do deserializacji XML. */
    public Osoba() {
        this.listaZadan = new ArrayList<>();
    }

    /**
     * Tworzy nową osobę z jednoczesną walidacją danych kontaktowych.
     * @param nazwa Imię i nazwisko.
     * @param numer Numer telefonu (podlega walidacji Google libphonenumber).
     * @param email Adres e-mail (podlega walidacji Apache Commons).
     * @throws IllegalArgumentException Jeśli dane są niepoprawne.
     */
    public Osoba(String nazwa, String numer, String email) {
        super();
        this.nazwa = nazwa;

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

        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Niepoprawny format email: " + email);
        }

        this.listaZadan = new ArrayList<>();
    }

    /** @return Nazwa osoby. */
    public String getNazwa() { return nazwa; }
    /** @param nazwa Nowa nazwa osoby. */
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    /** @return Numer telefonu. */
    public String getNumer() { return numer; }
    /** @param numer Nowy numer telefonu. */
    public void setNumer(String numer) { this.numer = numer; }
    /** @return Adres email. */
    public String getEmail() { return email; }
    /** @param email Nowy adres email. */
    public void setEmail(String email) { this.email = email; }
    /** @return Lista zdarzeń przypisanych do osoby. */
    public ArrayList<Zdarzenie> getListaZadan() { return listaZadan; }

    /**
     * Przypisuje nowe zdarzenie do osoby.
     * @param noweZdarzenie Zdarzenie do dodania.
     */
    public void dodajZadanie(Zdarzenie noweZdarzenie) {
        this.listaZadan.add(noweZdarzenie);
    }

    /**
     * Usuwa zdarzenie z listy osoby.
     * @param zadanieDoUsuniecia Zdarzenie do usunięcia.
     */
    public void usunZadanie(Zdarzenie zadanieDoUsuniecia) {
        this.listaZadan.remove(zadanieDoUsuniecia);
    }

    /** @return ID z bazy danych. */
    public int getId() { return id; }
    /** @param id Nowe ID. */
    public void setId(int id) { this.id = id; }

    /** @return Tekstowy opis osoby. */
    @Override
    public String toString() {
        return "Osoba [Imię i nazwisko: " + getNazwa() + ", Numer tel: " + getNumer() + ", Email: " + getEmail() + ", Liczba zadań: "
                + listaZadan.size() + "]";
    }

    /**
     * Sortuje osoby alfabetycznie według nazwiska.
     * @param innaOsoba Obiekt do porównania.
     * @return Wynik porównania nazwisk.
     */
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