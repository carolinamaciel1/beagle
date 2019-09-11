package br.com.zup.beagleui.framework.core

data class Flex (
    val flexWrap: FlexWrap? = null,
    val justifyContent: JustifyContent? = null,
    val alignItems: Alignment? = null,
    val alignSelf: Alignment? = null,
    val alignContent: Alignment? = null,
    val basis: String? = null,
    val grow: Double? = null,
    val shrink: Int? = null
)

/*
data class Flex (
    val flexWrap: FlexWrap = FlexWrap.NO_WRAP,
    val justifyContent: JustifyContent = JustifyContent.FLEX_START,
    val alignItems: Alignment = Alignment.STRETCH,
    val alignSelf: Alignment = Alignment.AUTO,
    val alignContent: Alignment = Alignment.FLEX_START,
    val basis: String = "0",
    val grow: Double = 0.0,
    val shrink: Int = 0
)
*/

enum class ItemDirection {
    INHERIT,
    LTR,
    RTL
}

enum class FlexDirection {
    ROW,
    ROW_REVERSE,
    COLUMN,
    COLUMN_REVERSE,
}

enum class FlexWrap {
    NO_WRAP,
    WRAP,
    WRAP_REVERSE
}

enum class JustifyContent {
    FLEX_START,
    CENTER,
    FLEX_END,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY
}

enum class Alignment {
    FLEX_START,
    CENTER,
    FLEX_END,
    SPACE_BETWEEN,
    SPACE_AROUND,
    BASELINE,
    AUTO,
    STRETCH
}
