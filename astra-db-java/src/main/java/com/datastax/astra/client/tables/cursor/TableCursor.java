package com.datastax.astra.client.tables.cursor;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.paging.CursorState;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.exceptions.CursorException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import lombok.Getter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of a cursor across the find items
 *
 * @param <T>
 *       type of the table
 */
public class TableCursor<T> implements Iterable<T>, Closeable, Cloneable {

    /**
     * Input table reference
     */
    @Getter
    private final Table<T> table;

    /**
     * Input Filter provided.
     * Immutable as not setter is provided.
     */
    private Filter filter;

    /**
     * Input Find options. Where will change the different options.
     * Immutable as not setter is provided.
     */
    private TableFindOptions tableFindOptions;

    /**
     * Cursor state.
     */
    private CursorState state;

    /**
     * Records to process
     */
    private List<T> buffer;

    /**
     * Current page
     */
    private Page<T> currentPage;

    /**
     * How many consumed in the current buffer.
     */
    @Getter
    private int consumedCount;

    /**
     * Cursor to iterate on the result of a query.
     *
     * @param table
     *      source table
     * @param filter
     *      current filter
     * @param options
     *      options of the find operation
     */
    public TableCursor(Table<T> table, Filter filter, TableFindOptions options) {
        this.table = table;
        this.filter = filter;
        this.tableFindOptions = options;
        this.state = CursorState.IDLE;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /**
     * Constructor by copy. Once cloning the cursor is set back at the beginning.
     *
     * @param tableCursor
     *      previous cursor
     */
    private TableCursor(TableCursor<T> tableCursor) {
        this.state = CursorState.IDLE;
        this.table = tableCursor.table;
        this.tableFindOptions = tableCursor.tableFindOptions;
        this.filter = tableCursor.filter;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    /** {@inheritDoc} */
    @Override
    public TableCursor<T> clone() {
        return new TableCursor<>(this);
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newFilter
     *      a new filter
     */
    public TableCursor<T> filter(Filter newFilter) {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.filter = newFilter;
        return newTableCursor;
    }

    /**
     * Immutable methods that return a new Cursor instance.
     *
     * @param newProjection
     *      a new projection
     */
    public TableCursor<T> project(Projection... newProjection) {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.tableFindOptions.projection(newProjection);
        return newTableCursor;
    }

    public TableCursor<T> sort(Sort... sort) {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.tableFindOptions.sort(sort);
        return newTableCursor;
    }

    public TableCursor<T> limit(int newLimit) {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.limit(newLimit);
        return newTableCursor;
    }

    public TableCursor<T> skip(int newSkip) {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.skip(newSkip);
        return newTableCursor;
    }

    public TableCursor<T> includeSimilarity() {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.includeSimilarity();
        return newTableCursor;
    }

    public TableCursor<T> includeSortVector() {
        checkIdleState();
        TableCursor<T> newTableCursor = this.clone();
        newTableCursor.includeSortVector();
        return newTableCursor;
    }

    /**
     * Change the state of the cursor to close.
     */
    @Override
    public void close() {
        this.state = CursorState.CLOSED;
    }

    /**
     * Rewind the cursor to the beginning.
     */
    private void rewind() {
        this.state = CursorState.IDLE;
        this.buffer.clear();
        this.consumedCount = 0;
    }

    // Buffer consumption
    public List<T> consumeBuffer(int n) {
        if (state == CursorState.CLOSED || state == CursorState.IDLE) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        int count = 0;
        while (!buffer.isEmpty() && count < n) {
            result.add(buffer.remove(0));
            count++;
        }
        return result;
    }

    /**
     * Validate that the cursor is in the IDLE state.
     */
    private void checkIdleState() {
        if (state != CursorState.IDLE) {
            throw new CursorException("Cannot modify cursor after it has been started.", state.toString());
        }
    }

    // Iterator implementation
    @Override
    public Iterator<T> iterator() {
        return new CursorIterator();
    }

    /**
     * Iterator about options
     */
    private class CursorIterator implements Iterator<T> {

        @Override
        public boolean hasNext() {
            if (state == CursorState.CLOSED) {
                return false;
            }
            if (state == CursorState.IDLE) {
                state = CursorState.STARTED;
            }
            if (!buffer.isEmpty()) {
                return true;
            }
            // Fetch next batch of documents into buffer (if buffer is empty)
            fetchNextBatch();
            return !buffer.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T rawDoc = buffer.remove(0);
            consumedCount++;
            return (T) rawDoc;
        }
    }

    // Fetch next batch of documents
    private void fetchNextBatch() {
        if (currentPage == null) {
            currentPage = table.findPage(filter, tableFindOptions);
            buffer.addAll(currentPage.getResults());
        } else if (currentPage.getPageState().isPresent()) {
            tableFindOptions.pageState(currentPage.getPageState().get());
            currentPage = table.findPage(filter, tableFindOptions);
            buffer.addAll(currentPage.getResults());
        }
    }

    // Additional methods
    public boolean hasNext() {
        return iterator().hasNext();
    }

    public T next() {
        return iterator().next();
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>();
        try {
            forEach(result::add);
        } finally {
            close();
        }
        return result;
    }

    /**
     * Access the size of the buffer.
     *
     * @return
     *      buffer count
     */
    public int getBufferedCount() {
        return buffer.size();
    }

    /**
     * Retrieve keyspace name.
     *
     * @return
     *      keyspace name
     */
    public String getKeyspace() {
        return table.getKeyspaceName();
    }

}
