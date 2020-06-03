# PingTag
PingTag is an addon for LabyMod, this shows the ping of the player.
# Server Support
You can disbale PingTag on the server side by sending a JSON to the [LMC](https://docs.labymod.net/pages/server/protocol/) channel.
The message key is `pingtag`<br>
The JSON must look like this:
```json
{
  "allowed": false
}
```