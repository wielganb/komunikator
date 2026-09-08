# B&E Messenger — v0.3.0

Nowoczesny demonstracyjny klient Androida. Nadal bez backendu — celem tej wersji jest dopracowanie UX/UI i przygotowanie interfejsu pod późniejsze połączenie z prywatnym serwerem OVH.

## v0.3 — co działa

- nowy interfejs rozmów,
- karty rozmów 1:1,
- nagłówek rozmowy z użytkownikiem,
- czytelne dymki wiadomości,
- rozróżnienie nadawcy,
- emoji picker,
- wybór zdjęcia/pliku przez Android Storage Access Framework,
- podgląd wybranego załącznika,
- demonstracyjne dodawanie załącznika do wiadomości,
- wieloliniowe pole wiadomości,
- przewijana historia rozmowy,
- wylogowanie,
- brak komunikacji sieciowej — celowo.

## E2EE

W v0.3 nie udajemy, że mamy prawdziwe E2EE: bez dwóch klientów i serwera nie ma jeszcze pełnego kanału end-to-end.

Docelowo szyfrowanie wdrożymy na warstwie Matrix/Olm/Megolm. Nie będziemy projektować własnego protokołu kryptograficznego.

## Codemagic

Workflow `android-debug` buduje:

`app/build/outputs/apk/debug/app-debug.apk`

Workflow `android-release` buduje:

`app/build/outputs/apk/release/app-release-unsigned.apk`

## Następny etap — v0.4

Po zaakceptowaniu wyglądu:
- backend na OVH,
- HTTPS,
- konta użytkowników,
- sesje Android/Web,
- prywatne pokoje 1:1,
- prawdziwe wiadomości,
- upload mediów,
- E2EE.
