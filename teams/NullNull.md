# NullNull
## Team Introduction
Hello
- Team leader
    - `Yerim Kim` : Development, General Management
- Team members
    - `Inhwa Park`: Development, GitHub, Review
    - `Dain Jeong` : Development, Design
    - `Seohyun Park` : Development, Presentation
    - `Hamin Park` : Development, Github
    - `Dahye Jung` : Development, Design
    - `Sumin Seak` : Development, Review
## Team Requirements
- Controls the HUD within the game play screen.
## Detailed Requirements
- Score
- the rest of player's life
- Show game wave steps
- the number of enemies left
- the time left
- Boss physical bar (when boss exists)
- List and number of items player has
## Dependencies
1. SwComputing (Records&AchievementsSystem) : Display the remaining score needed to reach the highest record.
2. SwComputing (Records & Achievements System): When an achievement is unlocked during gameplay, display an achievement notification on the screen for 2 seconds.
3. systemd (Currency System): Display the amount of coins obtained during the current game.
4. Pro9 (Player & Enemy Ship Variety): To show the boss health bar, it is necessary to check the enemy type and whether it is a boss.
5. Team Scrap (Two-Player Mode): In the case of two-player multiplayer, provide an alternative HUD layout.
