### Description:

This plugin stores a player's inventory in a chest on death for later retrieval. 
It has many options, but configuration and use has been kept as simple as possible.

### Features:

* Places a single or double chest at player's death location containing the player's inventory
* Optional sign placement on chest, showing player name and death date
* Configurable list of block types that chests can replace
* WorldGuard, GriefPrevention, PreciousStones and Towny aware, will not place a chest where the player does not have access. Enabled individually.
* Option to check protection plugins on chest access. Enabled individually, disabled by default.
* added WorldGuard 7.0 compatibility.
* Searches a configurable distance from death location to find a suitable chest location
* Optionally require players to have chest(s) in inventory
* Chest protection allows only owners access. Also makes chests explosion proof!
* Optionally allow player's killer to access their chest, for pvp looting.
* Prevent concurrent access to chests when killer looting is enabled. (v1.4)
* Optional quick-loot feature allows one click (sneak-punch) chest looting
* Configurable expiration time for death chests, at which point they will break and drop their contents
* Custom messages and language localization
* Per world enabled in configuration
* Uses sqlite for persistent storage
* Prevents creative mode players from opening death chests (v1.3)
* Optionally prevent players from placing items in death chests (v1.4)
* Optional sound effects! (v1.4)
* A perfect compliment to SavageDeathCompass and SavageGraveyards

### Commands:

Command | Description
------- | -----------
/deathchest&nbsp;list&nbsp;[username] | Displays a list of player's death chest locations. supply a username to list another player's deathchests, or type an asterisk (*) to list all deathchests.
/deathchest&nbsp;reload | Reloads configuration file and messages.
/deathchest&nbsp;status | Displays version info and some config settings.
/deathchest&nbsp;help&nbsp;[command] | Displays a brief help message and command usage.

### Permissions:

Permission | Description | Default
---------- | ----------- | -------
deathchest.player | Default player permissions | true
deathchest.chest | Enable death chests for player. | true
deathchest.loot | Allows player to quick-loot death chests by sneak-punching. | true
deathchest.doublechest | Allows placement of double chests if necessary. | true
deathchest.admin | Default administrator permissions | op
deathchest.freechest | Overrides require-chest configuration setting. | op
deathchest.creative-access | Overrides creative mode chest opening restriction. | op
deathchest.loot.other | Allow player to loot other player's chests. | op
deathchest.reload | Allows use of plugin reload command. | op
deathchest.list | Allows a user to view a list of their death chests and locations. | op
deathchest.list.other | Allows a user to view a list of other players death chests and locations. | op

### Installation
Put the jar in your plugins folder and restart your server. Edit the generated configuration file to your liking, 
then reload the plugin settings with the /deathchest reload command. No server restart necessary!

### Configuration
All configuration changes can be made without needing to restart your server. Just issue the reload command when 
you are satisfied with your settings in config.yml.
