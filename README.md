# Plutonium Debug

A small, original Meteor Client addon for **loaded-world diagnostics**. It is designed for server-friendly investigation and deliberately does not include packet manipulation, chunk forcing, hidden-block reads, anti-cheat evasion, or "player bypass" behavior.

## Included modules

- **Sus Chunk Finder** — reports mature crops, nether wart, kelp, and cave vines that are already present in client-loaded chunks.
- **Amethyst ESP** — highlights already-loaded amethyst clusters at or below a configurable Y level.
- **Advanced Block ESP** — highlights a curated set of placed/base-like blocks, including bookshelves, note blocks, workstations, and amethyst blocks.
- **Spawner Nametags** — labels mob spawners the client has already received.
- **Block Notifier** — rate-limited chat notifications for a configurable block selection.
- **Home Reset** — a deliberate one-shot helper that sends `/home delete <slot>` followed by `/home set <slot>` when activated. Confirm that your server supports those commands before use.
- **Relog / player activity requests** — intentionally not implemented. A client cannot legitimately establish hidden player activity or bypass an anti-cheat; relogging should use Meteor/server controls with the server owner's permission.
- **Plutonium Debug HUD** — a dedicated Meteor HUD group with a compact diagnostic status element.
- **Loaded Region Map HUD** — a compact map of chunks already loaded by the client, with three editable notes and a list of configured Plutonium Debug hotkeys underneath.

## Safety model

Every scanner checks `World.isChunkLoaded` before reading a block. The addon never requests chunks, uses packet tricks, scans server-side state, or attempts to see through deepslate. This keeps its scope transparent: it visualizes only information the normal client already has and does not claim to bypass anti-cheat.

## Build

Use Java 21 and a compatible Meteor Client/Fabric dependency set:

```bash
gradle build
```

The output JAR is written to `build/libs/`. Place it in the Minecraft `mods` directory alongside the matching Meteor Client version.

## Server rules

Get server-owner approval before enabling any client addon. Disable modules where server rules prohibit automation or block highlighting.
