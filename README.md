# Droidcord
A Discord client for old Android <4.x devices. Uses proxy servers for the [HTTP](https://github.com/gtrxAC/discord-j2me/blob/main/proxy) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection. Currently work-in-progress.

## How to build
1. Install Android Studio. I use Android Studio 1.0 (yes the very first one) from 2014 though newer versions probably works fine.
2. Clone the repository (idk if you can clone a git repo directly from modern Android Studio since I use 1.0 which doesn't have one)
3. Run the project.

## Status
### Working
* Logging in
* Server list
* Channel list
* Message reading
* Message sending
* Gateway/live message updates

### Not implemented
* DM list
* Message editing
* Message deleting
* Replying to messages
* Reading older messages
* Direct messages and group DMs
* Attachment viewing
* Attachment sending
* Unread message indicators
* Jumping to messages (e.g. replies)
* Initiating DM conversations
* Ping indicators
* Reactions and emojis

## Credits
- [@gtrxac](https://github.com/gtrxAC) for his [Discord J2ME](https://github.com/gtrxAC/discord-j2me) project where most of the code came from.
- [@shinovon](https://github.com/shinovon) for their [JSON library](https://github.com/shinovon/NNJSON) (yes I know I can just use any other JSON library that works with Java 7 or whatever the hell Android 1.x uses but I'm too lazy so screw it)