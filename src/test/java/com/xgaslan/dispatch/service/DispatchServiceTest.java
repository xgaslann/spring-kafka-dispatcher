package com.xgaslan.dispatch.service;

import com.xgaslan.dispatch.message.DispatchPreparing;
import com.xgaslan.dispatch.message.OrderCreated;
import com.xgaslan.dispatch.message.OrderDispatched;
import com.xgaslan.dispatch.util.TestEventData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @InjectMocks
    private DispatchService service;

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    @Mock
    private KafkaTemplate<String, Object> kafkaProducerMock;

    @Test
    void process_Success() throws Exception {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);

        when(kafkaProducerMock.send(anyString(), any(DispatchPreparing.class)))
                .thenReturn(future);

        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class)))
                .thenReturn(future);

        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(testEvent);

        verify(kafkaProducerMock, times(1))
                .send(eq(DISPATCH_TRACKING_TOPIC), any(OrderDispatched.class));

        verify(kafkaProducerMock, times(1))
                .send(eq("order.dispatched"), any(OrderDispatched.class));
    }

    @Test
    void testProcess_DispatchTrackingProducerThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        String dispatchTrackingErrorMessage  = "Dispatch tracking producer failure";

        doThrow(new RuntimeException(dispatchTrackingErrorMessage))
                .when(kafkaProducerMock)
                .send(eq(DISPATCH_TRACKING_TOPIC), any(DispatchPreparing.class));

        Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEvent));

        verify(kafkaProducerMock, times(1))
                .send(eq(DISPATCH_TRACKING_TOPIC), any(DispatchPreparing.class));
        verifyNoMoreInteractions(kafkaProducerMock);

        assertThat(exception.getMessage(), equalTo(dispatchTrackingErrorMessage));
    }

    @Test
    void testProcess_OrderDispatchedProducerThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        String dispatchTrackingErrorMessage  = "Order dispatched producer failure";

        doThrow(new RuntimeException(dispatchTrackingErrorMessage))
                .when(kafkaProducerMock)
                .send(eq(ORDER_DISPATCHED_TOPIC), any(OrderDispatched.class));

        Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEvent));

        verify(kafkaProducerMock, times(1))
                .send(eq(DISPATCH_TRACKING_TOPIC), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1))
                .send(eq(ORDER_DISPATCHED_TOPIC), any(OrderDispatched.class));

        assertThat(exception.getMessage(), equalTo(dispatchTrackingErrorMessage));
    }
}