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
package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.time.Duration;

public final class StarttimerCommand extends CommandInfo {

    public StarttimerCommand(final MaintenancePlugin plugin) {
        super(plugin, "timer");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 2)) return;

        final Duration duration = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[1]);
        if (duration == null) {
            sender.send(getHelpMessage());
            return;
        }
        if (plugin.isMaintenance()) {
            sender.send(getMessage("alreadyEnabled"));
            return;
        }

        plugin.startMaintenanceRunnable(duration, true);
        sender.send(getMessage("starttimerStarted", "%TIME%", plugin.getRunnable().getTime()));
        // Webhook
        eu.kennytv.maintenance.core.util.WebhookUtil.send("enabled_with_timer", "{time}", plugin.getRunnable().getTime());
    }
}
