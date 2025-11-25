package org.kalendarz;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    // Metoda do wyświetlania manu
    public static void wyswietlMenu() {
        System.out.println("\n--- MENU GŁÓWNE KALENDARZA ---");
        // --- WYŚWIETLANIE I SORTOWANIE ---
        System.out.println("1. Wyświetl Osoby (wg nazwiska)");
        System.out.println("2. Wyświetl Zdarzenia (wg daty)");
        System.out.println("3. Pokaż Zdarzenia wg popularności (Comparator)");

        // --- DODAWANIE INTERAKTYWNE ---
        System.out.println("4. DODAJ NOWĄ OSOBĘ");
        System.out.println("5. DODAJ NOWE ZDARZENIE");

        // --- TRWAŁOŚĆ DANYCH ---
        System.out.println("6. Zapisz wszystkie dane do XML");
        System.out.println("7. Wczytaj dane z XML");

        // --- WYJŚCIE ---
        System.out.println("0. Wyjdź z programu (Zapis i Zamknij)");
        System.out.print("Wybór: ");
    }

    public static void main(String[] args) {

        // xml - do operacji na plikach, scanner - do czytania klawiatury.
        ObslugaXML xml = new ObslugaXML();
        Scanner scanner = new Scanner(System.in);

        // Listy to nasza "baza danych w RAM".
        ArrayList<Osoba> listaOsob = new ArrayList<>();
        ArrayList<Zdarzenie> listaZdarzen = new ArrayList<>();

        // Próbujemy wczytać dane z dysku na starcie.
        listaOsob = xml.wczytajOsoby("kontakty.xml");
        listaZdarzen = xml.wczytajZdarzenia("zdarzenia.xml");


        // Jeśli plik nie istniał lub był pusty, lista będzie pusta.
        // Wtedy wchodzimy w ten if i ładujemy "Dane Startowe" (Hard-coding),
        if (listaOsob.isEmpty()) {
            System.out.println("Brak zapisanych danych XML.");

            // 1. Wprowadzanie danych Osoba
            listaOsob.add(new Osoba("Anna Zielińska", "+48501101101", "anna.z@example.com"));
            listaOsob.add(new Osoba("Marek Kowalski", "+48602202202", "marek.k@example.com"));
            listaOsob.add(new Osoba("Piotr Nowak", "+48721303303", "piotr.n@example.com"));
            listaOsob.add(new Osoba("Ewa Wójcik", "+48785404404", "ewa.w@example.com"));
            listaOsob.add(new Osoba("Krzysztof Jankowski", "+48880505505", "krzysztof.j@example.com"));
            listaOsob.add(new Osoba("Zofia Adamska", "+48512606606", "zofia.a@example.com"));
            listaOsob.add(new Osoba("Jan Bąk", "+48660707707", "jan.b@example.com"));
            listaOsob.add(new Osoba("Karolina Czarnecka", "+48888808808", "karo.c@example.com"));
            listaOsob.add(new Osoba("Tomasz Dąbrowski", "+48505909909", "tomek.d@example.com"));
            listaOsob.add(new Osoba("Alicja Majewska", "+48690000000", "alicja.m@example.com"));

            // 2. Wprowadzanie Zdarzeń
            // Blok try-catch jest wymagany, bo konstruktor URL może rzucić wyjątek MalformedURLException
            // (np. gdybyśmy wpisali "httttpp://cośtam").
            try {
                listaZdarzen.add(new Zdarzenie("Spotkanie Zarządu", LocalDateTime.of(2025, 11, 10, 10, 0), new URL("http://meetings.corp.com/zarzad")));
                listaZdarzen.add(new Zdarzenie("Obiad z Inwestorem", LocalDateTime.of(2025, 11, 15, 13, 30), new URL("http://restauracja.com/rezerwacja")));
                listaZdarzen.add(new Zdarzenie("Projekt Beta - Demo", LocalDateTime.of(2025, 11, 25, 14, 0), new URL("http://demo.projbeta.com/live")));
                listaZdarzen.add(new Zdarzenie("Szkolenie BHP i PPOŻ", LocalDateTime.of(2025, 12, 1, 9, 0), new URL("http://szkolenia.firma.com/bhp")));
                listaZdarzen.add(new Zdarzenie("Wigilia Firmowa", LocalDateTime.of(2025, 12, 20, 18, 0), new URL("http://wigilia.pl/info")));
                listaZdarzen.add(new Zdarzenie("Urodziny Szefa", LocalDateTime.of(2025, 12, 5, 19, 30), new URL("http://tort.com/lista")));
                listaZdarzen.add(new Zdarzenie("Kwartał Finansowy Q4", LocalDateTime.of(2025, 12, 30, 8, 0), new URL("http://finanse.pl/Q4")));
                listaZdarzen.add(new Zdarzenie("Wizyta Techników Serwisowych", LocalDateTime.of(2025, 11, 6, 16, 30), new URL("http://serwis.com/zgloszenie")));
                listaZdarzen.add(new Zdarzenie("Testowanie Nowej Apki", LocalDateTime.of(2025, 11, 28, 11, 0), new URL("http://testy.dev/platforma")));
                listaZdarzen.add(new Zdarzenie("Urlop Planowany", LocalDateTime.of(2026, 1, 1, 12, 0), new URL("http://urlopy.com/status")));
            } catch (MalformedURLException e) {
                System.err.println("Błąd inicjalizacji URL: " + e.getMessage());
            }

            // 3. Tworzenie relacji (tylko raz, przy pierwszym uruchomieniu!)
            listaZdarzen.get(0).dodajOsobe(listaOsob.get(0));
            listaOsob.get(0).dodajZadanie(listaZdarzen.get(0));
            listaZdarzen.get(0).dodajOsobe(listaOsob.get(1));
            listaOsob.get(1).dodajZadanie(listaZdarzen.get(0));
            listaZdarzen.get(1).dodajOsobe(listaOsob.get(2));
            listaOsob.get(2).dodajZadanie(listaZdarzen.get(1));
            listaZdarzen.get(1).dodajOsobe(listaOsob.get(3));
            listaOsob.get(3).dodajZadanie(listaZdarzen.get(1));

        } else {
            System.out.println("Pomyślnie wczytano dane z plików XML. Uruchamiam menu.");
        }

        // --- FAZA INTERAKTYWNA (GŁÓWNA PĘTLA) ---
        int wybor;

        while (true) {
            wyswietlMenu();
            //Sprawdzamy, czy użytkownik wpisał liczbę (zabezpieczenie przed wpisaniem "abc").
            if (scanner.hasNextInt()) {
                wybor = scanner.nextInt();
                scanner.nextLine();
                // Nowoczesny Switch (Java 14+) - strzałki -> są czytelniejsze niż dwukropki i break.
                switch (wybor) {
                    case 1 -> {
                        // sort(null) oznacza: użyj naturalnego porządku zdefiniowanego w klasie Osoba
                        // (metoda compareTo interfejsu Comparable - pewnie po nazwisku).
                        listaOsob.sort(null);
                        System.out.println("\n--- KONTAKTY (WG NAZWISKA) ---");
                        // Method reference: System.out::println to skrót od x -> System.out.println(x)
                        listaOsob.forEach(System.out::println);
                    }
                    case 2 -> {
                        // sort(null) używa compareTo z klasy Zdarzenie
                        listaZdarzen.sort(null); // Sortowanie domyślne (Comparable - wg daty)
                        System.out.println("\n--- ZDARZENIA (WG DATY) ---");
                        listaZdarzen.forEach(System.out::println);
                    }
                    case 3 -> {
                        // Tutaj podajemy WŁASNY komparator.
                        // Nie używamy domyślnego sortowania po dacie, ale innej logiki (liczba uczestników).
                        listaZdarzen.sort(new SortowanieZdarzenieLiczbaOsob());
                        System.out.println("\n--- ZDARZENIA (WG POPULARNOŚCI) ---");
                        listaZdarzen.forEach(System.out::println);
                    }
                    case 4 -> {
                        // Logika dodawania osoby z pętlami walidacyjnymi while(!poprawny...).
                        // Użytkownik nie wyjdzie z pętli, dopóki nie poda poprawnego formatu (try-catch wyłapie błędy).
                        String email = "";
                        String telefon = "";
                        System.out.println("\n--- DODAJ NOWĄ OSOBĘ ---");
                        System.out.print("Wprowadź Imię i Nazwisko: ");
                        String nazwa = scanner.nextLine();
                        boolean poprawnyNumer = false;
                        while(!poprawnyNumer) {
                            try {
                                System.out.print("Wprowadź Numer Telefonu (+XX...): ");
                                telefon = scanner.nextLine();
                                poprawnyNumer = true;
                            } catch (Exception e) {
                                System.out.println("Niepoprawny numer telefonu");
                            }
                        }
                        boolean poprawnyMail = false;
                        while(!poprawnyMail) {
                            try {
                                System.out.print("Wprowadź Adres Email: ");
                                email = scanner.nextLine();
                                poprawnyMail = true;
                            } catch (Exception e) {
                                System.out.println("Niepoprawny adres email");
                            }
                        }
                        Osoba nowaOsoba = new Osoba(nazwa, telefon, email);
                        listaOsob.add(nowaOsoba);

                    }
                    // Dodawanie zdarzenia. Trzeba parsować String na LocalDateTime.
                    case 5 ->  {
                        LocalDateTime dataCzas = null;
                        URL link = null;
                        System.out.println("\n--- DODAJ ZDARZENIE ---");
                        System.out.print("Podaj nazwę: ");
                        String nazwa = scanner.nextLine();
                        boolean poprawnaData = false;
                        while (!poprawnaData) {
                            try {
                                // Parsowanie daty: LocalDate.parse wymaga formatu RRRR-MM-DD (standard ISO).
                                // Jeśli użytkownik wpisze "01.01.2025", poleci wyjątek DateTimeParseException.
                                System.out.print("Wprowadź datę (format: RRRR-MM-DD): ");
                                String dataStr = scanner.nextLine();

                                System.out.print("Wprowadź czas (format: HH:MM): ");
                                String czasStr = scanner.nextLine();

                                LocalDate data = LocalDate.parse(dataStr);
                                LocalTime czas = LocalTime.parse(czasStr);

                                // Łączenie daty i czasu
                                dataCzas = LocalDateTime.of(data, czas);
                                poprawnaData = true;
                            } catch (DateTimeParseException e) {
                                System.err.println("BŁĄD WEJŚCIA: Zły format daty/czasu. Spróbuj ponownie.");
                            }
                        }
                        boolean poprawnyLink = false;
                        while (!poprawnyLink) {
                            try {
                                System.out.print("Podaj link: ");
                                String email = scanner.nextLine();
                                link = new URL(email);
                                poprawnyLink = true;
                            } catch (MalformedURLException e) {
                                System.err.println("BŁĄD WEJŚCIA: Zły format URL. Spróbuj ponownie.");
                            }
                        }

                            Zdarzenie noweZdarzenie = new Zdarzenie(nazwa, dataCzas, link);
                            listaZdarzen.add(noweZdarzenie);

                    }
                    case 6 ->{
                        // Ręczny zapis. Przekazujemy aktualny stan list do metody zapisz...
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        System.out.println("Zapis zakończony.");
                    }
                    case 7 -> {
                        // Ręczny odczyt. Nadpisujemy zmienne listaOsob nowymi danymi z pliku.
                        listaOsob = xml.wczytajOsoby("kontakty.xml");
                        listaZdarzen = xml.wczytajZdarzenia("zdarzenia.xml");
                        System.out.println("Wczytano dane z plików XML.");
                    }
                    case 0 -> {
                        // Autosave przy wyjściu
                        System.out.println("Zapisuję zmiany i kończę pracę.");
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        scanner.close();
                        return; // Wyjście z metody main, koniec programu
                    }
                    default ->{
                        System.out.println("Nieznana opcja. Spróbuj ponownie, wybierając liczbę z menu (0-5).");
                    }
                }
            } else {
                // Jeśli użytkownik wpisał tekst zamiast liczby w menu:
                System.out.println("Nieprawidłowe wejście. Wprowadź liczbę.");
                scanner.nextLine(); // Czyścimy bufor skanera z błędnych danych, żeby nie było pętli nieskończonej.
            }
        }
    }
}