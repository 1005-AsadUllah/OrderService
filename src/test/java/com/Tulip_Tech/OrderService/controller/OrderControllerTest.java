package com.Tulip_Tech.OrderService.controller;


import com.Tulip_Tech.OrderService.model.Dto.CreateOrderRequest;
import com.Tulip_Tech.OrderService.model.Payment_Mode;
import com.Tulip_Tech.OrderService.service.OrderService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void when_PlaceOrder_Success() throws Exception {
        CreateOrderRequest cr =  new CreateOrderRequest(1L,2L,2000L, Payment_Mode.CASH);

        long expectedOrderId = 99L;
        when(orderService.placeOrder(any(CreateOrderRequest.class)))
                .thenAnswer(invocation -> ResponseEntity.ok(expectedOrderId));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/order/placeOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cr))
        )
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedOrderId)))
                .andReturn();

    }



}
