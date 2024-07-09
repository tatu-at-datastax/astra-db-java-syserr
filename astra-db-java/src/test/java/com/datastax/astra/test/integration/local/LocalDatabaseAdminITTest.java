package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.NamespaceOptions;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnabledIfEnvironmentVariable(named = "ENABLED_TEST_DATA_API_LOCAL", matches = "true")
class LocalDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected DatabaseAdmin initDatabaseAdmin() {
        return DataAPIClients.createDefaultLocalDatabase().getDatabaseAdmin();
    }

    @Test
    void shouldCreateNamespaceSimpleStrategy() {
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        dbAdmin.createNamespace("ns2", NamespaceOptions.simpleStrategy(1));
        assertThat(dbAdmin.namespaceExists("ns2")).isTrue();
        Database ns2 = dbAdmin.getDatabase("ns2");
        assertThat(ns2).isNotNull();
    }

    @Test
    void shouldCreateNamespaceNetworkStrategy() {
        // Given
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        // When
        dbAdmin.createNamespace("ns3", NamespaceOptions.networkTopologyStrategy(Map.of("datacenter1", 1)));
        assertThat(dbAdmin.namespaceExists("ns3")).isTrue();
        Database ns3 = dbAdmin.getDatabase("ns3");
        assertThat(ns3).isNotNull();

        // non-passing case
        final NamespaceOptions options = NamespaceOptions.networkTopologyStrategy(Map.of("invalid", 1));
        assertThatExceptionOfType(DataApiException.class).isThrownBy(() ->
                dbAdmin.createNamespace("ns4", options));

        // DROP NAMESPACES
        dbAdmin.dropNamespace("ns3");
        assertThat(dbAdmin.namespaceExists("ns3")).isFalse();
        dbAdmin.dropNamespaceAsync("ns3");

        // non-passing case
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropNamespace(null))
                .withMessage("Parameter 'namespaceName' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropNamespace(""))
                .withMessage("Parameter 'namespaceName' should be null nor empty");
    }

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return null;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return null;
    }

    @Override
    protected String getRegion() {
        return "";
    }
}
