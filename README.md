# Zettelkasten - nach Luhmann 
<img src="src/main/resources/de/danielluedecke/zettelkasten/resources/icons/zkn3-256x256.png" height="128" align="right"/>

<p>
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Release">
        <img src="https://img.shields.io/github/release/Zettelkasten-Team/Zettelkasten.svg" />
    </a>
     <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Downloads">
        <img src="https://img.shields.io/github/downloads/Zettelkasten-Team/Zettelkasten/total.svg" />
     </a>       
     <a href="https://github.com/Zettelkasten-Team/Zettelkasten/issues" alt="Resolution time">
        <img src="http://isitmaintained.com/badge/resolution/Zettelkasten-Team/Zettelkasten.svg" />
    </a>
     <a href="https://github.com/Zettelkasten-Team/Zettelkasten/issues" alt="Open Issues">
        <img src="http://isitmaintained.com/badge/open/Zettelkasten-Team/Zettelkasten.svg" />
     </a>            
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/graphs/contributors" alt="Contributors">
        <img src="https://img.shields.io/github/contributors/Zettelkasten-Team/Zettelkasten" />
    </a>
    <img src="https://github.com/Zettelkasten-Team/Zettelkasten/workflows/Java%20CI%20with%20Maven/badge.svg" alt="build status"/>
</p>


Der elektronische Zettelkasten ist ein Programm, das sich am Arbeitsprinzip des Zettelkastens von Niklas Luhmann orientiert. Die offizielle Homepage zum Programm ist [http://zettelkasten.danielluedecke.de](http://zettelkasten.danielluedecke.de).
<br/>
<br/>
![screenshot](http://zettelkasten.danielluedecke.de/img/gallery/zkn1.png)

## Download
Die aktuellste Programmversion [kann hier heruntergeladen](https://github.com/Zettelkasten-Team/Zettelkasten/releases) werden.

## Zettelkasten bauen

Zum Bauen des Zettelkasten wird das Java Development Kit (JDK) 8 oder höher benötigt, empfohlen wird aber für zukünftige Versionen das JDK 11.
Zusätzlich wird Maven 3 verwendet. 

Zum Klonen des Repositories wird außerdem Git benötigt.

`git clone git@github.com:Zettelkasten-Team/Zettelkasten.git`

Anschließend kann das Projekt mit folgendem Befehl gebaut werden:

* `mvn clean package`

Die Build-Ergebnisse befinden sich dann im `target`-Ordner


## Lizenz
Lizensiert unter der GPLv3. Für mehr Informationen siehe die LICENSE.md
