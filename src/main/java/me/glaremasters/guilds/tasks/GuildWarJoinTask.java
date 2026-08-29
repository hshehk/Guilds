package me.glaremasters.guilds.tasks;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.SchedulerUtils;
import me.glaremasters.guilds.utils.WarUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Handles the war join countdown without Bukkit's single-thread scheduler. */
public final class GuildWarJoinTask {
    private final Guilds guilds;
    private int timeLeft;
    private final int readyTime;
    private final List<UUID> players;
    private final String joinMsg;
    private final String readyMsg;
    private final GuildChallenge challenge;
    private final ChallengeHandler challengeHandler;
    private final String notifyType;
    private ScheduledTask task;

    public GuildWarJoinTask(Guilds guilds, int timeLeft, int readyTime, List<UUID> players, String joinMsg, String readyMsg, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.timeLeft = timeLeft;
        this.readyTime = readyTime;
        this.players = players;
        this.joinMsg = joinMsg;
        this.readyMsg = readyMsg;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
        this.notifyType = guilds.getSettingsHandler().getMainConf().getProperty(WarSettings.NOTIFY_TYPE);
    }

    public void start() {
        this.task = SchedulerUtils.runGlobalRepeating(guilds, 0L, 20L, this::run);
    }

    private void run() {
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                SchedulerUtils.runEntity(guilds, player, () -> WarUtils.notify(
                        notifyType, joinMsg.replace("{amount}", String.valueOf(timeLeft)), guilds.getAdventure().player(player)));
            }
        });

        timeLeft--;
        if (timeLeft != 0) {
            return;
        }

        challenge.setJoinble(false);
        if (!challengeHandler.checkEnoughJoined(challenge)) {
            challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
            challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_JOINED);
            challenge.getArena().setInUse(false);
            challengeHandler.removeChallenge(challenge.getId());
            cancel();
            return;
        }

        List<UUID> warReady = Stream.concat(challenge.getChallengePlayers().stream(), challenge.getDefendPlayers().stream()).collect(Collectors.toList());
        new GuildWarReadyTask(guilds, readyTime, warReady, readyMsg, challenge, challengeHandler).start();
        cancel();
    }

    private void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
