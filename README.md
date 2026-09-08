# B&E Messenger — v0.1.0

Pierwszy, kompilowalny prototyp aplikacji Android.

## Co działa w v0.1

- ekran logowania,
- przejście do listy rozmów,
- prywatna rozmowa 1:1 jako model UI,
- wysyłanie wiadomości w trybie lokalnego demo,
- wylogowanie,
- przygotowany `codemagic.yaml`,
- architektura gotowa do późniejszego podłączenia backendu OVH.

## Ważne

Ta wersja NIE łączy się jeszcze z serwerem OVH i nie jest jeszcze prawdziwym komunikatorem sieciowym.
To celowy etap 0.1: najpierw sprawdzamy GitHub → Codemagic → APK.

## Build lokalny / Codemagic

W Codemagic wybierz repozytorium GitHub i workflow `android-debug`.
Artefaktem będzie:

`app/build/outputs/apk/debug/app-debug.apk`

## Następny etap

Po potwierdzeniu, że APK się buduje, podłączymy backend:
- własny VPS OVH,
- API / Matrix,
- prawdziwe konta użytkowników,
- sesję logowania,
- prywatne rozmowy 1:1,
- komunikację Android ↔ WWW.
