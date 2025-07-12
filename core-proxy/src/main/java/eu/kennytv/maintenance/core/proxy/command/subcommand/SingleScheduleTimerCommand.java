/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SingleScheduleTimerCommand extends ProxyCommandInfo {

    public SingleScheduleTimerCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, null);
    }

    @Override
    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission("timer") || sender.hasPermission("maintenance.singleserver.timer");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 3) {
            if (checkPermission(sender, "timer")) return;

            final Duration enableIn = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[1]);
            final Duration duration = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[2], false);
            if (enableIn == null || duration == null) {
                sender.send(getHelpMessage());
                return;
            }
            if (plugin.isMaintenance()) {
                sender.send(getMessage("alreadyEnabled"));
                return;
            }

            plugin.scheduleMaintenanceRunnable(enableIn, duration);
            sender.send(getMessage(
                    "scheduletimerStarted",
                    "%TIME%", plugin.getRunnable().getTime(),
                    "%DURATION%", plugin.getFormattedTime((int) duration.getSeconds())
            ));
        } else if (args.length == 4) {
            if (checkPermission(sender, "singleserver.timer")) return;

            final Duration enableIn = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[2], false);
            final Duration duration = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[3], false);
            if (enableIn == null || duration == null) {
                sender.send(getHelpMessage());
                return;
            }

            final Server server = plugin.getCommandManager().checkSingleTimerServerArg(sender, args[1]);
            if (server == null) return;
            if (plugin.isMaintenance(server)) {
                sender.send(getMessage("singleServerAlreadyEnabled", "%SERVER%", server.getName()));
                return;
            }

            final MaintenanceRunnableBase runnable = plugin.scheduleSingleMaintenanceRunnable(server, enableIn, duration);
            sender.send(getMessage(
                    "singleScheduletimerStarted",
                    "%TIME%", runnable.getTime(),
                    "%DURATION%", plugin.getFormattedTime((int) duration.getSeconds()),
                    "%SERVER%", server.getName()
            ));
        } else {
            sender.send(getHelpMessage());
        }
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        return args.length == 2 && sender.hasMaintenancePermission("singleserver.timer") ? plugin.getCommandManager().getServersCompletion(args[1].toLowerCase()) : Collections.emptyList();
    }
}
