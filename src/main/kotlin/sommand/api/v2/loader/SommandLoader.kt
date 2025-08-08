package sommand.api.v2.loader

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import sommand.api.v2.BukkitSommandSource
import sommand.api.v2.CommandRegistry
import sommand.api.v2.SommandDispatcher
import sommand.api.v2.dsl.SommandBuilder
import sommand.api.v2.impl.DefaultDispatcher
import sommand.api.v2.node.RootNode
import java.lang.reflect.Field

/**
 * Loader responsible for:
 * - Running the DSL builder
 * - Registering root commands into Bukkit's CommandMap
 * - Registering permission nodes
 */
class SommandLoader(
    private val plugin: JavaPlugin,
    private val dispatcher: SommandDispatcher = DefaultDispatcher()
) {

    fun load(block: SommandBuilder.() -> Unit) {
        val builder = SommandBuilder(plugin)
        builder.block()

        // Register permissions first
        registerPermissions()

        // Register commands
        registerCommands()
    }

    private fun registerPermissions() {
        val pm = plugin.server.pluginManager
        val added = mutableSetOf<String>()
        CommandRegistry.allDistinct().forEach { root ->
            walkRegisterPermissions(root, pm, added)
        }
    }

    private fun walkRegisterPermissions(
        node: sommand.api.v2.node.SommandNode,
        pluginManager: org.bukkit.plugin.PluginManager,
        added: MutableSet<String>
    ) {
        node.permission?.let { permNode ->
            if (added.add(permNode)) {
                if (pluginManager.getPermission(permNode) == null) {
                    val perm = Permission(permNode, PermissionDefault.OP)
                    pluginManager.addPermission(perm)
                }
            }
        }
        node.children.forEach { child ->
            walkRegisterPermissions(child, pluginManager, added)
        }
    }

    private fun registerCommands() {
        val commandMap = obtainCommandMap() ?: error("Unable to obtain CommandMap from server.")
        CommandRegistry.allDistinct().forEach { root ->
            val dynamic = DynamicSommandCommand(root, dispatcher, plugin)
            // Avoid duplicate registration
            if (commandMap.getCommand(root.name) == null) {
                commandMap.register(plugin.name.lowercase(), dynamic)
            }
        }
    }

    private fun obtainCommandMap(): CommandMap? {
        // Paper exposes server.commandMap, but to remain broadly compatible we reflect.
        return try {
            val server = Bukkit.getServer()
            val field: Field = server.javaClass.getDeclaredField("commandMap")
            field.isAccessible = true
            field.get(server) as? CommandMap
        } catch (ex: Exception) {
            plugin.logger.severe("Failed to acquire CommandMap: ${ex.message}")
            null
        }
    }

    /**
     * Command wrapper bridging to our dispatcher.
     */
    private class DynamicSommandCommand(
        private val root: RootNode,
        private val dispatcher: SommandDispatcher,
        private val pluginInstance: Plugin
    ) : Command(root.name, root.description ?: "Command ${root.name}", "", root.aliases),
        PluginIdentifiableCommand {

        override fun getPlugin(): Plugin = pluginInstance

        override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
            if (root.permission != null && !sender.hasPermission(root.permission)) {
                sender.sendMessage("You do not have permission to execute this command.")
                return true
            }
            val success = dispatcher.dispatch(
                source = BukkitSommandSource(sender),
                label = label,
                tokens = args.toList(),
                root = root
            )
            if (!success) {
                sender.sendMessage(buildUsage(root))
            }
            return true
        }

        override fun tabComplete(
            sender: CommandSender,
            alias: String,
            args: Array<out String>
        ): MutableList<String> {
            if (root.permission != null && !sender.hasPermission(root.permission)) {
                return mutableListOf()
            }
            val suggestions = dispatcher.suggest(
                source = BukkitSommandSource(sender),
                label = alias,
                tokens = args.toList(),
                root = root
            )
            return suggestions.toMutableList()
        }

        private fun buildUsage(node: RootNode): String {
            // Simple usage builder (only shows literal/argument names one level deep)
            val parts = mutableListOf<String>()
            node.children.forEach { child ->
                parts += when {
                    child.argument != null -> "<${child.argument!!.name}>"
                    else -> child.name
                }
            }
            return "Usage: /${node.name} ${parts.joinToString(" ")}"
        }
    }
}