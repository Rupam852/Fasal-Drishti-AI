package com.fasaldrishti.app.domain.model

data class MandiRecord(
    val commodity: String,
    val hindiName: String,
    val variety: String,
    val market: String,
    val district: String,
    val state: String,
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int,
    val priceChange: Int, // positive or negative from yesterday (in ₹/quintal)
    val unit: String = "₹ / Quintal",
    val arrivalDate: String = "Today",
    val aiAdvice: String,
    val isSellFavorable: Boolean
)
