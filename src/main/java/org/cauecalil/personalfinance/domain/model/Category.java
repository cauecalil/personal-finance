package org.cauecalil.personalfinance.domain.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {
    private String id;
    private String description;
    private String descriptionTranslated;
    private String parentId;

    public String getDisplayDescription() {
        return descriptionTranslated != null ? descriptionTranslated : description;
    }
}
