package com.Tulip_Tech.OrderService.service;

import com.Tulip_Tech.OrderService.client.PaymentServiceClient;
import com.Tulip_Tech.OrderService.client.ProductServiceClient;
import com.Tulip_Tech.OrderService.entity.OrderEntity;
import com.Tulip_Tech.OrderService.mapper.OrderMapper;
import com.Tulip_Tech.OrderService.model.Payment_Mode;
import com.Tulip_Tech.OrderService.model.domain.Order;
import com.Tulip_Tech.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest2 {

    @Mock
    private  OrderRepository orderRepository;

    @Mock
    private  ProductServiceClient productServiceClient;

    @Mock
    private  PaymentServiceClient paymentServiceClient;

    @Mock
    private  OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Test
    void when_getAll_success(){
        OrderEntity orderEntity = new OrderEntity(1L, 2L, 2L, "ACCEPT", Instant.now(), 1000L, Payment_Mode.CASH);
        OrderEntity orderEntity1 = new OrderEntity(2L, 3L, 3L, "ACCEPT", Instant.now(), 600L, Payment_Mode.BKASH);

        List<OrderEntity> orderEntities = List.of(orderEntity,orderEntity1);
        when(orderRepository.findAll()).thenReturn(orderEntities);

        Order mappedOrder1 = new Order(1L, 2L, 2L, Instant.now(),1000L, Payment_Mode.CASH,  null, null);
        Order mappedOrder2 = new Order(2L, 3L, 3L,Instant.now(), 200L, Payment_Mode.BKASH, null, null);

        when(orderMapper.EntityToOrder(orderEntity)).thenReturn(mappedOrder1);
        when(orderMapper.EntityToOrder(orderEntity1)).thenReturn(mappedOrder2);

        when(productServiceClient.getProductById(orderEntity.getProductId())).thenReturn(new Order.ProductDetails(2L,"Iphone 17",500L,2L));
        when(productServiceClient.getProductById(orderEntity1.getProductId())).thenReturn(new Order.ProductDetails(3L,"Samsung S50",300L,3L));

        when(paymentServiceClient.getPaymentByOrderId(orderEntity.getOrderId()))
                .thenReturn(new Order.PaymentDetails(1L, orderEntity.getOrderId(), Payment_Mode.CASH, "ref123", Instant.now(), "SUCCESS", orderEntity.getTotalAmount()));

        when(paymentServiceClient.getPaymentByOrderId(orderEntity1.getOrderId()))
                .thenReturn(new Order.PaymentDetails(2L, orderEntity1.getOrderId(), Payment_Mode.BKASH, "ref456", Instant.now(), "SUCCESS", orderEntity1.getTotalAmount()));

        List<Order> result = orderServiceImpl.getAll();

        verify(orderRepository).findAll();
        verify(orderMapper,times(1)).EntityToOrder(orderEntity);
        verify(orderMapper,times(1)).EntityToOrder(orderEntity1);
        verify(productServiceClient,times(1)).getProductById(orderEntity.getProductId());
        verify(productServiceClient,times(1)).getProductById(orderEntity1.getProductId());
        verify(paymentServiceClient,times(1)).getPaymentByOrderId(orderEntity.getOrderId());
        verify(paymentServiceClient,times(1)).getPaymentByOrderId(orderEntity1.getOrderId());

        assertEquals(2,result.size(),"Expected two orders returned");
        assertNotNull(result.get(0).getProductDetails(),"First order's product details should not be null");
        assertNotNull(result.get(1).getProductDetails(),"Second order's product details should not be null");
        assertNotNull(result.get(0).getPaymentDetails(),"First order's payment details should not be null");
        assertNotNull(result.get(1).getPaymentDetails(),"Second order's payment details should not be null");

        System.out.println("Number of orders: " + result.size());
        System.out.println("First order product details: " + result.get(0).getProductDetails().getProductName());
        System.out.println("Second order payment details: " + result.get(1).getPaymentDetails());

    }
}
