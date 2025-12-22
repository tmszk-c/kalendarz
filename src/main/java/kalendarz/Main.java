package kalendarz;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final int OPCJA_WYSWIETL_OSOBY = 1;
    private static final int OPCJA_WYSWIETL_ZDARZENIA = 2;
    private static final int OPCJA_ZDARZENIA_POPULARNOSC = 3;
    private static final int OPCJA_DODAJ_OSOBE = 4;
    private static final int OPCJA_DODAJ_ZDARZENIE = 5;
    private static final int OPCJA_USUN_OSOBE = 6;
    private static final int OPCJA_USUN_ZDARZENIE = 7;
    private static final int OPCJA_ZAPISZ_XML = 8;
    private static final int OPCJA_WCZYTAJ_XML = 9;
    private static final int OPCJA_WYCZYSC_RAM = 10;
    private static final int OPCJA_POLACZ_RELACJE = 11;
    private static final int OPCJA_WYJSCIE = 0;

    private static int licznikAkcji = 0;
    private static final int LIMIT_AKCJI_DO_ZAPISU = 10;

    public static void wyswietlMenu() {
        System.out.println("\n--- MENU GŁÓWNE KALENDARZA ---");
        System.out.println(OPCJA_WYSWIETL_OSOBY + ". Wyświetl Osoby");
        System.out.println(OPCJA_WYSWIETL_ZDARZENIA + ". Wyświetl Zdarzenia");
        System.out.println(OPCJA_ZDARZENIA_POPULARNOSC + ". Zdarzenia wg popularności");
        System.out.println("--- EDYCJA ---");
        System.out.println(OPCJA_DODAJ_OSOBE + ". Dodaj Osobę");
        System.out.println(OPCJA_DODAJ_ZDARZENIE + ". Dodaj Zdarzenie");
        System.out.println(OPCJA_USUN_OSOBE + ". Usuń Osobę");
        System.out.println(OPCJA_USUN_ZDARZENIE + ". Usuń Zdarzenie");
        System.out.println("--- DANE ---");
        System.out.println(OPCJA_ZAPISZ_XML + ". Zapisz do XML");
        System.out.println(OPCJA_WCZYTAJ_XML + ". Wczytaj z XML");
        System.out.println(OPCJA_WYCZYSC_RAM + ". Wyczyść RAM");
        System.out.println(OPCJA_POLACZ_RELACJE + ". Połącz Osobę ze Zdarzeniem");
        System.out.println("--- KONIEC ---");
        System.out.println(OPCJA_WYJSCIE + ". Wyjdź");
        System.out.print("Wybór: ");
    }

    public static void main(String[] args) {
        ObslugaXML xml = new ObslugaXML();
        ObslugaBazy baza = new ObslugaBazy();
        Scanner scanner = new Scanner(System.in);

        //testPolaczenia(baza);

        ArrayList<Osoba> listaOsob = new ArrayList<>();
        ArrayList<Zdarzenie> listaZdarzen = new ArrayList<>();

     // Próba wczytania z bazy
        System.out.println("Inicjalizacja danych...");
        boolean sukcesBaza = baza.wczytajDane(listaOsob, listaZdarzen);

        if (!sukcesBaza) {
            System.out.println("[INFO] Próba awaryjnego wczytania danych z plików XML...");
            
            ArrayList<Osoba> zXmlOsoby = xml.wczytajOsoby("kontakty.xml");
            ArrayList<Zdarzenie> zXmlZdarzenia = xml.wczytajZdarzenia("zdarzenia.xml");

            if (!zXmlOsoby.isEmpty() || !zXmlZdarzenia.isEmpty()) {
                listaOsob.addAll(zXmlOsoby);
                listaZdarzen.addAll(zXmlZdarzenia);
                System.out.println("[SUKCES] Wczytano dane z plików XML.");
            } else {
                System.out.println("[INFO] Pliki XML są puste lub nie istnieją. Rozpoczynanie z pustą listą.");
            }
        }

        int wybor;
        while (true) {
            wyswietlMenu();

            if (scanner.hasNextInt()) {
                wybor = scanner.nextInt();
                scanner.nextLine();

                switch (wybor) {
                    case OPCJA_WYSWIETL_OSOBY -> {
                        listaOsob.sort(null);
                        wyswietlListe(listaOsob, "KONTAKTY");
                    }
                    case OPCJA_WYSWIETL_ZDARZENIA -> {
                        listaZdarzen.sort(null);
                        wyswietlListe(listaZdarzen, "ZDARZENIA");
                    }
                    case OPCJA_ZDARZENIA_POPULARNOSC -> {
                        listaZdarzen.sort(new SortowanieZdarzenieLiczbaOsob());
                        wyswietlListe(listaZdarzen, "ZDARZENIA (POPULARNOŚĆ)");
                    }
                    case OPCJA_DODAJ_OSOBE -> {
                        System.out.println("\n--- DODAJ OSOBĘ ---");
                        String nazwa = wczytajNiepustyTekst(scanner, "Imię i Nazwisko");
                        System.out.print("Telefon: ");
                        String telefon = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        try {
                            listaOsob.add(new Osoba(nazwa, telefon, email));
                            System.out.println("SUKCES: Dodano osobę.");

                            xml.zapiszOsoby(listaOsob, "kontakty.xml");
                            obsluzAutoZapis(baza, listaOsob, listaZdarzen);

                        } catch (IllegalArgumentException e) {
                            System.err.println("Błąd: " + e.getMessage());
                        }
                    }
                    case OPCJA_DODAJ_ZDARZENIE -> {
                        System.out.println("\n--- DODAJ ZDARZENIE ---");
                        String nazwa = wczytajNiepustyTekst(scanner, "Nazwa zdarzenia");
                        LocalDateTime dataCzas = wczytajDateICzas(scanner);
                        URL link = wczytajURL(scanner);

                        listaZdarzen.add(new Zdarzenie(nazwa, dataCzas, link));
                        System.out.println("SUKCES: Dodano zdarzenie.");

                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                    }
                    case OPCJA_USUN_OSOBE -> {
                        if (listaOsob.isEmpty()) {
                            System.out.println("Brak osób do usunięcia.");
                        } else {
                            wyswietlListe(listaOsob, "Wybierz ID do usunięcia");
                            int idx = wczytajIndeks(scanner);

                            if (idx >= 0 && idx < listaOsob.size()) {
                                Osoba usuwana = listaOsob.get(idx);
                                for (Zdarzenie z : listaZdarzen) z.usunOsobe(usuwana); // Czyść relacje
                                listaOsob.remove(idx);
                                System.out.println("Usunięto: " + usuwana.getNazwa());

                                xml.zapiszOsoby(listaOsob, "kontakty.xml");
                                xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                                obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                            } else {
                                System.out.println("Błędny indeks.");
                            }
                        }
                    }
                    case OPCJA_USUN_ZDARZENIE -> {
                        if (listaZdarzen.isEmpty()) {
                            System.out.println("Brak zdarzeń do usunięcia.");
                        } else {
                            wyswietlListe(listaZdarzen, "Wybierz ID do usunięcia");
                            int idx = wczytajIndeks(scanner);

                            if (idx >= 0 && idx < listaZdarzen.size()) {
                                Zdarzenie usuwane = listaZdarzen.get(idx);
                                for (Osoba o : listaOsob) o.usunZadanie(usuwane); // Czyść relacje
                                listaZdarzen.remove(idx);
                                System.out.println("Usunięto: " + usuwane.getTytul());

                                xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                                xml.zapiszOsoby(listaOsob, "kontakty.xml");
                                obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                            } else {
                                System.out.println("Błędny indeks.");
                            }
                        }
                    }
                    case OPCJA_ZAPISZ_XML -> {
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        System.out.println("Zapisano XML.");
                    }
                    case OPCJA_WCZYTAJ_XML -> {
                        listaOsob = xml.wczytajOsoby("kontakty.xml");
                        listaZdarzen = xml.wczytajZdarzenia("zdarzenia.xml");
                        System.out.println("Wczytano XML.");
                    }
                    case OPCJA_WYCZYSC_RAM -> {
                        listaOsob.clear();
                        listaZdarzen.clear();
                        System.out.println("Wyczyszczono RAM.");
                    }
                    case OPCJA_POLACZ_RELACJE -> {
                        if (listaOsob.isEmpty() || listaZdarzen.isEmpty()) {
                            System.out.println("Za mało danych.");
                        } else {
                            wyswietlListe(listaOsob, "Wybierz Osobę");
                            int io = wczytajIndeks(scanner);
                            wyswietlListe(listaZdarzen, "Wybierz Zdarzenie");
                            int iz = wczytajIndeks(scanner);

                            if (io >= 0 && io < listaOsob.size() && iz >= 0 && iz < listaZdarzen.size()) {
                                Osoba o = listaOsob.get(io);
                                Zdarzenie z = listaZdarzen.get(iz);
                                o.dodajZadanie(z);
                                z.dodajOsobe(o);
                                System.out.println("Połączono: " + o.getNazwa() + " <-> " + z.getTytul());
                            } else {
                                System.out.println("Błędne indeksy.");
                            }
                        }
                    }
                    case OPCJA_WYJSCIE -> {
                        System.out.println("Zamykanie...");
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        baza.zapiszDane(listaOsob, listaZdarzen);
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("Nieznana opcja.");
                }
            } else {
                System.out.println("Wprowadź liczbę!");
                scanner.nextLine();
            }
        }
    }

    // --- METODY POMOCNICZE  ---

    // Metoda obsługująca auto-zapis
    private static void obsluzAutoZapis(ObslugaBazy baza, ArrayList<Osoba> osoby, ArrayList<Zdarzenie> zdarzenia) {
        licznikAkcji++;
        if (licznikAkcji >= LIMIT_AKCJI_DO_ZAPISU) {
            System.out.println("[AUTO-SAVE] Osiągnięto limit zmian. Zapisuję do Bazy...");
            baza.zapiszDane(osoby, zdarzenia);
            licznikAkcji = 0;
        }
    }

    private static void wyswietlListe(ArrayList<?> lista, String tytul) {
        System.out.println("\n--- " + tytul + " ---");
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("[" + i + "] " + lista.get(i));
        }
    }

    // Proste metody wczytujące, żeby nie śmiecić pętlami w main
    private static String wczytajNiepustyTekst(Scanner sc, String komunikat) {
        String tekst = "";
        while (tekst.trim().isEmpty()) {
            System.out.print(komunikat + ": ");
            tekst = sc.nextLine();
        }
        return tekst;
    }

    private static int wczytajIndeks(Scanner sc) {
        System.out.print("Podaj ID: ");
        if (sc.hasNextInt()) {
            int i = sc.nextInt();
            sc.nextLine();
            return i;
        }
        sc.nextLine();
        return -1;
    }

    private static LocalDateTime wczytajDateICzas(Scanner sc) {
        while (true) {
            try {
                System.out.print("Data (RRRR-MM-DD): ");
                LocalDate d = LocalDate.parse(sc.nextLine());
                System.out.print("Czas (HH:MM): ");
                LocalTime t = LocalTime.parse(sc.nextLine());
                return LocalDateTime.of(d, t);
            } catch (DateTimeParseException e) {
                System.out.println("Błąd formatu daty/czasu. Spróbuj ponownie.");
            }
        }
    }

    private static URL wczytajURL(Scanner sc) {
        while (true) {
            try {
                System.out.print("Link (http...): ");
                return new URL(sc.nextLine());
            } catch (MalformedURLException e) {
                System.out.println("Zły format URL.");
            }
        }
    }
}