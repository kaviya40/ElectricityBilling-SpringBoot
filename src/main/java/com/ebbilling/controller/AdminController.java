package com.ebbilling.controller;

import com.ebbilling.entity.*;
import com.ebbilling.service.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userService;
    private final CustomerServiceImpl customerService;
    private final BillServiceImpl billService;

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",     userService.countByRole(User.Role.CUSTOMER));
        model.addAttribute("totalCustomers", customerService.count());
        model.addAttribute("totalBills",     billService.countAll());
        model.addAttribute("paidBills",      billService.countPaid());
        model.addAttribute("pendingBills",   billService.countPending());
        model.addAttribute("overdueBills",   billService.countOverdue());
        model.addAttribute("totalRevenue",   billService.totalRevenue());
        model.addAttribute("dueSoon",        billService.dueSoon(3));
        model.addAttribute("recentBills",    billService.findAll().stream()
                .sorted(Comparator.comparing(Bill::getCreatedAt).reversed()).limit(5).toList());

        // Chart data
        List<Object[]> rev = billService.monthlyRevenue();
        List<String>   labels  = new ArrayList<>();
        List<Double>   amounts = new ArrayList<>();
        for (Object[] r : rev.stream().limit(6).toList()) {
            labels.add(r[0]+" "+r[1]);
            amounts.add(r[2] instanceof BigDecimal bd ? bd.doubleValue() : 0.0);
        }
        Collections.reverse(labels); Collections.reverse(amounts);
        model.addAttribute("chartLabels",  labels);
        model.addAttribute("chartAmounts", amounts);
        return "admin/dashboard";
    }

    // ── Customers ─────────────────────────────────────────────────────────────
    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "admin/customers";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@RequestParam String name, @RequestParam String meterNumber,
                              @RequestParam String address, @RequestParam(required=false) String phone,
                              @RequestParam(required=false) String email,
                              @RequestParam String connectionType,
                              @RequestParam String ownerUsername,
                              RedirectAttributes ra) {
        try {
            customerService.add(name, meterNumber, address, phone, email,
                    Customer.ConnectionType.valueOf(connectionType), ownerUsername);
            ra.addFlashAttribute("success", "Customer added successfully!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/update/{id}")
    public String updateCustomer(@PathVariable Long id, @RequestParam String name,
                                 @RequestParam String address, @RequestParam(required=false) String phone,
                                 @RequestParam(required=false) String email,
                                 @RequestParam String connectionType, RedirectAttributes ra) {
        try {
            customerService.update(id, name, address, phone, email, Customer.ConnectionType.valueOf(connectionType));
            ra.addFlashAttribute("success", "Customer updated successfully!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes ra) {
        try { customerService.delete(id); ra.addFlashAttribute("success", "Customer deleted."); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/customers";
    }

    // ── Bills ─────────────────────────────────────────────────────────────────
    @GetMapping("/bills")
    public String bills(Model model) {
        model.addAttribute("bills",     billService.findAll());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("months",    List.of("January","February","March","April","May","June",
                                                "July","August","September","October","November","December"));
        model.addAttribute("years",     List.of(2023,2024,2025,2026));
        return "admin/bills";
    }

    @PostMapping("/bills/generate")
    public String generateBill(@RequestParam Long customerId, @RequestParam int unitsConsumed,
                               @RequestParam String billDate, @RequestParam String billingMonth,
                               @RequestParam int billingYear, RedirectAttributes ra) {
        try {
            billService.generate(customerId, unitsConsumed, LocalDate.parse(billDate), billingMonth, billingYear);
            ra.addFlashAttribute("success", "Bill generated successfully!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/bills";
    }

    @PostMapping("/bills/pay/{id}")
    public String markPaid(@PathVariable Long id, @RequestParam(defaultValue="CASH") String paymentMode,
                           RedirectAttributes ra) {
        try { billService.markPaid(id, paymentMode); ra.addFlashAttribute("success", "Bill marked as paid!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/bills";
    }

    @PostMapping("/bills/unpay/{id}")
    public String markUnpaid(@PathVariable Long id, RedirectAttributes ra) {
        try { billService.markUnpaid(id); ra.addFlashAttribute("success", "Bill marked as unpaid."); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/bills";
    }

    @PostMapping("/bills/delete/{id}")
    public String deleteBill(@PathVariable Long id, RedirectAttributes ra) {
        try { billService.delete(id); ra.addFlashAttribute("success", "Bill deleted."); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/bills";
    }

    @GetMapping("/bills/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = billService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bill-"+id+".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/bills/qr/{id}")
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(billService.generateQr(id));
    }

    // ── Users ─────────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username, @RequestParam String fullName,
                          @RequestParam String email, @RequestParam String password,
                          @RequestParam(required=false) String phone, @RequestParam String role,
                          RedirectAttributes ra) {
        try {
            userService.register(username, fullName, email, password, phone, User.Role.valueOf(role));
            ra.addFlashAttribute("success", "User added successfully!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try { userService.delete(id); ra.addFlashAttribute("success", "User deleted."); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/users";
    }
}
