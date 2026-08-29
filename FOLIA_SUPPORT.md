# Paper/Folia 26.2 support

Guilds is built against `io.papermc.paper:paper-api:26.2.build.119-stable` and requires Java 25.

The plugin declares `folia-supported: true` and uses Paper's Global, Entity, Region, and Async scheduler APIs instead of the legacy Bukkit scheduler.

The Gradle run target is pinned to Minecraft/Paper 26.2 with Java 25.

## Build

```bash
./gradlew clean build --no-daemon
```

The resulting plugin is `build/libs/Guilds-<version>.jar`.
