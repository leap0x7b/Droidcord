# Droidcord [![Android CI](https://github.com/leap0x7b/Droidcord/actions/workflows/android.yml/badge.svg)](https://github.com/leap0x7b/Droidcord/actions/workflows/android.yml)
A Discord client for old Android <4.x devices. Uses proxy servers for the [HTTP](https://github.com/gtrxAC/discord-j2me/blob/main/proxy) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection. Currently work-in-progress.

[Download](https://nightly.link/leap0x7b/Droidcord/workflows/android/master/app-release.apk)

## Status
### Working
* Logging in
* Server list
* Channel list
* Direct messages and group DMs
* Message reading
* Message sending
* Gateway/live message updates
* Attachment viewing

### Not implemented
* Message editing
* Message deleting
* Replying to messages
* Reading older messages
* Attachment sending
* Unread message indicators
* Jumping to messages (e.g. replies)
* Ping indicators
* Reactions and emojis
* Settings

## Credits
- [@gtrxac](https://github.com/gtrxAC) for his [Discord J2ME](https://github.com/gtrxAC/discord-j2me) project where most of the code came from.
- [@shinovon](https://github.com/shinovon) for their [JSON library](https://github.com/shinovon/NNJSON) (yes I know I can just use any other JSON library that works with Java 7 or whatever the hell Android 1.x uses but I'm too lazy so screw it)
