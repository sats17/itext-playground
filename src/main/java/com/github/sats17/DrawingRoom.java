package com.github.sats17;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.File;

public class DrawingRoom {

    public static float metersToPoints(double meters) {
        final double inches = meters * 39.3701;
        final double points = inches * 72;
        return (float) points;
    }


    public static void main(String[] args) throws Exception {

        String out = "drawing-room.pdf";

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.addNewPage(PageSize.A4);

        PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());

        // ---- PAGE ----
        float pageWidth = PageSize.A4.getWidth();   // 595
        float pageHeight = PageSize.A4.getHeight(); // 842

        // ---- FLOOR PLAN (meters) ----
        float roomWidth = 6.09f;   // 6 meters
        float roomHeight = 4.26f;  // 4 meters

        float hallHeight = roomHeight;
        float hallWidth = roomWidth / 3;

        float kitchenHeight = roomHeight;
        float kitchenWidth = roomWidth / 3;

        float bedRoomHeight = roomHeight / 2;
        float bedRoomWidth = roomWidth / 3;

        float doorSizeInMeter = 0.9144f; // 3 Feet
        float doorSizeInPoints = metersToPoints(doorSizeInMeter);

        float roomWidthInPoints = metersToPoints(roomWidth);
        float roomHeightInPoints = metersToPoints(roomHeight);

        float hallHeightInPoints = metersToPoints(hallHeight);
        float hallWidthInPoints = metersToPoints(hallWidth);

        float kitchenHeightInPoints = metersToPoints(kitchenHeight);
        float kitchenWidthInPoints = metersToPoints(kitchenWidth);

        float bedRoomHeightInPoints = metersToPoints(bedRoomHeight);
        float bedRoomWidthInPoints = metersToPoints(bedRoomWidth);

        float widthRatio = roomWidthInPoints / pageWidth;
        float heightRatio = roomHeightInPoints / pageHeight;
        float scale = 1f / Math.max(widthRatio, heightRatio);

        // ---- DRAW OUTER BOUNDARY ----
        float scaledWidth = roomWidthInPoints * scale;
        float scaledHeight = roomHeightInPoints * scale;

        float offsetX = (pageWidth - scaledWidth) / 2f;
        float offsetY = (pageHeight - scaledHeight) / 2f;

        canvas.rectangle(offsetX, offsetY, scaledWidth, scaledHeight);
        canvas.stroke();
        // Done drawing outer boundary

        // Draw hall boundary
        float scaledHallWidth = hallWidthInPoints * scale;
        float scaledHallHeight = hallHeightInPoints * scale;

        canvas.rectangle(offsetX, offsetY, scaledHallWidth, scaledHallHeight);
        canvas.stroke();
        // Done drawing hall

        // Draw kitchen boundary
        float kitchenOffsetX = offsetX + scaledHallWidth;
        float kitchenOffsetY = offsetY;

        float scaledKitchenWidth = kitchenWidthInPoints * scale;
        float scaledKitchenHeight = kitchenHeightInPoints * scale;

        canvas.rectangle(kitchenOffsetX, kitchenOffsetY, scaledKitchenWidth, scaledKitchenHeight);
        canvas.stroke();
        // Done kitchen boundary

        // Draw bedroom boundary
        float bedRoomOffsetX = kitchenOffsetX + scaledKitchenWidth;
        float bedRoomOffsetY = offsetY;

        float scaledBedRoomWidth = bedRoomWidthInPoints * scale;
        float scaledBedRoomHeight = bedRoomHeightInPoints * scale;

        canvas.rectangle(bedRoomOffsetX, bedRoomOffsetY, scaledBedRoomWidth, scaledBedRoomHeight);
        canvas.stroke();
        // Done bedroom boundary

        // Start doors
        // Main door, hall
        float mainDoorMoveToX = offsetX;
        float mainDoorMoveToY = offsetY + scaledHallHeight;

        float mainDoorLineToX = offsetX + (doorSizeInPoints * scale);
        float mainDoorLineToY = mainDoorMoveToY; // Y line is similar to main moveToPoint

        canvas
                .setStrokeColor(ColorConstants.RED)
                .setLineWidth(4f)
                .moveTo(mainDoorMoveToX, mainDoorMoveToY)
                .lineTo(mainDoorLineToX, mainDoorLineToY)
                .stroke();

        // hall door, hall
        float hallDoorMoveToX = offsetX + scaledHallWidth;;
        float hallDoorMoveToY = offsetY + (scaledHallHeight / 3);

        float hallDoorLineToX = offsetX + scaledHallWidth;;
        float hallDoorLineToY = hallDoorMoveToY + (doorSizeInPoints * scale); // Y line is similar to main moveToPoint

        canvas
                .setStrokeColor(ColorConstants.RED)
                .setLineWidth(4f)
                .moveTo(hallDoorMoveToX, hallDoorMoveToY)
                .lineTo(hallDoorLineToX, hallDoorLineToY)
                .stroke();




//        canvas.rectangle(
//                offsetX + 0 * scale,
//                offsetY + 0 * scale,
//                scaledWidth * scale,
//                scaledHeight * scale
//        );
//        canvas.stroke();

        pdf.close();

        System.out.println("PDF created: " + new File(out).getAbsolutePath());
    }
}
