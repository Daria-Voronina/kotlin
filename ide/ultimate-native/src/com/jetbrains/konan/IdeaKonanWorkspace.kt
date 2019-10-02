/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package com.jetbrains.konan

import com.intellij.execution.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import org.jdom.Element
import org.jetbrains.kotlin.konan.KonanVersion
import org.jetbrains.kotlin.konan.KonanVersionImpl
import org.jetbrains.kotlin.konan.MetaVersion
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.util.DependencyDirectories
import java.io.File
import java.util.*
import kotlin.collections.HashSet

private val DEBUG_INTRODUCED = KonanVersionImpl(MetaVersion.DEV, 1, 3, 50, -1)
private val DEBUG_UPDATE_1 = KonanVersionImpl(MetaVersion.DEV, 1, 3, 60, -1)

fun compare(lhs: KonanVersion, rhs: KonanVersion): Int {
    if (lhs.major != rhs.major) return lhs.major - rhs.major
    if (lhs.minor != rhs.minor) return lhs.minor - rhs.minor
    if (lhs.maintenance != rhs.maintenance) return lhs.maintenance - rhs.maintenance
    return 0
}

@State(name = "KonanWorkspace", storages = [(Storage(StoragePathMacros.WORKSPACE_FILE))])
class IdeaKonanWorkspace(val project: Project) : PersistentStateComponent<Element>, ProjectComponent {

    val executables = HashSet<KonanExecutable>()
    private val basePath = File(project.basePath!!)
    var konanHome: String? = null

    val lldbHome: File
        get() {
            val propertiesPath = "$konanHome/konan/konan.properties"
            val hostDependenciesKey = "dependencies.${HostManager.host}"

            val propertiesFile = File(propertiesPath).apply {
                if (!exists()) throw ExecutionException("Kotlin/Native properties file is absent at $propertiesPath")
            }

            val hostDependencies = Properties().apply {
                propertiesFile.inputStream().use(::load)
            }.getProperty(hostDependenciesKey) ?: throw ExecutionException("No property $hostDependenciesKey at $propertiesPath")

            val lldbRelative = hostDependencies.split(" ").firstOrNull { it.startsWith("lldb-") }
                ?: throw ExecutionException("Property $hostDependenciesKey at $propertiesPath does not specify lldb")

            return DependencyDirectories.defaultDependenciesRoot.resolve(lldbRelative)
        }

    var konanVersion: KonanVersion? = null
        set(value) {
            value?.let {
                if (compare(it, DEBUG_INTRODUCED) < 0) {
                    KonanLog.MESSAGES.createNotification(
                        KonanBundle.message("warning.versionPrior1_3_50", it),
                        NotificationType.WARNING
                    ).notify(project)
                } else if (compare(it, DEBUG_UPDATE_1) < 0) {
                    KonanLog.MESSAGES.createNotification(
                        KonanBundle.message("warning.versionPrior1_3_60", it),
                        NotificationType.WARNING
                    ).notify(project)

                }
            }
            field = value
        }

    val isDebugPossible: Boolean
        get() {
            konanVersion?.let {
                return compare(it, DEBUG_INTRODUCED) >= 0
            }

            return false
        }

    init {
        val listener = MyListener(this)
        val connection = project.messageBus.connect()
        connection.subscribe(ExecutionTargetManager.TOPIC, listener)
    }

    override fun getState(): Element? {
        val stateElement = Element("state")
        val executablesElement = Element(XmlKonanWorkspace.nodeAllExecutables)
        executables.toSortedSet().forEach {
            val element = Element(XmlKonanWorkspace.nodeExecutable)
            it.writeToXml(element, basePath)
            executablesElement.addContent(element)
        }
        stateElement.addContent(executablesElement)
        konanHome?.let { stateElement.setAttribute(XmlKonanWorkspace.attributeKonanHome, it) }
        return stateElement
    }

    override fun loadState(stateElement: Element) {
        stateElement.getChild(XmlKonanWorkspace.nodeAllExecutables)?.getChildren(XmlKonanWorkspace.nodeExecutable)?.forEach {
            val executable = KonanExecutable.readFromXml(it, basePath) ?: return@forEach
            executables.add(executable)
        }

        konanHome = stateElement.getAttributeValue(XmlKonanWorkspace.attributeKonanHome)?.also {

        }
    }

    private class MyListener(private val workspace: IdeaKonanWorkspace) : ExecutionTargetListener {
        override fun activeTargetChanged(target: ExecutionTarget) {
            workspace.updateSelectedTarget(target)
        }
    }

    fun updateSelectedTarget(target: ExecutionTarget) {
        val configuration = RunManager.getInstance(project).selectedConfiguration?.configuration ?: return
        if (target is IdeaKonanExecutionTarget && configuration is IdeaKonanRunConfiguration) {
            configuration.selectedTarget = target
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): IdeaKonanWorkspace = project.getComponent(IdeaKonanWorkspace::class.java)
    }

    override fun projectOpened() {
        super.projectOpened()

        val runManager = RunManager.getInstance(project)

        runManager.selectedConfiguration?.apply {
            (configuration as? IdeaKonanRunConfiguration)?.let {
                it.selectedTarget = it.executable?.executionTargets?.firstOrNull()
            }
        }

        updateSelectedTarget(ExecutionTargetManager.getActiveTarget(project))

        konanHome?.let { konanVersion = getKotlinNativeVersion(it) }
    }
}