package fxIllustratorColorParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* A class that parses all swatches from a given Adobe Illustrator file into fxCSS-Syntax.
* This way properly named swatchpanels from styleguides can be automatically used for css and do not have to be transferred by hand.
*
* Colornames of the format: "My color (<hexcode>)" will be turned into "-fx-my-color" for readability reasons
* Colornames of the format: "R=xyz G=xyz B=xyz" will be IGNORED!
*
* Colornames can be outputted in either "rgb(r, g, b)" or hexadecimal "#rrggbb" syntax.
*
* @author Fabian Wildgrube on 29/08/2017
*
 */
public class IllustratorColorParser {

    private String filename;
    private static int longestColorName = 0;
    private ArrayList<IllustratorColor> invalidColors;

    /**
     * Constructor
     * @param filename absolute Path to the illustrator file to parse
     */
    public IllustratorColorParser(String filename) {
        this.filename = filename;
    }

    /**
    *   Parses Colors from the objects Illustrator File
    *   @param mode -> describes which way the colors should be formatted for output (plain color names with rgb or hex values, or css-syntax names and rgb or hex definitions)
    *
    *   @return an ArrayList of Strings, where each element is one color in fxCSS-Syntax
     */
    public ArrayList<String> parseColors(ColorParseModes mode) throws IOException {
        BufferedReader br = null;
        ArrayList<IllustratorColor> colorsFound = new ArrayList<>();
        invalidColors = new ArrayList<>();
        ArrayList<String> outputcolors = new ArrayList<>();

        br = new BufferedReader(new FileReader(filename));

        String sCurrentLine;
        String filecontent = "";
        String endofmetadata = "</x:xmpmeta>";

        //Read file into single string. Stop reading lines, once the metadata of the illustrator file has ended.
        while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.equals(endofmetadata)) {
            filecontent += sCurrentLine;
        }
        //close File
        if (br != null) br.close();

        //Create a Regex-Matcher, that will find all Swatches in the filecontent
        Matcher m = createIllustratorColorMatcher(filecontent);
        //Find all swatches
        while (m.find()){
            //Extract the name and colorvalues from the block, that the Matcher found
            String colorname = m.group(1);
            String red = m.group(2);
            String green = m.group(3);
            String blue = m.group(4);

            //Create new Color from the values
            IllustratorColor readcolor = new IllustratorColor(colorname, red, green, blue);

            //valid colors will be scanned for their length (for formatting the output properly) and then added to the found-colours Arraylist
            if (readcolor.hasvalidColorname()){
                if (readcolor.getcssnamelength() > longestColorName) { longestColorName = readcolor.getcssnamelength(); }
                colorsFound.add(readcolor);
            } else {
                invalidColors.add(readcolor);
            }
        }

        //Convert Colors to the chosen Syntax before returning them
        for(IllustratorColor color : colorsFound){
            switch (mode){
                case HEX_CSS:
                    outputcolors.add(color.getHEXfxCSS());
                    break;
                case RGB_CSS:
                    outputcolors.add(color.getRGBfxCSS());
                    break;
                case HEX_PLAIN:
                    outputcolors.add(color.getHEXplain());
                    break;
                case RGB_PLAIN:
                    outputcolors.add(color.getRGBplain());
                    break;
            }
        }

        if (outputcolors.size() == 0) outputcolors.add("No valid colors were found.");

        return outputcolors;
    }


    /** Creates a Matcher for the given inputtext. This matcher will find Colors in the illustrator format
       Swatches in an illustrator File usually have this form:
            <rdf:li rdf:parseType="Resource">
               <xmpG:swatchName>R=6 G=6 B=7</xmpG:swatchName>
               <xmpG:type>PROCESS</xmpG:type>
               <xmpG:tint>100.000000</xmpG:tint>
               <xmpG:mode>RGB</xmpG:mode>
               <xmpG:red>6</xmpG:red>
               <xmpG:green>6</xmpG:green>
               <xmpG:blue>7</xmpG:blue>
            </rdf:li>
        Basic idea is to search the filecontent for blocks that look like this and extract the colorname and value.
        This is done by using a regular expression

        @param inputtext - the text, that will be searched for occurences of the regex
        @return A Matcher for the inputtext and the regex for illustrator swatches
    */
    private Matcher createIllustratorColorMatcher(String inputtext){
        //Swatches in an illustrator file have two different combinations of mode, type and tint information
        //To recognize and catch all swatches the regex needs to have an or statement for the different combinations
        //Swatchname is always first and the three color components are always last. The or statement will be in the middle

        String swatchname = "<xmpG:swatchName>([^<]*)</xmpG:swatchName>"; //includes a group to catch the actual name in the final regex
        String mode = "<xmpG:mode>RGB</xmpG:mode>";
        String type = "<xmpG:type>PROCESS</xmpG:type>";
        String tint = "<xmpG:tint>100.000000</xmpG:tint>";

        String mode_type = mode + "[ \t\n\f\r]*" + type;
        String type_tint_mode = type + "[ \t\n\f\r]*" + tint + "[ \t\n\f\r]*" + mode;

        String mode_typeORtype_tint_mode = "(?:" + mode_type + "|" + type_tint_mode + ")";

        String whitespaces = "[ \t\n\f\r]*";

        Pattern p=Pattern.compile("<rdf:li rdf:parseType=\"Resource\">[ \t\n\f\r]*" + swatchname + whitespaces + mode_typeORtype_tint_mode + whitespaces + "<xmpG:red>([0-9]{1,3})</xmpG:red>" + whitespaces + "<xmpG:green>([0-9]{1,3})</xmpG:green>" + whitespaces + "<xmpG:blue>([0-9]{1,3})</xmpG:blue>" + whitespaces + "</rdf:li>");
        return p.matcher(inputtext);
    }

    public ArrayList<IllustratorColor> getInvalidColors() {
        return invalidColors;
    }

    /**
     * Helper class to handle the conversion from separate strings for name and color values into JavaFX Syntax colors
     */
    static class IllustratorColor {
        private String colornameraw, colorname, colornameforcss, red, green, blue;
        private String hexcode;
        private boolean colornamevalid;

        public IllustratorColor(String name, String red, String green, String blue){
            //Read Color values
            this.red = red;
            this.green = green;
            this.blue = blue;

            //convert them to hexcode
            int nr_red = Integer.parseInt(red);
            String hex_red = Integer.toHexString(nr_red);

            int nr_green = Integer.parseInt(green);
            String hex_green = Integer.toHexString(nr_green);

            int nr_blue = Integer.parseInt(blue);
            String hex_blue = Integer.toHexString(nr_blue);

            this.hexcode = hex_red + hex_green + hex_blue;

            //Read Colorname
            this.colornamevalid = true;
            this.colornameraw = name;
            colorname = colornameraw.trim();

            //Check if colorname is valid
            //invalid color names will be ignored by the parser, i.e. they will not be part of the output list
            //Invalid is a color name of the form: "R=xyz G=xyz B=xyz"
            Pattern p = Pattern.compile("(?i)R=\\d{1,3} *G=\\d{1,3} *B=\\d{1,3} *(Kopie)?.*");
            Matcher m = p.matcher(colorname);
            if(m.matches()){
                colornamevalid = false;
            } else {
                //Valid colornames are brought into css Syntax -> remove unwanted characters, replace whitespace
                colorname = colorname.replaceAll("\\([^)]*\\)", ""); //cut off stuff like "(#ab42df)" from a color name
                colorname = colorname.replaceAll("[\\[\\]]", ""); //erase "[" or "]" from the name
                colorname = colorname.trim(); //trim remaining whitespace

                this.colornameforcss = "-fx-" + colorname.replaceAll("[.,_ ]*([.,_ ]| +)", "-").toLowerCase(); //replace all deliminators that a designer might use with "-"
            }
        }

        String getRGBfxCSS(){
            String fxcss = colornameforcss + ": ";

            //Whitespace for alignment of all color statements
            int namelengthdifference = longestColorName - this.getcssnamelength();
            for (int i = 0; i < namelengthdifference; i++){ fxcss += " "; }

            fxcss += "rgb(" + red + ", " + green + ", " + blue + ");";
            return  fxcss;
        }

        String getRGBplain(){
            String plainname = colorname + ": ";

            //Whitespace for alignment of all color statements
            int namelengthdifference = longestColorName - this.getplainnamelength();
            for (int i = 0; i < namelengthdifference; i++){ plainname += " "; }

            plainname += "Red: " + red + ", Green: " + green + ", Blue: " + blue;
            return plainname;
        }

        String getHEXfxCSS(){
            String fxcss = colornameforcss + ": ";

            //Whitespace for alignment of all color statements
            int namelengthdifference = longestColorName - this.getcssnamelength();
            for (int i = 0; i < namelengthdifference; i++){ fxcss += " "; }

            fxcss += "#" + hexcode + ";";
            return  fxcss;
        }

        String getHEXplain(){
            String plainname = colorname + ": ";

            //Whitespace for alignment of all color statements
            int namelengthdifference = longestColorName - this.getplainnamelength();
            for (int i = 0; i < namelengthdifference; i++){ plainname += " "; }

            plainname += "#" + hexcode + ";";
            return plainname;
        }

        int getcssnamelength(){ return colornameforcss.length(); }

        int getplainnamelength(){ return  colorname.length(); }

        @Override
        public String toString(){
            return colorname + ": (" + red + ", " + green + ", " + blue + ")";
        }

        @Override
        public boolean equals(Object other){
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof IllustratorColor))return false;
            IllustratorColor otherColor = (IllustratorColor) other;

            return this.hexcode.equals(otherColor.getHexcode());
        }

        boolean hasvalidColorname(){
            return colornamevalid;
        }

        public String getColornameraw() {
            return colornameraw;
        }

        private String getHexcode() {
            return hexcode;
        }
    }
}
