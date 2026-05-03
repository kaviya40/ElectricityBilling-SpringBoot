package com.ebbilling.controller;

import com.ebbilling.entity.*;
import com.ebbilling.service.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerServiceImpl customerService;
    private final BillServiceImpl billService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        List<Customer> customers = customerService.findByUser(user.getUsername());
        model.addAttribute("customers", customers);

        long totalBills = 0, paidBills = 0, pendingBills = 0;
        BigDecimal totalPaid = BigDecimal.ZERO, totalPending = BigDecimal.ZERO;
        List<Bill> allBills = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Double> units = new ArrayList<>();

        for (Customer c : customers) {
            List<Bill> bills = billService.findByCustomer(c.getId());
            allBills.addAll(bills);
            totalBills += bills.size();
            for (Bill b : bills) {
                if (b.getStatus() == Bill.BillStatus.PAID) { paidBills++; totalPaid = totalPaid.add(b.getTotalAmount()); }
                else pendingBills++;
            }
            if (!customers.isEmpty()) {
                List<Object[]> usage = billService.monthlyUsage(c.getId());
                for (Object[] u : usage.stream().limit(6).toList()) {
                    labels.add(u[0]+" "+u[1]);
                    units.add(u[2] instanceof Number n ? n.doubleValue() : 0.0);
                }
            }
        }
        Collections.reverse(labels); Collections.reverse(units);

        model.addAttribute("totalBills",   totalBills);
        model.addAttribute("paidBills",    paidBills);
        model.addAttribute("pendingBills", pendingBills);
        model.addAttribute("totalPaid",    totalPaid);
        model.addAttribute("recentBills",  allBills.stream()
                .sorted(Comparator.comparing(Bill::getBillDate).reversed()).limit(5).toList());
        model.addAttribute("dueSoon",      billService.dueSoon(3));
        model.addAttribute("chartLabels",  labels);
        model.addAttribute("chartUnits",   units);
        return "customer/dashboard";
    }

    @GetMapping("/bills")
    public String bills(@AuthenticationPrincipal User user, Model model) {
        List<Customer> customers = customerService.findByUser(user.getUsername());
        List<Bill> allBills = new ArrayList<>();
        for (Customer c : customers) allBills.addAll(billService.findByCustomer(c.getId()));
        allBills.sort(Comparator.comparing(Bill::getBillDate).reversed());
        model.addAttribute("bills", allBills);
        return "customer/bills";
    }

    @GetMapping("/bills/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bill-"+id+".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(billService.generatePdf(id));
    }

    @GetMapping("/bills/qr/{id}")
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(billService.generateQr(id));
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("customers", customerService.findByUser(user.getUsername()));
        return "customer/profile";
    }
}
