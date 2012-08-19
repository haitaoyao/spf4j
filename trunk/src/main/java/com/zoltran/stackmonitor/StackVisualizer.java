/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zoltran.stackmonitor;

import com.zoltran.base.HtmlUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author zoly
 */
public class StackVisualizer {

    private StackVisualizer() {
    }
    private static final String[] COLORS = {"#CCE01B",
        "#DDE01B", "#EEE01B", "#FFE01B", "#FFD01B",
        "#FFC01B", "#FFA01B", "#FF901B", "#FF801B",
        "#FF701B", "#FF601B", "#FF501B", "#FF401B"};

    public static void generateHtmlTable(Writer writer, Method m, SampleNode node, int tableWidth, int maxDepth) throws IOException {
        Map<Method, SampleNode> subNodes = node.getSubNodes();
        writer.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"overflow:hidden;table-layout:fixed;width:").
                append(Integer.toString(tableWidth)).append("px\">\n");
        int totalSamples = node.getCount();

        if (subNodes != null && maxDepth > 0) {
            writer.append("<tr style=\"height:1em\">");
            for (Map.Entry<Method, SampleNode> entry : subNodes.entrySet()) {
                int width = entry.getValue().getCount() * tableWidth / totalSamples;
                writer.append("<td style=\"vertical-align:bottom; width:").append(Integer.toString(width)).append("px\">");
                generateHtmlTable(writer, entry.getKey(), entry.getValue(), width, maxDepth - 1);
                writer.append("</td>");
            }
            writer.append("<td></td>");
            writer.append("</tr>\n");
        }
        writer.append("<tr style=\"height:1em\" ><td ");
        if (subNodes != null) {
            writer.append("colspan=\"").append(Integer.toString(subNodes.size() + 1)).append("\" ");
        }
        writer.append(" title=\"");
        m.toHtmlWriter(writer);
        writer.append(":");
        writer.append(Integer.toString(node.getCount()));
        writer.append("\" style=\"overflow:hidden;width:100%;vertical-align:bottom ;background:").
                append(COLORS[(int) (Math.random() * COLORS.length)]).append("\">");
        m.toHtmlWriter(writer);
        writer.append(":");
        writer.append(Integer.toString(totalSamples));
        writer.append("</td></tr>\n");

        writer.append("</table>\n");
    }

    public static void generateSvg(Writer writer, Method m, SampleNode node, int x, int y, int width, int maxDepth, String idPfx) throws IOException {
        writer.append("<svg width=\"" + width + "\" height= \"" + (15 * node.height() + 15) + "\" onload=\"" + idPfx + "init(evt)\" >\n");
        writer.append("<script type=\"text/ecmascript\">\n"
                + "<![CDATA[\n"
                + "var " + idPfx + "tooltip;\n"
                + "var " + idPfx + "tooltip_bg;\n"
                + "function " + idPfx + "init(evt)\n"
                + "  {\n"
                + "    if ( window.svgDocument == null )\n"
                + "    {\n"
                + "      svgDocument = evt.target.ownerDocument;\n"
                + "    }\n"
                + "    " + idPfx + "tooltip = svgDocument.getElementById('" + idPfx + "tooltip');"
                + "" + idPfx + "tooltip_bg = svgDocument.getElementById('" + idPfx + "tooltip_bg');"
                + "  }\n"
                + "function " + idPfx + "ss(evt, mouseovertext, xx, yy)\n"
                + "{\n"
                + "  " + idPfx + "tooltip.setAttributeNS(null,\"x\",xx );\n"
                + "  " + idPfx + "tooltip.setAttributeNS(null,\"y\",yy+13 );\n"
                + "  " + idPfx + "tooltip.firstChild.data = mouseovertext;\n"
                + "  " + idPfx + "tooltip.setAttributeNS(null,\"visibility\",\"visible\");\n"
                + "length = " + idPfx + "tooltip.getComputedTextLength();\n"
                + "" + idPfx + "tooltip_bg.setAttributeNS(null,\"width\",length+8);\n"
                + " " + idPfx + "tooltip_bg.setAttributeNS(null,\"x\",xx);\n"
                + "" + idPfx + "tooltip_bg.setAttributeNS(null,\"y\",yy);\n"
                + "" + idPfx + "tooltip_bg.setAttributeNS(null,\"visibility\",\"visibile\");"
                + "}"
                + "\n"
                + "function " + idPfx + "hh()\n"
                + "{\n"
                + "  " + idPfx + "tooltip.setAttributeNS(null,\"visibility\",\"hidden\");\n"
                + "" + idPfx + "tooltip_bg.setAttributeNS(null,\"visibility\",\"hidden\");\n"
                + "}"
                + "]]>"
                + "</script>");

        generateSubSvg(writer, m, node, x, y + 15, width - 100, maxDepth, idPfx);

        writer.append("<rect fill=\"rgb(255,255,255)\" id=\"" + idPfx + "tooltip_bg\"\n"
                + "      x=\"0\" y=\"0\" rx=\"4\" ry=\"4\"\n"
                + "      width=\"55\" height=\"17\" visibility=\"hidden\"/>");
        writer.append("<text font-size=\"12\" font-family=\"Verdana\" fill=\"rgb(0,0,0)\"  id=\"" + idPfx + "tooltip\" x=\"0\" y=\"0\" visibility=\"hidden\">Tooltip</text>");
        writer.append("</svg>\n");
    }

    public static void generateSubSvg(Writer writer, Method m, SampleNode node, int x, int y, int width, int maxDepth, String idPfx) throws IOException {


        Map<Method, SampleNode> subNodes = node.getSubNodes();

        int totalSamples = node.getCount();
        String id = idPfx + "ix" + x + "y" + y;
        String content = HtmlUtils.htmlEscape(m.toString() + ":" + Integer.toString(totalSamples));
        writer.append("<g onmouseover=\"" + idPfx + "ss(evt,'" + content + "'," + x + ", " + y + " )\" onmouseout=\"" + idPfx + "hh()\">");
        writer.append("<rect id=\"" + id + "\" x=\"" + x + "\" y=\"" + y + "\" width=\"" + width + "\" height=\"15\" fill=\""
                + COLORS[(int) (Math.random() * COLORS.length)] + "\"  />");

        writer.append("<text x=\"" + x + "\" y=\"" + (y + 13) + "\" font-size=\"12\" font-family=\"Verdana\" fill=\"rgb(0,0,0)\" "
                + " >");
        writer.append(content.substring(0, Math.min(width / 9, content.length())));
        writer.append("</text>\n");
        writer.append("</g>");

        if (subNodes != null && maxDepth > 0) {
            int rx = 0;
            for (Map.Entry<Method, SampleNode> entry : subNodes.entrySet()) {
                int cwidth = entry.getValue().getCount() * width / totalSamples;
                generateSubSvg(writer, entry.getKey(), entry.getValue(), rx + x, y + 15, cwidth, maxDepth - 1, idPfx);
                rx += cwidth;
            }

        }
    }
}
