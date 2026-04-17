package com.cauecalil.personalfinance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI personalFinanceOpenApi() {
        Info info = new Info()
                .title("Personal Finance API")
                .description("This API manages the personal finance synchronization lifecycle, including credential setup, bank connection management, data synchronization, and analytical dashboard endpoints for accounts, transactions, categories, and cashflow.");

        info.setContact(new Contact()
                .name("Caue Calil")
                .email("cauecalil@gmail.com"));

        return new OpenAPI()
                .info(info)
                .addTagsItem(new Tag().name("Accounts").description("Operations for listing synchronized financial accounts."))
                .addTagsItem(new Tag().name("Bank Connections").description("Operations for registering, listing, and removing connected banks."))
                .addTagsItem(new Tag().name("Connect Token").description("Operations for generating Pluggy connect tokens."))
                .addTagsItem(new Tag().name("Credentials").description("Operations for storing, checking, and deleting gateway credentials."))
                .addTagsItem(new Tag().name("Dashboard").description("Operations for retrieving portfolio metrics, categories, and cashflow analytics."))
                .addTagsItem(new Tag().name("Heartbeat").description("Operations for signaling desktop application liveness."))
                .addTagsItem(new Tag().name("Sync").description("Operations for triggering end-to-end synchronization of banking data."))
                .addTagsItem(new Tag().name("Transactions").description("Operations for paginated transaction retrieval with filtering and sorting."));
    }
}