/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.metrics;

import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.utils.Logger;

/**
 * A metric collector that doesn't do anything.
 */
@SdkPublicApi
public final class NoOpMetricCollector implements MetricCollector {
    private static final Logger log = Logger.loggerFor(NoOpMetricCollector.class);
    private static final NoOpMetricCollector INSTANCE = new NoOpMetricCollector();

    private NoOpMetricCollector() {
    }

    @Override
    public String name() {
        return "NoOp";
    }

    @Override
    public <T> void reportMetric(SdkMetric<T> metric, T data) {
        log.trace(() -> "Metrics reported: " + data);
    }

    @Override
    public MetricCollector createChild(String name) {
        throw new UnsupportedOperationException("No op collector does not support createChild");
    }

    @Override
    public MetricCollection collect() {
        throw new UnsupportedOperationException("No op collector does not support collect");
    }

    public static NoOpMetricCollector create() {
        return INSTANCE;
    }
}
