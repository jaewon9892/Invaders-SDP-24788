# No.1
## Team Introduction
Goal: Our team's goal is to create a dynamic and fun game by designing and developing a large variety of items to be used in the game "Space Invader"

Vision:
- a modular scalable item system
- a balanced item design system
  
Team members:
- Lim Kang Jun: Team Leader
- Jang Gi Jun: Github Manager, QA Manager
- Kim Min Chan: Director
- Lee Jun Myeong: Director, Developer + (sound)
- Jo In Jun: Developer + (visual)
- Kim Isak: Developer
## Team Requirements
- screen/GameScreen: Item spawn, despawn and collision.
- entity/Item
- entity/ItemPool
- engine/ItemManager
## Detailed Requirements
- Item Collision System || Pick up items upon collision
- Item Drop System on Enemy Destruction
- Point Item || Adds points when picked up
- Healing Item || Restores health when picked up
- Item Management || Handles items spawned on the field and removes those that go off-screen
## Dependencies on Other Teams
- SystemD || Currency System  → Potential shop system collaboration

	Files: engine/DrawManager, screen/GameScreen etc

- An Pans Man Fan || SoundEffects/BGM → dependency on item pickup/activation sound effects

- 666 || Visual Effects System → Item graphics, Item effects

	Files: engine/DrawManager.java etc

- Team scraps || Two-player Mode → Item pickup/effects for different players

	Files: engine/GameState, screen/GameScreen

- TEAM4 || Gameplay HUD → Possible shop system collaboration