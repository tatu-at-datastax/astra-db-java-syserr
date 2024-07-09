package com.datastax.astra.test.integration.dev;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractCollectionITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Allow to test Collection information.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
class AstraDevCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        //return initAstraDatabase(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        //return initAstraDatabase(AstraEnvironment.DEV, CloudProviderType.GCP, "us-central1");
        Database db = initializeDatabase(AstraEnvironment.DEV, CloudProviderType.AWS, "us-west-2");

        return db;
    }



}
