package org.example.mobilyecommerce.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                // Category caches
                "category",
                "categories",
                "rootCategories",
                "childCategoryIds",

                // Branding cache
                "brandingInfo",

                // Product caches
                "product",
                "products",
                "productsByCategory",
                "productsByParentCategory",
                "latestProducts",
                "topViewedProducts",
                "topSearchedProducts",
                "productsByVerification"
        );
    }
}