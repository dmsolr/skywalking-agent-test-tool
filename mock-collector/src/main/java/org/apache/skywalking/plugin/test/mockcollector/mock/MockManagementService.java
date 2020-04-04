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

package org.apache.skywalking.plugin.test.mockcollector.mock;

import io.grpc.stub.StreamObserver;
import org.apache.skywalking.apm.network.common.Commands;
import org.apache.skywalking.apm.network.management.InstancePingPkg;
import org.apache.skywalking.apm.network.management.InstanceProperties;
import org.apache.skywalking.apm.network.management.ManagementServiceGrpc;

public class MockManagementService extends ManagementServiceGrpc.ManagementServiceImplBase {

    @Override
    public void reportInstanceProperties(InstanceProperties request, StreamObserver<Commands> responseObserver) {
        responseObserver.onNext(Commands.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void keepAlive(InstancePingPkg request, StreamObserver<Commands> responseObserver) {
        responseObserver.onNext(Commands.newBuilder().build());
        responseObserver.onCompleted();
    }

}
