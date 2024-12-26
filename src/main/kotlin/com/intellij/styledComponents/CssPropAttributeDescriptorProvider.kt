package com.intellij.styledComponents

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageDescriptor
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.jsx.JSXAttributeDescriptorImpl
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider

private const val STYLED_COMPONENTS_PACKAGE_NAME = "styled-components"

private val JS_STRING_TYPE = JSNamedTypeFactory.createStringPrimitiveType(JSTypeSource.EMPTY)

private class CssPropAttributeDescriptorProvider : XmlAttributeDescriptorsProvider {
    override fun getAttributeDescriptors(tag: XmlTag?): Array<XmlAttributeDescriptor> {
        if (tag != null && isCssPropSupported(tag)) {
            return arrayOf(createCssPropertyDescriptor(tag))
        }
        return emptyArray()
    }

    override fun getAttributeDescriptor(name: String?, tag: XmlTag?): XmlAttributeDescriptor? {
        if (tag != null && isCssPropSupported(tag) && name.equals("css")) {
            return createCssPropertyDescriptor(tag)
        }
        return null
    }

    private fun createCssPropertyDescriptor(tag: XmlTag): XmlAttributeDescriptor {
        val implicit = JSLocalImplicitElementImpl("css", JS_STRING_TYPE, tag, null)
        return JSXAttributeDescriptorImpl.create("css", implicit, JS_STRING_TYPE, true)
    }

    private fun isCssPropSupported(tag: XmlTag): Boolean {
        if (!DialectDetector.isJSX(tag)) {
            return false
        }
        val containingFile = tag.containingFile?.originalFile
        if (containingFile == null || containingFile.virtualFile == null) {
            return false
        }
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return true
        }
        val virtualFile = containingFile.virtualFile
        val project = containingFile.project
        return CachedValuesManager.getManager(project).getCachedValue(containingFile) {
            val styledComponentsPackage = getNodePackage(STYLED_COMPONENTS_PACKAGE_NAME, project, virtualFile)
            val hasCssProp = styledComponentsPackage?.version?.isGreaterOrEqualThan(4, 0, 0) == true
            CachedValueProvider.Result(hasCssProp, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                    ProjectRootManager.getInstance(project))
        }
    }

    private fun getNodePackage(packageName: String, project: Project, virtualFile: VirtualFile): NodePackage? {
        val nodePackage = NodePackageDescriptor(packageName).findFirstDirectDependencyPackage(project, null, virtualFile)
        return if (nodePackage.isValid) nodePackage else null
    }
}
