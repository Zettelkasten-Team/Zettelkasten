Zettelkasten - nach Luhmann - für Windows, Mac OS X und Linux
------------------------------------------------------------------------------
Der elektronische Zettelkasten ist ein Programm, das sich am Arbeitsprinzip des Zettelkastens von Niklas Luhmann orientiert. Die offizielle Homepage zum Programm ist [http://zettelkasten.danielluedecke.de](http://zettelkasten.danielluedecke.de).

### Download
Die aktuellste Programmversion [kann hier heruntergeladen](http://zettelkasten.danielluedecke.de/download.php) werden.

### Dokumentation und Anleitungen
Ausführliche Anleitungen zu den verschiedenen Funktionen gibt es [in der Wiki](http://zettelkasten.danielluedecke.de/wiki/doku.php). Fragen und Anregungen zum Zettelkasten können in der [Mailingliste](https://de.groups.yahoo.com/neo/groups/zettelkasten/info) gestellt werden.

### Changelog der aktuellen Entwicklerversion

#### Neue Funktionen und Änderungen
* In der Registerkarte _Folgezettel_ kann jetzt die gesamte Zettelfolge inklusive übergeordneter Ausgangszettel angezeigt werden.
* Über das Suchen-Menü bzw. das Menü _Folgezettel_ können jetzt Ausgangszettel (_Stammzettel_) von Folgezetteln gesucht werden ([#61](https://github.com/sjPlot/Zettelkasten/issues/61)).
* Im Schreibtischfenster können jetzt nur die direkten Folgezettel eines Zettels, oder aber alle Folgezettel inkl. Unterfolgezettel eingefügt werden.
* Zettel im Schreibtischfenster, die einen Folgezettel enthalten, werden durch ein eigenes Symbol in der Baumansicht hervorgehoben. Dies kann im Menü _Ansicht_ deaktiviert werden.
* Die manuelle Sortierung von Zetteln (z.B. das Verschieben in der Registerkarte _Überschriften_) wurde entfernt, da diese Funktion den Im- und Export zu fehleranfällig machte.
* Folgezettel in der Registerkarte können jetzt standardmäßig nur bis zu einer bestimmten Ebene aufgeklappt werden ([#73](https://github.com/sjPlot/Zettelkasten/issues/73)).
* Fußnoten-Zitierstil beim LaTex-Export verwendet nun `\footcite` statt `footnote{\cite}` ([#63](https://github.com/sjPlot/Zettelkasten/issues/63)).
* Erweiterte LaTex-Exportoptionen: Erstellen der Präambel und Konvertieren von Umlauten ist optional ([#1](https://github.com/sjPlot/Zettelkasten/issues/1) und [#62](https://github.com/sjPlot/Zettelkasten/issues/62)).
* Literaturfußnoten können jetzt auch Seitenzahlen beinhalten. Diese müssen durch einen `:` von der Literatur-Indexnummer getrennt werden, bspw. `[fn 666:31]` wird zu `Luhmann 1984, S.31`.
* Seitenzahlen in Literaturfußnoten werden beim LaTex-Export berücksichtigt, bspw. `[fn 666:31]` wird zu `\cite[S.31]{luhmann1984sozsys}`.
* Beim Einfügen von Literaturfußnoten werden automatisch Klammern um die Referenzen gesetzt. Beim Export ins LaTex-Format mit der Option `footcite` werden diese Klammern automatisch entfernt ([#65](https://github.com/sjPlot/Zettelkasten/issues/65)).
* Die Suche im Zettel aktualisiert die Zettelansicht (Hervorheben der gefundenen Suchstellen) erst nach drücken der Eingabetaste. Dies verhindert bei längeren Zetteln, dass die Suchbegriffeingabe nicht hakt ([#70](https://github.com/sjPlot/Zettelkasten/issues/70)).
* Die Funktion _BibTex-Datei neu laden_ aktualisiert automatisch vorhandene Literatureinträge ([#75](https://github.com/sjPlot/Zettelkasten/issues/75)).

#### Behobene Fehler
* Schlagwörter importieren von BibTex-Einträgen (ohne neuen Eintrag zu erstellen) funktionierte nicht ([#33](https://github.com/sjPlot/Zettelkasten/issues/33)).
* Querverweise mit Tooltips mit Anführungszeichen funktionierten nicht ([#53](https://github.com/sjPlot/Zettelkasten/issues/53)).
* Bildpfade beim LaTex-Export wurden nicht korrekt übersetzt ([#51](https://github.com/sjPlot/Zettelkasten/issues/51)).
* Schwierigkeiten bei vertikaler Größenveränderung von Feldern unter OS X wurden behoben, der mit der Maus ziehbare Bereich vergrößert ([#49](https://github.com/sjPlot/Zettelkasten/issues/49)).
* Dateipfade für das Einfügen von Bildern, Anhängen etc. wurden vergessen ([#20](https://github.com/sjPlot/Zettelkasten/issues/20)).
* Beim Exportieren wurden nicht alle Format-Tags entfernt.
* Tooltips bei Querverweisen wurden im Schreibtisch- und Suchergebnisfenster nicht angezeigt ([#69](https://github.com/sjPlot/Zettelkasten/issues/69)).
* Wenn Zettel im Schreibtisch per Drag'n'Drop verschoben wurden, wurden Kommentare gelöscht ([#68](https://github.com/sjPlot/Zettelkasten/issues/68)).
* Bei bestimmten BibTex-Einträgen wurden Herausgeber nicht korrekt dargestellt.
* URL's mit eckigen Klammer wurden nicht korrekt verlinkt ([#82](https://github.com/sjPlot/Zettelkasten/issues/82)).

### Screenshots
Aktuelle Screenshots [gibt es hier](http://zettelkasten.danielluedecke.de/gallery.php).
