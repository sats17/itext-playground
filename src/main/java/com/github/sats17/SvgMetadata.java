package com.github.sats17;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class SvgMetadata {

    public static class Metadata {
        public float minX;
        public float minY;
        public float viewBoxWidth;
        public float viewBoxHeight;
        public float contentWidth;
        public float contentHeight;
        public String preserveAspectRatio;

        @Override
        public String toString() {
            return "SVG Metadata{" +
                    "minX=" + minX +
                    ", minY=" + minY +
                    ", viewBoxWidth=" + viewBoxWidth +
                    ", viewBoxHeight=" + viewBoxHeight +
                    ", contentWidth=" + contentWidth +
                    ", contentHeight=" + contentHeight +
                    ", preserveAspectRatio='" + preserveAspectRatio + '\'' +
                    '}';
        }
    }

    /**
     * Reads SVG metadata from InputStream.
     * @param svgStream InputStream of SVG file
     * @return Metadata object with viewBox, content bounds, and aspect ratio
     */
    public static Metadata readMetadata(InputStream svgStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(svgStream);

            Element svgElement = doc.getDocumentElement();

            // Read viewBox
            String viewBoxAttr = svgElement.getAttribute("viewBox");
            if (viewBoxAttr == null || viewBoxAttr.isEmpty()) {
                throw new IllegalArgumentException("SVG has no viewBox attribute");
            }
            String[] vb = viewBoxAttr.trim().split("\\s+");
            if (vb.length != 4) {
                throw new IllegalArgumentException("Invalid viewBox: " + viewBoxAttr);
            }

            Metadata metadata = new Metadata();
            metadata.minX = Float.parseFloat(vb[0]);
            metadata.minY = Float.parseFloat(vb[1]);
            metadata.viewBoxWidth = Float.parseFloat(vb[2]);
            metadata.viewBoxHeight = Float.parseFloat(vb[3]);

            // Optional: read preserveAspectRatio
            metadata.preserveAspectRatio = svgElement.getAttribute("preserveAspectRatio");

            // Approximate content bounds from paths (simplistic)
            NodeList paths = svgElement.getElementsByTagName("path");
            float minXContent = Float.MAX_VALUE;
            float minYContent = Float.MAX_VALUE;
            float maxXContent = Float.MIN_VALUE;
            float maxYContent = Float.MIN_VALUE;

            for (int i = 0; i < paths.getLength(); i++) {
                Element path = (Element) paths.item(i);
                String d = path.getAttribute("d");
                if (d == null || d.isEmpty()) continue;

                // Very simple parser: find numbers after M/m/L/l
                String[] tokens = d.replaceAll("[A-Za-z]", " ").trim().split("[ ,]+");
                for (int t = 0; t + 1 < tokens.length; t += 2) {
                    try {
                        float x = Float.parseFloat(tokens[t]);
                        float y = Float.parseFloat(tokens[t + 1]);
                        if (x < minXContent) minXContent = x;
                        if (y < minYContent) minYContent = y;
                        if (x > maxXContent) maxXContent = x;
                        if (y > maxYContent) maxYContent = y;
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (paths.getLength() > 0) {
                metadata.contentWidth = maxXContent - minXContent;
                metadata.contentHeight = maxYContent - minYContent;
            } else {
                // fallback: content = full viewBox
                metadata.contentWidth = metadata.viewBoxWidth;
                metadata.contentHeight = metadata.viewBoxHeight;
            }

            return metadata;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read SVG metadata", e);
        }
    }
}
