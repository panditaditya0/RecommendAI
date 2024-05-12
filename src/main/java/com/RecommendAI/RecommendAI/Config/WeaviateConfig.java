package com.RecommendAI.RecommendAI.Config;

import com.RecommendAI.RecommendAI.Services.WeaviateQueryService;
import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.misc.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class WeaviateConfig {
    private final Logger logger = LoggerFactory.getLogger(WeaviateConfig.class);

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public io.weaviate.client.WeaviateClient weaviateClientMethod() {
        logger.info("CREATING weaviateClientMethod");
        Config config = new Config("http", "164.92.160.25:8080");
        io.weaviate.client.WeaviateClient client = new io.weaviate.client.WeaviateClient(config);
        Result<Meta> meta = client.misc().metaGetter().run();
        if (meta.getError() == null) {
            logger.info("meta.hostname: %s\n", meta.getResult().getHostname());
            logger.info("meta.version: %s\n", meta.getResult().getVersion());
            logger.info("meta.modules: %s\n", meta.getResult().getModules());
        } else {
            logger.info("Error: %s\n", meta.getError().getMessages());
        }
        return client;
    }
}
