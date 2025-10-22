package com.Tulip_Tech.OrderService.service;


import com.Tulip_Tech.OrderService.client.PaymentServiceClient;
import com.Tulip_Tech.OrderService.client.ProductServiceClient;
import com.Tulip_Tech.OrderService.entity.OrderEntity;
import com.Tulip_Tech.OrderService.exception.CustomException;
import com.Tulip_Tech.OrderService.exception.ServiceUnavailableException;
import com.Tulip_Tech.OrderService.mapper.OrderMapper;
import com.Tulip_Tech.OrderService.model.Dto.CreateOrderRequest;
import com.Tulip_Tech.OrderService.model.Dto.CreatePaymentRequest;
import com.Tulip_Tech.OrderService.model.domain.Order;
import com.Tulip_Tech.OrderService.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderMapper orderMapper;

    private static final String PRODUCT_SERVICE = "productService";
    private static final String PAYMENT_SERVICE = "paymentService";
    private static final String GET_PRODUCT_DETAILS = "getProductDetails";

    @Override
    public ResponseEntity<?> placeOrder(CreateOrderRequest createOrderRequest) {

        //reduce the quantity of product
        try {
            callProductServiceReduceQuantity(createOrderRequest.productId(), createOrderRequest.quantity());

            OrderEntity orderEntity = orderMapper.createOrderEntity(createOrderRequest);
            orderRepository.save(orderEntity);

            log.info("calling payment service to complete the payment");
            CreatePaymentRequest request = new CreatePaymentRequest(
                    orderEntity.getOrderId(),
                    orderEntity.getTotalAmount(),
                    orderEntity.getPayment_mode(),
                    "Me"
            );

            callPaymentServiceDoPayment(request);

            log.info("Order Placed with orderId={}", orderEntity);
            return ResponseEntity.ok(orderEntity.getOrderId());


        }catch (ServiceUnavailableException ex){

            log.error("Service Unavailable during order placement: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Dependency Service Unavailable: " + ex.getMessage());

        }
        catch (CustomException ex) {

            log.error("Error occurred while calling ProductService: {}", ex.getMessage());
            return ResponseEntity.status(ex.getHttpStatus()).body(ex.getMessage());

        } catch (Exception ex) {

            log.error("Unexpected error occurred: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " + ex.getMessage());

        }


    }

    @Override
    public List<Order> getAll() {
        List<OrderEntity> orderEntities = orderRepository.findAll();

        return orderEntities.parallelStream().map(orderEntity -> {
            Order order = orderMapper.EntityToOrder(orderEntity);

            try {
                Order.ProductDetails productDetails = productServiceClient.getProductById(orderEntity.getProductId());
                order.setProductDetails(productDetails);
            } catch (Exception ex) {
                log.error("Error occurred while fetching product details for productId {}: {}", orderEntity.getProductId(), ex.getMessage());
                order.setProductDetails(null);
            }

            try {
                order.setPaymentDetails(paymentServiceClient.getPaymentByOrderId(orderEntity.getOrderId()));
            }catch (Exception ex) {
                log.error("Error occurred while fetching payment details for orderId {}: {}", orderEntity.getOrderId(), ex.getMessage());
                order.setPaymentDetails(null);
            }
            return order;
        }).toList();
    }

    @CircuitBreaker(name = PRODUCT_SERVICE, fallbackMethod = "reduceQuantityFallback")
    private void callProductServiceReduceQuantity(Long productId, long quantity) {

        log.info("Calling ProductService to reduce quantity for productId: {}", productId);
        productServiceClient.reduceQuantity(productId, quantity);
        log.info("Product quantity reduced successfully for productId: {}", productId);
    }

    private void reduceQuantityFallback(Long productId, long quantity, Throwable t) {

        log.error("Circuit Breaker OPEN or FAILED for ProductService. reduceQuantity for productId: {} failed. Error: {}", productId, t.getMessage());
        throw new ServiceUnavailableException("Product Service is down or timed out. Inventory check failed.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @CircuitBreaker(name = PAYMENT_SERVICE, fallbackMethod = "doPaymentFallback")
    private void callPaymentServiceDoPayment(CreatePaymentRequest request){

        log.info("Attempting to process payment for orderId: {}", request.orderId());
        paymentServiceClient.doPayment(request);
        log.info("Payment processed successfully for orderId: {}", request.orderId());
    }

    private void doPaymentFallback(CreatePaymentRequest request, Throwable t) {

        log.error("Circuit Breaker OPEN or FAILED for PaymentService. doPayment for orderId: {} failed. Error: {}", request.orderId(), t.getMessage());
        throw new ServiceUnavailableException("Payment Service is down or timed out. Payment could not be processed.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
