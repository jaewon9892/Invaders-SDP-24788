# Rain Development

## Team Introduction

Our Rain Development team's goal is to design and implement a core level structure that provides players with constant fun and challenges. Beyond simply listing enemies, we want to build a flexible and scalable system that systematically manages enemy deployments, wave flows, and difficulty escalation curves. Our vision is to maximize the replay value of the game by allowing players to experience new and exciting challenges as they progress. The team's name has a neutral meaning. It literally means "non-development," or "develop as if you're not developing." We aim to build a flexible system that allows anyone to design and expand new levels as if they were doing creative activities, rather than developing. Our team's strengths are that they are very good at bug fixes and have experience making games. So, I can be confident that I can flexibly cope with the problems I want to solve as a middleman in collaboration between teams.

- Leader
    - `Jaehun Jung`
- Team Member
    - `Hyunjun Kim`
    - `Jinyoung Park`
    - `Seonwoo Kang`
    - `Seongwon Hwang`
    - `Jaeseon Kim`
    - `Changjeong Choi`

## Team Requirements
Our team is responsible for all planning and implementation related to the game's ‘levels’. We focus on implementing two main aspects
- Wave composition for each stage
- Difficulty scaling based on level

## Detailed Requirements
1. Implementation of JSON-based level configuration file loader
2. Development of enemy wave generation and management module
3. Design of special gimmick levels (implementation of diverse level patterns and events)
4. Implementation of difficulty scaling logic
- Enemies' base movement speed and numbers will gradually increase proportionally to level variables as the game progresses into later stages.
5. Implementation of the Boss Stage Trigger System
- Implement a trigger system where clearing a set number of levels (ex. 5 normal levels) initiates a special ‘Boss Stage’. This level consists of combat against a single boss entity instead of regular enemy waves.

## Dependencies on Other Teams
1. Player & Enemy Ship Variety (Team - Pro 9): (Highly Dependent)
Designing diverse levels absolutely requires a wide variety of enemy types. We must receive enemy unit assets created by this team, each with distinct movement patterns, attack methods, and health, so we can strategically place them in levels to create engaging waves.
2. Item System (Team - No_1)
As part of level design, we want to include elements that reward players for clearing specific waves or defeating special enemies. To achieve this, we need functions developed by Team 5 to spawn or drop items (e.g., power-ups, score items) at specific coordinates. Additionally, during level design, we will structure levels so that using certain items makes clearing them easier, encouraging players to devise more strategic approaches and enhancing enjoyment.
3. Two-Player Mode (Team - Team Scrap)
We aim to implement levels that offer greater fun and strategic depth when two players cooperate, going beyond simple single-player gameplay. To achieve this, the core features of the two-player system implemented by Team 2 are absolutely essential.
