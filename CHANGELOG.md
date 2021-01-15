<a name="v3.3.1"></a>
# [3.3.1](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.3.0...v3.3.1) (2020-04-06)
### Bug fixes
- Look-and-Feel Seaglass was removed, due to compatibiity issues with JDK 11 ([210](https://github.com/Zettelkasten-Team/Zettelkasten/issues/210)).

<a name="v3.3.0"></a>
# [3.3.0](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.7...v3.3.0) (2020-04-02)
### Changes
- Registration of ".zkn3" file ending temprarily deactivated.
- Font size of GUI adapted to high resolution screens.
- Code optimizations.
### Bug fixes 
- Note-IDs were missing in XML exports ([171](https://github.com/Zettelkasten-Team/Zettelkasten/issues/171)).
- The preference setting "At program startup: Show last displayed note" lead in certain situtations to program startup issues (compare [178](https://github.com/Zettelkasten-Team/Zettelkasten/issues/178)). 
<a name="3.2.7"></a>
# [3.2.7](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.6...3.2.7) (2015-10-16)

### New functions
- Start and member notes of note sequences are indicated in the _Titles_ tab ([#96](https://github.com/Zettelkasten-Team/Zettelkasten/issues/96)). The order of the column _Note sequence_ in the _Titles_ tab is deactivated by default, since the frist initialization of the table takes a while. The order can be activated via the preference setting _Table and Listviews_.
- The export of notes in Markdown, text, or LaTeX can now create single files for each note ([#13](https://github.com/Zettelkasten-Team/Zettelkasten/issues/13) and [#77](https://github.com/Zettelkasten-Team/Zettelkasten/issues/77)).
- Literature foot notes can use the Bibkey instead of the number of the literature reference (i.e.`[fn luhsozsys:123]`is converted into `Luhmann 1984: 123`).
- The bracketing of literature foot notes can be turned of in the preferences ([#91](https://github.com/Zettelkasten-Team/Zettelkasten/issues/91)).
- Via the menu _Search_ (Sub menu _Notes_) member notes of a notes sequence can be searched for, even if they are member notes or starting notes of a notes sequence.
- Via the menu _Search_ (Sub menu _Notes_) notes can be search for which do _not_ contain links (and which are not referred by links themselves) ([#34](https://github.com/Zettelkasten-Team/Zettelkasten/issues/34)).
- The order of the table columns is preserved for the next program startup ([#72](https://github.com/Zettelkasten-Team/Zettelkasten/issues/72)).
- New preferences, to _not_ remove format tags during a search. This benefits the search performance, but removes format-interrupted words rom the search results (i.e. a search for `Zettelkasten` does not find `[f]Zettel[/f]kasten`).
### Changes
- In the export into Markdown, text, or LaTeX, the footnotes are replaced by formated author references (if case the literature is linked with Bibkey).
- Stability improvements of all read and write operations of files (Import, Export, Laden, Save, Auto-Backup...).
- Performance improvements of the search function ([#95](https://github.com/Zettelkasten-Team/Zettelkasten/issues/95)).
- Renamed _trails_ into _note sequences_ and _entry_ into _note_ in the English UI.
- If not already listed, literature, which is referenced while the creation or the editing of a note as a reference foot note in the text, is automatically added to the authors list of the note after finishing the editing.
### Bug fixes
- For file which were drag'n'droped into the text field of the New Entry window, no notification for moving or copying of the file existed.
- [#82](https://github.com/Zettelkasten-Team/Zettelkasten/issues/82) was reverted, due to problems with URLs in the BibTex author listing.
- In the tab _Authors_,  _Chapters_ and _Articles in books_ were swaped in the filter function ([#90](https://github.com/Zettelkasten-Team/Zettelkasten/issues/90)).
- File names with underscores conflicted with the Markdown interpreter ([#26](https://github.com/Zettelkasten-Team/Zettelkasten/issues/26)).
- Desk export into markdown did not declare Titles ([#56](https://github.com/Zettelkasten-Team/Zettelkasten/issues/56)).
- In the export window for text import, no note elements could be selected.
- Page number was not indicated for footnotes without Bibkey ([#74](https://github.com/Zettelkasten-Team/Zettelkasten/issues/74)).

<a name="3.2.6"></a>
# [3.2.6](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.5...3.2.6) (2015-09-25)

### Neue Funktionen
- In der Registerkarte _Folgezettel_ kann jetzt die gesamte Zettelfolge inklusive übergeordneter Ausgangszettel angezeigt werden.
- Über das Suchen-Menü bzw. das Menü _Folgezettel_ können jetzt Ausgangszettel (_Stammzettel_) von Folgezetteln gesucht werden ([#61](https://github.com/Zettelkasten-Team/Zettelkasten/issues/61)).
- Im Schreibtischfenster können jetzt nur die direkten Folgezettel eines Zettels, oder aber alle Folgezettel inkl. Unterfolgezettel eingefügt werden.
- Zettel im Schreibtischfenster, die einen Folgezettel enthalten, werden durch ein eigenes Symbol in der Baumansicht hervorgehoben. Dies kann im Menü _Ansicht_ deaktiviert werden.
- Erweiterte LaTex-Exportoptionen: Erstellen der Präambel und Konvertieren von Umlauten ist optional ([#1](https://github.com/Zettelkasten-Team/Zettelkasten/issues/1) und [#62](https://github.com/Zettelkasten-Team/Zettelkasten/issues/62)).
- Literaturfußnoten können jetzt auch Seitenzahlen beinhalten. Diese müssen durch einen `:` von der Literatur-Indexnummer getrennt werden, bspw. `[fn 666:31]` wird zu `Luhmann 1984, S.31`.
- Seitenzahlen in Literaturfußnoten werden beim LaTex-Export berücksichtigt, bspw. `[fn 666:31]` wird zu `\cite[S.31]{luhmann1984sozsys}`.
- Die Funktion _BibTex-Datei neu laden_ aktualisiert automatisch vorhandene Literatureinträge ([#75](https://github.com/Zettelkasten-Team/Zettelkasten/issues/75)).

### Änderungen
- Die manuelle Sortierung von Zetteln (z.B. das Verschieben in der Registerkarte _Überschriften_) wurde entfernt, da diese Funktion den Im- und Export zu fehleranfällig machte.
- Folgezettel in der Registerkarte können jetzt standardmäßig nur bis zu einer bestimmten Ebene aufgeklappt werden ([#73](https://github.com/Zettelkasten-Team/Zettelkasten/issues/73)).
- Fußnoten-Zitierstil beim LaTex-Export verwendet nun `\footcite` statt `footnote{\cite}` ([#63](https://github.com/Zettelkasten-Team/Zettelkasten/issues/63)).
- Beim Einfügen von Literaturfußnoten werden automatisch Klammern um die Referenzen gesetzt. Beim Export ins LaTex-Format mit der Option `footcite` werden diese Klammern automatisch entfernt ([#65](https://github.com/Zettelkasten-Team/Zettelkasten/issues/65)).
- Die Suche im Zettel aktualisiert die Zettelansicht (Hervorheben der gefundenen Suchstellen) erst nach drücken der Eingabetaste. Dies verhindert bei längeren Zetteln, dass die Suchbegriffeingabe nicht hakt ([#70](https://github.com/Zettelkasten-Team/Zettelkasten/issues/70)).

### Fehlerbehebungen
- Schlagwörter importieren von BibTex-Einträgen (ohne neuen Eintrag zu erstellen) funktionierte nicht ([#33](https://github.com/Zettelkasten-Team/Zettelkasten/issues/33)).
- Querverweise mit Tooltips mit Anführungszeichen funktionierten nicht ([#53](https://github.com/Zettelkasten-Team/Zettelkasten/issues/53)).
- Bildpfade beim LaTex-Export wurden nicht korrekt übersetzt ([#51](https://github.com/Zettelkasten-Team/Zettelkasten/issues/51)).
- Schwierigkeiten bei vertikaler Größenveränderung von Feldern unter OS X wurden behoben, der mit der Maus ziehbare Bereich vergrößert ([#49](https://github.com/Zettelkasten-Team/Zettelkasten/issues/49)).
- Dateipfade für das Einfügen von Bildern, Anhängen etc. wurden vergessen ([#20](https://github.com/Zettelkasten-Team/Zettelkasten/issues/20)).
- Beim Exportieren wurden nicht alle Format-Tags entfernt.
- Tooltips bei Querverweisen wurden im Schreibtisch- und Suchergebnisfenster nicht angezeigt ([#69](https://github.com/Zettelkasten-Team/Zettelkasten/issues/69)).
- Wenn Zettel im Schreibtisch per Drag'n'Drop verschoben wurden, wurden Kommentare gelöscht ([#68](https://github.com/Zettelkasten-Team/Zettelkasten/issues/68)).
- Bei bestimmten BibTex-Einträgen wurden Herausgeber nicht korrekt dargestellt.
- URL's mit eckigen Klammer wurden nicht korrekt verlinkt ([#82](https://github.com/Zettelkasten-Team/Zettelkasten/issues/82)).

<a name="3.2.5.1"></a>
# 3.2.5.1 (2015-05-29)

### Änderungen
- Mehrfachformatierungen während der Neueingabe wurden vereinfacht durch Beibehalten der Textauswahl. Damit lässt sich ein Wort oder Absatz schneller mit mehreren Formatierungen hintereinander auszeichnen ([#39](https://github.com/Zettelkasten-Team/Zettelkasten/issues/39)).
- `[code]` formatiert Code-Blocks mit eingerückten Leerzeichen (d.h. wandelt `[code]` in `<pre>` um). Inline-Code wird jetzt durch ` ausgezeichnet (`code`) ([#48](https://github.com/Zettelkasten-Team/Zettelkasten/issues/48)).
- Aktualisierung der [JDOM](http://www.jdom.org)\- und [OpenCSV](http://opencsv.sourceforge.net)-Bibliotheken.

### Fehlerbehebungen
- Mehrere Suchbegriffe im Suchfenster, durch Kommata getrennt, werden jetzt von Leerzeichen bereinigt ([#27](https://github.com/Zettelkasten-Team/Zettelkasten/issues/27)).
- Markdown-Zitat werden jetzt korrekt nach LaTeX exportiert ([#31](https://github.com/Zettelkasten-Team/Zettelkasten/issues/31)).
- Doppelte Zeilenumbrüche entfernen bei Neueingabe wird jetzt nur auf Auswahl angewendet ([#16](https://github.com/Zettelkasten-Team/Zettelkasten/issues/16)).
- Literaturangaben waren nicht bearbeitbar, wenn lediglich Groß- in Kleinbuchstaben oder umgekehrt geändert wurden. Fehler behoben, ändern von Groß-/Kleinschreibung in Literaturangaben jetzt über die Registerkarte _Literatur_ möglich ([#47](https://github.com/Zettelkasten-Team/Zettelkasten/issues/47)).
- Querverweise auf einen gelöschten Zettel ließen sich nicht entfernen ([#38](https://github.com/Zettelkasten-Team/Zettelkasten/issues/38)).
