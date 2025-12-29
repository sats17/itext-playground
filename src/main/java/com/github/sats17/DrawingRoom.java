package com.github.sats17;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.File;

public class DrawingRoom {

    public static void main(String[] args) throws Exception {

        String out = "drawing-room.pdf";

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.addNewPage(PageSize.A4);

        PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());

        // ---- PAGE ----
        float pageWidth = PageSize.A4.getWidth();   // 595
        float pageHeight = PageSize.A4.getHeight(); // 842

        // ---- FLOOR PLAN (POINTS) ----
        float planWidth = 79370f;   // 28 m
        float planHeight = 62362f;  // 22 m

        float widthRatio = planWidth / pageWidth;
        float heightRatio = planHeight / pageHeight;
        float scale = 1f / Math.max(widthRatio, heightRatio);

        float scaledWidth = planWidth * scale;
        float scaledHeight = planHeight * scale;

        float offsetX = (pageWidth - scaledWidth) / 2f;
        float offsetY = (pageHeight - scaledHeight) / 2f;

        // ---- DRAW OUTER BOUNDARY ----
        canvas.rectangle(offsetX, offsetY, scaledWidth, scaledHeight);
        canvas.stroke();

        // ---- ROOM ----
        float roomX = 17008f; // 6 m
        float roomY = 11339f; // 4 m
        float roomW = 14173f; // 5 m
        float roomH = 11339f; // 4 m

        canvas.rectangle(
                offsetX + roomX * scale,
                offsetY + roomY * scale,
                roomW * scale,
                roomH * scale
        );
        canvas.stroke();

        pdf.close();

        System.out.println("PDF created: " + new File(out).getAbsolutePath());
    }
}
