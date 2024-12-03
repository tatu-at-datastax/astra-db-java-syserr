package com.datastax.astra.client.tables.commands.options;

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

import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.core.commands.CommandType.TABLE_ADMIN;
import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

@Setter
@Accessors(fluent = true, chain = true)
public class DropTableOptions extends BaseOptions<DropTableOptions> {

    /** Improve syntax. */
    public static final DropTableOptions IF_EXISTS = new DropTableOptions().ifExists(true);

    /**
     * Condition to upsert the table.
     */
    boolean ifExists = true;

    public DropTableOptions() {
        super(null, TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Accessor for serialization.
     *
     * @return
     *      accessor for serialization
     */
    public boolean isIfExists() {
        return ifExists;
    }



}