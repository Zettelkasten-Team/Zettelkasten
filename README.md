# Zettelkasten
<img src="src/main/resources/de/danielluedecke/zettelkasten/resources/icons/zkn3-256x256.png" height="128" align="right"/>
<p>
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Release">
        <img src="https://img.shields.io/github/release/Zettelkasten-Team/Zettelkasten.svg" />
    </a>
     <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Downloads">
        <img src="https://img.shields.io/github/downloads/Zettelkasten-Team/Zettelkasten/total.svg" />
     </a>
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/graphs/contributors" alt="Contributors">
        <img src="https://img.shields.io/github/contributors/Zettelkasten-Team/Zettelkasten" />
    </a>
    <img src="https://github.com/Zettelkasten-Team/Zettelkasten/workflows/CI/badge.svg" alt="build status"/>
</p>
Zettelkasten is a knowledge management tool. It is inspired by Niklas Luhmann's note-taking system (Zettelkasten is German for slip box).

![screenshot](http://zettelkasten.danielluedecke.de/img/gallery/zkn1.png)
*Zettelkasten main window on macOS*

## Download Zettelkasten
Here you can download the latest version of Zettelkasten: [Releases](https://github.com/Zettelkasten-Team/Zettelkasten/releases).

**Note** to users:
> This information is relevant to users of Zettelkasten.
> To run Zettelkasten you need a Java Runtime Environment for Java 8. See [page](https://www.java.com/de/download/manual.jsp).

## Wiki
Here you can find and add content about Zettelkasten in a [Wiki](https://github.com/Zettelkasten-Team/Zettelkasten/wiki).

## Zettelkasten Build
**Note** to software developers:
> This information is only relevant to software developers.
> To build the Zettelkasten, you need a Java Development Kit (JDK) for Java 8 and Maven 3.

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

The GPLv3 applies. For detail information see [LICENSE](./LICENSE).
