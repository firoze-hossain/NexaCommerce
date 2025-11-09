
package com.roze.nexacommerce.order.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.order.entity.Order;
import com.roze.nexacommerce.order.entity.OrderAddress;
import com.roze.nexacommerce.order.entity.OrderItem;
import com.roze.nexacommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final OrderRepository orderRepository;

    // Professional color scheme
    private static final BaseColor PRIMARY_COLOR = new BaseColor(0, 0, 0);
    private static final BaseColor ACCENT_COLOR = new BaseColor(64, 64, 64);
    private static final BaseColor LIGHT_GRAY = new BaseColor(240, 240, 240);
    private static final BaseColor TABLE_HEADER_COLOR = new BaseColor(80, 80, 80);

    // Font definitions
    private static final Font COMPANY_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR);
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR);
    private static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, ACCENT_COLOR);
    private static final Font VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, PRIMARY_COLOR);
    private static final Font TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
    private static final Font TABLE_CONTENT_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, PRIMARY_COLOR);
    private static final Font TOTAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
    private static final Font FOOTER_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, ACCENT_COLOR);

    public byte[] generateOrderReceipt(String orderNumber) {
        try {
            log.info("Generating receipt for order: {}", orderNumber);

            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

            return generateCompactReceiptPdf(order);

        } catch (Exception e) {
            log.error("Error generating receipt for order: {}", orderNumber, e);
            throw new RuntimeException("Failed to generate receipt: " + e.getMessage(), e);
        }
    }

    private byte[] generateCompactReceiptPdf(Order order) {
        log.info("Generating compact PDF receipt for order: {}", order.getOrderNumber());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 30, 25);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            writer.setPageEvent(new CompactFooter());

            document.open();

            // Add all sections in correct order
            addCompactHeader(document, order);
            addOrderDetailsSection(document, order);
            addOrderItemsSection(document, order);
            addFinancialSummarySection(document, order);
            addShippingSection(document, order);

            document.close();

            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Compact PDF generated successfully - Size: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Error generating compact PDF", e);
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private void addCompactHeader(Document document, Order order) throws DocumentException {
        // Company header
        Paragraph company = new Paragraph("NEXACOMMERCE", COMPANY_FONT);
        company.setAlignment(Element.ALIGN_CENTER);
        company.setSpacingAfter(3f);
        document.add(company);

        // Tagline
        Paragraph tagline = new Paragraph("Order Confirmation",
                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, ACCENT_COLOR));
        tagline.setAlignment(Element.ALIGN_CENTER);
        tagline.setSpacingAfter(10f);
        document.add(tagline);

        // Order number and date
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(5f); // Reduced spacing

        PdfPCell orderCell = new PdfPCell(new Phrase("Order: " + order.getOrderNumber(), HEADER_FONT));
        orderCell.setBorder(PdfPCell.NO_BORDER);
        orderCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        String date = order.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        PdfPCell dateCell = new PdfPCell(new Phrase(date, VALUE_FONT));
        dateCell.setBorder(PdfPCell.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        headerTable.addCell(orderCell);
        headerTable.addCell(dateCell);
        document.add(headerTable);

        // NO SEPARATOR HERE - Customer info comes immediately after
    }

    private void addOrderDetailsSection(Document document, Order order) throws DocumentException {
        // Use a 2-column table for better layout
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(10f);

        // Get customer info with phone number
        String customerName = order.isGuestOrder() ? order.getGuestName() : order.getCustomer().getUser().getName();
        String contact = order.isGuestOrder() ? order.getGuestEmail() : order.getCustomer().getUser().getEmail();
        String customerPhone = order.isGuestOrder() ? order.getGuestPhone() :
                (order.getCustomer().getPhone() != null ? order.getCustomer().getPhone() : "N/A");

        // Add customer details in a compact format
        // Row 1: Customer and Status
        addCompactDetailRow(detailsTable, "Customer:", customerName);
        addCompactDetailRow(detailsTable, "Status:", order.getStatus().toString());

        // Row 2: Contact and Payment
        addCompactDetailRow(detailsTable, "Contact:", contact);
        addCompactDetailRow(detailsTable, "Payment:", order.getPaymentStatus().toString());

        // Row 3: Phone (spanning both columns would be messy, so keep in 2 columns)
        addCompactDetailRow(detailsTable, "Phone:", customerPhone);

        document.add(detailsTable);

        // Add separator AFTER customer details, before order items
        addSeparator(document);
    }

    private void addOrderItemsSection(Document document, Order order) throws DocumentException {
        Paragraph sectionHeader = new Paragraph("Order Items", HEADER_FONT);
        sectionHeader.setSpacingBefore(5f);
        sectionHeader.setSpacingAfter(8f);
        document.add(sectionHeader);

        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingAfter(12f);

        // Set column widths
        itemsTable.setWidths(new float[]{3, 1, 1.2f, 1.2f});

        // Table Header
        addCompactTableHeaderCell(itemsTable, "Product");
        addCompactTableHeaderCell(itemsTable, "Qty");
        addCompactTableHeaderCell(itemsTable, "Price");
        addCompactTableHeaderCell(itemsTable, "Total");

        // Table Rows
        for (OrderItem item : order.getOrderItems()) {
            addCompactProductCell(itemsTable, item.getProductName());
            addCompactTableCell(itemsTable, String.valueOf(item.getQuantity()), Element.ALIGN_CENTER);
            addCompactTableCell(itemsTable, "৳" + formatAmount(item.getPrice()), Element.ALIGN_RIGHT);
            addCompactTableCell(itemsTable, "৳" + formatAmount(item.getSubtotal()), Element.ALIGN_RIGHT);
        }

        document.add(itemsTable);
    }

    private void addFinancialSummarySection(Document document, Order order) throws DocumentException {
        PdfPTable financialTable = new PdfPTable(2);
        financialTable.setWidthPercentage(50);
        financialTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        financialTable.setSpacingAfter(15f);

        // Amount details
        addCompactFinancialRow(financialTable, "Subtotal:", "৳" + formatAmount(order.getTotalAmount()));
        addCompactFinancialRow(financialTable, "Shipping:", "৳" + formatAmount(order.getShippingAmount()));
        addCompactFinancialRow(financialTable, "Tax:", "৳" + formatAmount(order.getTaxAmount()));
        addCompactFinancialRow(financialTable, "Discount:", "-৳" + formatAmount(order.getDiscountAmount()));

        if (order.getCouponDiscount() != null && order.getCouponDiscount().compareTo(BigDecimal.ZERO) > 0) {
            addCompactFinancialRow(financialTable, "Coupon:", "-৳" + formatAmount(order.getCouponDiscount()));
        }

        // Separator
        PdfPCell separatorCell = new PdfPCell(new Phrase(" "));
        separatorCell.setBorder(PdfPCell.TOP);
        separatorCell.setBorderColor(ACCENT_COLOR);
        separatorCell.setBorderWidth(0.5f);
        separatorCell.setColspan(2);
        separatorCell.setFixedHeight(8f);
        financialTable.addCell(separatorCell);

        // Grand Total
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", TOTAL_FONT));
        totalLabelCell.setBorder(PdfPCell.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(3f);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("৳" + formatAmount(order.getFinalAmount()), TOTAL_FONT));
        totalValueCell.setBorder(PdfPCell.NO_BORDER);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPadding(3f);

        financialTable.addCell(totalLabelCell);
        financialTable.addCell(totalValueCell);

        document.add(financialTable);
    }

    private void addShippingSection(Document document, Order order) throws DocumentException {
        PdfPTable shippingTable = new PdfPTable(1);
        shippingTable.setWidthPercentage(100);
        shippingTable.setSpacingBefore(10f);

        PdfPCell shippingCell = new PdfPCell();
        shippingCell.setBorder(PdfPCell.BOX);
        shippingCell.setBorderWidth(0.5f);
        shippingCell.setBorderColor(ACCENT_COLOR);
        shippingCell.setPadding(10f);
        shippingCell.setBackgroundColor(LIGHT_GRAY);

        Paragraph shippingHeader = new Paragraph("Shipping Address",
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR));
        shippingHeader.setSpacingAfter(8f);

        shippingCell.addElement(shippingHeader);

        if (order.getShippingAddress() != null) {
            OrderAddress address = order.getShippingAddress();

            Paragraph name = new Paragraph(address.getFullName(), VALUE_FONT);
            name.setSpacingAfter(3f);

            Paragraph addressLine = new Paragraph(address.getAddressLine(), VALUE_FONT);
            addressLine.setSpacingAfter(3f);

            Paragraph areaCity = new Paragraph(address.getArea() + ", " + address.getCity(), VALUE_FONT);
            areaCity.setSpacingAfter(3f);

            Paragraph phone = new Paragraph("Phone: " + address.getPhone(), VALUE_FONT);

            shippingCell.addElement(name);
            shippingCell.addElement(addressLine);
            shippingCell.addElement(areaCity);
            shippingCell.addElement(phone);
        } else {
            Paragraph noAddress = new Paragraph("Shipping address not provided", VALUE_FONT);
            shippingCell.addElement(noAddress);
        }

        shippingTable.addCell(shippingCell);
        document.add(shippingTable);
    }

    // ========== HELPER METHODS ==========

    /**
     * Method for compact detail rows in 2-column layout
     */
    private void addCompactDetailRow(PdfPTable table, String label, String value) {
        // Label cell
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(1f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        // Value cell
        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(1f);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    private void addCompactTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(TABLE_HEADER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addCompactProductCell(PdfPTable table, String productName) {
        PdfPCell cell = new PdfPCell(new Phrase(productName, TABLE_CONTENT_FONT));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(4f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addCompactTableCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_CONTENT_FONT));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addCompactFinancialRow(PdfPTable table, String label, String value) {
        // Label cell
        PdfPCell labelCell = new PdfPCell(new Phrase(label, VALUE_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(2f);
        table.addCell(labelCell);

        // Value cell
        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(2f);
        table.addCell(valueCell);
    }

    private void addSeparator(Document document) throws DocumentException {
        Paragraph separator = new Paragraph();
        LineSeparator line = new LineSeparator();
        line.setLineColor(ACCENT_COLOR);
        line.setLineWidth(0.3f);
        separator.add(line);
        separator.setSpacingAfter(8f);
        separator.setSpacingBefore(2f);
        document.add(separator);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    // Footer class
    private class CompactFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                Phrase footer = new Phrase(
                        "Thank you for your order! • support@nexacommerce.com • +880 1234-567890",
                        FOOTER_FONT
                );

                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 15, 0);

            } catch (Exception e) {
                log.error("Error adding footer", e);
            }
        }
    }
}