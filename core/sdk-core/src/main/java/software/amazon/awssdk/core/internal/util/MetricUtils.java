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

package software.amazon.awssdk.core.internal.util;

import static software.amazon.awssdk.core.client.config.SdkClientOption.METRIC_PUBLISHER;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import software.amazon.awssdk.annotations.SdkProtectedApi;
import software.amazon.awssdk.core.RequestOverrideConfiguration;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.client.config.SdkClientConfiguration;
import software.amazon.awssdk.core.internal.http.RequestExecutionContext;
import software.amazon.awssdk.http.HttpMetric;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.metrics.MetricCollector;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.utils.OptionalUtils;
import software.amazon.awssdk.utils.Pair;

/**
 * Utility methods for working with metrics.
 *
 * TODO: Can these be made into internal APIs to reduce the protected surface area? (e.g. do not use this from the clients, put
 * it into the core). If not, this should be moved to a non-internal package.
 */
@SdkProtectedApi
public final class MetricUtils {

    private MetricUtils() {
    }

    /**
     * Resolve the correct metric publisher to use. The publisher set on the request always takes precedence.
     *
     * @param clientConfig The client configuration.
     * @param requestConfig The request override configuration.
     * @return The metric publisher to use.
     */
    public static Optional<MetricPublisher> resolvePublisher(SdkClientConfiguration clientConfig,
                                                             SdkRequest requestConfig) {
        // TODO: remove this and use the overload instead
        Optional<MetricPublisher> requestOverride = requestConfig.overrideConfiguration()
                                                                 .flatMap(RequestOverrideConfiguration::metricPublisher);
        if (requestOverride.isPresent()) {
            return requestOverride;
        }
        return Optional.ofNullable(clientConfig.option(METRIC_PUBLISHER));
    }

    /**
     * Resolve the correct metric publisher to use. The publisher set on the request always takes precedence.
     *
     * @param clientConfig The client configuration.
     * @param requestConfig The request override configuration.
     * @return The metric publisher to use.
     */
    public static Optional<MetricPublisher> resolvePublisher(SdkClientConfiguration clientConfig,
                                                             RequestOverrideConfiguration requestConfig) {
        if (requestConfig != null) {
            return OptionalUtils.firstPresent(requestConfig.metricPublisher(), () -> clientConfig.option(METRIC_PUBLISHER));
        }

        return Optional.ofNullable(clientConfig.option(METRIC_PUBLISHER));
    }

    /**
     * Measure the duration of the given callable.
     *
     * @param c The callable to measure.
     * @return A {@code Pair} containing the result of {@code c} and the duration.
     */
    public static <T> Pair<T, Duration> measureDuration(Supplier<T> c) {
        long start = System.nanoTime();
        T result = c.get();
        Duration d = Duration.ofNanos(System.nanoTime() - start);
        return Pair.of(result, d);
    }

    /**
     * Measure the duration of the given callable.
     *
     * @param c The callable to measure.
     * @return A {@code Pair} containing the result of {@code c} and the duration.
     */
    public static <T> Pair<T, Duration> measureDurationUnsafe(Callable<T> c) throws Exception {
        long start = System.nanoTime();
        T result = c.call();
        Duration d = Duration.ofNanos(System.nanoTime() - start);
        return Pair.of(result, d);
    }

    public static void collectHttpMetrics(MetricCollector metricCollector, SdkHttpFullResponse httpResponse) {
        metricCollector.reportMetric(HttpMetric.HTTP_STATUS_CODE, httpResponse.statusCode());
    }

    public static MetricCollector createAttemptMetricsCollector(RequestExecutionContext context) {
        return context.executionContext()
                      .metricCollector()
                      .createChild("ApiCallAttempt");
    }
}