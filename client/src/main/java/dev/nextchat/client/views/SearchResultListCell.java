package dev.nextchat.client.views;

import dev.nextchat.client.models.Model;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SearchResultListCell extends ListCell<Model.SearchResultItem> {
    private HBox hbox = new HBox(10);
    private FontAwesomeIconView iconView = new FontAwesomeIconView();
    private VBox textVBox = new VBox(2);
    private Label nameLabel = new Label();
    private Label descriptionLabel = new Label();

    public SearchResultListCell() {
        super();
        iconView.setSize("2em");
        descriptionLabel.setStyle("-fx-font-size: 1.2em; -fx-text-fill: #666;");
        textVBox.getChildren().addAll(nameLabel, descriptionLabel);
        hbox.getChildren().addAll(iconView, textVBox);
        hbox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(Model.SearchResultItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            nameLabel.setText(item.name());

            if (item.type() == Model.SearchResultType.USER) {
                iconView.setGlyphName(FontAwesomeIcon.USER.name());
                iconView.setFill(Color.DODGERBLUE);
                descriptionLabel.setText("User");
            } else if (item.type() == Model.SearchResultType.GROUP) {
                iconView.setGlyphName(FontAwesomeIcon.GROUP.name());
                iconView.setFill(Color.FORESTGREEN);
                descriptionLabel.setText(
                        item.description() != null && !item.description().isEmpty()
                                ? item.description()
                                : "Group"
                );
            }

            setGraphic(hbox);
        }
    }
}
