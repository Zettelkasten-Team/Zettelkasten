<a name="v3.3.1"></a>
# [3.3.1](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.3.0...v3.3.1) (2020-04-06)
### Bug fixes
- Look-and-Feel Seaglass was removed, due to compatibility issues with JDK 11 ([210](https://github.com/Zettelkasten-Team/Zettelkasten/issues/210)).

<a name="v3.3.0"></a>
# [3.3.0](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.7...v3.3.0) (2020-04-02)
### Changes
- Registration of ".zkn3" file ending temporarily deactivated.
- Font size of GUI adapted to high resolution screens.
- Code optimizations.
### Bug fixes 
- Note-IDs were missing in XML exports ([171](https://github.com/Zettelkasten-Team/Zettelkasten/issues/171)).
- The preference setting "At program startup: Show last displayed note" lead in certain situations to program startup issues (compare [178](https://github.com/Zettelkasten-Team/Zettelkasten/issues/178)). 
<a name="3.2.7"></a>
# [3.2.7](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.6...3.2.7) (2015-10-16)

### New functions
- Start and member notes of note sequences are indicated in the _Titles_ tab ([#96](https://github.com/Zettelkasten-Team/Zettelkasten/issues/96)). The order of the column _Note sequence_ in the _Titles_ tab is deactivated by default, since the first initialization of the table takes a while. The order can be activated via the preference setting _Table and Listviews_.
- The export of notes in Markdown, text, or LaTeX can now create single files for each note ([#13](https://github.com/Zettelkasten-Team/Zettelkasten/issues/13) and [#77](https://github.com/Zettelkasten-Team/Zettelkasten/issues/77)).
- Literature footnotes can use the Bibkey instead of the number of the literature reference (i.e.`[fn luhsozsys:123]`is converted into `Luhmann 1984: 123`).
- The bracketing of literature footnotes can be turned off in the preferences ([#91](https://github.com/Zettelkasten-Team/Zettelkasten/issues/91)).
- Via the menu _Search_ (Sub menu _Notes_) member notes of a notes sequence can be searched for, even if they are member notes or starting notes of a notes sequence.
- Via the menu _Search_ (Sub menu _Notes_) notes can be search for which do _not_ contain links (and which are not referred by links themselves) ([#34](https://github.com/Zettelkasten-Team/Zettelkasten/issues/34)).
- The order of the table columns is preserved for the next program startup ([#72](https://github.com/Zettelkasten-Team/Zettelkasten/issues/72)).
- New preferences, do _not_ remove format tags during a search. This benefits the search performance, but removes format-interrupted words from the search results (i.e. a search for `Zettelkasten` does not find `[f]Zettel[/f]kasten`).
### Changes
- In the export into Markdown, text, or LaTeX, the footnotes are replaced by formatted author references (if case the literature is linked with Bibkey).
- Stability improvements of all read and write operations of files (Import, Export, Laden, Save, Auto-Backup...).
- Performance improvements of the search function ([#95](https://github.com/Zettelkasten-Team/Zettelkasten/issues/95)).
- Renamed _trails_ into _note sequences_ and _entry_ into _note_ in the English UI.
- If not already listed, literature, which is referenced while the creation or the editing of a note as a reference footnote in the text, is automatically added to the authors list of the note after finishing the editing.
### Bug fixes
- For file which were drag'n'dropped into the text field of the New Entry window, no notification for moving or copying of the file existed.
- [#82](https://github.com/Zettelkasten-Team/Zettelkasten/issues/82) was reverted, due to problems with URLs in the BibTex author listing.
- In the tab _Authors_,  _Chapters_ and _Articles in books_ were swapped in the filter function ([#90](https://github.com/Zettelkasten-Team/Zettelkasten/issues/90)).
- File names with underscores conflicted with the Markdown interpreter ([#26](https://github.com/Zettelkasten-Team/Zettelkasten/issues/26)).
- Desk export into Markdown did not declare titles ([#56](https://github.com/Zettelkasten-Team/Zettelkasten/issues/56)).
- In the export window for text import, no note elements could be selected.
- Page number was not indicated for footnotes without Bibkey ([#74](https://github.com/Zettelkasten-Team/Zettelkasten/issues/74)).

<a name="3.2.6"></a>
# [3.2.6](https://github.com/Zettelkasten-Team/Zettelkasten/compare/3.2.5...3.2.6) (2015-09-25)

### New functions
- In the tab _note sequence_ the full note sequence as well as the starting note is shown.
- _Origin of sequence_ is accessible via the  search menu as well as the menu item _Note sequences_  for note sequences ([#61](https://github.com/Zettelkasten-Team/Zettelkasten/issues/61)).
- In the _Outliner_ window the option to show only the directly following notes or the complete note sequence including sub-note sequences can be inserted.
- Notes in the _Outliner_ window, which have a subsequent note, are highlighted by a symbol in the tree view. This feature can be deactivated in the _View_ menu item.
- Extended LaTeX export options: Preamble creation and umlaut conversion is optional ([#1](https://github.com/Zettelkasten-Team/Zettelkasten/issues/1) and [#62](https://github.com/Zettelkasten-Team/Zettelkasten/issues/62)).
- Literature footnotes can contain page numbers as well. The page number must be separated from the literature index number with a `:` , i.e. `[fn 666:31]` is replaced by `Luhmann 1984, S.31`.
- Page numbers are included during the LaTeX export, i.e. `[fn 666:31]` becomes `\cite[S.31]{luhmann1984sozsys}`.
- The function _Reload BibTex file_ updates existing literature items automatically ([#75](https://github.com/Zettelkasten-Team/Zettelkasten/issues/75)).

### Changes
- The manual sorting of notes (i.e. by moving in the tab _Titles_ ) was removed, since it resulted in an error-prone import and export.
- In the tab, note sequences can be extended up to a certain level by default ([#73](https://github.com/Zettelkasten-Team/Zettelkasten/issues/73)).
- In the LaTeX export, footnotes are created as `\footcite` instead of `footnote{\cite}` ([#63](https://github.com/Zettelkasten-Team/Zettelkasten/issues/63)).
- During the insert of literature footnotes, the references are inserted automatically in brackets. During the LaTeX export, with the option `footcite` the brackets are removed automatically ([#65](https://github.com/Zettelkasten-Team/Zettelkasten/issues/65)).
- The search within a note updates the view (highlighting of the search results) only after pressing the return key. This prevents lagging during the search for long notes. ([#70](https://github.com/Zettelkasten-Team/Zettelkasten/issues/70)).

### Bug fixes
- The keyword import from BibTex entries (without creating a new entry) did not work ([#33](https://github.com/Zettelkasten-Team/Zettelkasten/issues/33)).
- Links with tooltips with quotes did not work ([#53](https://github.com/Zettelkasten-Team/Zettelkasten/issues/53)).
- Image paths were not translated correctly in the LaTeX export ([#51](https://github.com/Zettelkasten-Team/Zettelkasten/issues/51)).
- Difficulties during the vertical scaling of fields for the OS X version corrected, the draggable area for the mouse enlarged ([#49](https://github.com/Zettelkasten-Team/Zettelkasten/issues/49)).
- File paths for the insertion of images, attachments, etc. were missing ([#20](https://github.com/Zettelkasten-Team/Zettelkasten/issues/20)).
- Not all format tags were removed during the export.
- Link tool tips were not shown in the _Outliner_ and _Search_ windows ([#69](https://github.com/Zettelkasten-Team/Zettelkasten/issues/69)).
- Moving notes by drag and drop removed comments in the _Outliner_ ([#68](https://github.com/Zettelkasten-Team/Zettelkasten/issues/68)).
- In some BibTex entries the publisher was not indicated correctly.
- URLs with square brackets were not linked correctly ([#82](https://github.com/Zettelkasten-Team/Zettelkasten/issues/82)).

<a name="3.2.5.1"></a>
# 3.2.5.1 (2015-05-29)

### Changes
- The multi formatting during the creation of new notes was simplified by keeping the font setting. Thus, a word or a paragraph can faster be declared with multiple sequential settings ([#39](https://github.com/Zettelkasten-Team/Zettelkasten/issues/39)).
- `[code]` formats code blocks with indentions (changes  `[code]` to `<pre>`). Inline code can be declared with ` (`code`) ([#48](https://github.com/Zettelkasten-Team/Zettelkasten/issues/48)).
- Update of [JDOM](http://www.jdom.org)\- and [OpenCSV](http://opencsv.sourceforge.net) libraries.

### Bug fixes
- In the search window, spaces in comma-separated search terms are removed ([#27](https://github.com/Zettelkasten-Team/Zettelkasten/issues/27)).
- Markdown citation are correctly exported into LaTeX ([#31](https://github.com/Zettelkasten-Team/Zettelkasten/issues/31)).
- Double line break are removed during the note creation only if the option is selected ([#16](https://github.com/Zettelkasten-Team/Zettelkasten/issues/16)).
- Literature references could not be changed if only the capitalization was changed. Bug fixed, capitalization in literature references can now changed in the _Literature_ tab ([#47](https://github.com/Zettelkasten-Team/Zettelkasten/issues/47)).
- Links to deleted notes could not be removed ([#38](https://github.com/Zettelkasten-Team/Zettelkasten/issues/38)).
