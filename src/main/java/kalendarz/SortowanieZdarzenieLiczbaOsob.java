
package kalendarz;
import java.util.Comparator;

/**
 * Komparator służący do sortowania zdarzeń według ich popularności.
 * Popularność jest definiowana jako liczba osób przypisanych do danego zdarzenia.
 */
public class SortowanieZdarzenieLiczbaOsob implements Comparator<Zdarzenie> {

    /**
     * Porównuje dwa zdarzenia na podstawie rozmiaru ich listy osób.
     * @param z1 Pierwsze zdarzenie do porównania.
     * @param z2 Drugie zdarzenie do porównania.
     * @return Wartość ujemna, zero lub dodatnia, zależnie od wyniku porównania liczby uczestników.
     */
    @Override
    public int compare(Zdarzenie z1, Zdarzenie z2) {
        // Pobieramy liczbę uczestników pierwszego zdarzenia
        int liczba1 = z1.getListaOsob().size();

        // Pobieramy liczbę uczestników drugiego zdarzenia
        int liczba2 = z2.getListaOsob().size();

        // Porównujemy te liczby (Integer.compare to bezpieczna metoda w Javie).
        // Wynik: Zdarzenia z mniejszą liczbą osób będą wyżej na liście.
        return Integer.compare(liczba1, liczba2);
    }
}