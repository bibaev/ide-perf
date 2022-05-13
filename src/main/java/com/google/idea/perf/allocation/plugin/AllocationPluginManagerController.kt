/*
 * Copyright 2022 Google LLC
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

package com.google.idea.perf.allocation.plugin

import com.intellij.memory.agent.MemoryAgent

object AllocationPluginManagerController {
    private var allocationPluginManager: AllocationPluginManager? = null
    private var isManagerInitialized = false

    fun getManager() = allocationPluginManager

    fun getManagerAndLoadAgent(): AllocationPluginManager? {
        if (!isManagerInitialized) {
            val agent = MemoryAgent.get()
            if (agent != null) {
                allocationPluginManager = AllocationPluginManager(agent)
            }
            isManagerInitialized = true
        }
        return allocationPluginManager
    }
}