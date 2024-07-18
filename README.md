# Droidcord
A Discord client for old Android <4.x devices. Uses proxy servers for the [HTTP](https://github.com/gtrxAC/discord-j2me/blob/main/proxy) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection. Currently work-in-progress.

## How to build
1. Install (Open)JDK 8 and an old version of Eclipse as ADT doesn't work with newer versions of Eclipse. (Andmore might work for the latest version of Eclipse though I didn't use that) I use Eclipse 4.2 (Juno) from 2012 though using newer versions should work fine as long as it's below version 4.9.
2. Install Android Development Tools from the Eclipse Marketplace. If Eclipse Marketplace doesn't exist on your installation of Eclipse, install it from Help > Install New Software... and search for Marketplace.
3. Copy the repository to your workspace folder. It should look something like `C:\User\[user]\[your workspace folder]\Droidcord` if you're on Windows or `/home/[user]/[your workspace folder]/Droidcord` if you're on Linux.
4. Run the project.

TODO: Make it build on Gradle and Android Studio.

## Status
### Working
* Logging in
* Server list

### Not implemented
* Channel lists
* Message reading, sending, editing, deleting
* Replying to messages
* Reading older messages
* Direct messages and group DMs
* Attachment viewing
* Attachment sending
* Gateway/live message updates
* Unread message indicators
* Jumping to messages (e.g. replies)
* Initiating DM conversations
* Ping indicators
* Reactions and emojis

## Credits
- [@gtrxac](https://github.com/gtrxAC) for his [Discord J2ME](https://github.com/gtrxAC/discord-j2me) project where most of the code came from.
- [@shinovon](https://github.com/shinovon) for their [JSON library](https://github.com/shinovon/NNJSON) (yes I know I can just use any other JSON library that works with Java 7 or whatever the hell Android 1.x uses but I'm too lazy so screw it)