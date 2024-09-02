/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.PlayerInteractOptionsHandler
import com.cobblemon.mod.common.client.net.SetClientPlayerDataHandler
import com.cobblemon.mod.common.client.net.animation.PlayPosableAnimationHandler
import com.cobblemon.mod.common.client.net.battle.*
import com.cobblemon.mod.common.client.net.callback.move.OpenMoveCallbackHandler
import com.cobblemon.mod.common.client.net.callback.party.OpenPartyCallbackHandler
import com.cobblemon.mod.common.client.net.callback.partymove.OpenPartyMoveCallbackHandler
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import com.cobblemon.mod.common.client.net.data.UnlockReloadPacketHandler
import com.cobblemon.mod.common.client.net.dialogue.DialogueClosedHandler
import com.cobblemon.mod.common.client.net.dialogue.DialogueOpenedHandler
import com.cobblemon.mod.common.client.net.effect.RunPosableMoLangHandler
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormEntityParticleHandler
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormParticleHandler
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import com.cobblemon.mod.common.client.net.gui.SummaryUIPacketHandler
import com.cobblemon.mod.common.client.net.npc.CloseNPCEditorHandler
import com.cobblemon.mod.common.client.net.npc.OpenNPCEditorHandler
import com.cobblemon.mod.common.client.net.pasture.ClosePastureHandler
import com.cobblemon.mod.common.client.net.pasture.OpenPastureHandler
import com.cobblemon.mod.common.client.net.pasture.PokemonPasturedHandler
import com.cobblemon.mod.common.client.net.pasture.PokemonUnpasturedHandler
import com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.client.net.sound.UnvalidatedPlaySoundS2CPacketHandler
import com.cobblemon.mod.common.client.net.spawn.SpawnExtraDataEntityHandler
import com.cobblemon.mod.common.client.net.starter.StarterUIPacketHandler
import com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.SwapClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.InitializePartyHandler
import com.cobblemon.mod.common.client.net.storage.party.MoveClientPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler
import com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.InitializePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.MoveClientPCPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.OpenPCHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCBoxPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCPokemonHandler
import com.cobblemon.mod.common.client.net.toast.ToastPacketHandler
import com.cobblemon.mod.common.client.net.trade.TradeAcceptanceChangedHandler
import com.cobblemon.mod.common.client.net.trade.TradeCancelledHandler
import com.cobblemon.mod.common.client.net.trade.TradeCompletedHandler
import com.cobblemon.mod.common.client.net.trade.TradeOfferExpiredHandler
import com.cobblemon.mod.common.client.net.trade.TradeOfferNotificationHandler
import com.cobblemon.mod.common.client.net.trade.TradeStartedHandler
import com.cobblemon.mod.common.client.net.trade.TradeUpdatedHandler
import com.cobblemon.mod.common.net.PacketRegisterInfo
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.battle.*
import com.cobblemon.mod.common.net.messages.client.callback.OpenMoveCallbackPacket
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyCallbackPacket
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyMoveCallbackPacket
import com.cobblemon.mod.common.net.messages.client.data.*
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueClosedPacket
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueOpenedPacket
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.net.messages.client.fishing.FishingBaitRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.fossil.FossilRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.fossil.NaturalMaterialRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.npc.CloseNPCEditorPacket
import com.cobblemon.mod.common.net.messages.client.npc.OpenNPCEditorPacket
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonPasturedPacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonUnpasturedPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.*
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnGenericBedrockPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokeballPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokemonPacket
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyReferencePacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.InitializePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.MoveClientPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.toast.ToastPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeAcceptanceChangedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeCancelledPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeCompletedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferExpiredPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferNotificationPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeStartedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeUpdatedPacket
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket
import com.cobblemon.mod.common.net.messages.server.BenchMovePacket
import com.cobblemon.mod.common.net.messages.server.RequestMoveSwapPacket
import com.cobblemon.mod.common.net.messages.server.RequestPlayerInteractionsPacket
import com.cobblemon.mod.common.net.messages.server.SelectStarterPacket
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import com.cobblemon.mod.common.net.messages.server.battle.RemoveSpectatorPacket
import com.cobblemon.mod.common.net.messages.server.battle.SpectateBattlePacket
import com.cobblemon.mod.common.net.messages.server.callback.move.MoveSelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.move.MoveSelectedPacket
import com.cobblemon.mod.common.net.messages.server.callback.party.PartyPokemonSelectedPacket
import com.cobblemon.mod.common.net.messages.server.callback.party.PartySelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyMoveSelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyPokemonMoveSelectedPacket
import com.cobblemon.mod.common.net.messages.server.dialogue.EscapeDialoguePacket
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import com.cobblemon.mod.common.net.messages.server.npc.SaveNPCPacket
import com.cobblemon.mod.common.net.messages.server.pasture.PasturePokemonPacket
import com.cobblemon.mod.common.net.messages.server.pasture.UnpastureAllPokemonPacket
import com.cobblemon.mod.common.net.messages.server.pasture.UnpasturePokemonPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetNicknamePacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonToPartyPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePartyPokemonToPCPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.ReleasePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket
import com.cobblemon.mod.common.net.messages.server.trade.AcceptTradeRequestPacket
import com.cobblemon.mod.common.net.messages.server.trade.CancelTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.ChangeTradeAcceptancePacket
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.UpdateTradeOfferPacket
import com.cobblemon.mod.common.net.serverhandling.ChallengeHandler
import com.cobblemon.mod.common.net.serverhandling.RequestInteractionsHandler
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler
import com.cobblemon.mod.common.net.serverhandling.battle.RemoveSpectatorHandler
import com.cobblemon.mod.common.net.serverhandling.battle.SpectateBattleHandler
import com.cobblemon.mod.common.net.serverhandling.callback.move.MoveSelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.move.MoveSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.callback.party.PartyPokemonSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.callback.party.PartySelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.partymove.PartyMoveSelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.partymove.PartyPokemonMoveSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.dialogue.EscapeDialogueHandler
import com.cobblemon.mod.common.net.serverhandling.dialogue.InputToDialogueHandler
import com.cobblemon.mod.common.net.serverhandling.evolution.AcceptEvolutionHandler
import com.cobblemon.mod.common.net.serverhandling.npc.SaveNPCHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.PasturePokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.UnpastureAllPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.UnpasturePokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pokemon.interact.InteractPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pokemon.update.SetNicknameHandler
import com.cobblemon.mod.common.net.serverhandling.starter.RequestStarterScreenHandler
import com.cobblemon.mod.common.net.serverhandling.starter.SelectStarterPacketHandler
import com.cobblemon.mod.common.net.serverhandling.storage.BenchMoveHandler
import com.cobblemon.mod.common.net.serverhandling.storage.RequestMoveSwapHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.MovePartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.ReleasePCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.SwapPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonToPartyHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePartyPokemonToPCHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.ReleasePartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.SwapPCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.UnlinkPlayerFromPCHandler
import com.cobblemon.mod.common.net.serverhandling.trade.AcceptTradeRequestHandler
import com.cobblemon.mod.common.net.serverhandling.trade.CancelTradeHandler
import com.cobblemon.mod.common.net.serverhandling.trade.ChangeTradeAcceptanceHandler
import com.cobblemon.mod.common.net.serverhandling.trade.OfferTradeHandler
import com.cobblemon.mod.common.net.serverhandling.trade.UpdateTradeOfferHandler
import com.cobblemon.mod.common.util.server
import net.minecraft.server.level.ServerPlayer

/**
 * Registers Cobblemon network packets.
 *
 * This class also contains short functions for dispatching our packets to a player, all players, or to the entire server.
 *
 * @author Hiroku, Licious
 * @since November 27th, 2021
 */
object CobblemonNetwork {
    //private val logger = LogUtils.getLogger()

    fun ServerPlayer.sendPacket(packet: NetworkPacket<*>) {
        sendPacketToPlayer(this, packet)
    }
    fun sendToServer(packet: NetworkPacket<*>) {
        //logger.info("[C->S] ${packet.type()}")
        Cobblemon.implementation.networkManager.sendToServer(packet)
    }
    fun sendToAllPlayers(packet: NetworkPacket<*>) = sendPacketToPlayers(server()!!.playerList.players, packet)
    fun sendPacketToPlayers(players: Iterable<ServerPlayer>, packet: NetworkPacket<*>) = players.forEach { sendPacketToPlayer(it, packet) }

    val s2cPayloads = generateS2CPacketInfoList()
    val c2sPayloads = generateC2SPacketInfoList()

    private fun generateS2CPacketInfoList(): List<PacketRegisterInfo<*>> {
        val list = mutableListOf<PacketRegisterInfo<*>>()

        // Pokemon Update Packets
        list.add(PacketRegisterInfo(FriendshipUpdatePacket.ID, FriendshipUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(MoveSetUpdatePacket.ID, MoveSetUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(NatureUpdatePacket.ID, NatureUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(ShinyUpdatePacket.ID, ShinyUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(SpeciesUpdatePacket.ID, SpeciesUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(NicknameUpdatePacket.ID, NicknameUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(HealthUpdatePacket.ID, HealthUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(ExperienceUpdatePacket.ID, ExperienceUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(StatusUpdatePacket.ID, StatusUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(CaughtBallUpdatePacket.ID, CaughtBallUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(BenchedMovesUpdatePacket.ID, BenchedMovesUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(GenderUpdatePacket.ID, GenderUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(AspectsUpdatePacket.ID, AspectsUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(AbilityUpdatePacket.ID, AbilityUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(EVsUpdatePacket.ID, EVsUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(IVsUpdatePacket.ID, IVsUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(HeldItemUpdatePacket.ID, HeldItemUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(PokemonStateUpdatePacket.ID, PokemonStateUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(TetheringUpdatePacket.ID, TetheringUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(TradeableUpdatePacket.ID, TradeableUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(SpeciesFeatureUpdatePacket.ID, SpeciesFeatureUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(OriginalTrainerUpdatePacket.ID, OriginalTrainerUpdatePacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(FormUpdatePacket.ID, FormUpdatePacket::decode, PokemonUpdatePacketHandler()))

        // Evolution start
        list.add(PacketRegisterInfo(AddEvolutionPacket.ID, AddEvolutionPacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(ClearEvolutionsPacket.ID, ClearEvolutionsPacket::decode, PokemonUpdatePacketHandler()))
        list.add(PacketRegisterInfo(RemoveEvolutionPacket.ID, RemoveEvolutionPacket::decode, PokemonUpdatePacketHandler()))
        // Evolution End

        // Storage Packets
        list.add(PacketRegisterInfo(InitializePartyPacket.ID, InitializePartyPacket::decode, InitializePartyHandler))
        list.add(PacketRegisterInfo(SetPartyPokemonPacket.ID, SetPartyPokemonPacket::decode, SetPartyPokemonHandler))
        list.add(PacketRegisterInfo(MoveClientPartyPokemonPacket.ID, MoveClientPartyPokemonPacket::decode, MoveClientPartyPokemonHandler))
        list.add(PacketRegisterInfo(SetPartyReferencePacket.ID, SetPartyReferencePacket::decode, SetPartyReferenceHandler))
        list.add(PacketRegisterInfo(InitializePCPacket.ID, InitializePCPacket::decode, InitializePCHandler))
        list.add(PacketRegisterInfo(MoveClientPCPokemonPacket.ID, MoveClientPCPokemonPacket::decode, MoveClientPCPokemonHandler))
        list.add(PacketRegisterInfo(SetPCBoxPokemonPacket.ID, SetPCBoxPokemonPacket::decode, SetPCBoxPokemonHandler))
        list.add(PacketRegisterInfo(SetPCPokemonPacket.ID, SetPCPokemonPacket::decode, SetPCPokemonHandler))
        list.add(PacketRegisterInfo(OpenPCPacket.ID, OpenPCPacket::decode, OpenPCHandler))
        list.add(PacketRegisterInfo(ClosePCPacket.ID, ClosePCPacket::decode, ClosePCHandler))
        list.add(PacketRegisterInfo(SwapClientPokemonPacket.ID, SwapClientPokemonPacket::decode, SwapClientPokemonHandler))
        list.add(PacketRegisterInfo(RemoveClientPokemonPacket.ID, RemoveClientPokemonPacket::decode, RemoveClientPokemonHandler))

        // UI Packets
        list.add(PacketRegisterInfo(SummaryUIPacket.ID, SummaryUIPacket::decode, SummaryUIPacketHandler))
        list.add(PacketRegisterInfo(InteractPokemonUIPacket.ID, InteractPokemonUIPacket::decode, InteractPokemonUIPacketHandler))
        list.add(PacketRegisterInfo(PlayerInteractOptionsPacket.ID, PlayerInteractOptionsPacket::decode, PlayerInteractOptionsHandler))

        // Starter packets
        list.add(PacketRegisterInfo(OpenStarterUIPacket.ID, OpenStarterUIPacket::decode, StarterUIPacketHandler))
        list.add(PacketRegisterInfo(SetClientPlayerDataPacket.ID, SetClientPlayerDataPacket::decode, SetClientPlayerDataHandler))

        // Battle packets
        list.add(PacketRegisterInfo(BattleEndPacket.ID, BattleEndPacket::decode, BattleEndHandler))
        list.add(PacketRegisterInfo(BattleInitializePacket.ID, BattleInitializePacket::decode, BattleInitializeHandler))
        list.add(PacketRegisterInfo(BattleQueueRequestPacket.ID, BattleQueueRequestPacket::decode, BattleQueueRequestHandler))
        list.add(PacketRegisterInfo(BattleFaintPacket.ID, BattleFaintPacket::decode, BattleFaintHandler))
        list.add(PacketRegisterInfo(BattleMakeChoicePacket.ID, BattleMakeChoicePacket::decode, BattleMakeChoiceHandler))
        list.add(PacketRegisterInfo(BattleHealthChangePacket.ID, BattleHealthChangePacket::decode, BattleHealthChangeHandler))
        list.add(PacketRegisterInfo(BattleSetTeamPokemonPacket.ID, BattleSetTeamPokemonPacket::decode, BattleSetTeamPokemonHandler))
        list.add(PacketRegisterInfo(BattleSwitchPokemonPacket.ID, BattleSwitchPokemonPacket::decode, BattleSwitchPokemonHandler))
        list.add(PacketRegisterInfo(BattleMessagePacket.ID, BattleMessagePacket::decode, BattleMessageHandler))
        list.add(PacketRegisterInfo(BattleCaptureStartPacket.ID, BattleCaptureStartPacket::decode, BattleCaptureStartHandler))
        list.add(PacketRegisterInfo(BattleCaptureEndPacket.ID, BattleCaptureEndPacket::decode, BattleCaptureEndHandler))
        list.add(PacketRegisterInfo(BattleCaptureShakePacket.ID, BattleCaptureShakePacket::decode, BattleCaptureShakeHandler))
        list.add(PacketRegisterInfo(BattleApplyPassResponsePacket.ID, BattleApplyPassResponsePacket::decode, BattleApplyPassResponseHandler))
        list.add(PacketRegisterInfo(BattleChallengeNotificationPacket.ID, BattleChallengeNotificationPacket::decode, BattleChallengeNotificationHandler))
        list.add(PacketRegisterInfo(BattleUpdateTeamPokemonPacket.ID, BattleUpdateTeamPokemonPacket::decode, BattleUpdateTeamPokemonHandler))
        list.add(PacketRegisterInfo(BattlePersistentStatusPacket.ID, BattlePersistentStatusPacket::decode, BattlePersistentStatusHandler))
        list.add(PacketRegisterInfo(BattleMadeInvalidChoicePacket.ID, BattleMadeInvalidChoicePacket::decode, BattleMadeInvalidChoiceHandler))
        list.add(PacketRegisterInfo(BattleMusicPacket.ID, BattleMusicPacket::decode, BattleMusicHandler))
        list.add(PacketRegisterInfo(BattleChallengeExpiredPacket.ID, BattleChallengeExpiredPacket::decode, BattleChallengeExpiredHandler))
        list.add(PacketRegisterInfo(BattleReplacePokemonPacket.ID, BattleReplacePokemonPacket::decode, BattleReplacePokemonHandler))
        list.add(PacketRegisterInfo(BattleTransformPokemonPacket.ID, BattleTransformPokemonPacket::decode, BattleTransformPokemonHandler))

        // Settings packets
        list.add(PacketRegisterInfo(ServerSettingsPacket.ID, ServerSettingsPacket::decode, ServerSettingsPacketHandler))

        // Data registries
        list.add(PacketRegisterInfo(AbilityRegistrySyncPacket.ID, AbilityRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(MovesRegistrySyncPacket.ID, MovesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(BerryRegistrySyncPacket.ID, BerryRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(SpeciesRegistrySyncPacket.ID, SpeciesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(PropertiesCompletionRegistrySyncPacket.ID, PropertiesCompletionRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(UnlockReloadPacket.ID, UnlockReloadPacket::decode, UnlockReloadPacketHandler))
        list.add(PacketRegisterInfo(StandardSpeciesFeatureSyncPacket.ID, StandardSpeciesFeatureSyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(GlobalSpeciesFeatureSyncPacket.ID, GlobalSpeciesFeatureSyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(SpeciesFeatureAssignmentSyncPacket.ID, SpeciesFeatureAssignmentSyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(NaturalMaterialRegistrySyncPacket.ID, NaturalMaterialRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(FossilRegistrySyncPacket.ID, FossilRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(NPCRegistrySyncPacket.ID, NPCRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(PokeRodRegistrySyncPacket.ID, PokeRodRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(ScriptRegistrySyncPacket.ID, ScriptRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))
        list.add(PacketRegisterInfo(FishingBaitRegistrySyncPacket.ID, FishingBaitRegistrySyncPacket::decode, DataRegistrySyncPacketHandler()))

        // Effects
        list.add(PacketRegisterInfo(SpawnSnowstormParticlePacket.ID, SpawnSnowstormParticlePacket::decode, SpawnSnowstormParticleHandler))
        list.add(PacketRegisterInfo(SpawnSnowstormEntityParticlePacket.ID, SpawnSnowstormEntityParticlePacket::decode, SpawnSnowstormEntityParticleHandler))
        list.add(PacketRegisterInfo(RunPosableMoLangPacket.ID, RunPosableMoLangPacket::decode, RunPosableMoLangHandler))

        // Hax
        list.add(PacketRegisterInfo(UnvalidatedPlaySoundS2CPacket.ID, UnvalidatedPlaySoundS2CPacket::decode, UnvalidatedPlaySoundS2CPacketHandler))
        list.add(PacketRegisterInfo(SpawnPokemonPacket.ID, SpawnPokemonPacket::decode, SpawnExtraDataEntityHandler()))
        list.add(PacketRegisterInfo(SpawnPokeballPacket.ID, SpawnPokeballPacket::decode, SpawnExtraDataEntityHandler()))
        list.add(PacketRegisterInfo(ToastPacket.ID, ToastPacket::decode, ToastPacketHandler))
        list.add(PacketRegisterInfo(SpawnGenericBedrockPacket.ID, SpawnGenericBedrockPacket::decode, SpawnExtraDataEntityHandler()))

        // Trade packets
        list.add(PacketRegisterInfo(TradeAcceptanceChangedPacket.ID, TradeAcceptanceChangedPacket::decode, TradeAcceptanceChangedHandler))
        list.add(PacketRegisterInfo(TradeCancelledPacket.ID, TradeCancelledPacket::decode, TradeCancelledHandler))
        list.add(PacketRegisterInfo(TradeCompletedPacket.ID, TradeCompletedPacket::decode, TradeCompletedHandler))
        list.add(PacketRegisterInfo(TradeUpdatedPacket.ID, TradeUpdatedPacket::decode, TradeUpdatedHandler))
        list.add(PacketRegisterInfo(TradeOfferNotificationPacket.ID, TradeOfferNotificationPacket::decode, TradeOfferNotificationHandler))
        list.add(PacketRegisterInfo(TradeOfferExpiredPacket.ID, TradeOfferExpiredPacket::decode, TradeOfferExpiredHandler))
        list.add(PacketRegisterInfo(TradeStartedPacket.ID, TradeStartedPacket::decode, TradeStartedHandler))

        // Pasture
        list.add(PacketRegisterInfo(OpenPasturePacket.ID, OpenPasturePacket::decode, OpenPastureHandler))
        list.add(PacketRegisterInfo(ClosePasturePacket.ID, ClosePasturePacket::decode, ClosePastureHandler))
        list.add(PacketRegisterInfo(PokemonPasturedPacket.ID, PokemonPasturedPacket::decode, PokemonPasturedHandler))
        list.add(PacketRegisterInfo(PokemonUnpasturedPacket.ID, PokemonUnpasturedPacket::decode, PokemonUnpasturedHandler))

        // Behaviours
        list.add(PacketRegisterInfo(PlayPosableAnimationPacket.ID, PlayPosableAnimationPacket::decode, PlayPosableAnimationHandler))

        // Move select packets
        list.add(PacketRegisterInfo(OpenMoveCallbackPacket.ID, OpenMoveCallbackPacket::decode, OpenMoveCallbackHandler))

        // Party select packets
        list.add(PacketRegisterInfo(OpenPartyCallbackPacket.ID, OpenPartyCallbackPacket::decode, OpenPartyCallbackHandler))

        // Party move select packets
        list.add(PacketRegisterInfo(OpenPartyMoveCallbackPacket.ID, OpenPartyMoveCallbackPacket::decode, OpenPartyMoveCallbackHandler))

        // Dialogue packets
        list.add(PacketRegisterInfo(DialogueClosedPacket.ID, DialogueClosedPacket::decode, DialogueClosedHandler))
        list.add(PacketRegisterInfo(DialogueOpenedPacket.ID, DialogueOpenedPacket::decode, DialogueOpenedHandler))

        // NPCs
        list.add(PacketRegisterInfo(CloseNPCEditorPacket.ID, CloseNPCEditorPacket::decode, CloseNPCEditorHandler))
        list.add(PacketRegisterInfo(OpenNPCEditorPacket.ID, OpenNPCEditorPacket::decode, OpenNPCEditorHandler))

        return list
    }

    private fun generateC2SPacketInfoList(): List<PacketRegisterInfo<*>> {
        val list = mutableListOf<PacketRegisterInfo<*>>()
        // Pokemon Update Packets
        list.add(PacketRegisterInfo(SetNicknamePacket.ID, SetNicknamePacket::decode, SetNicknameHandler))

        // Evolution Packets
        list.add(PacketRegisterInfo(AcceptEvolutionPacket.ID, AcceptEvolutionPacket::decode, AcceptEvolutionHandler))

        // Interaction Packets
        list.add(PacketRegisterInfo(InteractPokemonPacket.ID, InteractPokemonPacket::decode, InteractPokemonHandler))
        list.add(PacketRegisterInfo(RequestPlayerInteractionsPacket.ID, RequestPlayerInteractionsPacket::decode, RequestInteractionsHandler))

        // Storage Packets
        list.add(PacketRegisterInfo(SendOutPokemonPacket.ID, SendOutPokemonPacket::decode, SendOutPokemonHandler))
        list.add(PacketRegisterInfo(RequestMoveSwapPacket.ID, RequestMoveSwapPacket::decode, RequestMoveSwapHandler))
        list.add(PacketRegisterInfo(BenchMovePacket.ID, BenchMovePacket::decode, BenchMoveHandler))
        list.add(PacketRegisterInfo(BattleChallengePacket.ID, BattleChallengePacket::decode, ChallengeHandler))

        list.add(PacketRegisterInfo(MovePCPokemonToPartyPacket.ID, MovePCPokemonToPartyPacket::decode, MovePCPokemonToPartyHandler))
        list.add(PacketRegisterInfo(MovePartyPokemonToPCPacket.ID, MovePartyPokemonToPCPacket::decode, MovePartyPokemonToPCHandler))
        list.add(PacketRegisterInfo(ReleasePartyPokemonPacket.ID, ReleasePartyPokemonPacket::decode, ReleasePartyPokemonHandler))
        list.add(PacketRegisterInfo(ReleasePCPokemonPacket.ID, ReleasePCPokemonPacket::decode, ReleasePCPokemonHandler))
        list.add(PacketRegisterInfo(UnlinkPlayerFromPCPacket.ID, UnlinkPlayerFromPCPacket::decode, UnlinkPlayerFromPCHandler))

        // Starter packets
        list.add(PacketRegisterInfo(SelectStarterPacket.ID, SelectStarterPacket::decode, SelectStarterPacketHandler))
        list.add(PacketRegisterInfo(RequestStarterScreenPacket.ID, RequestStarterScreenPacket::decode, RequestStarterScreenHandler))

        list.add(PacketRegisterInfo(SwapPCPokemonPacket.ID, SwapPCPokemonPacket::decode, SwapPCPokemonHandler))
        list.add(PacketRegisterInfo(SwapPartyPokemonPacket.ID, SwapPartyPokemonPacket::decode, SwapPartyPokemonHandler))

        list.add(PacketRegisterInfo(MovePCPokemonPacket.ID, MovePCPokemonPacket::decode, MovePCPokemonHandler))
        list.add(PacketRegisterInfo(MovePartyPokemonPacket.ID, MovePartyPokemonPacket::decode, MovePartyPokemonHandler))

        list.add(PacketRegisterInfo(SwapPCPartyPokemonPacket.ID, SwapPCPartyPokemonPacket::decode, SwapPCPartyPokemonHandler))

        // Battle packets
        list.add(PacketRegisterInfo(BattleSelectActionsPacket.ID, BattleSelectActionsPacket::decode, BattleSelectActionsHandler))
        list.add(PacketRegisterInfo(SpectateBattlePacket.ID, SpectateBattlePacket::decode, SpectateBattleHandler))
        list.add(PacketRegisterInfo(RemoveSpectatorPacket.ID, RemoveSpectatorPacket::decode, RemoveSpectatorHandler))

        // Trade
        list.add(PacketRegisterInfo(AcceptTradeRequestPacket.ID, AcceptTradeRequestPacket::decode, AcceptTradeRequestHandler))
        list.add(PacketRegisterInfo(CancelTradePacket.ID, CancelTradePacket::decode, CancelTradeHandler))
        list.add(PacketRegisterInfo(ChangeTradeAcceptancePacket.ID, ChangeTradeAcceptancePacket::decode, ChangeTradeAcceptanceHandler))
        list.add(PacketRegisterInfo(OfferTradePacket.ID, OfferTradePacket::decode, OfferTradeHandler))
        list.add(PacketRegisterInfo(UpdateTradeOfferPacket.ID, UpdateTradeOfferPacket::decode, UpdateTradeOfferHandler))

        // Pasture
        list.add(PacketRegisterInfo(PasturePokemonPacket.ID, PasturePokemonPacket::decode, PasturePokemonHandler))
        list.add(PacketRegisterInfo(UnpasturePokemonPacket.ID, UnpasturePokemonPacket::decode, UnpasturePokemonHandler))
        list.add(PacketRegisterInfo(UnpastureAllPokemonPacket.ID, UnpastureAllPokemonPacket::decode, UnpastureAllPokemonHandler))

        // Move select packets
        list.add(PacketRegisterInfo(MoveSelectedPacket.ID, MoveSelectedPacket::decode, MoveSelectedHandler))
        list.add(PacketRegisterInfo(MoveSelectCancelledPacket.ID, MoveSelectCancelledPacket::decode, MoveSelectCancelledHandler))

        // Party select packets
        list.add(PacketRegisterInfo(PartyPokemonSelectedPacket.ID, PartyPokemonSelectedPacket::decode, PartyPokemonSelectedHandler))
        list.add(PacketRegisterInfo(PartySelectCancelledPacket.ID, PartySelectCancelledPacket::decode, PartySelectCancelledHandler))

        // Party move select packets
        list.add(PacketRegisterInfo(PartyPokemonMoveSelectedPacket.ID, PartyPokemonMoveSelectedPacket::decode, PartyPokemonMoveSelectedHandler))
        list.add(PacketRegisterInfo(PartyMoveSelectCancelledPacket.ID, PartyMoveSelectCancelledPacket::decode, PartyMoveSelectCancelledHandler))

        // Dialogue packets
        list.add(PacketRegisterInfo(EscapeDialoguePacket.ID, EscapeDialoguePacket::decode, EscapeDialogueHandler))
        list.add(PacketRegisterInfo(InputToDialoguePacket.ID, InputToDialoguePacket::decode, InputToDialogueHandler))

        // NPC packets
        list.add(PacketRegisterInfo(SaveNPCPacket.ID, SaveNPCPacket::decode, SaveNPCHandler))

        return list
    }

    fun sendPacketToPlayer(player: ServerPlayer, packet: NetworkPacket<*>) {
        //logger.info("[S->C] ${packet.type()}")
        Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet)
    }
}