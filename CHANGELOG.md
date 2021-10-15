#### v0.2.5 (2021-10-16)

Fix cloth-config2 dependency in fabric.mod.json

#### v0.2.4 (2021-10-16)

Tailored depedency requirement settings.

* Fabric should not stop using this mod with no other dependencies
    * Still this library would work without them in usual scenarios
    * Only would work wrongly if player teleports with vehicles
* CurseForge can suggest those dependencies through its own dependency resolution step
    * `player-vehicle-desync-fix` better be required, since weird behavior while teleporting with vehicle would be a disappointing experience
    * `mtq-fix` better be optional, since it can be too much for someone
    * `cloth-config` is required, but `modmenu` is optional
* Gradle has no risk of being too specific, since devs can modify those dependencies by themselves

#### v0.2.3 (2021-10-15)

Fix missing config translation.

#### v0.2.2 (2021-10-14)

Fix config object structure for server support (client is fine)

#### v0.2.1 (2021-10-14)

Added new features related to `Entity.moveToWorld()` method and portal support.
Tested on nether portals while riding a horse and leashed animals.
But still might be unstable though since it's my first attempt.

* Added another API related to the `Entity.moveToWorld()` method.
* Now you can also travel through portals with vehicles and leashed animals (thanks to elhertz for the idea)
* Fixed bug in command related to calculating the target rotation.

Added another required dependency (from the same author):

* [mtq-fix](https://www.curseforge.com/minecraft/mc-mods/mtq-fix)

#### v0.1.1 (2021-10-12)

First non alpha release.

Added required dependencies (from the same author):

* [player-vehicle-desync-fix](https://www.curseforge.com/minecraft/mc-mods/player-vehicle-desync-fix)

Changed homepage link to CurseForge.

#### v0.1.0 (2021-10-11)

First alpha release.
