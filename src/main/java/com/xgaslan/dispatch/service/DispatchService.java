package com.xgaslan.dispatch.service;

import com.xgaslan.dispatch.message.OrderCreated;
import com.xgaslan.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";

    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(OrderCreated payload) throws Exception{
        OrderDispatched orderDispatched = OrderDispatched
                .builder()
                .orderId(payload.getOrderId())
                .build();

        kafkaProducer.send(ORDER_DISPATCHED_TOPIC, orderDispatched).get();
    }
}
