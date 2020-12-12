package de.danielluedecke.zettelkasten.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatenTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initZettelkasten() {
    }

    @Test
    void getVersionInfo() {
    }

    @Test
    void getUserAttachmentPath() {
    }

    @Test
    void setUserAttachmentPath() {
    }

    @Test
    void getUserImagePath() {
    }

    @Test
    void setUserImagePath() {
    }

    @Test
    void getCurrentVersionInfo() {
    }

    @Test
    void isMetaModified() {
    }

    @Test
    void setMetaModified() {
    }

    @Test
    void isModified() {
    }

    @Test
    void setModified() {
    }

    @Test
    void getFilesToLoadCount() {
    }

    @Test
    void getFileToLoad() {
    }

    @Test
    void setZknData() {
    }

    @Test
    void getZknData() {
    }

    @Test
    void isNewVersion() {
    }

    @Test
    void isIncompatibleFile() {
    }

    @Test
    void appendZknData() {
    }

    @Test
    void setAuthorData() {
    }

    @Test
    void getAuthorData() {
    }

    @Test
    void setKeywordData() {
    }

    @Test
    void getKeywordData() {
    }

    @Test
    void setMetaInformationData() {
    }

    @Test
    void getMetaInformationData() {
    }

    @Test
    void setCompleteZknData() {
    }

    @Test
    void getZknDescription() {
    }

    @Test
    void setZknDescription() {
    }

    @Test
    void addZknDescription() {
    }

    @Test
    void duplicateEntry() {
    }

    @Test
    void updateVersionInfo() {
    }

    @Test
    void retrieveZettel() {
    }

    @Test
    void retrieveNonexistingKeywords() {
    }

    @Test
    void retrieveExportDocument() {
    }


    @Test
    void findKeywordInDatabase() {
    }

    @Test
    void getKeywordPosition() {
    }

    @Test
    void getBibkeyPosition() {
    }

    @Test
    void addKeyword() {
    }

    @Test
    void addKeywordsToDatabase() {
    }

    @Test
    void getKeyword() {
    }

    @Test
    void setKeyword() {
    }

    @Test
    void testSetKeyword() {
    }

    @Test
    void deleteKeyword() {
    }

    @Test
    void deleteZettel() {
    }

    @Test
    void deleteAuthorsFromEntry() {
    }

    @Test
    void deleteKeywordsFromEntry() {
    }

    @Test
    void existsInKeywords() {
    }

    @Test
    void testExistsInKeywords() {
    }

    @Test
    void existsInAuthors() {
    }

    @Test
    void testExistsInAuthors() {
    }

    @Test
    void mergeKeywords() {
    }

    @Test
    void mergeAuthors() {
    }

    @Test
    void addKeywordToEntry() {
    }

    @Test
    void addKeywordsToEntry() {
    }

    @Test
    void addAuthorToEntry() {
    }

    @Test
    void findAuthorInDatabase() {
    }

    @Test
    void getAuthorPosition() {
    }

    @Test
    void getAuthorBibKeyPosition() {
    }

    @Test
    void addAuthor() {
    }

    @Test
    void addEntry() {
    }

    @Test
    void testAddEntry() {
    }

    @Test
    void addEntryFromBibTex() {
    }

    @Test
    void setContentFromBibTexRemark() {
    }

    @Test
    void isContentFromBibTex() {
    }

    @Test
    void changeEntry() {
    }

    @Test
    void addLuhmannNumber() {
    }

    @Test
    void addManualLink() {
    }

    @Test
    void testAddManualLink() {
    }

    @Test
    void testAddManualLink1() {
    }

    @Test
    void deleteLuhmannNumber() {
    }

    @Test
    void insertLuhmannNumber() {
    }

    @Test
    void deleteManualLinks() {
    }

    @Test
    void testDeleteManualLinks() {
    }

    @Test
    void getLuhmannNumbers() {
    }

    @Test
    void getLuhmannNumbersAsString() {
    }

    @Test
    void getLuhmannNumbersAsInteger() {
    }

    @Test
    void getManualLinks() {
    }

    @Test
    void setManualLinks() {
    }

    @Test
    void getManualLinksAsString() {
    }

    @Test
    void getManualLinksAsSingleString() {
    }

    @Test
    void testSetManualLinks() {
    }

    @Test
    void getCurrentManualLinks() {
    }

    @Test
    void getCurrentManualLinksAsString() {
    }

    @Test
    void getAuthor() {
    }

    @Test
    void setAuthor() {
    }

    @Test
    void testSetAuthor() {
    }

    @Test
    void testSetAuthor1() {
    }

    @Test
    void testSetAuthor2() {
    }

    @Test
    void deleteAuthor() {
    }

    @Test
    void getEntryAsHtml() {
        // "converts" the data into a certain html layout
    }

    @Test
    void getZettelContent() {
        // plain entry content as it is stored in the XML-file, without htnml-conversion.
    }

    @Test
    void getZettelContentAsHtml() {
        // content is returned in HTML-format
    }

    @Test
    void getZettelRating() {
    }

    @Test
    void getZettelRatingCount() {
    }

    @Test
    void getZettelRatingAsString() {
    }

    @Test
    void addZettelRating() {
    }

    @Test
    void resetZettelRating() {
    }

    @Test
    void isEmpty() {
    }

    @Test
    void testIsEmpty() {
    }

    @Test
    void isDeleted() {
    }

    @Test
    void testIsDeleted() {
    }

    @Test
    void hasEntriesExcludingDeleted() {
    }

    @Test
    void getTimestamp() {
    }

    @Test
    void testGetTimestamp() {
    }

    @Test
    void getTimestampEdited() {
    }

    @Test
    void testGetTimestampEdited() {
    }

    @Test
    void getTimestampCreated() {
    }

    @Test
    void testGetTimestampCreated() {
    }

    @Test
    void getAttachments() {
    }

    @Test
    void hasAttachments() {
    }

    @Test
    void hasAuthors() {
    }

    @Test
    void hasKeywords() {
    }

    @Test
    void hasRemarks() {
    }

    @Test
    void getAttachmentsAsString() {
    }

    @Test
    void setAttachments() {
    }

    @Test
    void addAttachments() {
    }

    @Test
    void changeAttachment() {
    }

    @Test
    void deleteAttachment() {
    }

    @Test
    void getRemarks() {
    }

    @Test
    void setRemarks() {
    }

    @Test
    void getCleanRemarks() {
    }

    @Test
    void getCurrentKeywords() {
    }

    @Test
    void getKeywords() {
    }

    @Test
    void testGetKeywords() {
    }

    @Test
    void getSeparatedKeywords() {
    }

    @Test
    void getAuthors() {
    }

    @Test
    void getAuthorsWithIDandBibKey() {
    }

    @Test
    void getCount() {
    }

    @Test
    void addToHistory() {
    }

    @Test
    void canHistoryBack() {
    }

    @Test
    void canHistoryFore() {
    }

    @Test
    void historyBack() {
    }

    @Test
    void historyFore() {
    }

    @Test
    void gotoEntry() {
    }

    @Test
    void nextEntry() {
    }

    @Test
    void prevEntry() {
    }

    @Test
    void firstEntry() {
    }

    @Test
    void lastEntry() {
    }

    @Test
    void getKeywordIndexNumbers() {
    }

    @Test
    void setKeywordIndexNumbers() {
    }

    @Test
    void setAuthorIndexNumbers() {
    }

    @Test
    void getAuthorIndexNumbers() {
    }

    @Test
    void getKeywordFrequencies() {
    }

    @Test
    void testGetKeywordFrequencies() {
    }

    @Test
    void getKeywordFrequency() {
    }

    @Test
    void getAuthorFrequencies() {
    }

    @Test
    void testGetAuthorFrequencies() {
    }

    @Test
    void getAuthorFrequency() {
    }

    @Test
    void getAuthorBibKey() {
    }

    @Test
    void testGetAuthorBibKey() {
    }

    @Test
    void setAuthorBibKey() {
    }

    @Test
    void testSetAuthorBibKey() {
    }

    @Test
    void setCurrentZettelPos() {
    }

    @Test
    void setInitialHistoryPos() {
    }

    @Test
    void getCurrentZettelPos() {
    }

    @Test
    void getLinkStrength() {
    }

    @Test
    void getZettelTitle() {
    }

    @Test
    void setZettelTitle() {
    }

    @Test
    void changeEditTimeStamp() {
    }

    @Test
    void setZettelContent() {
    }

    @Test
    void getZettelContentUbbTagsRemoved() {
        // cleaned content of an entry
    }

    @Test
    void setKeywordlistUpToDate() {
    }

    @Test
    void isKeywordlistUpToDate() {
    }

    @Test
    void setClusterlistUpToDate() {
    }

    @Test
    void isClusterlistUpToDate() {
    }

    @Test
    void setAuthorlistUpToDate() {
    }

    @Test
    void isAuthorlistUpToDate() {
    }

    @Test
    void setTitlelistUpToDate() {
    }

    @Test
    void isTitlelistUpToDate() {
    }

    @Test
    void setAttachmentlistUpToDate() {
    }

    @Test
    void isAttachmentlistUpToDate() {
    }

    @Test
    void createExportEntries() {
    }


    @Test
    void fixWrongEditTags() {
    }

    @Test
    void db_updateZettelIDs() {
    }

    @Test
    void db_updateAuthorAndKeywordIDs() {
    }

    @Test
    void db_updateTimestampAttributes() {
    }

    @Test
    void db_updateRemoveZettelPosElements() {
    }

    @Test
    void db_updateInlineCodeFormatting() {
    }

    @Test
    void setTimestamp() {
    }

    @Test
    void setTimestampEdited() {
    }

    @Test
    void setTimestampCreated() {
    }

    @Test
    void testSetTimestampEdited() {
    }

    @Test
    void testSetTimestampCreated() {
    }

    @Test
    void testSetTimestamp() {
    }

    @Test
    void setZettelID() {
    }

    @Test
    void testSetZettelID() {
    }

    @Test
    void getZettelID() {
    }

    @Test
    void testGetZettelID() {
    }

    @Test
    void setLastAddedZettelID() {
    }

    @Test
    void getLastAddedZettelID() {
    }

    @Test
    void getAuthorID() {
    }

    @Test
    void testGetAuthorID() {
    }

    @Test
    void getKeywordID() {
    }

    @Test
    void testGetKeywordID() {
    }

    @Test
    void findZettelFromID() {
    }

    @Test
    void findAuthorFromID() {
    }

    @Test
    void findKeywordFromID() {
    }

    @Test
    void getZettelNumberFromID() {
    }

    @Test
    void getAuthorNumberFromID() {
    }

    @Test
    void getKeywordNumberFromID() {
    }

    @Test
    void zettelExists() {
    }

    @Test
    void testZettelExists() {
    }

    @Test
    void keywordExists() {
    }

    @Test
    void authorExists() {
    }

    @Test
    void hasAuthorID() {
    }

    @Test
    void hasKeywordID() {
    }

    @Test
    void setSaveOk() {
    }

    @Test
    void isSaveOk() {
    }

    @Test
    void getZettelForms() {
    }

    @Test
    void isTopLevelLuhmann() {
    }

    @Test
    void findParentlLuhmann() {
    }

    @Test
    void getAllLuhmannNumbers() {
    }

    @Test
    void testGetAllLuhmannNumbers() {
    }

    @Test
    void getAllManualLinks() {
    }

    @Test
    void hasLuhmannNumbers() {
    }

    @Test
    void hasManLinks() {
    }
}