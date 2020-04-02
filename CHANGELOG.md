<a name="3.3.0"></a>
# [3.3.0](https://github.com/sjPlot/Zettelkasten/compare/3.2.7...3.3.0) (2020-04-02)
### Änderungen
- Registrierung der Dateiendung ".zkn3" wurde temporär deaktiviert.
- Die Schriftgröße der Benutzeroberfläche wurde an höhere Bildschirmauflösungen angepasst.
- Der Programmcode wurde optimiert.
### Fehlerbehebungen
- Beim XML-Export wurden die Zettel-IDs nicht mit exportiert ([171](https://github.com/sjPlot/Zettelkasten/issues/171)).
- Falls die Option "Beim Programmstart: Zuletzt ausgewählten Zettel anzeigen" aktiviert war, konnte es in bestimmten Situationen dazu kommen, dass der Zettelkasten nicht mehr startete (siehe [178](https://github.com/sjPlot/Zettelkasten/issues/178)). 
<a name="3.2.7"></a>
# [3.2.7](https://github.com/sjPlot/Zettelkasten/compare/3.2.6...3.2.7) (2015-10-16)

### Neue Funktionen
- In der Registerkarte _Überschriften_ werden jetzt Zettel als Ausgangsfolgezettel oder Folgezettel markiert ([#96](https://github.com/sjPlot/Zettelkasten/issues/96)). Die Sortierung der Spalte _Folgezettel_ in der Registerkarte _Überschriften_ ist per Voreinstellung deakitiviert, da der erstmalige Aufbau der Tabelle länger dauern kann. Die Sortierung kann in den Einstellungen _Tabellen und Listen_ aktiviert werden.
- Der Export von Zetteln ins Markdown-, Text- und LaTex-Format kann jetzt auch jeden Zettel als einzelne Datei exportieren ([#13](https://github.com/sjPlot/Zettelkasten/issues/13) und [#77](https://github.com/sjPlot/Zettelkasten/issues/77)).
- Literaturfußnoten können jetzt auch den Bibkey statt der Nummer des Literatureintrags enthalten (`[fn luhsozsys:123]` würde z.B. umgewandelt in `Luhmann 1984: 123`).
- Das automatische Einklammern von Literaturfußnoten kann in den Einstellungen abgestellt werden ([#91](https://github.com/sjPlot/Zettelkasten/issues/91)).
- Über das Menü _Suchen_ (Untermenü _Zettel_) können jetzt Zettel gesucht werden, die Teil einer Folgezettelsequenz sind (entweder Ausgangs- oder Folgezettel).
- Über das Menü _Suchen_ (Untermenü _Zettel_) können jetzt Zettel gesucht werden, die _keine_ Querverweise enthalten (und auf die nicht durch Querverweise verwiesen wird) ([#34](https://github.com/sjPlot/Zettelkasten/issues/34)).
- Die Sortierung der Tabellenspalten wird gespeichert, sodass diese beim erneuten Programmstart wiederhergestellt wird ([#72](https://github.com/sjPlot/Zettelkasten/issues/72)).
- Neue Einstellung, um Formatierungs-Tags bei einer Suche _nicht_ zu entfernen. Dadurch wird die Suchgeschwindigkeit erhöht, jedoch werden Wörter, die durch Formatierungen unterbrochen werden, nicht gefunden (eine Suche nach `Zettelkasten` findet dann nicht `[f]Zettel[/f]kasten`).
### Änderungen
- Beim Export ins Markdown-, Text- und LaTex-Format werden Fußnoten durch formatierte Autorenangaben ersetzt (sofern die Literatur mit Bibkey verknüpft ist).
- Verbesserte Stabilität aller Lese- und Schreib-Operationen von Daten (Import, Export, Laden, Speichern, Auto-Backup...).
- Geschwindigkeitsverbesserungen der Suchfunktionen ([#95](https://github.com/sjPlot/Zettelkasten/issues/95)).
- Im englischen Interface wurden _trails_ in _note sequences_ und _entry_ in _note_ umbenannt.
- Literatur, die während der Neueingabe oder Bearbeiten eines Zettels als Literaturfußnote im Text referenziert wird, wird beim Beenden der Eingabe automatisch als Autorenangabe zum Zettel hinzugefügt, falls dies noch nicht geschehen ist.
### Fehlerbehebungen
- Bei Dateien, die im Neueingabefenster per Drag'n'Drop ins Textfeld gezogen wurden, kam keine Abfrage zum Verschieben oder Kopieren der Dateien.
- Änderung [#82](https://github.com/sjPlot/Zettelkasten/issues/82) wurde rückgängig gemacht, da es Probleme mit URLs in BibTex-Autorenangaben gab.
- In der Registerkarte _Literatur_ wurden _Buchkapitel_ und _Artikel in Büchern_ bei der Filterfunktion vertauscht ([#90](https://github.com/sjPlot/Zettelkasten/issues/90)).
- Dateinamen mit Unterstrichen kollidierten mit Markdowninterpretation ([#26](https://github.com/sjPlot/Zettelkasten/issues/26)).
- Schreibtischexport ins Markdown deklarierte keine Überschriften ([#56](https://github.com/sjPlot/Zettelkasten/issues/56)).
- Im Exportfenster konnten beim Export ins Textformat keine Zettelelemente ausgewählt werden.
- Bei Fußnoten ohne Bibkey wurde die Seitenzahl nicht angezeigt ([#74](https://github.com/sjPlot/Zettelkasten/issues/74)).

<a name="3.2.6"></a>
# [3.2.6](https://github.com/sjPlot/Zettelkasten/compare/3.2.5...3.2.6) (2015-09-25)

### Neue Funktionen
- In der Registerkarte _Folgezettel_ kann jetzt die gesamte Zettelfolge inklusive übergeordneter Ausgangszettel angezeigt werden.
- Über das Suchen-Menü bzw. das Menü _Folgezettel_ können jetzt Ausgangszettel (_Stammzettel_) von Folgezetteln gesucht werden ([#61](https://github.com/sjPlot/Zettelkasten/issues/61)).
- Im Schreibtischfenster können jetzt nur die direkten Folgezettel eines Zettels, oder aber alle Folgezettel inkl. Unterfolgezettel eingefügt werden.
- Zettel im Schreibtischfenster, die einen Folgezettel enthalten, werden durch ein eigenes Symbol in der Baumansicht hervorgehoben. Dies kann im Menü _Ansicht_ deaktiviert werden.
- Erweiterte LaTex-Exportoptionen: Erstellen der Präambel und Konvertieren von Umlauten ist optional ([#1](https://github.com/sjPlot/Zettelkasten/issues/1) und [#62](https://github.com/sjPlot/Zettelkasten/issues/62)).
- Literaturfußnoten können jetzt auch Seitenzahlen beinhalten. Diese müssen durch einen `:` von der Literatur-Indexnummer getrennt werden, bspw. `[fn 666:31]` wird zu `Luhmann 1984, S.31`.
- Seitenzahlen in Literaturfußnoten werden beim LaTex-Export berücksichtigt, bspw. `[fn 666:31]` wird zu `\cite[S.31]{luhmann1984sozsys}`.
- Die Funktion _BibTex-Datei neu laden_ aktualisiert automatisch vorhandene Literatureinträge ([#75](https://github.com/sjPlot/Zettelkasten/issues/75)).

### Änderungen
- Die manuelle Sortierung von Zetteln (z.B. das Verschieben in der Registerkarte _Überschriften_) wurde entfernt, da diese Funktion den Im- und Export zu fehleranfällig machte.
- Folgezettel in der Registerkarte können jetzt standardmäßig nur bis zu einer bestimmten Ebene aufgeklappt werden ([#73](https://github.com/sjPlot/Zettelkasten/issues/73)).
- Fußnoten-Zitierstil beim LaTex-Export verwendet nun `\footcite` statt `footnote{\cite}` ([#63](https://github.com/sjPlot/Zettelkasten/issues/63)).
- Beim Einfügen von Literaturfußnoten werden automatisch Klammern um die Referenzen gesetzt. Beim Export ins LaTex-Format mit der Option `footcite` werden diese Klammern automatisch entfernt ([#65](https://github.com/sjPlot/Zettelkasten/issues/65)).
- Die Suche im Zettel aktualisiert die Zettelansicht (Hervorheben der gefundenen Suchstellen) erst nach drücken der Eingabetaste. Dies verhindert bei längeren Zetteln, dass die Suchbegriffeingabe nicht hakt ([#70](https://github.com/sjPlot/Zettelkasten/issues/70)).

### Fehlerbehebungen
- Schlagwörter importieren von BibTex-Einträgen (ohne neuen Eintrag zu erstellen) funktionierte nicht ([#33](https://github.com/sjPlot/Zettelkasten/issues/33)).
- Querverweise mit Tooltips mit Anführungszeichen funktionierten nicht ([#53](https://github.com/sjPlot/Zettelkasten/issues/53)).
- Bildpfade beim LaTex-Export wurden nicht korrekt übersetzt ([#51](https://github.com/sjPlot/Zettelkasten/issues/51)).
- Schwierigkeiten bei vertikaler Größenveränderung von Feldern unter OS X wurden behoben, der mit der Maus ziehbare Bereich vergrößert ([#49](https://github.com/sjPlot/Zettelkasten/issues/49)).
- Dateipfade für das Einfügen von Bildern, Anhängen etc. wurden vergessen ([#20](https://github.com/sjPlot/Zettelkasten/issues/20)).
- Beim Exportieren wurden nicht alle Format-Tags entfernt.
- Tooltips bei Querverweisen wurden im Schreibtisch- und Suchergebnisfenster nicht angezeigt ([#69](https://github.com/sjPlot/Zettelkasten/issues/69)).
- Wenn Zettel im Schreibtisch per Drag'n'Drop verschoben wurden, wurden Kommentare gelöscht ([#68](https://github.com/sjPlot/Zettelkasten/issues/68)).
- Bei bestimmten BibTex-Einträgen wurden Herausgeber nicht korrekt dargestellt.
- URL's mit eckigen Klammer wurden nicht korrekt verlinkt ([#82](https://github.com/sjPlot/Zettelkasten/issues/82)).

<a name="3.2.5.1"></a>
# 3.2.5.1 (2015-05-29)

### Änderungen
- Mehrfachformatierungen während der Neueingabe wurden vereinfacht durch Beibehalten der Textauswahl. Damit lässt sich ein Wort oder Absatz schneller mit mehreren Formatierungen hintereinander auszeichnen ([#39](https://github.com/sjPlot/Zettelkasten/issues/39)).
- `[code]` formatiert Code-Blocks mit eingerückten Leerzeichen (d.h. wandelt `[code]` in `<pre>` um). Inline-Code wird jetzt durch ` ausgezeichnet (`code`) ([#48](https://github.com/sjPlot/Zettelkasten/issues/48)).
- Aktualisierung der [JDOM](http://www.jdom.org)\- und [OpenCSV](http://opencsv.sourceforge.net)-Bibliotheken.

### Fehlerbehebungen
- Mehrere Suchbegriffe im Suchfenster, durch Kommata getrennt, werden jetzt von Leerzeichen bereinigt ([#27](https://github.com/sjPlot/Zettelkasten/issues/27)).
- Markdown-Zitat werden jetzt korrekt nach LaTeX exportiert ([#31](https://github.com/sjPlot/Zettelkasten/issues/31)).
- Doppelte Zeilenumbrüche entfernen bei Neueingabe wird jetzt nur auf Auswahl angewendet ([#16](https://github.com/sjPlot/Zettelkasten/issues/16)).
- Literaturangaben waren nicht bearbeitbar, wenn lediglich Groß- in Kleinbuchstaben oder umgekehrt geändert wurden. Fehler behoben, ändern von Groß-/Kleinschreibung in Literaturangaben jetzt über die Registerkarte _Literatur_ möglich ([#47](https://github.com/sjPlot/Zettelkasten/issues/47)).
- Querverweise auf einen gelöschten Zettel ließen sich nicht entfernen ([#38](https://github.com/sjPlot/Zettelkasten/issues/38)).
