// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.uicommon;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import veriblock.wallet.core.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;

import java.io.File;
import java.text.NumberFormat;
import java.util.Optional;

public class ControlHelper {

    private static final Glyph CONFIRM_INFO = new Glyph("FontAwesome", FontAwesome.Glyph.QUESTION_CIRCLE).color(
        Color.valueOf("#ffffff")).sizeFactor(2);

    private static final Glyph CONFIRM_ALERT = new Glyph("FontAwesome", FontAwesome.Glyph.WARNING).color(
            Color.valueOf("orange")).sizeFactor(2);

    private static final Glyph CONFIRM_ERROR = new Glyph("FontAwesome", FontAwesome.Glyph.TIMES_CIRCLE).color(
            Color.valueOf("ff6666")).sizeFactor(2);

    private static final Glyph CONFIRM_SUCCESS = new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE).color(
            Color.valueOf("0ab45b")).sizeFactor(2);

    public static void setImage(ImageView image, String filePath) {
        //Must handle redistributable for deploy
        //set image like so:
        String fileRoot = "images/";

        image.setImage(new Image(Utils.resourceAsExternal(fileRoot + filePath)));
    }

    public static double getDouble(TextField textField)
    {
        String s = textField.getText();
        if (s.length() == 0)
        {
            return 0;
        }
        else {
            return Double.parseDouble(s);
        }
    }

    public static int getInteger(TextField textField, Integer resultIfBadInput)
    {
        String s = textField.getText();
        if (s.length() == 0)
        {
            return 0;
        }
        else {
            Integer result = resultIfBadInput;

            try {
                result = Integer.parseInt(s);
            }
            catch (Exception ex)
            {
                result = resultIfBadInput;
            }
            return result;
        }
    }

    public static void setStyleClass(Styleable control, String className)
    {
        if (control == null)
        {
            return;
        }
        control.getStyleClass().clear();
        control.getStyleClass().add(className);
    }

    public static void makePaneFillContainer(Pane childPanel, Pane parentPanel)
    {
        childPanel.prefWidthProperty().bind(parentPanel.widthProperty());
        childPanel.prefHeightProperty().bind(parentPanel.heightProperty());
    }

    public static void setToolTip(Control control, String tooltipText)
    {
        Tooltip t = control.getTooltip();
        if (t == null)
        {
            control.setTooltip(new Tooltip(tooltipText));
        }
        else
        {
            t.setText(tooltipText);
        }
    }

    public enum MaskType
    {
        None,
        DigitsOnly,
        VbkAddress,
        VbkAmountPositive,
        Hex
    }

    public static void setTextFieldProperties(TextInputControl textField, MaskType maskType)
    {
        Integer maxLength = 30; //default
        setTextFieldProperties(textField, maskType,  maxLength);
    }

    public static void setTextFieldProperties(TextInputControl textField, MaskType maskType, Integer maxLength) {

        //set length, inferred from mask type


        // add masking:
        textField.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable, String oldValue,
                                 String newValue) {

                 //avoid: local variables referenced from an inner class must be final or effectively final
                 Integer finalMaxLength = maxLength;

                 switch (maskType) {

                     case VbkAddress:
                         finalMaxLength = 36;

                         // ^V[A-Za-z0-9]{0,36}$
                         /*
                         Must be able to type it as we go
                        YES:
                        V4HHyTxGHaxXzNCyy6t9Ce5fn5HTET
                        NO:
                        ascbsjdfjdsfd

                          */
                         if (newValue.length() == 0)
                         {
                             //ok
                         }
                         else if (!newValue.matches("^V[A-Za-z0-9]{0,35}$")) {
                             textField.setText(oldValue);
                         }
                         break;

                     case VbkAmountPositive:
                         finalMaxLength = 17;
                         //Allow up to 8 decimals
                         //Set mask
                         // ^(\d+(?:\.\d{0,8})?)$
                         /*
                        0.123
                        5.65
                        23
                        345.12345678

                        No:
                        aaaa
                        345.123456789
                         */
                         if (!newValue.matches("^(\\d{0,8}(?:\\.\\d{0,8})?)$")) {
                             textField.setText(oldValue);
                         }
                         break;

                     case DigitsOnly:
                         //Set mask
                         if (!newValue.matches("\\d*")) {
                             textField.setText(newValue.replaceAll("[^\\d]", ""));
                         }
                         break;

                     case Hex:
                         if (newValue.length() == 0)
                         {
                             //ok
                         }
                         else if (!newValue.matches("^[ABCDEFabcdef0-9]*$")) {
                             textField.setText(oldValue);
                         }
                         break;
                 }


                 //Update max length
                 if (textField != null && textField.getText() != null) {
                     if (textField.getText().length() > finalMaxLength) {
                         String s = textField.getText().substring(0, finalMaxLength);
                         textField.setText(s);
                     }
                 }
             }
         }
        );
    }

    //Adds the '<' and '>'
    public static void setPromptText(TextInputControl textField, String promptText)
    {
        promptText = "<" + promptText + ">";
        textField.setPromptText(promptText);
    }

    public static void setLabelCopyProperties(TextInputControl textField)
    {
        setLabelCopyProperties(textField, false);
    }
    public static void setLabelCopyProperties(TextInputControl textField, boolean setStyle)
    {
        if (textField == null)
        {
            return;
        }
        //Use a textfield as a label.
        //Use CSS to hide the styles
        textField.setEditable(false);

        if(setStyle) {
            ControlHelper.setStyleClass(textField, "copyable-label");
            if (textField instanceof TextArea)
            {
                textField.getStyleClass().add("copyable-labelTextArea");
            }
        }
    }

    //region Dialogs

    public static Object showCustomDialog(String pageName, AppContext appContext, String promptMessage)
    {
        return showCustomDialog(pageName, appContext, promptMessage, null);
    }

    public static Object showCustomDialog(String pageName, AppContext appContext, String promptMessage, NavigationData navigationData) {

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        // Create the custom dialog.
        Dialog<Object> dialog = new Dialog<>();

        //NOTE JavaFX hack --> window red X close button won't work unless a CANCEL/CLOSE is added.
        //But this will add a button to the lower bar, and bypass any workflow. User could just exit
        //ButtonType bt = new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE);
        //dialog.getDialogPane().getButtonTypes().add(bt);

        dialog.setTitle(lm.getString("window.dialog.title"));
        if (promptMessage != null) {
            dialog.setHeaderText(promptMessage);
        }

        try {
            FXMLLoader loader = ControlBuilder.getFXMLLoader(pageName);

            //Add frame
            AnchorPane panelOuter = new AnchorPane();
            ControlHelper.setStyleClass(panelOuter, "mainPane");

            AnchorPane panel = loader.load();

            BorderPane border = new BorderPane();
            panelOuter.getChildren().add(border);

            double borderSize = 5;
            AnchorPane.setBottomAnchor(border, borderSize);
            AnchorPane.setLeftAnchor(border, borderSize);
            AnchorPane.setRightAnchor(border, borderSize);
            AnchorPane.setTopAnchor(border, borderSize);

            border.setCenter(panel);

            DialogController controller = loader.getController();
            controller.initData(appContext, navigationData);   //must pass this in!
            controller.setDialog(dialog);

            dialog.getDialogPane().setContent(panelOuter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        //Set style
        //UNDECORATED --> won't be able to move window, won't have border (including no Red X)
        //DECORATED --> still has border and red X
        dialog.initStyle(StageStyle.UTILITY);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Utils.resourceAsExternal("default.css"));
        dialogPane.getStyleClass().add("dialog");

        Optional<Object> result = dialog.showAndWait();
        if (result == null || result == Optional.empty())
        {
            return null;
        }
        else
        {
            return result.get();
        }
    }

    public static boolean showAlertYesNoDialog(ValidationInfo vi)
    {
        return showAlertYesNoDialog(vi, null);
    }
    public static boolean showAlertYesNoDialog(ValidationInfo vi, String strCopyableText) {
        Alert alert = _showStandardDialog(vi, strCopyableText, Alert.AlertType.CONFIRMATION);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }

    public static boolean showAlertYesNoDialog(String message)
    {
        ValidationInfo vi = new ValidationInfo();
        vi.setMessageInfo(message);
        return showAlertYesNoDialog(vi, null);
    }

    public static void showConfirmDialog(ValidationInfo vi) {
        showConfirmDialog(vi, null);
    }
    public static void showConfirmDialog(ValidationInfo vi, String strCopyableText) {
        Alert alert = _showStandardDialog(vi, strCopyableText, Alert.AlertType.INFORMATION);
        alert.showAndWait();
    }

    private static Alert _showStandardDialog(ValidationInfo vi, String strCopyableText, Alert.AlertType alertType) {
        String message = vi.getMessage();

        LocaleModule lm = LocaleManager.getInstance().getModule(LocaleModuleResource.Main);

        Alert alert = new Alert(alertType);
        //alert.setTitle(lm.getString("general.confirmYesNo"));
        alert.setTitle(lm.getString("window.dialog.title"));
        alert.setHeaderText(message);

        //Set copyable section
        if (strCopyableText != null) {
            TextArea textCopy = new TextArea();
            textCopy.setMinHeight(300.0);
            textCopy.setWrapText(true);
            //textCopy.setMinWidth(300);
            //textCopy.setMinHeight(200);
            textCopy.setText(strCopyableText);
            ControlHelper.setLabelCopyProperties(textCopy);
            ControlHelper.setStyleClass(textCopy, "copyable-label");
            textCopy.getStyleClass().add("dialog-text-area");   //override
            alert.getDialogPane().setContent(textCopy);
        }

        if (vi.isWarning()) {
            alert.setGraphic(CONFIRM_ALERT);
        }
        else if (vi.isError())
        {
            alert.setGraphic(CONFIRM_ERROR);
        }
        else if (vi.isSuccess())
        {
            alert.setGraphic(CONFIRM_SUCCESS);
        }
        else {
            alert.setGraphic(CONFIRM_INFO);
        }
        //alert.initStyle(StageStyle.UNDECORATED);
        alert.initStyle(StageStyle.UTILITY);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Utils.resourceAsExternal("default.css"));
        dialogPane.getStyleClass().add("dialog");

        return alert;
    }

    public static String clickChooseNCDirectory(AppContext appContext, String strTitle, String strDefaultDir)
    {
        Stage primaryStage = appContext.UIManager.getPrimaryStage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(strTitle);
        directoryChooser.setInitialDirectory(new File(strDefaultDir));
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        String resultDir = "";

        if(selectedDirectory == null){
            resultDir = null;
        }else{
            resultDir = selectedDirectory.getAbsolutePath();
        }
        return resultDir;
    }


    //endregion

    public static void setTableViewNoResultsFoundMessage(TableView grid, String message)
    {
        grid.setPlaceholder(new Label(message));
    }

    //------------------

    //region TableView

    //Based on https://gist.github.com/Roland09/6fb31781a64d9cb62179
    //modified to just pull text
    //NOTE - does not work with invisible columns (messes up the order)
    //So put all invisible columns at the end of the grid
    public static void installCopyPasteHandler(TableView<?> table) {

        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // install copy/paste keyboard handler
        table.setOnKeyPressed(new TableKeyEventHandler());

        setupContextMenu(table);

    }

    private static void setupContextMenu(TableView<?> table) {
        ContextMenu cm = new ContextMenu();

        String strCopyLabel = (LocaleManager.getInstance().getModule(LocaleModuleResource.Main).getString("general.grid.rightClick.CopyText"));
        MenuItem mi1 = new MenuItem(strCopyLabel);

        mi1.setOnAction((ActionEvent event) -> {
            copySelectionToClipboard(table);
        });

        cm.getItems().add(mi1);

        table.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if (t.getButton() == MouseButton.SECONDARY) {
                    cm.show(table, t.getScreenX(), t.getScreenY());
                }
            }
        });
    }

    /**
     * Copy/Paste keyboard event handler.
     * The handler uses the keyEvent's source for the clipboard data. The source must be of type TableView.
     */
    private static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination pasteKeyCodeCompination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);

        public void handle(final KeyEvent keyEvent) {

            if (copyKeyCodeCompination.match(keyEvent)) {

                if( keyEvent.getSource() instanceof TableView) {

                    // copy to clipboard
                    copySelectionToClipboard( (TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();

                }

            }
            else if (pasteKeyCodeCompination.match(keyEvent)) {

                if( keyEvent.getSource() instanceof TableView) {

                    // copy to clipboard
                   // pasteFromClipboard( (TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();

                }

            }

        }

    }

    /**
     * Get table selection and copy it to the clipboard.
     * @param table
     */
    private static void copySelectionToClipboard(TableView<?> table) {

        StringBuilder clipboardString = new StringBuilder();

        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;

        for (TablePosition position : positionList) {

            int row = position.getRow();
            int col = position.getColumn();

            // determine whether we advance in a row (tab) or a column
            // (newline).
            if (prevRow == row) {

                clipboardString.append('\t');

            } else if (prevRow != -1) {

                clipboardString.append('\n');

            }

            // create string from cell
            String text = "";

            Object observableValue = table.getColumns().get(col).getCellObservableValue( row);

            // null-check: provide empty string for nulls
            if (observableValue == null) {
                text = "";
            }
            else {
                //just get text
                text = ((ReadOnlyObjectWrapper) observableValue).getValue().toString();
            }
            /*
            else if( observableValue instanceof DoubleProperty) { // TODO: handle boolean etc

                text = numberFormatter.format( ((DoubleProperty) observableValue).get());

            }
            else if( observableValue instanceof IntegerProperty) {

                text = numberFormatter.format( ((IntegerProperty) observableValue).get());

            }
            else if( observableValue instanceof StringProperty) {

                text = ((StringProperty) observableValue).get();

            }
            else {
                System.out.println("Unsupported observable value: " + observableValue);
            }
*/

            // add new item to clipboard
            clipboardString.append(text);

            // remember previous
            prevRow = row;
        }

        // create clipboard content
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());

        // set clipboard content
        Clipboard.getSystemClipboard().setContent(clipboardContent);

    }

    private static NumberFormat numberFormatter = NumberFormat.getNumberInstance();

    //endregion

    //Created a hyperlink, passes in the selected row to its click method
    public static <S, T> void setGridColumnHyperlink(TableColumn<S,T> myColumn, GenericFunction methodClick)
    {
        myColumn.setCellFactory(
                new Callback<TableColumn<S, T>, TableCell<S, T>>() {

                    @Override
                    public TableCell<S, T> call(final TableColumn<S, T> param) {
                        final TableCell<S, T> cell = new TableCell<S, T>() {

                            //Use final, else UI gets screwy
                            //final Button btn = new Button("...");
                            final Hyperlink btn = new Hyperlink("...");

                            //NOTE --> this could be re-called, it MUST be idempotent
                            @Override
                            public void updateItem(T item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {

                                    int iRowIndex = getIndex();

                                    btn.setOnAction(event -> {
                                        methodClick.doWork (myColumn.getTableView().getItems().get(iRowIndex));
                                    });

                                    btn.setText(item.toString());

                                    //Setting class didn't work, needed to set styles directly
                                    btn.setStyle(Styles.CSS_GRID_LINK_COLOR);

                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                }
        );
    }
}
