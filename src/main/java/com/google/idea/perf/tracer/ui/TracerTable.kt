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

package com.google.idea.perf.tracer.ui

import com.google.idea.perf.tracer.TracepointStats
import com.google.idea.perf.tracer.ui.TracerTableModel.Column
import com.google.idea.perf.util.formatNsInMs
import com.google.idea.perf.util.formatNum
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel
import javax.swing.SortOrder
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter

// Things to improve:
// * Update column width if numbers get too large.
// * Change font color based on how recently a number has changed.
// * Use color to differentiate class names from method names.

/** Displays a list of tracepoints alongside their call counts and timing measurements. */
class TracerTable(private val model: TracerTableModel) : JBTable(model) {
    private val tracepointDetailsManager = TracepointDetailsManager(this)

    init {
        configureTracerTableOrTree(this)
        addMouseListener(MyMouseListener())
        addKeyListener(MyKeyListener())
        rowSorter = MyTableRowSorter(model)
    }

    // Show the tracepoint details popup upon double-click.
    private inner class MyMouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if (e.clickCount < 2) return
            val row = rowAtPoint(e.point)
            if (row == -1) return
            showTracepointDetailsForRow(row)
        }
    }

    // Show the tracepoint details popup upon hitting <enter>.
    private inner class MyKeyListener : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            if (e.keyCode != KeyEvent.VK_ENTER) return
            if (selectionModel.isSelectionEmpty) return
            val row = selectionModel.leadSelectionIndex
            showTracepointDetailsForRow(row)
        }
    }

    class MyTableRowSorter(model: TableModel) : TableRowSorter<TableModel>(model) {
        init {
            sortsOnUpdates = true
            toggleSortOrder(Column.WALL_TIME.ordinal)
        }

        // Limit sorting directions.
        override fun toggleSortOrder(col: Int) {
            val alreadySorted = sortKeys.any {
                it.column == col && it.sortOrder != SortOrder.UNSORTED
            }
            if (alreadySorted) return
            val order = when (Column.valueOf(col)) {
                Column.TRACEPOINT -> SortOrder.ASCENDING
                Column.CALLS, Column.WALL_TIME, Column.MAX_WALL_TIME -> SortOrder.DESCENDING
            }
            sortKeys = listOf(SortKey(col, order))
        }
    }

    private fun showTracepointDetailsForRow(row: Int) {
        val modelRow = rowSorter.convertRowIndexToModel(row)
        val data = model.data?.get(modelRow) ?: return
        tracepointDetailsManager.showTracepointDetails(data)
    }

    override fun createDefaultTableHeader(): JTableHeader {
        return object: JBTableHeader() {
            init {
                // Override the renderer that JBTableHeader sets.
                // The default, center-aligned renderer looks better.
                defaultRenderer = createDefaultRenderer()
            }
        }
    }

    fun setTracepointStats(newStats: List<TracepointStats>) {
        model.setTracepointStats(newStats)
        tracepointDetailsManager.updateTracepointDetails(newStats)
    }

    companion object {

        /** Configuration common to both [TracerTable] and [TracerTree]. */
        fun configureTracerTableOrTree(table: JBTable) {
            table.font = EditorUtil.getEditorFont()
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            table.setShowGrid(false)

            // Ensure the row heights for TracerTable and TracerTree match each other.
            table.rowHeight = JBUI.scale(20)

            // Column rendering.
            val columnModel = table.columnModel
            for (col in Column.values) {
                val tableColumn = columnModel.getColumn(col.ordinal)

                // Hide some less-important columns for now.
                // Eventually we may give the user the ability to choose which columns are visible.
                if (col == Column.MAX_WALL_TIME) {
                    table.removeColumn(tableColumn)
                }

                // Column widths.
                tableColumn.minWidth = 100
                tableColumn.preferredWidth = when (col) {
                    Column.TRACEPOINT -> Integer.MAX_VALUE
                    Column.CALLS, Column.WALL_TIME, Column.MAX_WALL_TIME -> 100
                }

                // Locale-aware and unit-aware rendering for numbers.
                when (col) {
                    Column.CALLS, Column.WALL_TIME, Column.MAX_WALL_TIME -> {
                        tableColumn.cellRenderer = object : DefaultTableCellRenderer() {
                            init {
                                horizontalAlignment = SwingConstants.RIGHT
                            }

                            override fun setValue(value: Any?) {
                                if (value == null) return super.setValue(value)
                                val formatted = when (col) {
                                    Column.CALLS -> formatNum(value as Long)
                                    Column.WALL_TIME,
                                    Column.MAX_WALL_TIME -> formatNsInMs(value as Long)
                                    Column.TRACEPOINT -> error("tracepoints are not numbers")
                                }
                                super.setValue(formatted)
                            }
                        }
                    }
                    Column.TRACEPOINT -> {}
                }
            }
        }
    }
}
