package me.glaremasters.guilds.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.SchedulerUtils;

/** Expires a pending war challenge using the global scheduler. */
public final class GuildWarChallengeCheckTask {
    private final Guilds guilds;
    private final GuildChallenge challenge;
    private final ChallengeHandler challengeHandler;
    private ScheduledTask task;

    public GuildWarChallengeCheckTask(Guilds guilds, GuildChallenge challenge, ChallengeHandler challengeHandler) {
        this.guilds = guilds;
        this.challenge = challenge;
        this.challengeHandler = challengeHandler;
    }

    public void start(long delayTicks) {
        this.task = SchedulerUtils.runGlobalLater(guilds, delayTicks, this::run);
    }

    private void run() {
        if (challengeHandler.getChallenge(challenge.getId()) == null || challenge.isAccepted()) {
            return;
        }

        challenge.getChallenger().sendMessage(guilds.getCommandManager(), Messages.WAR__GUILD_EXPIRED_CHALLENGE,
                "{guild}", challenge.getDefender().getName());
        challenge.getDefender().sendMessage(guilds.getCommandManager(), Messages.WAR__TARGET_EXPIRED_CHALLENGE);
        challenge.getArena().setInUse(false);
        challengeHandler.removeChallenge(challenge);
    }
}
