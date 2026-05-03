package com.ebbilling.service.impl;

import com.ebbilling.entity.Bill;
import com.ebbilling.entity.Customer;
import com.ebbilling.entity.Payment;
import com.ebbilling.repository.BillRepository;
import com.ebbilling.repository.PaymentRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl {

    private final BillRepository billRepository;
    private final CustomerServiceImpl customerService;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal FIXED = new BigDecimal("50.00");
    private static final BigDecimal R1 = new BigDecimal("1.50");
    private static final BigDecimal R2 = new BigDecimal("3.00");
    private static final BigDecimal R3 = new BigDecimal("5.00");

    public Bill generate(Long customerId, int units, LocalDate billDate, String month, int year) {
        Customer customer = customerService.findById(customerId);
        if (billRepository.existsByCustomerAndBillingMonthAndBillingYear(customer, month, year))
            throw new RuntimeException("Bill already exists for " + month + " " + year);

        BigDecimal unitCharge = calcUnits(units);
        BigDecimal total = unitCharge.add(FIXED);
        String billNo = "EBMS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int)(Math.random()*9000+1000));

        Bill bill = Bill.builder()
                .billNumber(billNo).customer(customer).unitsConsumed(units)
                .unitCharges(unitCharge).fixedCharges(FIXED)
                .penaltyCharges(BigDecimal.ZERO).totalAmount(total)
                .billDate(billDate).dueDate(billDate.plusDays(30))
                .status(Bill.BillStatus.PENDING)
                .billingMonth(month).billingYear(year).build();
        return billRepository.save(bill);
    }

    private BigDecimal calcUnits(int u) {
        if (u <= 100) return R1.multiply(BigDecimal.valueOf(u));
        if (u <= 300) return R1.multiply(BigDecimal.valueOf(100))
                               .add(R2.multiply(BigDecimal.valueOf(u - 100)));
        return R1.multiply(BigDecimal.valueOf(100))
                 .add(R2.multiply(BigDecimal.valueOf(200)))
                 .add(R3.multiply(BigDecimal.valueOf(u - 300)));
    }

    public Bill findById(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    public List<Bill> findAll() { return billRepository.findAll(); }

    public List<Bill> findByCustomer(Long cid) { return billRepository.findByCustomerId(cid); }

    public long countAll()    { return billRepository.count(); }
    public long countPaid()   { return billRepository.countByStatus(Bill.BillStatus.PAID); }
    public long countPending(){ return billRepository.countByStatus(Bill.BillStatus.PENDING); }
    public long countOverdue(){ return billRepository.countByStatus(Bill.BillStatus.OVERDUE); }
    public BigDecimal totalRevenue(){ BigDecimal r=billRepository.getTotalRevenue(); return r==null?BigDecimal.ZERO:r; }

    public List<Object[]> monthlyRevenue() { return billRepository.getMonthlyRevenue(); }
    public List<Object[]> monthlyUsage(Long cid) { return billRepository.getMonthlyUsage(cid); }

    public Bill markPaid(Long id, String mode) {
        Bill bill = findById(id);
        bill.setStatus(Bill.BillStatus.PAID);
        bill.setPaymentDate(LocalDate.now());
        Payment.PaymentMode pm = Payment.PaymentMode.CASH;
        try { pm = Payment.PaymentMode.valueOf(mode.toUpperCase()); } catch(Exception ignored){}
        paymentRepository.save(Payment.builder()
                .transactionId("TXN-"+ UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .bill(bill).amountPaid(bill.getTotalAmount()).paymentMode(pm).build());
        return billRepository.save(bill);
    }

    public Bill markUnpaid(Long id) {
        Bill bill = findById(id);
        bill.setStatus(Bill.BillStatus.PENDING);
        bill.setPaymentDate(null);
        paymentRepository.findByBillId(id).ifPresent(paymentRepository::delete);
        return billRepository.save(bill);
    }

    public void delete(Long id) { billRepository.deleteById(id); }

    public List<Bill> dueSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return billRepository.findByStatus(Bill.BillStatus.PENDING).stream()
                .filter(b -> !b.getDueDate().isAfter(cutoff) && !b.getDueDate().isBefore(LocalDate.now()))
                .toList();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void applyPenalties() {
        billRepository.findByStatus(Bill.BillStatus.PENDING).stream()
                .filter(b -> b.getDueDate().isBefore(LocalDate.now()))
                .forEach(b -> {
                    BigDecimal pen = b.getTotalAmount().multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
                    b.setPenaltyCharges(b.getPenaltyCharges().add(pen));
                    b.setTotalAmount(b.getTotalAmount().add(pen));
                    b.setStatus(Bill.BillStatus.OVERDUE);
                    billRepository.save(b);
                });
    }

    public byte[] generatePdf(Long id) {
        Bill b = findById(id);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            Document doc = new Document(new PdfDocument(writer));
            DeviceRgb blue = new DeviceRgb(30,136,229);
            DeviceRgb lb   = new DeviceRgb(227,242,253);

            doc.add(new Paragraph("⚡ ELECTRICITY BILL MANAGEMENT SYSTEM")
                    .setFontSize(18).setBold().setFontColor(blue).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Official Bill Receipt").setFontSize(11)
                    .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            Table info = new Table(UnitValue.createPercentArray(new float[]{50,50})).useAllAvailableWidth();
            addRow(info,"Bill Number", b.getBillNumber(), lb, false);
            addRow(info,"Bill Date",   b.getBillDate().toString(), ColorConstants.WHITE, false);
            addRow(info,"Due Date",    b.getDueDate().toString(), lb, false);
            addRow(info,"Status",      b.getStatus().name(), ColorConstants.WHITE, false);
            doc.add(info); doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("CUSTOMER DETAILS").setBold().setFontColor(blue));
            Table cust = new Table(UnitValue.createPercentArray(new float[]{50,50})).useAllAvailableWidth();
            addRow(cust,"Name",            b.getCustomer().getName(), lb, false);
            addRow(cust,"Meter Number",    b.getCustomer().getMeterNumber(), ColorConstants.WHITE, false);
            addRow(cust,"Address",         b.getCustomer().getAddress(), lb, false);
            addRow(cust,"Connection Type", b.getCustomer().getConnectionType().name(), ColorConstants.WHITE, false);
            doc.add(cust); doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("BILL CHARGES").setBold().setFontColor(blue));
            Table charges = new Table(UnitValue.createPercentArray(new float[]{65,35})).useAllAvailableWidth();
            charges.addHeaderCell(hdrCell("Description",blue));
            charges.addHeaderCell(hdrCell("Amount (₹)",blue));
            addRow(charges,"Billing Period: "+b.getBillingMonth()+" "+b.getBillingYear(),"", lb, false);
            addRow(charges,"Units Consumed: "+b.getUnitsConsumed()+" kWh","₹"+b.getUnitCharges(), ColorConstants.WHITE, false);
            addRow(charges,"Fixed Charges","₹"+b.getFixedCharges(), lb, false);
            if(b.getPenaltyCharges()!=null && b.getPenaltyCharges().compareTo(BigDecimal.ZERO)>0)
                addRow(charges,"Late Penalty","₹"+b.getPenaltyCharges(), ColorConstants.WHITE, false);
            charges.addCell(new Cell().add(new Paragraph("TOTAL AMOUNT").setBold()).setBackgroundColor(blue).setFontColor(ColorConstants.WHITE).setPadding(6));
            charges.addCell(new Cell().add(new Paragraph("₹"+b.getTotalAmount()).setBold()).setBackgroundColor(blue).setFontColor(ColorConstants.WHITE).setPadding(6));
            doc.add(charges);

            doc.add(new Paragraph("\nTariff: 0-100 units @₹1.5 | 101-300 @₹3 | Above 300 @₹5 | Fixed ₹50")
                    .setFontSize(9).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("For queries: 1800-XXX-XXXX | www.ebms.com")
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("PDF error: "+e.getMessage(),e); }
    }

    private void addRow(Table t, String k, String v, com.itextpdf.kernel.colors.Color bg, boolean bold){
        Paragraph pk = new Paragraph(k); if(bold) pk.setBold();
        Paragraph pv = new Paragraph(v); if(bold) pv.setBold();
        t.addCell(new Cell().add(pk).setBackgroundColor(bg).setPadding(5));
        t.addCell(new Cell().add(pv).setBackgroundColor(bg).setPadding(5));
    }

    private Cell hdrCell(String text, DeviceRgb color){
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(color).setFontColor(ColorConstants.WHITE).setPadding(5);
    }

    public byte[] generateQr(Long id) {
        Bill b = findById(id);
        String content = "BILL:"+b.getBillNumber()+"|CUSTOMER:"+b.getCustomer().getName()
                +"|AMOUNT:"+b.getTotalAmount()+"|DUE:"+b.getDueDate();
        try {
            BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(m,"PNG",baos);
            return baos.toByteArray();
        } catch(Exception e){ throw new RuntimeException("QR error",e); }
    }
}
