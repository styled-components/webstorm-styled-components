package com.intellij.styledComponents

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns

@State(name = "StyledComponentsInjections")
class CustomInjectionsConfiguration : PersistentStateComponent<CustomInjectionsConfiguration.InjectionsState> {
    private var myState: InjectionsState? = null
    private var myPatterns: List<PlaceInfo> = emptyList()

    override fun getState(): InjectionsState? {
        return myState
    }

    override fun loadState(newState: InjectionsState) {
        updatePatterns(newState)
        myState = newState
    }

    private fun updatePatterns(newState: InjectionsState) {
        myPatterns = (newState.prefixes ?: emptyArray()).map {
            PlaceInfo(taggedTemplate(PlatformPatterns.psiElement(JSExpression::class.java)
                    .withFirstChild(withNameStartingWith(it.trim().split('.')))
            ), COMPONENT_PROPS_PREFIX, COMPONENT_PROPS_SUFFIX)
        }
    }

    fun getInjectionPlaces(): List<PlaceInfo> {
        return myPatterns
    }

    fun getTagPrefixes(): Array<String> {
        return myState?.prefixes ?: emptyArray()
    }

    fun setTagPrefixes(prefixes: Array<String>) {
        val newState = InjectionsState(prefixes)
        myState = newState
        updatePatterns(newState)
    }

    class InjectionsState(@Suppress("unused") var prefixes: Array<String>? = null)

    companion object {
        fun instance(project: Project): CustomInjectionsConfiguration = ServiceManager.getService(project, CustomInjectionsConfiguration::class.java)
    }
}