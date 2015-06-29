/*
 * Copyright (c) 2015, draque
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package PolyGlot;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * This is a helper class, which deals with formatted text in Java
 *
 * @author draque
 */
public class FormattedTextHelper {
    private final static String black = "black";
    private final static String red = "red";
    private final static String gray = "gray";
    private final static String green = "green";
    private final static String yellow = "yellow";
    private final static String blue = "blue";
    private final static String color = "color";
    private final static String face = "face";
    private final static String size = "size";

    /**
     * Restores to the JTextPane the formatted text values encoded in the saved
     * value string
     * @param savedVal value to restore formatted text from
     * @param pane Text pane to restore formatted text to.
     * @param core Dictionary Core (needed for references)
     * @throws javax.swing.text.BadLocationException if unable to load
     */
    public static void restoreFromString(String savedVal, JTextPane pane, DictCore core) throws BadLocationException {
        String remaining = savedVal;
        pane.setText("");
        Color fontColor = Color.black;
        String font = "";
        int fontSize = -1;
                
        while (!remaining.isEmpty()) {
            String nextNode = getNextNode(remaining);
            Font conFont = core.getPropertiesManager().getFontCon();
            
            remaining = remaining.substring(nextNode.length(), remaining.length());
            
            if (nextNode.startsWith("<font")) {
                
                font = extractFamily(nextNode);
                fontSize = extractSize(nextNode);
                fontColor = extractColor(nextNode);
                
                if (font.equals(conFont.getFamily())) {
                    font = PGTUtil.conLangFont;
                }
            } else if (nextNode.startsWith("</font")) {
                // do nothing
            } else {
                Document doc = pane.getDocument();
                
                MutableAttributeSet aset = new SimpleAttributeSet();
                if (font.equals(PGTUtil.conLangFont)) {
                    nextNode = "\u202e" + nextNode;
                    StyleConstants.setFontFamily(aset, conFont.getFamily());
                } else if (!font.equals("")) {
                    // TODO: In the future, consider whether to leverege this logic to allow arbitrary fonts in grammar section text
                    nextNode = "\u202c" + nextNode;
                    StyleConstants.setFontFamily(aset, font);
                } else {
                    nextNode = "\u202c" + nextNode;
                }
                
                if (fontSize != -1) {
                    StyleConstants.setFontSize(aset, fontSize);
                }
                
                StyleConstants.setForeground(aset, fontColor);
                
                if (!nextNode.equals("")){
                    doc.insertString(doc.getLength(), nextNode, aset);
                }
            }
        }
    }
    
    /**
     * Given an HTML <font~> node, return the font family
     * @param targetNode string value of HTML node
     * @return string value of family name, blank if none
     */
    private static Color extractColor(String targetNode) {
        Color ret = Color.black;
        
        int pos = targetNode.indexOf(color) + 7;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        strip = strip.substring(0, pos);
        
        switch (strip) {
            case black:
                ret = Color.black;
                break;
            case red:
                ret = Color.red;
                break;
            case blue:
                ret = Color.blue;
                break;
            case gray:
                ret = Color.gray;
                break;
            case yellow:
                ret = Color.yellow;
                break;
            case green:
                ret = Color.green;
                break;
            default:
                ret = Color.black;
                break;
        }
                
        return ret;
    }
    
    /**
     * Given an HTML <font~> node, return the font family
     * @param targetNode string value of HTML node
     * @return string value of family name, blank if none
     */
    private static String extractFamily(String targetNode) {
        String ret = "";
        
        int pos = targetNode.indexOf(face) + 6;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        ret = strip.substring(0, pos);
        
        return ret;
    }
    
    /**
     * Given an HTML <font~> node, return the font size
     * @param targetNode string value of HTML node
     * @return integer value of family size, -1 if none
     */
    private static int extractSize(String targetNode) {
        int ret = -1;
        
        int pos = targetNode.indexOf(size) + 6;
        
        if (pos == -1) {
            return ret;
        }
        
        String strip = targetNode.substring(pos);
        pos = strip.indexOf("\"");
        ret = Integer.parseInt(strip.substring(0, pos));
        
        return ret;
    }
    
    /**
     * Gets next node from saved text
     * @param fromText remaining text from which to pull node
     * @return next node in string form
     */
    private static String getNextNode(String fromText) {
        String ret;
        
        if (fromText.startsWith("<font") ||
                fromText.startsWith("</font")) {
            int pos = fromText.indexOf('>');
            ret = fromText.substring(0, pos + 1);
        } else {
            int posStart = fromText.indexOf("<font");
            int posEnd = fromText.indexOf("</font");
            
            // get the nearest start/end of a font ascription
            int pos;
            if (posStart == -1 && posEnd == -1) {
                pos = fromText.length();
            } else if (posStart == -1) {
                pos = posEnd;
            } else if (posEnd == -1) {
                pos = posStart;
            } else if (posStart < posEnd) {
                pos = posStart;
            } else {
                pos = posEnd;
            }
            
            ret = fromText.substring(0, pos);
        }
        
        return ret;
    }
    
    /**
     * Creates and returns string representing complex formatted text, which 
     * can be saved. Filters out all RTL and LTR characters before returning.
     * @param pane JTextPane containing text to be saved
     * @return encoded values of pane
     * @throws BadLocationException if unable to create string format
     */
    public static String storageFormat(JTextPane pane) throws BadLocationException {
        String ret = storeFormatRecurse(pane.getDocument().getDefaultRootElement(), pane);
        return ret.replace("\u202e", "").replace("\u202c", "");
    }

    /**
     * Recursing method implementing functionality of storageFormat()
     * @param e element to be cycled through
     * @param pane top parent JTextPane
     * @return string format value of current node and its children
     * @throws BadLocationException if unable to create string format
     */
    private static String storeFormatRecurse(Element e, JTextPane pane) throws BadLocationException {
        String ret = "";
        int ec = e.getElementCount();

        if (ec == 0) {
            int start = e.getStartOffset();
            int len = e.getEndOffset() - start;
            if (start < pane.getDocument().getLength()) {
                AttributeSet a = e.getAttributes();
                String font = StyleConstants.getFontFamily(a);
                String fontColor = colorToText(StyleConstants.getForeground(a));
                int fontSize = StyleConstants.getFontSize(a);
                ret += "<font face=\"" + font + "\""
                        + "size=\"" + fontSize + "\""
                        + "color=\"" + fontColor + "\"" + ">";
                ret += pane.getDocument().getText(start, len);
                ret += "</font>";
            }
        } else {
            for (int i = 0; i < ec; i++) {
                ret += storeFormatRecurse(e.getElement(i), pane);
            }
        }

        return ret;
    }

    /**
     * Gets standardized string value for color
     *
     * @param c color to get standard value for
     * @return string format standard value
     */
    public static String colorToText(Color c) {
        String ret;

        // Java 1.6 can't switch on an enum...
        if (c == Color.black) {
            ret = black;
        } else if (c == Color.red) {
            ret = red;
        } else if (c == Color.green) {
            ret = green;
        } else if (c == Color.yellow) {
            ret = yellow;
        } else if (c == Color.blue) {
            ret = blue;
        } else if (c == Color.gray) {
            ret = gray;
        } else {
            ret = black;
        }

        return ret;
    }
    
    public static Color textToColor(String color) {
        Color ret;
        
        switch (color) {
            case black:
                ret = Color.black;
                break;
            case red:
                ret = Color.red;
                break;
            case green:
                ret = Color.green;
                break;
            case yellow:
                ret = Color.yellow;
                break;
            case blue:
                ret = Color.blue;
                break;
            case gray:
                ret = Color.gray;
                break;
            default:
                ret = Color.black;
                break;
        }
        
        return ret;
    }
}
