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

import com.google.idea.perf.AgentLoader
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.memory.agent.AllocationListener
import com.intellij.memory.agent.MemoryAgent
import java.lang.System
import java.lang.instrument.Instrumentation
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class AllocationPluginManager(private val agent: MemoryAgent) {
    val pluginIdToSize: MutableMap<String, Long> = ConcurrentHashMap()

    fun resetPluginClassesList() {
        pluginIdToSize.clear()
    }

    fun removePlugin(plugin: IdeaPluginDescriptor) {
        pluginIdToSize.remove(plugin.name)
    }

    fun addPlugin(plugin: IdeaPluginDescriptor) {
        val classLoader = plugin.pluginClassLoader
        val instrumentation = AgentLoader.instrumentation ?: return
        val size = agent.getRetainedSizeByClassLoaders(arrayOf(classLoader as ClassLoader))
        pluginIdToSize[plugin.name] = size[0]
    }

    fun addPlugins(plugins: List<IdeaPluginDescriptor>) {
        val classLoaders = plugins.filter {it.isEnabled}.map { it.pluginClassLoader }
        val sizes = agent.getRetainedSizeByClassLoaders(classLoaders.toTypedArray())
        //var s = 0L
        for (i in 0 until plugins.size){
            pluginIdToSize[plugins[i].name] = sizes[i]
            //s += sizes[i]
        }
//        System.out.println(s)
//        System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
    }
}