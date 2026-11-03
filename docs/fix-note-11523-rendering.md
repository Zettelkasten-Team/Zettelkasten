# Scope `fix-note-11523-rendering`

Ziel: Die Formatierungswarnung für Zettel 11523 mit dem bereitgestellten
Original reproduzieren und den verursachenden Renderingschritt korrigieren.
Änderungen beschränken sich auf Renderer, Regressionstests und diesen Bericht.
Keine Änderung an persönlichen Daten, Einstellungen, Validator, Suchansicht
oder Quit-Pfad. Es wird keine Markdown-Syntax eingeführt; die optionale
Constrained-Markdown-Betriebsart ist hier nicht implementiert.

Basis: `main` auf `0f7d9d58635d7c7f2360bbcebc37c5b59cf5af41`,
synchron mit `origin/main`. `AGENTS.md` und der bisherige Bericht wurden
gelesen. Der Working Tree enthielt bereits diesen unversionierten Bericht
sowie zwei unversionierte Dateien aus dem Quit-Scope, die unverändert blieben.

## Originalreproduktion

Die lokale Probe liest `zettel-11523-original.txt` als UTF-8 und legt den Text
in einer In-Memory-Instanz von `Daten` ab. Das Anlegen über `addEntry()`
überführt Zeilenumbrüche wie üblich in `[br]`; die so abgelegte Eingabe
bleibt vor und nach beiden Renderaufrufen identisch. Es wurde keine
persönliche Zettelkastendatenbank geöffnet. Aus der gespeicherten Konfiguration
wurde der Markdown-Schalter ermittelt: `markdownactivated=1`. Die Probe
setzt den Schalter in einer eigenen Konfiguration und prüft auch die
Gegenprobe mit deaktiviertem Markdown.

Die Probe verwendet `Daten.getEntryAsHtml(..., FRAME_SEARCH)`, den
sanitisierten Renderer `HtmlUbbUtil.getEntryAsHTMLSanitized()` und für beide
Fassungen `HtmlValidator.isValidHTML()`. `SearchResultsFrame.java:1423–1454`
verwendet dieselben Rückgabewerte: normal anzeigen, bei Ablehnung den
Fallback versuchen und erst bei doppelter Ablehnung die Warnung anzeigen.
Die Entscheidung wurde ohne Suchhervorhebung und ohne Öffnen eines
Swing-Fensters nachvollzogen.

| Markdown | Normales HTML vorher | Fallback vorher | Normales HTML nachher | Fallback nachher |
| --- | --- | --- | --- | --- |
| An (gespeicherter Modus) | abgelehnt | abgelehnt | akzeptiert | akzeptiert |
| Aus | akzeptiert | akzeptiert | akzeptiert | akzeptiert |

Die maßgebliche Stelle beginnt in Zeile 247 der Originaldatei: Die
Markdown-Überschrift der zweiten Ebene steht unmittelbar vor
der nummerierten Liste in Zeilen 248–252. Das normale HTML hatte die Struktur
`<h3>…<ol><li>…</li>…</ol></h3>`. Swing meldete
`end.missing h3 ? ?` bei Position 24753 und `unmatched.endtag h3 ? ?`
bei Position 25252. Im Fallback waren es dieselben Diagnosen bei 24496
und 24995. Der Validator akzeptierte das rohe C/M-UBB-Nesting; der
Warnpfad wird durch das erzeugte HTML ausgelöst.

## Eingrenzung und Korrektur

Die Originalzeilen 247–248 allein reproduzieren beide Diagnosen. Mit
anonymisiertem und verkürztem Inhalt genügt:

```markdown
## H
1. A
```

Vorher: `<h3>H<ol><li>A</li></ol></h3>`. Ohne Überschrift, ohne Liste
oder mit einer Leerzeile zwischen beiden werden die Fassungen akzeptiert.
Auch die isolierten Originalabschnitte mit Pipe-Tabelle und wörtlichen
HTML-Tags in Inline-Code (Zeilen 13–22) sowie mit einfachen, doppelten und
dreifachen Backticks (Zeilen 70–72 und 208) lösen diese Ablehnung nicht aus.
Sanitization ändert zwar andere Teile der Projektion, erhält aber die
auslösende Zeilenfolge und benutzt dieselbe Markdown-Konvertierung.

Ursache in `HtmlUbbUtil.java:1500–1520` am Ausgangs-HEAD: Die Regex für
nummerierte und unnummerierte Listen konsumiert den vorangehenden
Zeilenumbruch. Die nachfolgende Überschriften-Regex erfasst mit `(.*)`
damit auch das erzeugte Listen-HTML. Swing schließt das `h3` implizit am
Listenbeginn und beanstandet das spätere explizite Ende.

`HtmlUbbUtil.java:1501–1505` erkennt nun Überschriften vor der
Listenkonvertierung, solange die Quellzeilen noch getrennt sind. Die
Listenregeln selbst bleiben gleich, ebenso das Zusammenführen benachbarter
Listeneinträge. Im vollständigen normalen Markdown-HTML der Originalprobe
ist die einzige Änderung die Position von `</h3>`, jetzt unmittelbar vor
`<ol>`. Tabellen, Links, Code und maskierte HTML-Beispiele bleiben in
dieser Projektion bytegleich. Der normale Suchpfad erreicht damit direkt
die Anzeige; der Fallback ist für den Originaltext nicht mehr nötig.

## Regression und Grenzen

Die lokale Originalprobe enthält eine Assertion auf die Anzeigeentscheidung:
vor der Änderung Exit 1 mit `Search results would show the formatting warning`,
danach Exit 0. Die zwei dauerhaften JUnit-Tests in
`HtmlUbbUtilMarkdownProjectionTest.java:93–105` prüfen das gültige HTML
für die minimale nummerierte Liste und für die ebenfalls betroffene
Aufzählung mit zwei Einträgen, Code und Fettdruck. Beide schlugen im
Vorher-Lauf an der HTML-Validierung fehl (11 Tests, 2 Fehlschläge, 0
übersprungen). Der vollständige Originaltext und private URLs sind kein
Repository-Fixture.

Der Validator prüft bestimmte Strukturdiagnosen des Swing-Parsers. Die
Reproduktion ist keine allgemeine HTML-Konformitätsprüfung, kein Test
aller Markdown-Dialekte. Die separate manuelle Sichtprüfung von Zettel 11523
wurde erfolgreich abgeschlossen.

### Konkreter synthetischer Fallback-Grenzfall

Eingabe:

```text
[h1][q]A[/q][/h1]
```

HTML-Struktur des normalen und bereinigten Inhalts:

```html
<h2><div class="zitat">A</div></h2>
```

`replaceUbbToHtml()` erzeugt die Überschrift bei `HtmlUbbUtil:1550–1556` und
das Zitat-`div` bei `1563`. Ein Block-`div` kann nicht als Inhalt dieser
Überschrift bestehen; Swing schliesst die Überschrift beim Blockbeginn.
Die anschliessende explizite Endmarke passt dann nicht mehr. Der Validator
meldet in der Probe:

```text
end.missing h2 ? ?
unmatched.endtag h2 ? ?
```

Warum der Fallback hier nicht hilft: Die Eingabe enthält weder C/M-Paare noch
Markdown-Bilder oder spitze Klammern. `sanitizeEntryContentForHtml()` gibt sie
unverändert zurück; auch der auf C/M begrenzte Normalizer verändert sie nicht.
Die Emphasis-/Anchor-Nachbehandlung repariert keine `h2`/`div`-Hierarchie.

Dies ist ein neutraler, tatsächlich ausgeführter Beleg für eine Warnung trotz
paarweise geschlossener Eingabetags. Es ist **kein Beleg**, dass Zettel 11523
diese Struktur enthält, und keine vollständige Aussage über zulässige UBB-
Blockverschachtelungen. Für diesen künstlichen Fall ist eine reale HTML-
Strukturbeschwerde sichtbar; ein Parser-False-Positive wurde damit nicht
nachgewiesen. Keine pauschale Parser-Ausnahme wurde eingeführt.

Probequelle: `target/note-11523-analysis/RenderProbe.java`.
Ergebnislog: `target/note-11523-analysis/structure-probe-final.log`.
Beide sind lokale Buildartefakte und nicht versioniert.

## Testnachweise

Unter Temurin 25.0.5 auf macOS, nach dem Fix:

| Lauf | Gemeldet | Übersprungen | Fehler / Fehlschläge |
| --- | ---: | ---: | ---: |
| `mvn '-Dtest=HtmlUbbUtil*Test,UbbNesting*Test' test` (JUnit) | 45 | 6 | 0 |
| `mvn test` (JUnit) | 112 | 8 | 0 |
| `mvn test` (TestNG) | 55 | 3 | 0 |

Beide Maven-Läufe endeten mit `BUILD SUCCESS`. Der gezielte Lauf enthält
keine TestNG-Fälle. Der Gesamtlauf verwendete ein separates `user.home`
unter `target/note-11523-analysis/`.

In `target/surefire-reports/TEST-de.danielluedecke.zettelkasten.util.HtmlUbbUtilMarkdownProjectionTest.xml`
stehen beide neuen Methoden als `testcase` ohne `skipped`, `failure`
oder `error` (11 Tests, 0 übersprungen). Die Kopie des Berichts aus dem
gezielten Lauf liefert dasselbe Ergebnis.

Lokale Nachweise unter `target/note-11523-analysis/`: `OriginalProbe.java`,
`original-before.log`, `original-after.log`, `regression-before.log`,
`regression-targeted-after.xml`, `targeted-after.log` und `full-after.log`.
`git diff --check` war erfolgreich.

**Status: `completed`** für den nachgewiesenen Renderfehler der
bereitgestellten Eingabe und die abgesicherten Listenvarianten.
