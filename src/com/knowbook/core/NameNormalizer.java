package com.knowbook.core;

import com.knowbook.model.Category;

public interface NameNormalizer {

    String normalizeProfessionName(String name);

    String getProfessionUrl(String normalizedName);

}
