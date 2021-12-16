/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2021 Matt
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.cmd.bukkit;

import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.Command;
import dev.triumphteam.cmd.core.CommandManager;
import dev.triumphteam.cmd.core.exceptions.CommandRegistrationException;
import dev.triumphteam.cmd.core.execution.ExecutionProvider;
import dev.triumphteam.cmd.core.execution.SyncExecutionProvider;
import dev.triumphteam.cmd.core.sender.SenderMapper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class BukkitCommandManager<S> extends CommandManager<S> {

    private final Plugin plugin;

    private final Map<String, Command> commands = new HashMap<>();

    private final SenderMapper<S, CommandSender> senderMapper;

    private final ExecutionProvider syncExecutionProvider = new SyncExecutionProvider();
    private final ExecutionProvider asyncExecutionProvider;

    private final CommandMap commandMap;
    private final Map<String, org.bukkit.command.Command> bukkitCommands;

    private BukkitCommandManager(
            @NotNull final Plugin plugin,
            @NotNull final SenderMapper<S, CommandSender> senderMapper
    ) {
        this.plugin = plugin;
        this.senderMapper = senderMapper;
        this.asyncExecutionProvider = new BukkitAsyncExecutionProvider(plugin);

        this.commandMap = getCommandMap();
        this.bukkitCommands = getBukkitCommands(commandMap);
    }

    /**
     * Creates a new instance of the {@link BukkitCommandManager}.
     * This factory adds all the defaults based on the default sender {@link CommandSender}.
     *
     * @param plugin The {@link Plugin} instance created.
     * @return A new instance of the {@link BukkitCommandManager}.
     */
    @NotNull
    @Contract("_ -> new")
    public static BukkitCommandManager<CommandSender> create(@NotNull final Plugin plugin) {
        final BukkitCommandManager<CommandSender> commandManager = new BukkitCommandManager<>(plugin, new BukkitSenderMapper());
        //setUpDefaults(commandManager);
        return commandManager;
    }

    @Override
    public void registerCommand(@NotNull final BaseCommand baseCommand) {
        //final BukkitCommand bukkitCommand = new BukkitCommandProcessor(baseCommand, getArgumentRegistry(), getRequirementRegistry(), getMessageRegistry()).create();

        // TODO multiple classes
        /*if (!bukkitCommand.addSubCommands(baseCommand)) {
            return;
        }

        final String commandName = bukkitCommand.getName();

        final org.bukkit.command.Command oldCommand = commandMap.getCommand(commandName);

        // From ACF (https://github.com/aikar/commands)
        // To allow commands to be registered on the plugin.yml
        if (oldCommand instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand) oldCommand).getPlugin() == plugin) {
            unregisterCommand(baseCommand);
        }

        // Registering
        commandMap.register(commandName, plugin.getName(), bukkitCommand);
        commands.put(commandName, bukkitCommand);*/
    }

    @Override
    public void unregisterCommand(@NotNull final BaseCommand command) {
        // TODO add a remove functionality
    }

    /**
     * Gets the Command Map to register the commands
     *
     * @return The Command Map
     */
    @NotNull
    private CommandMap getCommandMap() {
        try {
            final Server server = Bukkit.getServer();
            final Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);

            return (CommandMap) getCommandMap.invoke(server);
        } catch (final Exception ignored) {
            throw new CommandRegistrationException("Unable get Command Map. Commands will not be registered!");
        }
    }

    @NotNull
    private Map<String, org.bukkit.command.Command> getBukkitCommands(@NotNull final CommandMap commandMap) {
        try {
            final Field bukkitCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            bukkitCommands.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) bukkitCommands.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandRegistrationException("Unable get Bukkit commands. Commands might not be registered correctly!");
        }
    }

}
