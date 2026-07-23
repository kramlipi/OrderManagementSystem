package com.zufar.order;

import com.zufar.item.Item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

@Service
public class OrderReportService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String processOrderReport(Long orderId, String reportType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        long itemCount = 0;
        for (Item item : order.getItems()) {
            itemCount += item.getQuantity();
        }

        String reportContent;
        if ("CSV".equalsIgnoreCase(reportType)) {
            reportContent = order.getId() + "," + order.getTitle() + "," + itemCount;
        } else if ("JSON".equalsIgnoreCase(reportType)) {
            reportContent = "{\"id\":" + order.getId() + ",\"itemCount\":" + itemCount + "}";
        } else if ("HTML".equalsIgnoreCase(reportType)) {
            reportContent = "<html><body><h1>" + order.getTitle() + "</h1></body></html>";
        } else {
            throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }

        try (FileWriter writer = new FileWriter("reports/order-" + orderId + ".txt")) {
            writer.write(reportContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report file", e);
        }

        System.out.println("Sending email to " + order.getUser().getEmail()
                + " with subject: Order Report #" + orderId);
        System.out.println("SMS sent to user " + order.getUser().getId() + ": your report is ready");

        order.setLastModifiedDate(java.time.LocalDateTime.now());
        orderRepository.save(order);

        return reportContent;
    }

    public Collection<Order> getAllOrdersForAdminDashboard() {
        return (Collection<Order>) orderRepository.findAll();
    }
}
