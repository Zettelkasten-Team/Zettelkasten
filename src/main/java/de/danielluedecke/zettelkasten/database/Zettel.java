package de.danielluedecke.zettelkasten.database;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "zettel")
@XmlAccessorType(XmlAccessType.FIELD)
public class Zettel {
    @XmlElement(name = "content")
    private String content;

    @XmlElementWrapper(name = "keywords")
    @XmlElement(name = "keyword")
    private List<String> keywords;

    @XmlElementWrapper(name = "followerNumbers")
    @XmlElement(name = "followerNumber")
    private List<String> followerNumbers;

    @XmlElementWrapper(name = "links")
    @XmlElement(name = "link")
    private List<String> links;

    @XmlElementWrapper(name = "manualLinks")
    @XmlElement(name = "manualLink")
    private List<String> manualLinks;

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getFollowerNumbers() {
        return followerNumbers;
    }

    public void setFollowerNumbers(List<String> followerNumbers) {
        this.followerNumbers = followerNumbers;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<String> getManualLinks() {
        return manualLinks;
    }

    public void setManualLinks(List<String> manualLinks) {
        this.manualLinks = manualLinks;
    }
}
