package com.datastax.astra.client.observer;

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

import com.datastax.astra.client.model.api.ApiData;
import com.datastax.astra.client.model.api.ApiError;
import com.datastax.astra.internal.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;
import java.util.UUID;

import static com.datastax.astra.internal.utils.AnsiUtils.cyan;
import static com.datastax.astra.internal.utils.AnsiUtils.magenta;
import static com.datastax.astra.internal.utils.AnsiUtils.yellow;

/**
 * Logging of the command.
 */
public class LoggingCommandObserver implements CommandObserver {

    private final Logger logger;
    /**
     * Log level.
     */
    private final Level logLevel;

    /**
     * Initialize with the logLevel.
     *
     * @param sourceName
     *      source name
     */
    public LoggingCommandObserver(String sourceName) {
        this(Level.DEBUG, sourceName);
    }

    /**
     * Initialize with the logLevel.
     *
     * @param sourceClass
     *      list source class
     */
    public LoggingCommandObserver(Class<?> sourceClass) {
        this(Level.DEBUG, sourceClass);
    }

    /**
     * Initialize with the logLevel.
     *
     * @param logLevel
     *      current log level
     * @param sourceClass
     *      source class
     */
    public LoggingCommandObserver(Level logLevel, Class<?> sourceClass) {
        this.logLevel = logLevel;
        this.logger   = LoggerFactory.getLogger(sourceClass);
    }

    /**
     * Initialize with the logLevel.
     *
     * @param logLevel
     *      current log level
     * @param sourceName
     *      source name
     */
    public LoggingCommandObserver(Level logLevel, String sourceName) {
        this.logLevel = logLevel;
        this.logger   = LoggerFactory.getLogger(sourceName);
    }

    /** {@inheritDoc} */
    @Override
    public void onCommand(ExecutionInfos executionInfo) {
        if (executionInfo != null) {
            String req = UUID.randomUUID().toString().substring(30);
            // Log Command
            log("Command [" + cyan(executionInfo.getCommand().getName()) + "] with id [" + cyan(req) + "]");
            log(magenta("[" + req + "][request]") + "=" + yellow("{}"),
                    JsonUtils.marshallForDataApi(executionInfo.getCommand()));
            log(magenta("[" + req + "][response]") + "=" + yellow("{}"),
                    JsonUtils.marshallForDataApi(executionInfo.getResponse()));
            log(magenta("[" + req + "][responseTime]") + "=" + yellow("{}") + " millis.",
                    executionInfo.getExecutionTime());
            // Log Data
            ApiData data = executionInfo.getResponse().getData();
            if (data != null && data.getDocument() != null) {
                log(magenta("[" + req + "][apiData/document]") + "=" + yellow("1 document retrieved, id='{}'"), data.getDocument().getId(Object.class));
            }
            if (data != null && data.getDocuments() != null) {
                log(magenta("[" + req + "][apiData/documents]") + "=" + yellow("{} document(s)."), data.getDocuments().size());
            }

            // Log Errors
            List<ApiError> errors = executionInfo.getResponse().getErrors();
            if (errors != null) {
                log(magenta("[" + req + "][errors]") + "="+ yellow("{}") +" errors detected.", errors.size());
                for (ApiError error : errors) {
                    log(magenta("[" + req + "][errors]")+ "="+ yellow("{} [code={}]"), error.getErrorMessage(), error.getErrorCode());
                }
            }
        }
    }

    /**
     * Convenient method to adjust dynamically the log level.
     * @param message
     *      log message
     * @param params
     *      arguments for the log message.
     */
    public void log(String message, Object... params) {
        switch (this.logLevel) {
            case TRACE:
                logger.trace(message, params);
                break;
            case DEBUG:
                logger.debug(message, params);
                break;
            case INFO:
                logger.info(message, params);
                break;
            case WARN:
                logger.warn(message, params);
                break;
            case ERROR:
                logger.error(message, params);
                break;
        }
    }
}