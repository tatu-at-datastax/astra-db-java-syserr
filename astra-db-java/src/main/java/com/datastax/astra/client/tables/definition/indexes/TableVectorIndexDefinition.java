package com.datastax.astra.client.tables.definition.indexes;

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

import com.datastax.astra.client.core.vector.SimilarityMetric;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a definition for table vector indices, allowing configuration of
 * vector-specific options such as similarity metrics and source models.
 * This class provides a fluent interface for building vector index definitions.
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * TableVectorIndexDefinition vectorIndexDefinition = new TableVectorIndexDefinition()
 *     .column("feature_vector")
 *     .metric(SimilarityMetric.COSINE)
 *     .sourceModel("model_name")
 *     .options(new TableVectorIndexDefinitionOptions());
 * }
 * </pre>
 */
@Getter
@NoArgsConstructor
public class TableVectorIndexDefinition extends TableBaseIndexDefinition {

    /** Options for configuring the vector index. */
    private TableVectorIndexDefinitionOptions options;

    /**
     * Sets the name of the column for the vector index.
     *
     * @param column the name of the column containing vector data.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition column(String column) {
        this.column = column;
        return this;
    }

    /**
     * Configures the similarity metric to be used for the vector index.
     *
     * @param metric an instance of {@link SimilarityMetric} representing the similarity metric.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition metric(SimilarityMetric metric) {
        if (options == null) {
            this.options = new TableVectorIndexDefinitionOptions();
        }
        this.options.metric = metric.getValue();
        return this;
    }

    /**
     * Sets the source model for the vector index.
     *
     * @param sourceModel the name of the source model to be associated with the vector index.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition sourceModel(String sourceModel) {
        if (options == null) {
            this.options = new TableVectorIndexDefinitionOptions();
        }
        this.options.sourceModel = sourceModel;
        return this;
    }

    /**
     * Configures the options for the vector index.
     *
     * @param options an instance of {@link TableVectorIndexDefinitionOptions} containing vector index options.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition options(TableVectorIndexDefinitionOptions options) {
        this.options = options;
        return this;
    }
}
