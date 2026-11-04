# DISCONTINUED
Droidcord has been discontinued in favor of [Discofi](https://github.com/gtrxAC/Discofi-Android), a currently work-in-progress successor to Droidcord and Discord J2ME for both Android and J2ME which has less features than Discord J2ME but way more than Droidcord. If you want to use Discord on a legacy Android device, please instead use either Discofi if you want a native Android experience or Discord J2ME on J2ME Loader if you want a more complete feature set.

# Droidcord [![Android CI](https://github.com/leap0x7b/Droidcord/actions/workflows/android.yml/badge.svg)](https://github.com/leap0x7b/Droidcord/actions/workflows/android.yml)
A Discord client for old Android <4.x devices. Uses proxy servers for the [HTTP](https://github.com/gtrxAC/discord-j2me/blob/main/proxy) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection. Currently work-in-progress.

[Download nightly build](https://nightly.link/leap0x7b/Droidcord/workflows/android/main/app-release.apk)

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
* Reading older messages

### Not implemented
* Message editing
* Message deleting
* Replying to messages
* Attachment sending
* Unread message indicators
* Jumping to messages (e.g. replies)
* Ping indicators
* Reactions and emojis
* Settings

## Credits
- [@gtrxac](https://github.com/gtrxAC) for his [Discord J2ME](https://github.com/gtrxAC/discord-j2me) project where most of the code came from.
- [@shinovon](https://github.com/shinovon) for their [JSON library](https://github.com/shinovon/NNJSON) (yes I know I can just use any other JSON library that works with Java 7 or whatever the hell Android 1.x uses but I'm too lazy so screw it)
