# streaming-clients

## Intended Use Case

restream.io in combination with hitbox and twitch.

Currently this project accesses twitch IRC and HitBox websockets API.
We are utlizing our own simple IRC client based on Apache commons-net Telnet implementation.

## Features

* configuration via xml config files
* seamless reconnects
* play a sound for each chat message (twitch+hitbox, useful for those who don't watch their chat too much)
* write chat messages to a log file and delete the log file after 30 seconds of inactivity (good for including chat in the stream; twitch+hitbox)
* play a sound for each new follower (hitbox only atm)
* write latest follower to a text file for OBS inclusion (hitbox only)
* each sound file does not get played back more than once every 30 secs. if it has been played more recently than 30 secs ago, the next playback will be scheduled accordingly.
* update HitBox and Twitch game titles based on stream titles at start. This eliminates the need to set the game titles individually via the restream interface, just mention it in the stream title and add the used game titles to the generic.xml config file.

## General Implementation Structure

There are the specific Twitch, HitBox etc. client implementations. Those get handled by
specific CCMs (client connection managers), each of which runs in its own TimerTask at regular
intervals and checks the connection state. If something is wrong with the connection, we drop the entire
Twitch, HitBox etc. client and set up a new one, each time making sure we re-setup the chat/follower listener
structure.



--
[![Build Status](https://travis-ci.org/jjYBdx4IL/streaming-clients.png?branch=master)](https://travis-ci.org/jjYBdx4IL/streaming-clients)
devel/java/github/streaming-clients@7146
