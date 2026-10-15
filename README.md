# ATLAS

**Adaptive Tagging, Loadouts & Asset Services**

A game-agnostic Java backend project for managing cosmetic catalogs, player-owned entitlements, saved loadouts, and searchable item metadata.

ATLAS explores the engineering challenges behind cosmetic inventory systems in online games, with an emphasis on data integrity, object-oriented design, maintainability, and automated testing.

**Developer:** Michael Payams  
**Technology:** Java, Spring Boot, Jackson, JUnit, Maven  
**Status:** In active development

## Current implementation

The first development milestone established the cosmetic catalog foundation:

- Domain models for cosmetic items, including identifiers, types, tags, and metadata.
- Input validation and immutable collection access.
- Repository interfaces separating domain operations from storage implementations.
- An in-memory catalog using concurrent collections.
- Atomic duplicate-identifier protection.
- 16 passing JUnit tests covering domain validation and repository behavior.

JSON catalog ingestion and additional catalog search functionality are currently under development.

Player entitlements, saved loadouts, persistent storage, and additional service functionality remain planned features.

For the full project requirements and development roadmap, see [PROJECT_SPECIFICATION.md](PROJECT_SPECIFICATION.md).

## Development workflow and AI assistance

ATLAS is an independently developed educational project that uses generative AI as a planning and project-management aid.

I established a workflow in which ChatGPT acts as a simulated engineering project manager, organizing development into numbered milestones and implementation tickets.

The workflow follows a consistent cycle:

1. **Milestone planning:** AI helps translate project requirements into smaller, achievable engineering tasks with defined objectives and acceptance criteria.
2. **Independent implementation:** I write and integrate the application code, develop tests, and make implementation decisions.
3. **Review and feedback:** I present implementation results, test output, and design decisions for AI-assisted review.
4. **Revision:** I investigate feedback, correct issues, rerun tests, and verify that the milestone meets its requirements.
5. **Progression:** Once a milestone is accepted, I move to the next development task.

For example, during the initial catalog milestone, review feedback identified the need for atomic duplicate-ID handling under concurrent access. I incorporated this requirement into the repository implementation and validated the behavior through automated testing.

The project specification was also developed with generative AI assistance. AI may contribute suggestions during planning, design discussions, and code review, but I remain responsible for evaluating recommendations, implementing the software, and verifying its behavior.

This workflow is intended to develop both my technical skills and my ability to work with structured requirements, review feedback, and clearly defined deliverables.

## Project independence

ATLAS is a personal educational project. It is not affiliated with, sponsored by, or endorsed by Epic Games, Fortnite, or any other game publisher.

The project is designed to be game-agnostic and uses original or appropriately licensed sample content.
