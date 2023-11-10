# Zettelkasten

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


Zettelkasten is a program for knowledge management. It is inspired by the note-taking system of Niklas Luhmann (Zettelkasten is German for slip-box).

![screenshot](http://zettelkasten.danielluedecke.de/img/gallery/zkn1.png)
*Zettelkasten main window on macOS*

## Download

Find the latest release [here](https://github.com/Zettelkasten-Team/Zettelkasten/releases).

## Wiki

Find content about Zettelkasten in our Wiki [here](https://github.com/Zettelkasten-Team/Zettelkasten/wiki).

## Zettelkasten Build

To build Zettelkasten, you need a Java Development Kit (JDK) 8 and Maven 3.

To get a local repository, use Git:

```shell
git clone git@github.com:Zettelkasten-Team/Zettelkasten.git
```

Then, in the root directory of the local repository, run the following command line:

```shell
mvn clean package
```
Note: `mvn` is [Apache Maven](https://maven.apache.org/install.html). Confirm with `mvn -v` in a new shell.

Find the build result in your `target` directory.


## License

The GPLv3 applies. For detail information see LICENSE.md
