/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.plugin.test.mockcollector.mock;

import com.google.common.collect.Lists;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.common.v3.Commands;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.v3.Log;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentReference;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.apm.network.language.agent.v3.TraceSegmentReportServiceGrpc;
import org.apache.skywalking.plugin.test.mockcollector.entity.Segment;
import org.apache.skywalking.plugin.test.mockcollector.entity.Span;
import org.apache.skywalking.plugin.test.mockcollector.entity.ValidateData;

@Slf4j
public class MockTraceSegmentService extends TraceSegmentReportServiceGrpc.TraceSegmentReportServiceImplBase {

    @Override
    public StreamObserver<SegmentObject> collect(StreamObserver<Commands> responseObserver) {
        return new StreamObserver<SegmentObject>() {
            @Override
            public void onNext(SegmentObject segmentObject) {
                if (segmentObject.getSpansList().size() == 0) {
                    return;
                }

                Segment.SegmentBuilder builder = Segment.builder();
                List<Span> spans = Lists.newArrayList();
                for (SpanObject spanObject : segmentObject.getSpansList()) {
                    Span.SpanBuilder spanBuilder = Span.builder();
                    spanBuilder.operationName(spanObject.getOperationName())
                               .parentSpanId(spanObject.getParentSpanId())
                               .spanId(spanObject.getSpanId())
                               .spanLayer(spanObject.getSpanLayer().name())
                               .startTime(spanObject.getStartTime())
                               .endTime(spanObject.getEndTime())
                               .componentId(spanObject.getComponentId())
                               .componentName(spanObject.getComponent())
                               .isError(spanObject.getIsError())
                               .spanType(spanObject.getSpanType().name())
                               .peer(spanObject.getPeer());

                    for (Log log : spanObject.getLogsList()) {
                        spanBuilder.logEvent(log.getDataList());
                    }
                    for (KeyStringValuePair tags : spanObject.getTagsList()) {
                        spanBuilder.tags(tags.getKey(), tags.getValue());
                    }
                    for (SegmentReference ref : spanObject.getRefsList()) {
                        // FIXME parentServiceInstance
                        spanBuilder.ref(new Span.SegmentRef(ref));
                    }

                    spans.add(spanBuilder.build());
                }
                builder.segmentId(segmentObject.getTraceSegmentId()).spans(spans);

                ValidateData.INSTANCE.getSegmentItem()
                                     .addSegmentItem(segmentObject.getService(), builder.build());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Commands.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }

}
