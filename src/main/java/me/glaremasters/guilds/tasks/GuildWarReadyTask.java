package me.glaremasters.guilds.tasks;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.challenges.GuildWarStartEvent;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.SchedulerUtils;
import me.glaremasters.guilds.utils.WarUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Handles the war ready countdown using Paper/Folia schedulers. */
public final class GuildWarReadyTask {
    private final Guilds guilds;
    private int timeLeft;
    private final List<UUID> players;
    private final String message;
    private final GuildChallenge challenge;
    private final ChallengeHandler challengeHandler;
    private final String notifyType;
    private ScheduledTask task;
    private boolean preparingFinalLists;

    public GuildWarReadyTask(Guilds guilds, int timeLeft, List<UUID> players, String message, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.timeLeft = timeLeft;
        this.players = players;
        this.message = message;
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
                        notifyType, message.replace("{amount}", String.valueOf(timeLeft)), guilds.getAdventure().player(player)));
            }
        });

        timeLeft--;
        if (timeLeft != 0) {
            return;
        }
        if (preparingFinalLists) {
            return;
        }

        challenge.setJoinble(false);
        if (!challengeHandler.checkEnoughOnline(challenge.getChallenger(), challenge.getDefender(), challenge.getMinPlayersPerSide())) {
            challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_ON);
            challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__NOT_ENOUGH_ON);
            challenge.getArena().setInUse(false);
            challengeHandler.removeChallenge(challenge);
            cancel();
            return;
        }

        preparingFinalLists = true;
        challengeHandler.prepareFinalListsAsync(challenge, this::finishWar);
    }

    private void finishWar() {
        List<String> heldBack = new ArrayList<>();
        while (challenge.getAliveDefenders().size() > challenge.getAliveChallengers().size()) {
            UUID last = Iterables.getLast(challenge.getAliveDefenders().entrySet()).getKey();
            heldBack.add(getPlayerName(last));
            challenge.getAliveDefenders().remove(last);
        }
        while (challenge.getAliveChallengers().size() > challenge.getAliveDefenders().size()) {
            UUID last = Iterables.getLast(challenge.getAliveChallengers().entrySet()).getKey();
            heldBack.add(getPlayerName(last));
            challenge.getAliveChallengers().remove(last);
        }

        if (!heldBack.isEmpty()) {
            String heldBackMessage = Joiner.on(", ").join(heldBack);
            challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__REMOVED_FOR_SIZE, "{players}", heldBackMessage);
            challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__REMOVED_FOR_SIZE, "{players}", heldBackMessage);
        }

        challengeHandler.sendToArena(challenge.getAliveChallengers(), challenge.getArena().getChallengerLoc());
        challengeHandler.sendToArena(challenge.getAliveDefenders(), challenge.getArena().getDefenderLoc());
        challenge.setStarted(true);
        challenge.getDefender().setLastDefended(System.currentTimeMillis());
        Bukkit.getPluginManager().callEvent(new GuildWarStartEvent(challenge.getChallenger(), challenge.getDefender()));
        cancel();
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player == null ? uuid.toString() : player.getName();
    }

    private void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
