package com.github.sats17;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.File;

/**
 * This code generates a PDF representation of a room layout.
 *
 * The room contains items such as lights, sofas, and doors. Each item is defined
 * using real-world dimensions (meters or feet) and a position (x, y).
 *
 * The (x, y) positions come from the UI, which uses a Cartesian coordinate system
 * with (0, 0) at the center of the room.
 *
 * The goal is to mimic this Cartesian layout exactly in the PDF, so that every
 * item appears in the same relative position as shown in the UI.
 *
 * Since iText uses a PDF coordinate system where (0, 0) is at the bottom-left
 * corner of the page, the coordinate system must be transformed so that:
 *   - (0, 0) maps to the center of the page
 *   - Positive X goes right
 *   - Positive Y goes up
 *
 * After applying this transformation, all room elements can be drawn directly
 * using their original (x, y) values without additional offsets.
 */
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

        float pageCenterX = pageWidth / 2f;
        float pageCenterY = pageHeight / 2f;

        // Adjust the canvas to move (0,0) from bottom left to middle part of PDF.
        canvas.saveState();
        canvas.concatMatrix(1, 0, 0, 1, pageCenterX, pageCenterY);


        // Input and calculated static values ----
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
        // Done with input and static calculated values.


        // ---- DRAW OUTER BOUNDARY ----
        float scaledWidth = roomWidthInPoints * scale;
        float scaledHeight = roomHeightInPoints * scale;

        // This is the old offset calculation where we did not want our rectangle in cartesian graph way.
//        float offsetX = (pageWidth - scaledWidth) / 2f;
//        float offsetY = (pageHeight - scaledHeight) / 2f;

        // The reason to make these value negative because as we move our rectangle in cartesian graph position(0,0) in the middle,
        // hence the bottomLeft x and y needs to be in somewhere negative points so that our rectangle start drawing from
        // scaled bottom left part of page.
        float offsetX = -(pageWidth / 2f);
        float offsetY = -((pageHeight - scaledHeight) / 2f);

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
