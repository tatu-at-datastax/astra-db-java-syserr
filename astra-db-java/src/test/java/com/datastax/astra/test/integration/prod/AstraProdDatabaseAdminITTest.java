package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
public class AstraProdDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.PROD;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return CloudProviderType.AWS;
    }

    @Override
    protected String getRegion() {
        return "eu-west-1";
    }

}
