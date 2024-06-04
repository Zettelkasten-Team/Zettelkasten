package ch.dreyeck.zettelkasten.box;

import ch.dreyeck.zettelkasten.xml.Zettel;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

public class ZettelController {

    @FXML
    public TextArea textAreaZettelTitle;

    @FXML
    public HTMLEditor htmlEditorZettelContent;

    private Stage stageZettel = new Stage();

    public void show(Zettel selectedItem, Scene scene) {
        if (selectedItem != null) {
            textAreaZettelTitle.setText(selectedItem.getTitle());
            htmlEditorZettelContent.setHtmlText(selectedItem.getContent());
        } else add(selectedItem);

        stageZettel.setScene(scene);
        stageZettel.setTitle("Zettel");
        stageZettel.show();
    }

    public void add(Zettel selectedItem) {
    }
}
