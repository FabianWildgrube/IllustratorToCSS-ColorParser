package fxIllustratorColorParser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller class for handling view interactions of the Illustrator Parser App
 *
 * @author Fabian Wildgrube, 08/29/2017
 */
public class IllustratorColorParserController implements Initializable {

    @FXML TextField filename_txtfld;
    @FXML RadioButton radio_hex_css;
    @FXML RadioButton radio_rgb_css;
    @FXML RadioButton radio_rgb_plain;
    @FXML RadioButton radio_hex_plain;
    @FXML TextArea txtarea_output;

    private ToggleGroup parseModeToggles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parseModeToggles = new ToggleGroup();
        radio_hex_css.setToggleGroup(parseModeToggles);
        radio_rgb_css.setToggleGroup(parseModeToggles);
        radio_hex_plain.setToggleGroup(parseModeToggles);
        radio_rgb_plain.setToggleGroup(parseModeToggles);
        radio_hex_css.setSelected(true);
        radio_rgb_css.setSelected(false);
        filename_txtfld.setFocusTraversable(false);
    }


    /**
     * Uses an IllustratorColorParser to parse Colors from the specified File and display them in the Textarea
     */
    @FXML
    private void parseColors(){

        String filename = filename_txtfld.getText();

        try {
            IllustratorColorParser colorParser = new IllustratorColorParser(filename);
            ArrayList<String> parsedColors = colorParser.parseColors(getSelectedParseMode());

            String output = "";
            for(String color : parsedColors){
                output = output.concat(color + "\n");
            }
            txtarea_output.setText(output);

            if (colorParser.getInvalidColors().size() > 0){ //Show Invalid Colornames if there are any
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Colors found");
                alert.setHeaderText("There were " + colorParser.getInvalidColors().size() + " unnamed colors in your file!");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(this.getClass().getResource("images/illustratorColorParserIcon50.png").toString()));
                String invalidColorNames = "";
                int counter = 0;
                for (IllustratorColorParser.IllustratorColor color : colorParser.getInvalidColors()){
                    counter++;
                    if (counter > 20){
                        invalidColorNames += "...";
                        break;
                    } else {
                        invalidColorNames += color.toString() + "\n";
                    }
                }

                alert.setContentText(invalidColorNames);
                alert.showAndWait();
            }
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Reading File");
            alert.setHeaderText("There was an error reading the file");
            alert.setContentText("Please choose an intact illustrator file by clicking the button next to the Textfield that says 'Filename' and browsing to the location of your file.");

            alert.showAndWait();
        }
    }

    @FXML
    private void chooseFile(){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify your illustrator file");
        fileChooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("Adobe Illustrator", "*.ai") ); //Allow only illustrator files
        File file = fileChooser.showOpenDialog(stage);
        if (file != null){
            filename_txtfld.setText(file.getAbsolutePath());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Please select a file");
            alert.setHeaderText("You didn't select a file");
            alert.setContentText("Please make sure to select a file before trying to parse one ;)");

            alert.showAndWait();
        }
    }

    /**
     * Copies the contents of the Textarea to the clipboard.
     * Displays a warning if Textarea doesn't have any content
     */
    @FXML
    private void copyToClipboard(){
        if (txtarea_output.getText().length() > 2){
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(txtarea_output.getText());
            clipboard.setContent(content);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Copied to Clipboard");
            alert.setHeaderText(null);
            alert.setContentText("The Code was copied to your clipboard.");

            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error Copying");
            alert.setHeaderText("There is no code to copy");
            alert.setContentText("Please select and parse a file first.");

            alert.showAndWait();
        }
    }

    private ColorParseModes getSelectedParseMode(){
        RadioButton selectedRadioBtn = (RadioButton) parseModeToggles.getSelectedToggle();

        if (selectedRadioBtn.equals(radio_hex_css)){
            return ColorParseModes.HEX_CSS;
        } else if (selectedRadioBtn.equals(radio_hex_plain)){
            return  ColorParseModes.HEX_PLAIN;
        } else if (selectedRadioBtn.equals(radio_rgb_css)){
            return  ColorParseModes.RGB_CSS;
        } else if (selectedRadioBtn.equals(radio_rgb_plain)){
            return  ColorParseModes.RGB_PLAIN;
        } else {
            return ColorParseModes.HEX_CSS;
        }
    }


}
