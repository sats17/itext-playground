package com.github.sats17;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.svg.converter.SvgConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
 *
 * Note: The boundaries of doors are static for now. Only items location can be dynamic.
 */
public class DrawingRoom {

    public static float metersToPoints(double meters) {
        final double inches = meters * 39.3701;
        final double points = inches * 72;
        return (float) points;
    }

    public static InputStream downloadSvgAsStream(String SVG_URL) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SVG_URL))
                    .GET()
                    .build();

            HttpResponse<byte[]> response =
                    client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to download SVG. HTTP status: "
                        + response.statusCode());
            }

            return new ByteArrayInputStream(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Error downloading SVG", e);
        }
    }



    public static void main(String[] args) throws Exception {

        String out = "drawing-room.pdf";

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.addNewPage(PageSize.A4);

        PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());


        // ---- PAGE ----
        float pageWidth = PageSize.A4.getWidth();   // 595 pdf points
        float pageHeight = PageSize.A4.getHeight(); // 842 pdf points

        float pageCenterX = pageWidth / 2f;
        float pageCenterY = pageHeight / 2f;

        // Adjust the canvas to move (0,0) points from bottom left(default itext) to middle part of PDF.
        canvas.saveState();
        canvas.concatMatrix(1, 0, 0, 1, pageCenterX, pageCenterY);


        // Input and calculated static values ----
        float totalWidth = 6.09f;   // 6 meters
        float totalHeight = 4.26f;  // 4 meters
        float totalWidthInPoints = metersToPoints(totalWidth);
        float totalHeightInPoints = metersToPoints(totalHeight);

        float hallHeight = totalHeight;
        float hallWidth = totalWidth / 3;
        float hallHeightInPoints = metersToPoints(hallHeight);
        float hallWidthInPoints = metersToPoints(hallWidth);

        float kitchenHeight = totalHeight;
        float kitchenWidth = totalWidth / 3;
        float kitchenHeightInPoints = metersToPoints(kitchenHeight);
        float kitchenWidthInPoints = metersToPoints(kitchenWidth);

        float bedRoomHeight = totalHeight / 2;
        float bedRoomWidth = totalWidth / 3;
        float bedRoomHeightInPoints = metersToPoints(bedRoomHeight);
        float bedRoomWidthInPoints = metersToPoints(bedRoomWidth);


        float doorSizeInMeter = 0.9144f; // 3 Feet
        float doorSizeInPoints = metersToPoints(doorSizeInMeter);

        float toiletWidthInMeter = 0.4572f; // 1.5 feet
        float toiletHeightInMeter = 1.0668f; // 3.5 feet
        float toiletDoorSizeInMeter = 0.39624f; // 1.3 feet
        float toiletWidthInPoints = metersToPoints(toiletWidthInMeter);
        float toiletHeightInPoints = metersToPoints(toiletHeightInMeter);
        float toiletDoorSizeInPoints = metersToPoints(toiletDoorSizeInMeter);

        float washBasinSpaceWidthInPoints = metersToPoints(0.4572f); // 1.5 feet
        float washBasinSpaceHeightInPoints = metersToPoints(0.6096f); // 2 feet

        float bathRoomWidthInMeter = 1.0668f; // 3.5 feet
        float bathRoomHeightInMeter = 0.4572f; // 1.5 feet
        float bathRoomDoorSizeInMeter = 0.39624f; // 1.3 feet
        float bathRoomWidthInPoints = metersToPoints(bathRoomWidthInMeter);
        float bathRoomHeightInPoints = metersToPoints(bathRoomHeightInMeter);
        float bathRoomDoorSizeInPoints = metersToPoints(bathRoomDoorSizeInMeter);

        String kitchenIconSVGUrl = "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/utensils.svg";
        InputStream svgStream = downloadSvgAsStream(kitchenIconSVGUrl);
        float kitchenIconXOffset = -50f;
        float kitchenIconYOffset = -190f;
        float kitchenIconHeight = 0.9144f; // 1 Feet
        float kitchenIconWidth = 0.9144f;; // 1 Feet

        // Done with input and static calculated values.

        // As we want to fit real world room sizes into A4 page we need to calculate scale value.
        // Based on scale value we can draw the next items. Like making them fit into the page.
        float widthRatio = totalWidthInPoints / pageWidth;
        float heightRatio = totalHeightInPoints / pageHeight;
        float scale = 1f / Math.max(widthRatio, heightRatio);
        // Done calculating scale.


        // ---- DRAW OUTER BOUNDARY ----
        float scaledWidth = totalWidthInPoints * scale;
        float scaledHeight = totalHeightInPoints * scale;

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
        canvas.saveState(); // Making sure that doors state does not cause issue with borders
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

        // hall + kitchen door
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
        canvas.restoreState();
        // Done drawing hall + kitchen door

        // Bathroom draw
        float toiletXOffset = kitchenOffsetX + (washBasinSpaceWidthInPoints * scale);
        float toiletYOffset = kitchenOffsetY + (scaledHallHeight - (bathRoomHeightInPoints * scale) - (washBasinSpaceHeightInPoints * scale));

        float scaledToiletWidth = toiletWidthInPoints * scale;
        float scaledToiletHeight = toiletHeightInPoints * scale;

        // We considered widht and height based on where door is, hence rectangle got different here. No worries.
        canvas.rectangle(toiletXOffset, toiletYOffset, scaledToiletHeight, scaledToiletWidth);
        canvas.stroke();
        // Bathroom done

        // kitchen icon start
        float kitchenIconWidthInPoints = metersToPoints(kitchenIconWidth);
        float kitchenIconHeightInPoints = metersToPoints(kitchenIconHeight);

        byte[] svgBytes = svgStream.readAllBytes();

        InputStream metaStream = new ByteArrayInputStream(svgBytes);
        SvgMetadata.Metadata meta = SvgMetadata.readMetadata(metaStream);
        System.out.println(meta.toString());

        InputStream drawStream = new ByteArrayInputStream(svgBytes);

        float svgScale = (kitchenIconWidthInPoints * scale) / 102f;

        /**
         * SVG view box
         * viewBox="minX minY width height"
         * minX = Left edge of SVG coordinate space
         * minY = Top edge of SVG coordinate space
         * width = Width of SVG internal drawing
         * height = Height of SVG internal drawing
         */
        canvas.saveState();
        canvas.concatMatrix(svgScale, 0, 0, svgScale, kitchenIconXOffset, kitchenIconYOffset);
        SvgConverter.drawOnCanvas(drawStream, canvas);




        pdf.close();

        System.out.println("PDF created: " + new File(out).getAbsolutePath());
    }
}
