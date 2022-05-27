package com.intellij.styledComponents

import com.intellij.lang.LanguageNamesValidation
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.refactoring.JSNamesValidation
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorTextField
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.ItemRemovable
import com.intellij.util.ui.table.*
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

class StyledComponentsConfigurable(private val project: Project) : SearchableConfigurable {
  private val myConfiguration = CustomInjectionsConfiguration.instance(project)
  private val tagsModel = TagsModel()
  private val disposable = Disposer.newDisposable()

  private fun createPrefixesField() = object : JBListTable(JBTable(tagsModel), disposable) {
    override fun getRowRenderer(p0: Int): JBTableRowRenderer = object : EditorTextFieldJBTableRowRenderer(project,
                                                                                                          PlainTextFileType.INSTANCE,
                                                                                                          disposable) {
      override fun getText(p0: JTable?, index: Int): String = tagsModel.myTags[index]
    }

    override fun getRowEditor(p0: Int): JBTableRowEditor = object : JBTableRowEditor() {
      override fun getValue(): JBTableRow = JBTableRow { (getComponent(0) as EditorTextField).text }
      override fun prepareEditor(p0: JTable?, p1: Int) {
        layout = BorderLayout()
        val editor = EditorTextField(tagsModel.myTags[p1])
        editor.addDocumentListener(RowEditorChangeListener(0))
        val validator = ComponentValidator(disposable)
        editor.addDocumentListener(object : DocumentListener {
          override fun documentChanged(event: DocumentEvent) {
            validator.updateInfo(getErrorText(editor.text)?.let { ValidationInfo(it, editor) })
          }
        })
        add(editor, BorderLayout.NORTH)
        validator.updateInfo(getErrorText(editor.text)?.let { ValidationInfo(it, editor) })
      }

      override fun getFocusableComponents(): Array<JComponent> = arrayOf(preferredFocusedComponent)
      override fun getPreferredFocusedComponent(): JComponent = getComponent(0) as JComponent

      fun getErrorText(value: String?): String? {
        val trimmed = value?.trim() ?: ""
        val names = trimmed.split(".")
        if (trimmed.isBlank() || names.isEmpty()) {
          return "Value is empty"
        }
        return names.foldIndexed<String, String?>(null) { index, previous, string ->
          if (previous != null) {
            previous
          }
          else if (index == 0 && !LanguageNamesValidation.INSTANCE.forLanguage(JavascriptLanguage.INSTANCE).isIdentifier(string, project)) {
            "'$string' is not a valid JavaScript identifier"
          }
          else if (!JSNamesValidation.isIdentifierName(string)) {
            "'$string' is not a valid property name"
          }
          else {
            null
          }
        }
      }
    }
  }

  override fun isModified(): Boolean {
    return !getPrefixesFromUi().contentEquals((myConfiguration.getTagPrefixes()))
  }

  private fun getPrefixesFromUi(): Array<String> {
    return tagsModel.myTags.toTypedArray()
  }

  override fun getId(): String {
    return "styled-components"
  }

  override fun getDisplayName(): String {
    return StyledComponentsBundle.message("styled.components.configurable.title")
  }

  override fun apply() {
    myConfiguration.setTagPrefixes(getPrefixesFromUi())
    com.intellij.util.FileContentUtil.reparseFiles(project, emptyList(), true)
  }

  override fun reset() {
    tagsModel.setTags(myConfiguration.getTagPrefixes())
  }

  override fun createComponent(): JComponent {
    val tagPrefixesField = createPrefixesField()
    val table = ToolbarDecorator.createDecorator(tagPrefixesField.table).disableUpDownActions().createPanel()
    val component = LabeledComponent.create(
      table, StyledComponentsBundle.message("styled.components.configurable.label.tag.prefixes"))

    val panel = JPanel(BorderLayout())
    panel.add(component, BorderLayout.NORTH)
    return panel
  }

  override fun disposeUIResources() {
    Disposer.dispose(disposable)
  }

  private class TagsModel : AbstractTableModel(), ItemRemovable, EditableModel {
    var myTags: MutableList<String> = ArrayList()

    override fun getRowCount(): Int {
      return myTags.size
    }

    override fun getColumnCount(): Int {
      return 1
    }

    override fun getValueAt(row: Int, column: Int): Any = myTags[row]

    override fun setValueAt(o: Any?, row: Int, column: Int) {
      myTags[row] = o as String
      fireTableCellUpdated(row, column)
    }

    override fun addRow() {
      myTags.add("")
      val row = myTags.size - 1
      fireTableRowsInserted(row, row)
    }

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {}

    override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean {
      return false
    }

    override fun removeRow(idx: Int) {
      myTags.removeAt(idx)
      fireTableRowsDeleted(idx, idx)
    }

    fun setTags(tags: Array<String>) {
      myTags.clear()
      myTags.addAll(tags)
      fireTableStructureChanged()
    }
  }
}