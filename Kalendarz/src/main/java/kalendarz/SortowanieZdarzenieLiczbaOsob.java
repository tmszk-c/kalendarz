
package kalendarz;
import java.util.Comparator;

// Klasa zewnętrzna (Comparator) służąca do niestandardowego sortowania.
// Porównuje zdarzenia na podstawie tego, ile osób bierze w nich udział.
public class SortowanieZdarzenieLiczbaOsob implements Comparator<Zdarzenie> {

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