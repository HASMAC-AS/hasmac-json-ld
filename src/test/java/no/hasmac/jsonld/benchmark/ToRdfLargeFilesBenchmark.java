/*
 * Copyright 2024 HASMAC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package no.hasmac.jsonld.benchmark;

import no.hasmac.jsonld.JsonLdError;
import no.hasmac.jsonld.api.ToRdfApi;
import no.hasmac.jsonld.document.Document;
import no.hasmac.jsonld.loader.DocumentLoaderOptions;
import no.hasmac.jsonld.loader.FileLoader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5)
@BenchmarkMode({Mode.AverageTime})
@Fork(value = 1, jvmArgs = {"-Xmx1024M", "-Xms1024M"})
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ToRdfLargeFilesBenchmark {

    private Document datagovbeDcat;

    @Setup(Level.Invocation)
    public void setUp() throws URISyntaxException, JsonLdError {
        URL fileUrl = getClass().getClassLoader().getResource("benchmark/datagovbe/dcat.jsonld");

        datagovbeDcat = (new FileLoader()).loadDocument(fileUrl.toURI(), new DocumentLoaderOptions());
    }

    public static void main(String[] args) throws RunnerException, JsonLdError, URISyntaxException {

        for (int i = 0; i<20; i++){
            ToRdfLargeFilesBenchmark toRdfLargeFilesBenchmark = new ToRdfLargeFilesBenchmark();
            toRdfLargeFilesBenchmark.setUp();
            Object o = toRdfLargeFilesBenchmark.datagovbeDcat();
            System.out.println(o.hashCode());
        }

    }

    @Benchmark
    public Object datagovbeDcat() throws JsonLdError {
        return new ToRdfApi(datagovbeDcat).get();
    }

}
