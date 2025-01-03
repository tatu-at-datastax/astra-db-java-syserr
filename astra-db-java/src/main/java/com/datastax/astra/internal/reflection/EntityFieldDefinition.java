package com.datastax.astra.internal.reflection;

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

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import lombok.Data;

import java.lang.reflect.Method;

@Data
public class EntityFieldDefinition {

    // --- Java Types --
    private String   name;
    private Class<?> type;
    private Method   getter;
    private Method   setter;
    private Class<?> genericValueType;
    private Class<?> genericKeyType;

    // --- Table Hints --

    // @Column
    private String      columnName;
    private ColumnTypes columnType;
    private ColumnTypes valueType;
    private ColumnTypes keyType;
    private Integer     dimension;
    private SimilarityMetric metric;
    // @PartitionBy
    private Integer     partitionByPosition;
    // @PartitionSort
    private Integer     partitionSortPosition;
    private SortOrder   partitionSortOrder;
}
