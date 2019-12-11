package br.com.zup.beagleui.sample.widgets

import br.com.zup.beagleui.framework.widget.core.Widget

data class TextField(
    val id: String? = null,
    val description: String = "",
    val hint: String = "",
    val color: String = "#000000",
    val mask: String? = null,
    val inputType: TextFieldInputType? = null
) : Widget

enum class TextFieldInputType {
    NUMBER,
    PASSWORD,
    TEXT
}
