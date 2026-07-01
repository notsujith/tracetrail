package com.tracetrail.ingestion.buffer;

import com.tracetrail.ingestion.persistence.models.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tracetrail.ingestion.buffer", havingValue = "kafka")
public class KafkaSpanSink implements SpanSink {

    private final KafkaTemplate<String, SpanEnvelope> kafka;

    @Value("${tracetrail.ingestion.kafka.topic:tt.spans}")
    private String topic;

    @Override
    public void accept(long tenantId, List<Span> spans) {
        for (Span s : spans) {
            kafka.send(topic, s.getTraceId(), SpanEnvelope.from(s));
        }
    }
}