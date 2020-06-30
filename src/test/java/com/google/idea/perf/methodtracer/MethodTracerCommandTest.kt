/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.idea.perf.methodtracer

import org.junit.Test
import kotlin.test.assertEquals

private fun assertCommand(expected: MethodTracerCommand, actual: String) {
    assertEquals(expected, parseMethodTracerCommand(actual))
}

class MethodTracerCommandTest {
    @Test
    fun testCommandParser() {
        assertCommand(MethodTracerCommand.Clear, "clear")
        assertCommand(MethodTracerCommand.Reset, "reset")

        // Untrace commands.
        assertCommand(
            MethodTracerCommand.Trace(false, TraceOption.ALL, TraceTarget.PsiFinders),
            "untrace psi-finders"
        )
        assertCommand(
            MethodTracerCommand.Trace(false, TraceOption.ALL, TraceTarget.PsiFinders),
            "untrace all psi-finders"
        )
        assertCommand(
            MethodTracerCommand.Trace(false, TraceOption.ALL, TraceTarget.Tracer),
            "untrace tracer"
        )
        assertCommand(
            MethodTracerCommand.Trace(false, TraceOption.CALL_COUNT, TraceTarget.Tracer),
            "untrace count tracer"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                false,
                TraceOption.ALL,
                TraceTarget.Method("com.example.MyAction", "actionPerformed", emptyList())
            ),
            "untrace com.example.MyAction#actionPerformed"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                false,
                TraceOption.WALL_TIME,
                TraceTarget.Method("com.example.MyAction", "actionPerformed", emptyList())
            ),
            "untrace wall-time com.example.MyAction#actionPerformed"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                false,
                TraceOption.ALL,
                TraceTarget.Method(
                    "com.example.MyAction",
                    "actionPerformed",
                    listOf(ParameterIndex(0))
                )
            ),
            "untrace all com.example.MyAction#actionPerformed[0]"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                false,
                TraceOption.ALL,
                TraceTarget.Method(
                    "com.example.MyAction",
                    "actionPerformed",
                    listOf(ParameterIndex(0), ParameterIndex(1))
                )
            ),
            "untrace all com.example.MyAction#actionPerformed[0,1]"
        )

        // Trace commands.
        assertCommand(
            MethodTracerCommand.Trace(true, TraceOption.ALL, TraceTarget.PsiFinders),
            "trace psi-finders"
        )
        assertCommand(
            MethodTracerCommand.Trace(true, TraceOption.WALL_TIME, TraceTarget.PsiFinders),
            "trace wall-time psi-finders"
        )
        assertCommand(
            MethodTracerCommand.Trace(true, TraceOption.ALL, TraceTarget.Tracer),
            "trace tracer"
        )
        assertCommand(
            MethodTracerCommand.Trace(true, TraceOption.CALL_COUNT, TraceTarget.Tracer),
            "trace count tracer"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                true,
                TraceOption.ALL,
                TraceTarget.Method("com.example.MyAction", "actionPerformed", emptyList())
            ),
            "trace com.example.MyAction#actionPerformed"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                true,
                TraceOption.ALL,
                TraceTarget.Method("com.example.MyAction", "actionPerformed", emptyList())
            ),
            "trace all com.example.MyAction#actionPerformed"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                true,
                TraceOption.ALL,
                TraceTarget.Method(
                    "com.example.MyAction",
                    "actionPerformed",
                    listOf(ParameterIndex(0))
                )
            ),
            "trace all com.example.MyAction#actionPerformed[0]"
        )
        assertCommand(
            MethodTracerCommand.Trace(
                true,
                TraceOption.ALL,
                TraceTarget.Method(
                    "com.example.MyAction",
                    "actionPerformed",
                    listOf(ParameterIndex(0), ParameterIndex(1))
                )
            ),
            "trace all com.example.MyAction#actionPerformed[0,1]"
        )
    }
}