package com.intellij.styledComponents

import com.intellij.lang.LanguageNamesValidation
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.refactoring.JSNamesValidation
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ListItemsDialogWrapper
import java.awt.BorderLayout
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel

class StyledComponentsConfigurable(private val project: Project) : SearchableConfigurable {
    private val myConfiguration = CustomInjectionsConfiguration.instance(project)
    private val myTagPrefixesField = TextFieldWithBrowseButton()
    override fun isModified(): Boolean {
        return !Arrays.equals(getPrefixesFromUi(), (myConfiguration.getTagPrefixes()))
    }

    private fun getPrefixesFromUi(): Array<String> {
        return myTagPrefixesField.text.trim().split(",").map { it.trim() }.toTypedArray()
    }

    override fun getId(): String {
        return "styled-components"
    }

    override fun getDisplayName(): String {
        return "styled-components"
    }

    override fun apply() {
        myConfiguration.setTagPrefixes(getPrefixesFromUi())
        com.intellij.util.FileContentUtil.reparseFiles(project, emptyList(), true)
    }

    override fun reset() {
        myTagPrefixesField.text = myConfiguration.getTagPrefixes().joinToString()
    }

    override fun createComponent(): JComponent? {
        customizeField(myTagPrefixesField)
        val component = LabeledComponent.create(myTagPrefixesField, "Additional template tag prefixes:")

        val panel = JPanel(BorderLayout())
        panel.add(component, BorderLayout.NORTH)
        return panel
    }

    private fun customizeField(uiField: TextFieldWithBrowseButton) {
        uiField.textField.isEditable = false
        uiField.setButtonIcon(PlatformIcons.OPEN_EDIT_DIALOG_ICON)
        uiField.addActionListener {
            val tagListDialog = object : ListItemsDialogWrapper("Change template tags") {
                override fun createAddItemDialog(): String? {
                    return Messages.showInputDialog(project, "Template tag prefix:", "Add template tag",  Messages.getQuestionIcon(), null,
                            createPrefixValidator())
                }
            }

            tagListDialog.data = ListItemsDialogWrapper.createListPresentation(uiField.text)
            if (tagListDialog.showAndGet()) {
                uiField.text = ListItemsDialogWrapper.createStringPresentation(tagListDialog.data)
            }
        }
    }

    private fun createPrefixValidator(): InputValidatorEx {
        return object : InputValidatorEx {
            override fun getErrorText(value: String?): String? {
                val trimmed = value?.trim() ?: ""
                val names = trimmed.split(".")
                if (trimmed.isBlank() || names.isEmpty()) {
                    return "Value is empty"
                }
                return names.foldIndexed<String, String?>(null) { index, previous, string ->
                    if (previous != null) {
                        previous
                    } else if (index == 0 && !LanguageNamesValidation.INSTANCE.forLanguage(JavascriptLanguage.INSTANCE).isIdentifier(string, project)) {
                        "'$string' is not a valid JavaScript identifier"
                    } else if (!JSNamesValidation.isIdentifierName(string)) {
                        "'$string' is not a valid property name"
                    } else {
                        null
                    }
                }
            }

            override fun checkInput(value: String?): Boolean {
                return getErrorText(value) == null
            }

            override fun canClose(value: String?): Boolean {
                return checkInput(value)
            }
        }
    }
}