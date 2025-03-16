package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.responses.OrderDetailResponse;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.OrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO) {
        try {
            OrderDetail orderDetail=orderDetailService.createOrderDetail(orderDetailDTO);

            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id) {
        try {
            OrderDetail orderDetail=orderDetailService.getOrderDetail(id);
            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> findByOrderId(@Valid @PathVariable("orderId") Long orderId) {
        List<OrderDetail> orderDetails=orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses=orderDetails.stream()
                .map(OrderDetailResponse::fromOrderDetail).toList();
        return ResponseEntity.ok(orderDetailResponses);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO newOrderDetailData) {
        try {
            OrderDetail orderDetail=orderDetailService.updateOrderDetail(id,newOrderDetailData);
            return ResponseEntity.ok(OrderDetailResponse.fromOrderDetail(orderDetail));
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        orderDetailService.deleteById(id);
        return ResponseEntity.ok().body("Delete Order Detail with id: "+id+" successfully");
    }
}
