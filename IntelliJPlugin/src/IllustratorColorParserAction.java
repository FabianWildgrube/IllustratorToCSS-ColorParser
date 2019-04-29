import fxIllustratorColorParser.ColorParseModes;
import fxIllustratorColorParser.IllustratorColorParser;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Action for the illustrator Color parser IntelliJ plugin
 * The adds an action to the popup menu in the editor window.
 * Clicking that action opens a filechooser dialog, which lets the user select an illustrator file
 * The colors are parsed in fx-css hex syntax and pasted into the document at the current cursor position
 */
public class IllustratorColorParserAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        fileChooserDescriptor.setTitle("Select an Illustrator File");
        fileChooserDescriptor.setDescription("The Illustrator File will be scanned for colors. These will be converted into JavaFX CSS Syntax and pasted into your editor window.");
        fileChooserDescriptor.withFileFilter(new Condition<VirtualFile>() {
            @Override
            public boolean value(VirtualFile virtualFile) {
                return virtualFile.getExtension().equals("ai");
            }
        });

        FileChooserDialog fileChooserDialog =
                FileChooserFactory.getInstance().createFileChooser(fileChooserDescriptor, project, null);

        VirtualFile rootFolder = project.getBaseDir();
        VirtualFile[] files = fileChooserDialog.choose(project, rootFolder);
        if (files.length > 0) {
            parseAndInsertColors(anActionEvent, files[0].getPath());
        }


    }

    @Override
    public void update(AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null)
            return;
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(editor != null);
        event.getPresentation().setIcon(IconLoader.getIcon("/illustratorColorParserIcon20.png"));
    }

    void parseAndInsertColors(AnActionEvent anActionEvent, String absoluteFilePath){
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        //New instance of Runnable to make a replacement
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                IllustratorColorParser myParser = new IllustratorColorParser(absoluteFilePath);
                ArrayList<String> colors = null;
                try {
                    colors = myParser.parseColors(ColorParseModes.HEX_CSS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (colors != null){
                    String insertStr = "";
                    for (String color : colors){
                        insertStr += color + "\n";
                    }
                    document.replaceString(start, end, insertStr);
                } else {
                    System.out.println("Colors could not be parsed.");
                }
            }
        };
        //Making the replacement
        WriteCommandAction.runWriteCommandAction(project, runnable);
        selectionModel.removeSelection();
    }
}
