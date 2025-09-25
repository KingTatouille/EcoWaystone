# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EcoWaystone is a Minecraft Paper plugin built with Java 21 and Maven. The plugin provides teleportation through waystones without relying on commands, perfect for survival worlds where command teleporting feels overpowered.

**Package Structure:** `fr.mrbeams.ecoWaystone`
**Main Class:** `EcoWaystone.java` extends `JavaPlugin`
**Target API:** Paper 1.21.4

### Core Features
- **Waystone Creation:** Players create waystones using lodestones and link them with compasses/warp keys
- **Range Limiting:** Waystones have configurable ranges that can be boosted with valuable blocks (Netherite, Emerald, Diamond, Gold, Iron)
- **Power Requirements:** Optional respawn anchor power system for teleportation
- **Portal Sickness:** Random debuff system (Nausea X + Blindness) with configurable effects
- **Cross-Dimensional Travel:** Support for inter-dimensional teleportation with configurable ratios
- **Custom Warp Keys:** Alternative to compasses with custom crafting recipes
- **Waystone Naming:** Name waystones using name tags
- **Admin Controls:** Command blocks remove range limits, obsidian blocks disable waystones

## Build and Development Commands

### Maven Commands
- **Build the plugin:** `mvn clean package`
- **Compile only:** `mvn compile`
- **Clean build artifacts:** `mvn clean`

The project uses Maven Shade plugin to create a shaded JAR in the `target/` directory.

### Development Setup
- **Java Version:** 21 (required)
- **IDE:** IntelliJ IDEA (based on project location)
- **Plugin Output:** `target/EcoWaystone-1.0.jar`

## Plugin Configuration

- **Plugin Name:** EcoWaystone
- **Version:** 1.0
- **API Version:** 1.21
- **Load:** STARTUP
- **Author:** MrBeams

## Dependencies

- **Paper API:** 1.21.4-R0.1-SNAPSHOT (provided scope)
- Uses PaperMC repository for dependencies

## Plugin Architecture

### Required Components
1. **Waystone Management System**
   - Waystone creation, storage, and retrieval
   - Range calculation with boost blocks (Netherite, Emerald, Diamond, Gold, Iron)
   - Power system integration with respawn anchors
   - Naming system with name tags

2. **Teleportation System**
   - Warp key linking and validation
   - Distance checking and cross-dimensional ratios
   - Wait time with movement/damage cancellation
   - Portal sickness effects and prevention

3. **Configuration System**
   - Extensive config options (wait-time, distance limits, power requirements, etc.)
   - Localization support (lang-{locale}.yml files)
   - Runtime config modification via commands

4. **Command System**
   - `/waystones` info and help
   - `/waystones getkey` for warp key distribution
   - `/waystones config` for configuration management
   - `/waystones ratio` for dimensional ratio management
   - `/waystones reload` for config/advancement reloading

5. **Permission System**
   - `waystones.link` - Link waystones to warp keys
   - `waystones.getkey.self/all` - Warp key commands
   - `waystones.config` - Configuration access
   - `waystones.reload` - Reload permissions
   - `waystones.ratios` - Ratio management

6. **Advancement System**
   - 10 custom advancements for waystone usage milestones
   - Achievement tracking for various waystone interactions

### Key Implementation Notes
- Use Adventure API for text formatting (NamedTextColor instead of ChatColor)
- All messages must be externalized to YAML language files
- Support for custom warp key items vs compass functionality
- Block validation for boost materials and power sources
- Event handling for player interactions, movement, and damage