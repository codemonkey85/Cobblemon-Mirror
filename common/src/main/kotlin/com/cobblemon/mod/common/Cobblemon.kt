/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.CobblemonBuildDetails.smallCommitHash
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.EvolvePokemonContext
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.SeasonResolver
import com.cobblemon.mod.common.api.data.DataProvider
import com.cobblemon.mod.common.api.drop.CommandDropEntry
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.CobblemonEvents.BATTLE_VICTORY
import com.cobblemon.mod.common.api.events.CobblemonEvents.DATA_SYNCHRONIZED
import com.cobblemon.mod.common.api.events.CobblemonEvents.EVOLUTION_COMPLETE
import com.cobblemon.mod.common.api.events.CobblemonEvents.LEVEL_UP_EVENT
import com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_CAPTURED
import com.cobblemon.mod.common.api.events.CobblemonEvents.TRADE_COMPLETED
import com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer
import com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer
import com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer
import com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer
import com.cobblemon.mod.common.api.permission.PermissionValidator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculators
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffectRegistry
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceCalculator
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroups
import com.cobblemon.mod.common.api.pokemon.experience.StandardExperienceCalculator
import com.cobblemon.mod.common.api.pokemon.feature.ChoiceSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemProvider
import com.cobblemon.mod.common.api.pokemon.stats.EvCalculator
import com.cobblemon.mod.common.api.pokemon.stats.Generation8EvCalculator
import com.cobblemon.mod.common.api.pokemon.stats.StatProvider
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.scheduling.ServerRealTimeTaskTracker
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.CobblemonSpawningProspector
import com.cobblemon.mod.common.api.spawning.context.AreaContextResolver
import com.cobblemon.mod.common.api.spawning.prospecting.SpawningProspector
import com.cobblemon.mod.common.api.starter.StarterHandler
import com.cobblemon.mod.common.api.storage.PokemonStoreManager
import com.cobblemon.mod.common.api.storage.adapter.conversions.ReforgedConversion
import com.cobblemon.mod.common.api.storage.adapter.database.MongoDBStoreAdapter
import com.cobblemon.mod.common.api.storage.adapter.flatfile.FileStoreAdapter
import com.cobblemon.mod.common.api.storage.adapter.flatfile.JSONStoreAdapter
import com.cobblemon.mod.common.api.storage.adapter.flatfile.NBTStoreAdapter
import com.cobblemon.mod.common.api.storage.factory.FileBackedPokemonStoreFactory
import com.cobblemon.mod.common.api.storage.molang.NbtMoLangDataStoreFactory
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.player.PlayerDataStoreManager
import com.cobblemon.mod.common.api.storage.player.factory.JsonPlayerDataStoreFactory
import com.cobblemon.mod.common.api.storage.player.factory.MongoPlayerDataStoreFactory
import com.cobblemon.mod.common.api.tags.CobblemonEntityTypeTags
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.battles.BattleFormat
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.ShowdownThread
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.command.argument.DialogueArgumentType
import com.cobblemon.mod.common.command.argument.MoveArgumentType
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.command.argument.PokemonArgumentType
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.command.argument.PokemonStoreArgumentType
import com.cobblemon.mod.common.command.argument.SpawnBucketArgumentType
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.config.LastChangedVersion
import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.config.starter.StarterConfig
import com.cobblemon.mod.common.data.CobblemonDataProvider
import com.cobblemon.mod.common.events.AdvancementHandler
import com.cobblemon.mod.common.events.ServerTickHandler
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.permission.LaxPermissionValidator
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.aspects.GENDER_ASPECT
import com.cobblemon.mod.common.pokemon.aspects.SHINY_ASPECT
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution
import com.cobblemon.mod.common.pokemon.feature.TagSeasonResolver
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityPropertyType
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.cobblemon.mod.common.pokemon.properties.tags.PokemonFlagProperty
import com.cobblemon.mod.common.pokemon.stat.CobblemonStatProvider
import com.cobblemon.mod.common.starter.CobblemonStarterHandler
import com.cobblemon.mod.common.trade.TradeManager
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.ifDedicatedServer
import com.cobblemon.mod.common.util.isLaterVersion
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.removeAmountIf
import com.cobblemon.mod.common.util.server
import com.cobblemon.mod.common.world.feature.CobblemonPlacedFeatures
import com.cobblemon.mod.common.world.feature.ore.CobblemonOrePlacedFeatures
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.item.Items
import net.minecraft.item.NameTagItem
import net.minecraft.registry.RegistryKey
import net.minecraft.util.WorldSavePath
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.math.roundToInt

object Cobblemon {
    const val MODID = CobblemonBuildDetails.MOD_ID
    const val VERSION = CobblemonBuildDetails.VERSION
    const val CONFIG_PATH = "config/$MODID/main.json"
    val LOGGER: Logger = LogManager.getLogger()

    lateinit var implementation: CobblemonImplementation
    @Deprecated("This field is now a config value", ReplaceWith("Cobblemon.config.captureCalculator"))
    var captureCalculator: CaptureCalculator
        get() = this.config.captureCalculator
        set(value) {
            this.config.captureCalculator = value
        }
    var experienceCalculator: ExperienceCalculator = StandardExperienceCalculator
    var evYieldCalculator: EvCalculator = Generation8EvCalculator
    var starterHandler: StarterHandler = CobblemonStarterHandler()
    var isDedicatedServer = false
    val showdownThread = ShowdownThread()
    lateinit var config: CobblemonConfig
    var prospector: SpawningProspector = CobblemonSpawningProspector
    var areaContextResolver: AreaContextResolver = object : AreaContextResolver {}
    val bestSpawner = BestSpawner
    val battleRegistry = BattleRegistry
    var storage = PokemonStoreManager()
    var molangData = NbtMoLangDataStoreFactory
    lateinit var playerData: PlayerDataStoreManager
    lateinit var starterConfig: StarterConfig
    val dataProvider: DataProvider = CobblemonDataProvider
    var permissionValidator: PermissionValidator by Delegates.observable(LaxPermissionValidator().also { it.initialize() }) { _, _, newValue -> newValue.initialize() }
    var statProvider: StatProvider = CobblemonStatProvider
    var seasonResolver: SeasonResolver = TagSeasonResolver

    fun preInitialize(implementation: CobblemonImplementation) {
        this.implementation = implementation

        this.LOGGER.info("Launching Cobblemon ${CobblemonBuildDetails.VERSION}${if(CobblemonBuildDetails.SNAPSHOT) "-SNAPSHOT" else ""} ")
        if(CobblemonBuildDetails.SNAPSHOT) {
            this.LOGGER.info("  - Git Commit: ${smallCommitHash()} (https://gitlab.com/cable-mc/cobblemon/-/commit/${CobblemonBuildDetails.GIT_COMMIT})")
            this.LOGGER.info("  - Branch: ${CobblemonBuildDetails.BRANCH}")
        }

        implementation.registerPermissionValidator()
        implementation.registerSoundEvents()
        implementation.registerBlocks()
        implementation.registerItems()
        implementation.registerEntityTypes()
        implementation.registerEntityAttributes()
        implementation.registerBlockEntityTypes()
        implementation.registerWorldGenFeatures()
        implementation.registerParticles()

        DropEntry.register("command", CommandDropEntry::class.java)
        DropEntry.register("item", ItemDropEntry::class.java, isDefault = true)

        ExperienceGroups.registerDefaults()
        CaptureCalculators.registerDefaults()

        this.loadConfig()
//        CobblemonBlockPredicates.touch()
        CobblemonOrePlacedFeatures.register()
        CobblemonPlacedFeatures.register()
        this.registerArgumentTypes()

        CobblemonCriteria // Init the fields and register the criteria
        CobblemonGameRules // Init fields and register

        ShoulderEffectRegistry.register()

        DATA_SYNCHRONIZED.subscribe {
            storage.onPlayerDataSync(it)
            playerData.get(it).sendToPlayer(it)
            starterHandler.handleJoin(it)
            ServerSettingsPacket(this.config.preventCompletePartyDeposit, this.config.displayEntityLevelLabel).sendToPlayer(it)
        }
        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe {
            PCLinkManager.removeLink(it.player.uuid)
            BattleRegistry.getBattleByParticipatingPlayer(it.player)?.stop()
            storage.onPlayerDisconnect(it.player)
            playerData.onPlayerDisconnect(it.player)
            TradeManager.onLogoff(it.player)
        }
        PlatformEvents.PLAYER_DEATH.subscribe {
            PCLinkManager.removeLink(it.player.uuid)
            battleRegistry.getBattleByParticipatingPlayer(it.player)?.stop()
        }

        PlatformEvents.RIGHT_CLICK_ENTITY.subscribe { event ->
            if (event.player.getStackInHand(event.hand).item is NameTagItem && event.entity.type.isIn(CobblemonEntityTypeTags.CANNOT_HAVE_NAME_TAG)) {
                event.cancel()
            }
        }
        PlatformEvents.RIGHT_CLICK_BLOCK.subscribe { event ->
            val player = event.player
            val block = player.world.getBlockState(event.pos).block
            player.party().forEach { pokemon ->
                pokemon.lockedEvolutions
                    .filterIsInstance<BlockClickEvolution>()
                    .forEach { evolution ->
                        evolution.attemptEvolution(pokemon, BlockClickEvolution.BlockInteractionContext(block, player.world))
                    }
            }
        }

        // Register the grow_tumblestone advancement
        PlatformEvents.RIGHT_CLICK_BLOCK.subscribe { AdvancementHandler.onTumbleStonePlaced(it) }

        PlatformEvents.CHANGE_DIMENSION.subscribe {
            it.player.party().forEach { pokemon -> pokemon.entity?.recallWithAnimation() }
        }

        TrackedDataHandlerRegistry.register(Vec3DataSerializer)
        TrackedDataHandlerRegistry.register(StringSetDataSerializer)
        TrackedDataHandlerRegistry.register(PoseTypeDataSerializer)
        TrackedDataHandlerRegistry.register(IdentifierDataSerializer)

        // Lowest priority because this applies after luxury ball bonus as of gen 4
        CobblemonEvents.FRIENDSHIP_UPDATED.subscribe(Priority.LOWEST) { event ->
            var increment = (event.newFriendship - event.pokemon.friendship).toFloat()
            // Our Luxury ball spec is diff from official, but we will still assume these stack
            if (event.pokemon.heldItemNoCopy().isIn(CobblemonItemTags.IS_FRIENDSHIP_BOOSTER)) {
                increment += increment * 0.5F
            }
            event.newFriendship = event.pokemon.friendship + increment.roundToInt()
        }

        HeldItemProvider.register(CobblemonHeldItemManager, Priority.LOWEST)
    }

    fun initialize() {
        showdownThread.launch()

        // Start up the data provider.
        CobblemonDataProvider.registerDefaults()

        SHINY_ASPECT.register()
        GENDER_ASPECT.register()

        SpeciesFeatures.types["choice"] = ChoiceSpeciesFeatureProvider::class.java
        SpeciesFeatures.types["flag"] = FlagSpeciesFeatureProvider::class.java
        SpeciesFeatures.types["integer"] = IntSpeciesFeatureProvider::class.java

        SpeciesFeatures.register(
            DataKeys.CAN_BE_MILKED,
            FlagSpeciesFeatureProvider(keys = listOf(DataKeys.CAN_BE_MILKED), default = true))
        SpeciesFeatures.register(
            DataKeys.HAS_BEEN_SHEARED,
            FlagSpeciesFeatureProvider(keys = listOf(DataKeys.HAS_BEEN_SHEARED), default = false))

        CustomPokemonProperty.register(UncatchableProperty)
        CustomPokemonProperty.register(PokemonFlagProperty)
        CustomPokemonProperty.register(HiddenAbilityPropertyType)

        ifDedicatedServer {
            isDedicatedServer = true
        }

        PlatformEvents.SERVER_TICK_POST.subscribe {
            ServerTaskTracker.update(1/20F)
            ServerRealTimeTaskTracker.update()
        }
        PlatformEvents.SERVER_TICK_PRE.subscribe {
            ServerRealTimeTaskTracker.update()
        }
        PlatformEvents.SERVER_STARTING.subscribe { event ->
            val server = event.server
            playerData = PlayerDataStoreManager().also { it.setup(server) }

            val mongoClient: MongoClient?

            val pokemonStoreRoot = server.getSavePath(WorldSavePath.ROOT).resolve("pokemon").toFile()
            val storeAdapter = when (config.storageFormat) {
                "nbt", "json" -> {
                    val jsonFactory = JsonPlayerDataStoreFactory()
                    jsonFactory.setup(server)
                    playerData.setFactory(jsonFactory)

                    if (config.storageFormat == "nbt") {
                        NBTStoreAdapter(pokemonStoreRoot.absolutePath, useNestedFolders = true, folderPerClass = true)
                    } else {
                        JSONStoreAdapter(
                            pokemonStoreRoot.absolutePath,
                            useNestedFolders = true,
                            folderPerClass = true
                        )
                    }
                }

                "mongodb" -> {
                    try {
                        Class.forName("com.mongodb.client.MongoClient")

                        val mongoClientSettings = MongoClientSettings.builder()
                            .applyConnectionString(ConnectionString(config.mongoDBConnectionString))
                            .build()
                        mongoClient = MongoClients.create(mongoClientSettings)
                        val mongoFactory = MongoPlayerDataStoreFactory(mongoClient, config.mongoDBDatabaseName)
                        playerData.setFactory(mongoFactory)
                        MongoDBStoreAdapter(mongoClient, config.mongoDBDatabaseName)
                    } catch (e: ClassNotFoundException) {
                        LOGGER.error("MongoDB driver not found.")
                        throw e
                    }
                }

                else -> throw IllegalArgumentException("Unsupported storageFormat: ${config.storageFormat}")
            }
                .with(ReforgedConversion(server.getSavePath(WorldSavePath.ROOT))) as FileStoreAdapter<*>

            storage.registerFactory(
                priority = Priority.LOWEST,
                factory = FileBackedPokemonStoreFactory(
                    adapter = storeAdapter,
                    createIfMissing = true,
                    pcConstructor = { uuid -> PCStore(uuid).also { it.resize(config.defaultBoxCount) } }
                )
            )
        }

        PlatformEvents.SERVER_STOPPED.subscribe {
            storage.unregisterAll()
            playerData.saveAll()
        }
        PlatformEvents.SERVER_STARTED.subscribe {
            bestSpawner.onServerStarted()
            battleRegistry.onServerStarted()
        }
        PlatformEvents.SERVER_TICK_POST.subscribe { ServerTickHandler.onTick(it.server) }
        POKEMON_CAPTURED.subscribe { AdvancementHandler.onCapture(it) }
//        EGG_HATCH.subscribe { AdvancementHandler.onHatch(it) }
        BATTLE_VICTORY.subscribe { AdvancementHandler.onWinBattle(it) }
        EVOLUTION_COMPLETE.subscribe(Priority.LOWEST) { event ->
            AdvancementHandler.onEvolve(event)
        }
        LEVEL_UP_EVENT.subscribe { AdvancementHandler.onLevelUp(it) }
        TRADE_COMPLETED.subscribe { AdvancementHandler.onTradeCompleted(it) }

        BagItems.observable.subscribe {
            LOGGER.info("Starting dummy Showdown battle to force it to pre-load data.")
            battleRegistry.startBattle(
                BattleFormat.GEN_9_SINGLES,
                BattleSide(PokemonBattleActor(UUID.randomUUID(), BattlePokemon(Pokemon().initialize()), -1F)),
                BattleSide(PokemonBattleActor(UUID.randomUUID(), BattlePokemon(Pokemon().initialize()), -1F)),
                true
            ).ifSuccessful { it.mute = true }
        }

    }

    fun getLevel(dimension: RegistryKey<World>): World? {
        return if (isDedicatedServer) {
            server()?.getWorld(dimension)
        } else {
            val mc = MinecraftClient.getInstance()
            return mc.server?.getWorld(dimension) ?: mc.world
        }
    }

    fun loadConfig() {
        val configFile = File(CONFIG_PATH)
        configFile.parentFile.mkdirs()

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                val fileReader = FileReader(configFile)
                this.config = CobblemonConfig.GSON.fromJson(fileReader, CobblemonConfig::class.java)
                fileReader.close()
            } catch (exception: Exception) {
                LOGGER.error("Failed to load the config! Using default config until the following has been addressed:")
                this.config = CobblemonConfig()
                exception.printStackTrace()
            }

            val defaultConfig = CobblemonConfig()

            CobblemonConfig::class.memberProperties.forEach {
                val field = it.javaField!!
                it.isAccessible = true
                field.annotations.forEach {
                    when (it) {
                        is LastChangedVersion -> {
                            val defaultChangedVersion = it.version
                            val lastSavedVersion = config.lastSavedVersion
                            if (defaultChangedVersion.isLaterVersion(lastSavedVersion)) {
                                field.set(config, field.get(defaultConfig))
                            }
                        }
                        is IntConstraint -> {
                            var value = field.get(config)
                            if (value is Int) {
                                value = value.coerceIn(it.min, it.max)
                                field.set(config, value)
                            }
                        }
                    }
                }
            }
        } else {
            this.config = CobblemonConfig()
        }

        config.lastSavedVersion = VERSION
        this.saveConfig()

        bestSpawner.loadConfig()
        PokemonSpecies.observable.subscribe { starterConfig = this.loadStarterConfig() }
    }

    fun loadStarterConfig(): StarterConfig {
        if (config.exportStarterConfig) {
            val file = File("config/cobblemon/starters.json")
            file.parentFile.mkdirs()
            if (!file.exists()) {
                val config = StarterConfig()
                val pw = PrintWriter(file)
                StarterConfig.GSON.toJson(config, pw)
                pw.close()
                return config
            }
            val reader = FileReader(file)
            val config = StarterConfig.GSON.fromJson(reader, StarterConfig::class.java)
            reader.close()
            return config
        } else {
            return StarterConfig()
        }
    }

    fun saveConfig() {
        try {
            val configFile = File(CONFIG_PATH)
            val fileWriter = FileWriter(configFile)
            // Put the config to json then flush the writer to commence writing.
            CobblemonConfig.GSON.toJson(this.config, fileWriter)
            fileWriter.flush()
            fileWriter.close()
        } catch (exception: Exception) {
            LOGGER.error("Failed to save the config! Please consult the following stack trace:")
            exception.printStackTrace()
        }
    }

    private fun registerArgumentTypes() {
        this.implementation.registerCommandArgument(cobblemonResource("pokemon"), PokemonArgumentType::class, ConstantArgumentSerializer.of(PokemonArgumentType::pokemon))
        this.implementation.registerCommandArgument(cobblemonResource("pokemon_properties"), PokemonPropertiesArgumentType::class, ConstantArgumentSerializer.of(PokemonPropertiesArgumentType::properties))
        this.implementation.registerCommandArgument(cobblemonResource("spawn_bucket"), SpawnBucketArgumentType::class, ConstantArgumentSerializer.of(SpawnBucketArgumentType::spawnBucket))
        this.implementation.registerCommandArgument(cobblemonResource("move"), MoveArgumentType::class, ConstantArgumentSerializer.of(MoveArgumentType::move))
        this.implementation.registerCommandArgument(cobblemonResource("party_slot"), PartySlotArgumentType::class, ConstantArgumentSerializer.of(PartySlotArgumentType::partySlot))
        this.implementation.registerCommandArgument(cobblemonResource("pokemon_store"), PokemonStoreArgumentType::class, ConstantArgumentSerializer.of(PokemonStoreArgumentType::pokemonStore))
        this.implementation.registerCommandArgument(cobblemonResource("dialogue"), DialogueArgumentType::class, ConstantArgumentSerializer.of(DialogueArgumentType::dialogue))
    }

}
