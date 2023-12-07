// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2020 JosieToolkit contributors
// SPDX-FileCopyrightText: 2020 lordpipe
package josie.toolkit;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public interface JosieEvents extends Listener, EventExecutor {
    static <T extends Event> JosieEvents listen(Plugin plugin, Class<T> type, Consumer<T> listener) {
        return listen(plugin, type, EventPriority.NORMAL, listener);
    }

    static <T extends Event> JosieEvents listen(
            Plugin plugin, Class<T> type, EventPriority priority, Consumer<T> listener) {
        final JosieEvents events = ($, event) -> listener.accept(type.cast(event));
        Bukkit.getPluginManager().registerEvent(type, events, priority, events, plugin);
        return events;
    }

    default void unregister() {
        HandlerList.unregisterAll(this);
    }
}
