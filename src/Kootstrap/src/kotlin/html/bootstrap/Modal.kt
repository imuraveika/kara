package kotlin.html.bootstrap

import kara.*
import javax.servlet.http.*
import kotlin.html.*

class ModalBuilder() {
    var button: (A.() -> Unit)? = null
    var h: highlight = highlight.default
    var c: caliber = caliber.default

    fun button(h: highlight = highlight.default, size: caliber = caliber.default, b: A.() -> Unit) {
        button = b
        this.h = h
        this.c = size
    }

    var header: (HtmlBodyTag.() -> Unit)? = null
    fun header(content: HtmlBodyTag.() -> Unit) {
        header = content
    }

    var body: (HtmlBodyTag.() -> Unit)? = null
    fun body(c: HtmlBodyTag.() -> Unit) {
        body = c
    }

    var save: (BUTTON.() -> Unit)? = null
    fun save(c: BUTTON.() -> Unit) {
        save = c
    }

    var footer: (HtmlBodyTag.()->Unit)? = null
    fun footer(c: HtmlBodyTag.()->Unit) {
        footer = c
    }

    var dialogStyle: (StyledElement.() -> Unit)? = null
    fun dialogStyle(s: StyledElement.() -> Unit) {
        dialogStyle = s
    }
}

private var unique: Int = 0
private val uniqueId: String get() = "__mdl${unique++}"

fun HtmlBodyTag.modalShow(id: String, h: highlight = highlight.default, c: caliber = caliber.default, button: A.() -> Unit) {
    action("#$id".link(), h, c) {
        this["role"] = "button"
        this["data-toggle"] = "modal"
        button()
    }
}

fun HtmlBodyTag.modalDialog(id: String, content: ModalBuilder.() -> Unit, body: (DIV.(ModalBuilder) -> Unit)? = null) {
    val builder = ModalBuilder()
    builder.content()
    div(s("modal fade"), id) {
        this["tabindex"] = "-1"
        this["role"] = "dialog"
        this["aria-hidden"] = "true"
        div(s("modal-dialog")) {
            builder.dialogStyle ?. let {
                style {
                    it()
                }
            }

            div(s("modal-content")) {

                if (body == null) {
                    modalBody(builder)
                } else {
                    body(builder)
                }
            }
        }
    }
}

fun HtmlBodyTag.modalDialogForm(id: String, action: Link, formMethod: FormMethod = FormMethod.post, content: ModalBuilder.() -> Unit) {
    modalDialog(id, content) {
        form(form_horizontal) {
            this.action = action
            this.method = formMethod
            modalBody(it)
        }
    }
}

fun HtmlBodyTag.modalFrame(content: ModalBuilder.() -> Unit, body: DIV.(ModalBuilder) -> Unit) {
    val builder = ModalBuilder()
    builder.content()
    val id = uniqueId
    modalShow(id, builder.h, builder.c, builder.button!!)
    div(s("modal fade"), id) {
        this["tabindex"] = "-1"
        this["role"] = "dialog"
        this["aria-hidden"] = "true"
        div(s("modal-dialog")) {
            div(s("modal-content")) {
                body(builder)
            }
        }
    }
}

fun HtmlBodyTag.modal(content: ModalBuilder.() -> Unit) {
    modalFrame(content) {
        modalBody(it)
    }
}

fun HtmlBodyTag.modalForm(action: Link, formMethod: FormMethod = FormMethod.post, content: ModalBuilder.() -> Unit) {
    modalFrame(content) {
        form(form_horizontal) {
            this.action = action
            this.method = formMethod
            modalBody(it)
        }
    }
}

fun HtmlBodyTag.modalBody(builder: ModalBuilder) {
    val head = builder.header ?: throw RuntimeException("No header provided")
    val body = builder.body ?: throw RuntimeException("No content provided")
    val foot = builder.footer // Optional
    val submitButton = builder.save // Optional

    div(s("modal-header")) {
        a(close) {
            this["type"] = "button"
            this["aria-hidden"] = "true"
            this["data-dismiss"] = "modal"
            +"&times;"
        }

        h4(s("modal-title")) {
            head()
        }
    }

    div(s("modal-body")) {
        body()
    }

    div(s("modal-footer")) {
        if (foot != null) {
            if (submitButton != null) error("'save {}' won't work if 'footer {}' is defined")
            foot()
        }
        else {
            button(highlight.default) {
                buttonType = ButtonType.button
                this["aria-hidden"] = "true"
                this["data-dismiss"] = "modal"
                +"Close"
            }

            if (submitButton != null) {
                button(highlight.primary) {
                    buttonType = ButtonType.submit
                    submitButton()
                }
            }
        }
    }
}

class MODAL() : HtmlBodyTag(null, "div") {
}

fun <T : HtmlBodyTag> T.modal(dataUrl: Link, effect: String = "fade", content: T.() -> Unit) {
    withAttributes(content) {
        attribute("data-url", dataUrl.href())
        attribute("data-use", "modal")
    }
    div(s("modal $effect")) { }
}

fun dialog(content: ModalBuilder.() -> Unit): ActionResult {
    val builder = ModalBuilder()
    builder.content()
    return ModalResult() {
        div(s("modal-dialog")) {
            div(s("modal-content")) {
                modalBody(builder)
            }
        }
    }
}

fun dialogForm(action: Link, formMethod: FormMethod = FormMethod.post, content: ModalBuilder.() -> Unit): ActionResult {
    val builder = ModalBuilder()
    builder.content()
    return ModalResult() {
        div(s("modal-dialog")) {
            div(s("modal-content")) {
                form(form_horizontal) {
                    this.action = action
                    this.method = formMethod
                    modalBody(builder)
                }
            }
        }
    }
}

class ModalResult(val content: HtmlBodyTag.() -> Unit) : ActionResult {
    fun sendHtml(html: String, response: HttpServletResponse) {
        response.setContentType("text/html")
        val writer = response.getWriter()!!
        writer.write(html)
        writer.flush()
    }

    override fun writeResponse(context: ActionContext) {
        val modal = MODAL()
        with(modal) {
            c = s("modal-dialog")
            attribute("tabindex", "-1")
            attribute("role", "dialog")
            attribute("aria-hidden", "true")
            content()
        }
        sendHtml(modal.toString(), context.response)
    }
}

fun HtmlBodyTag.sampleModalDialog() {
    modal() {
        button {
            +"Show Modal"
        }
        header {
            h4 {
                +"The Modal Dialog"
            }
        }
        body {
            p {
                +"Text goes here"
            }
        }

        save {
            +"Save"
        }
    }
}
