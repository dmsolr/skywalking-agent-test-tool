/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.plugin.test.mockcollector.util;

import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.plugin.test.mockcollector.entity.Segment;

public class TraceSegmentHandler {
    private final Segment segment = new Segment();
    private String endpointName = Const.EMPTY_STRING;
    private long startTimeBucket;
    private long endTimeBucket;
    private int duration;
    private boolean isError;

    public void parseFirst(SpanObject span, SegmentObject segmentObject) {
        segment.setSegmentId(segmentObject.getTraceSegmentId());
        segment.setLatency(duration);
        segment.setStartTime(startTimeBucket);
        segment.setEndTime(endTimeBucket);
        segment.setIsError(isError ? 1 : 0);
        segment.setDataBinary(segmentObject.toByteArray());
        segment.setVersion(3);

        endpointName = span.getOperationName();
    }

    public void parseEntry(SpanObject span, SegmentObject segmentObject) {
        endpointName = span.getOperationName();
    }

    public void parseSegment(SegmentObject segmentObject) {
        segment.setTraceId(segmentObject.getTraceId());
        segmentObject.getSpansList().forEach(span -> {
            if (startTimeBucket == 0 || startTimeBucket > span.getStartTime()) {
                startTimeBucket = span.getStartTime();
            }
            if (span.getEndTime() > endTimeBucket) {
                endTimeBucket = span.getEndTime();
            }
            if (!isError && span.getIsError()) {
                isError = true;
            }
        });
        final long accurateDuration = endTimeBucket - startTimeBucket;
        duration = accurateDuration > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) accurateDuration;
    }

    public Segment build() {
        segment.setEndpointName(endpointName);
        return segment;
    }
}
