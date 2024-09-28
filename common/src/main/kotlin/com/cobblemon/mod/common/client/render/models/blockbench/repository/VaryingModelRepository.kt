/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.render.ModelLayer
import com.cobblemon.mod.common.client.render.ModelVariationSet
import com.cobblemon.mod.common.client.render.SpriteType
import com.cobblemon.mod.common.client.render.VaryingRenderableResolver
import com.cobblemon.mod.common.client.render.models.blockbench.*
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.client.render.models.blockbench.blockentity.BlockEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.fossil.FossilModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.AncientPokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.BeastBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.AlakazamModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArbokModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArcanineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArticunoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BeedrillModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BellsproutModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BulbasaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ButterfreeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CaterpieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ChanseyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharizardModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharmanderModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharmeleonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ClefableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ClefairyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CloysterModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CuboneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DewgongModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DiglettAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DiglettModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DittoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DodrioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DoduoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DragonairModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DragoniteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DratiniModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DrowzeeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DugtrioAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DugtrioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.EeveeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.EkansModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ElectabuzzModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ElectrodeHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ElectrodeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ExeggcuteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ExeggutorAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ExeggutorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FarfetchdGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FarfetchdModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FearowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FlareonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GastlyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GastlyShinyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GengarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GeodudeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GloomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolbatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GoldeenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolduckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolemModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GravelerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GrimerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GrowlitheModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GyaradosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HaunterModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HitmonchanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HitmonleeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HorseaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HypnoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.IvysaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JigglypuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JolteonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JynxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KabutoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KabutopsModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KadabraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KakunaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KangaskhanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KoffingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KrabbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.LaprasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.LickitungModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachampModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachokeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachopModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagikarpModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagmarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagnemiteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagnetonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MankeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MarowakModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MeowthAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MeowthGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MeowthModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MetapodModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MewModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MewtwoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MoltresModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MrmimeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MukModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidokingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoqueenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoranfModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoranmModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidorinaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidorinoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NinetalesAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NinetalesModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OddishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OmanyteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OmastarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OnixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ParasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ParasectModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PersianAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PersianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeotModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeottoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PikachuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PinsirModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwagModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwhirlModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwrathModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PonytaGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PonytaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PorygonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PrimeapeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PsyduckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaichuAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaichuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RapidashGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RapidashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaticateAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaticateModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RattataAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RattataModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RhydonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RhyhornModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SandshrewModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SandslashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ScytherModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeadraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeakingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ShellderModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SlowbroModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SlowpokeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SpearowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SquirtleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.StarmieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.StaryuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TangelaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TaurosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TentacoolModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TentacruelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VaporeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenomothModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenonatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenusaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VictreebelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VileplumeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VoltorbHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VoltorbModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VulpixAlolanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VulpixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WartortleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeedleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeepinbellModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeezingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WigglytuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ZapdosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ZubatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.AipomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.AmpharosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.AriadosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.BayleefModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.BellossomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.BlisseyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ChikoritaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ChinchouModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CleffaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CrobatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CroconawModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CyndaquilHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CyndaquilModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.DonphanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ElekidModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.EspeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.FeraligatrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.FlaaffyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ForretressModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.FurretModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.GligarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.GranbullModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.HeracrossModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.HitmontopModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.HoothootModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.IgglybuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.KingdraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.LanturnModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.LarvitarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MagbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MagcargoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MagcargoShinyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MareepModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MeganiumModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MiltankModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MisdreavusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MurkrowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.NatuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.NoctowlModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PhanpyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PichuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PiloswineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PinecoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PolitoedModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.Porygon2Model
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PupitarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QuagsireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QuilavaHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QuilavaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QwilfishHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QwilfishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ScizorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SentretModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ShuckleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SkarmoryModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SlowkingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SlugmaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SlugmaShinyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SmeargleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SmoochumModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SneaselHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SneaselModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SnubbullModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SpinarakModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.StantlerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SteelixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SudowoodoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SwinubModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TeddiursaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TotodileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TyphlosionHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TyphlosionModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TyranitarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TyrogueModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.UmbreonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.UrsaringModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.WooperModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.WooperPaldeanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.XatuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.YanmaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.AggronModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.AnorithModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ArmaldoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.AronModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BaltoyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BarboachModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BeldumModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BlazikenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BreloomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CacneaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CacturneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CameruptModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CarvanhaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ChimechoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ClamperlModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ClaydolModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CombuskenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CradilyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.DusclopsModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.DuskullModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ExploudModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.FlygonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.GardevoirModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.GorebyssModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.GrovyleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.HariyamaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.HuntailModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.IllumiseModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.KirliaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LaironModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LileepModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LinooneGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LinooneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LombreModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LotadModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LoudredModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LudicoloModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LunatoneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.LuvdiscModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MakuhitaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MarshtompModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MasquerainModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MawileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MetagrossModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MetangModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MightyenaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MinunModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MudkipModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.NincadaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.NinjaskModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.NosepassModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.NumelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.NuzleafModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.PelipperModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.PlusleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.PoochyenaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.RaltsModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.RayquazaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.RelicanthModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.RoseliaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SableyeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SceptileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SeedotModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SharpedoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ShedinjaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ShiftryModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ShroomishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SolrockModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SpindaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SurskitModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SwampertModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SwellowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TaillowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TorchicModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TorkoalModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TrapinchModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TreeckoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TropiusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.VibravaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.VolbeatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WailmerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WailordModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WhiscashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WhismurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WingullModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ZigzagoonGalarianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.ZigzagoonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.AmbipomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BastiodonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BibarelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BidoofModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BonslyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BudewModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BuizelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BunearyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.CarnivineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ChatotModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ChimcharModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ChinglingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.CombeeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.CranidosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.DrifblimModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.DrifloonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.DusknoirModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ElectivireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.EmpoleonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.FloatzelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GabiteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GalladeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GarchompModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GibleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GlaceonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GliscorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GrotleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HappinyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HippopotasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HippowdonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HonchkrowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.InfernapeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.KricketotModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.KricketuneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LeafeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LickilickyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LopunnyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LucarioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LuxioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LuxrayModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MagmortarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MagnezoneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MamoswineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MimejrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MismagiusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MonfernoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PachirisuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PiplupModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PorygonzModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PrinplupModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ProbopassModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.RampardosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.RhyperiorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.RioluModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.RoseradeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ShieldonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ShinxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.SpiritombModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.StaraptorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.StaraviaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.StarlyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.TangrowthModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.TorterraCherryModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.TorterraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.TurtwigModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.VespiquenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.WeavileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.YanmegaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.AlomomolaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ArchenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ArcheopsModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BasculinModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BearticModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BeheeyemModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BoldoreModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BouffalantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CarracostaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ChandelureModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CofagrigusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ConkeldurrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CottoneeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CrustleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CryogonalModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CubchooModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DarmanitanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DarmanitanZenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DarumakaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DeerlingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DeinoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DewottHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DewottModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DurantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DwebbleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ElgyemModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.EmboarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.EmolgaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.FerroseedModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.FerrothornModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.FrillishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.GalvantulaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.GigalithModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.GolettModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.GolurkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.GurdurrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.HeatmorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.HerdierModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.HydreigonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.JellicentModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.JoltikModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.KlangModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.KlinkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.KlinklangModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.KrokorokModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.KrookodileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LampentModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LarvestaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LilligantHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LilligantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LillipupModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.LitwickModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.MaractusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.OshawottHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.OshawottModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.PatratModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.PetililHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.PetililModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.PidoveModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.PigniteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.RoggenrolaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SamurottHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SamurottModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SandileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SawsbuckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ScolipedeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SerperiorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ServineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SigilyphModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SnivyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.StoutlandModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SwoobatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.TepigModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.TimburrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.TirtougaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.TranquillModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.UnfezantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.VenipedeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.VolcaronaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.WatchogModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.WhimsicottModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.WhirlipedeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.WoobatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.YamaskModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ZoroarkHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ZoroarkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ZoruaHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ZoruaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.ZweilousModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.AegislashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.AmauraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.AurorusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.AvaluggModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.BergmiteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.BraixenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.BunnelbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.CarbinkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.ChesnaughtModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.ChespinModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.DelphoxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.DiggersbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.DoubladeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.DragalgeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FennekinModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FlabebeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FletchinderModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FletchlingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FloetteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FlorgesModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FroakieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.FrogadierModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GoodraHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GoodraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GoomyHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GoomyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GourgeistModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.GreninjaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.HonedgeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.KlefkiModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.PhantumpModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.PumpkabooModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.QuilladinModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.ScatterbugModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SkrelpModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SliggooHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SliggooModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SpewpaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SylveonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.TalonflameModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.TrevenantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.TyrantrumModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.TyruntModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.VivillonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.XerneasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.BewearModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.BounsweetModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.BrionneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.ComfeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.CrabominableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.CrabrawlerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.CutieflyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DartrixHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DartrixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DecidueyeHisuianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DecidueyeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DhelmiseModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.FomantisModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.GolisopodModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.HakamoOModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.IncineroarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.JangmoOModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.KomalaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.KommoOModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.LittenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.LurantisModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.MimikyuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.MorelullModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.MudbrayModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.MudsdaleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.NaganadelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PoipoleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PopplioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PrimarinaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PyukumukuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.RibombeeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.RowletHisuiBiasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.RowletModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.SalanditModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.SalazzleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.ShiinoticModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.SteeneeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.StuffulModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.TorracatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.TsareenaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.TurtonatorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.WimpodModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.WishiwashiSchoolingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.WishiwashiSoloModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.AlcremieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ArctovishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ArctozoltModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ArrokudaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.BarraskewdaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.BasculegionModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.BoltundModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CentiskorchModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CinderaceModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CopperajahModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CorviknightModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CorvisquireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CufantModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DracovishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DracozoltModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DragapultModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DrakloakModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DreepyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DrizzileModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.DubwoolModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.EiscueModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.EldegossModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.FalinksModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.GossifleurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.GrimmsnarlModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.GrookeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ImpidimpModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.InteleonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.KleavorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.MilceryModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.MorgremModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.NickitModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ObstagoonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.OverqwilModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.PerrserkerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.RabootModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.RillaboomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.RookideeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ScorbunnyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.SirfetchdModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.SizzlipedeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.SneaslerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.SobbleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.StonjournerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ThievulModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.ThwackeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.UrsalunaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.WoolooModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.WyrdeerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.YamperModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.AnnihilapeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.ArmarougeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.CeruledgeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.CetitanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.CetoddleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.CharcadetModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.ClodsireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.CrocalorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.DachsbunModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.EspathraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.FidoughModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.FlittleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.FloragatoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.FuecocoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GarganaclModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GholdengoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GimmighoulChestModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GimmighoulRoamingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GlimmetModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GlimmoraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.GrafaiaiModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.IronleavesModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.LechonkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.MabosstiffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.MaschiffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.MausholdModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.MausholdfourModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.MeowscaradaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.NacliModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.NaclstackModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.OinkologneFemaleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.OinkologneMaleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.QuaquavalModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.QuaxlyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.QuaxwellModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.RevavroomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.ShroodleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.SkeledirgeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.SprigatitoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.SquawkabillyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.TandemausModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.TatsugiriModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.TinkatinkModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.TinkatonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.TinkatuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.VaroomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen9.WalkingwakeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.cobblemon.mod.common.client.util.exists
import com.cobblemon.mod.common.util.adapters.ExpressionAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.Vec3dAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import com.cobblemon.mod.common.util.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.function.BiFunction
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.phys.Vec3

/**
 * A repository for [PosableModel]s. Can be parameterized with [PosableModel] itself or a subclass.
 * This will handle the loading of all factors of [PosableModel]s, including variations, posers, models, and indirectly
 * the animations by providing directories for the [BedrockAnimationRepository] to read from. This class will also
 * hang onto poser instances for reuse.
 *
 * @author Hiroku
 * @since February 28th, 2023
 */
object VaryingModelRepository {
    val posers = mutableMapOf<ResourceLocation, (Bone) -> PosableModel>()
    val variations = mutableMapOf<ResourceLocation, VaryingRenderableResolver>()
    val texturedModels = mutableMapOf<ResourceLocation, Bone>()

    private val types = listOf(
        "pokemon",
        "fossils",
        "npcs",
        "poke_balls",
        "generic",
        "block_entities",
    )

    val poserDirectories: List<Pair<String, Class<out PosableModel>>> = listOf(
        "bedrock/posers" to PosableModel::class.java,
        "bedrock/pokemon/posers" to PokemonPosableModel::class.java,
        "bedrock/fossils/posers" to FossilModel::class.java,
        "bedrock/block_entities/posers" to BlockEntityModel::class.java,
        "bedrock/npcs/posers" to PosableModel::class.java,
        "bedrock/poke_balls/posers" to PosableModel::class.java,
        "bedrock/generic/posers" to PosableModel::class.java,
    )

    val variationDirectories: List<String> = listOf(
        "bedrock/species",
        "bedrock/pokemon/resolvers"
    ) + types.map { "bedrock/$it/variations" }

    val modelDirectories: List<String> = listOf(
        "bedrock/models"
    ) + types.map { "bedrock/$it/models" }

    val animationDirectories: List<String> = listOf(
        "bedrock/animations"
    ) + types.map { "bedrock/$it/animations" }

    val fallback: ResourceLocation = cobblemonResource("substitute")

    val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Vec3::class.java, Vec3dAdapter)
            .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
            .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
            .registerTypeAdapter(PosableModel::class.java, JsonModelAdapter(::PosableModel))
            .registerTypeAdapter(PokemonPosableModel::class.java, JsonModelAdapter(::PokemonPosableModel))
            .registerTypeAdapter(FossilModel::class.java, JsonModelAdapter(::FossilModel))
            .registerTypeAdapter(BlockEntityModel::class.java, JsonModelAdapter(::BlockEntityModel))
            .excludeFieldsWithModifiers()
            .registerTypeAdapter(Pose::class.java, PoseAdapter { JsonModelAdapter.model!! })
            .create()
    }

    fun loadJsonPoser(json: String, poserClass: Class<out PosableModel>): (Bone) -> PosableModel {
        // Faster to deserialize during asset load rather than rerunning this every time a poser is constructed.
        val jsonObject = gson.fromJson(json, JsonObject::class.java)
        return {
            JsonModelAdapter.modelPart = it
            gson.fromJson(jsonObject, poserClass).also {
                it.poses.forEach { (poseName, pose) -> pose.poseName = poseName }
            }
        }
    }

    fun registerPosers(resourceManager: ResourceManager) {
        posers.clear()
        registerInBuiltPosers()
        registerJsonPosers(resourceManager)
        Cobblemon.LOGGER.info("Loaded ${posers.size} posers.")
    }

    fun registerInBuiltPosers() {
        inbuilt("azure_ball", ::PokeBallModel)
        inbuilt("beast_ball", ::BeastBallModel)
        inbuilt("cherish_ball", ::PokeBallModel)
        inbuilt("citrine_ball", ::PokeBallModel)
        inbuilt("dive_ball", ::PokeBallModel)
        inbuilt("dream_ball", ::PokeBallModel)
        inbuilt("dusk_ball", ::PokeBallModel)
        inbuilt("fast_ball", ::PokeBallModel)
        inbuilt("friend_ball", ::PokeBallModel)
        inbuilt("great_ball", ::PokeBallModel)
        inbuilt("heal_ball", ::PokeBallModel)
        inbuilt("heavy_ball", ::PokeBallModel)
        inbuilt("level_ball", ::PokeBallModel)
        inbuilt("love_ball", ::PokeBallModel)
        inbuilt("lure_ball", ::PokeBallModel)
        inbuilt("luxury_ball", ::PokeBallModel)
        inbuilt("master_ball", ::PokeBallModel)
        inbuilt("moon_ball", ::PokeBallModel)
        inbuilt("nest_ball", ::PokeBallModel)
        inbuilt("net_ball", ::PokeBallModel)
        inbuilt("park_ball", ::PokeBallModel)
        inbuilt("poke_ball", ::PokeBallModel)
        inbuilt("premier_ball", ::PokeBallModel)
        inbuilt("quick_ball", ::PokeBallModel)
        inbuilt("repeat_ball", ::PokeBallModel)
        inbuilt("roseate_ball", ::PokeBallModel)
        inbuilt("safari_ball", ::PokeBallModel)
        inbuilt("slate_ball", ::PokeBallModel)
        inbuilt("sport_ball", ::PokeBallModel)
        inbuilt("strange_ball", ::PokeBallModel)
        inbuilt("timer_ball", ::PokeBallModel)
        inbuilt("ultra_ball", ::PokeBallModel)
        inbuilt("verdant_ball", ::PokeBallModel)
        inbuilt("ancient_poke_ball", ::AncientPokeBallModel)
        inbuilt("ancient_citrine_ball", ::AncientPokeBallModel)
        inbuilt("ancient_verdant_ball", ::AncientPokeBallModel)
        inbuilt("ancient_azure_ball", ::AncientPokeBallModel)
        inbuilt("ancient_roseate_ball", ::AncientPokeBallModel)
        inbuilt("ancient_slate_ball", ::AncientPokeBallModel)
        inbuilt("ancient_ivory_ball", ::AncientPokeBallModel)
        inbuilt("ancient_great_ball", ::AncientPokeBallModel)
        inbuilt("ancient_ultra_ball", ::AncientPokeBallModel)
        inbuilt("ancient_feather_ball", ::AncientPokeBallModel)
        inbuilt("ancient_wing_ball", ::AncientPokeBallModel)
        inbuilt("ancient_jet_ball", ::AncientPokeBallModel)
        inbuilt("ancient_heavy_ball", ::AncientPokeBallModel)
        inbuilt("ancient_leaden_ball", ::AncientPokeBallModel)
        inbuilt("ancient_gigaton_ball", ::AncientPokeBallModel)

        inbuilt("bulbasaur", ::BulbasaurModel)
        inbuilt("ivysaur", ::IvysaurModel)
        inbuilt("venusaur", ::VenusaurModel)
        inbuilt("charmander", ::CharmanderModel)
        inbuilt("charmeleon", ::CharmeleonModel)
        inbuilt("charizard", ::CharizardModel)
        inbuilt("squirtle", ::SquirtleModel)
        inbuilt("wartortle", ::WartortleModel)
        inbuilt("caterpie", ::CaterpieModel)
        inbuilt("metapod", ::MetapodModel)
        inbuilt("butterfree", ::ButterfreeModel)
        inbuilt("weedle", ::WeedleModel)
        inbuilt("kakuna", ::KakunaModel)
        inbuilt("beedrill", ::BeedrillModel)
        inbuilt("rattata", ::RattataModel)
        inbuilt("raticate", ::RaticateModel)
        inbuilt("rattata_alolan", ::RattataAlolanModel)
        inbuilt("raticate_alolan", ::RaticateAlolanModel)
        inbuilt("eevee", ::EeveeModel)
        inbuilt("magikarp", ::MagikarpModel)
        inbuilt("gyarados", ::GyaradosModel)
        inbuilt("pidgey", ::PidgeyModel)
        inbuilt("pidgeotto", ::PidgeottoModel)
        inbuilt("pidgeot", ::PidgeotModel)
        inbuilt("diglett", ::DiglettModel)
        inbuilt("dugtrio", ::DugtrioModel)
        inbuilt("zubat", ::ZubatModel)
        inbuilt("cleffa", ::CleffaModel)
        inbuilt("clefable", ::ClefableModel)
        inbuilt("clefairy", ::ClefairyModel)
        inbuilt("krabby", ::KrabbyModel)
        inbuilt("paras", ::ParasModel)
        inbuilt("parasect", ::ParasectModel)
        inbuilt("mankey", ::MankeyModel)
        inbuilt("primeape", ::PrimeapeModel)
        inbuilt("oddish", ::OddishModel)
        inbuilt("gloom", ::GloomModel)
        inbuilt("vileplume", ::VileplumeModel)
        inbuilt("bellossom", ::BellossomModel)
        inbuilt("voltorb", ::VoltorbModel)
        inbuilt("electrode", ::ElectrodeModel)
        inbuilt("lapras", ::LaprasModel)
        inbuilt("ekans", ::EkansModel)
        inbuilt("machop", ::MachopModel)
        inbuilt("machoke", ::MachokeModel)
        inbuilt("machamp", ::MachampModel)
        inbuilt("alakazam", ::AlakazamModel)
        inbuilt("arbok", ::ArbokModel)
        inbuilt("arcanine", ::ArcanineModel)
        inbuilt("articuno", ::ArticunoModel)
        inbuilt("bellsprout", ::BellsproutModel)
        inbuilt("chansey", ::ChanseyModel)
        inbuilt("cloyster", ::CloysterModel)
        inbuilt("crobat", ::CrobatModel)
        inbuilt("cubone", ::CuboneModel)
        inbuilt("dewgong", ::DewgongModel)
        inbuilt("ditto", ::DittoModel)
        inbuilt("dodrio", ::DodrioModel)
        inbuilt("doduo", ::DoduoModel)
        inbuilt("dragonair", ::DragonairModel)
        inbuilt("dragonite", ::DragoniteModel)
        inbuilt("dratini", ::DratiniModel)
        inbuilt("drowzee", ::DrowzeeModel)
        inbuilt("electabuzz", ::ElectabuzzModel)
        inbuilt("exeggcute", ::ExeggcuteModel)
        inbuilt("exeggutor", ::ExeggutorModel)
        inbuilt("exeggutor_alolan", ::ExeggutorAlolanModel)
        inbuilt("farfetchd", ::FarfetchdModel)
        inbuilt("farfetchd_galarian", ::FarfetchdGalarianModel)
        inbuilt("fearow", ::FearowModel)
        inbuilt("flareon", ::FlareonModel)
        inbuilt("gastly", ::GastlyModel)
        inbuilt("gastly_shiny", ::GastlyShinyModel)
        inbuilt("gengar", ::GengarModel)
        inbuilt("geodude", ::GeodudeModel)
        inbuilt("golbat", ::GolbatModel)
        inbuilt("goldeen", ::GoldeenModel)
        inbuilt("golduck", ::GolduckModel)
        inbuilt("golem", ::GolemModel)
        inbuilt("graveler", ::GravelerModel)
        inbuilt("grimer", ::GrimerModel)
        inbuilt("growlithe", ::GrowlitheModel)
        inbuilt("haunter", ::HaunterModel)
        inbuilt("hitmonchan", ::HitmonchanModel)
        inbuilt("hitmonlee", ::HitmonleeModel)
        inbuilt("horsea", ::HorseaModel)
        inbuilt("hypno", ::HypnoModel)
        inbuilt("jigglypuff", ::JigglypuffModel)
        inbuilt("jolteon", ::JolteonModel)
        inbuilt("jynx", ::JynxModel)
        inbuilt("kabuto", ::KabutoModel)
        inbuilt("kabutops", ::KabutopsModel)
        inbuilt("kadabra", ::KadabraModel)
        inbuilt("kangaskhan", ::KangaskhanModel)
        inbuilt("koffing", ::KoffingModel)
        inbuilt("krabby", ::KrabbyModel)
        inbuilt("lickitung", ::LickitungModel)
        inbuilt("magmar", ::MagmarModel)
        inbuilt("magnemite", ::MagnemiteModel)
        inbuilt("magneton", ::MagnetonModel)
        inbuilt("marowak", ::MarowakModel)
        inbuilt("meowth", ::MeowthModel)
        inbuilt("mew", ::MewModel)
        inbuilt("mewtwo", ::MewtwoModel)
        inbuilt("moltres", ::MoltresModel)
        inbuilt("mrmime", ::MrmimeModel)
        inbuilt("muk", ::MukModel)
        inbuilt("nidoking", ::NidokingModel)
        inbuilt("nidoqueen", ::NidoqueenModel)
        inbuilt("nidoranf", ::NidoranfModel)
        inbuilt("nidoranm", ::NidoranmModel)
        inbuilt("nidorina", ::NidorinaModel)
        inbuilt("nidorino", ::NidorinoModel)
        inbuilt("ninetales", ::NinetalesModel)
        inbuilt("omanyte", ::OmanyteModel)
        inbuilt("omastar", ::OmastarModel)
        inbuilt("onix", ::OnixModel)
        inbuilt("persian", ::PersianModel)
        inbuilt("pikachu", ::PikachuModel)
        inbuilt("pinsir", ::PinsirModel)
        inbuilt("poliwag", ::PoliwagModel)
        inbuilt("poliwhirl", ::PoliwhirlModel)
        inbuilt("poliwrath", ::PoliwrathModel)
        inbuilt("politoed", ::PolitoedModel)
        inbuilt("ponyta", ::PonytaModel)
        inbuilt("porygon", ::PorygonModel)
        inbuilt("psyduck", ::PsyduckModel)
        inbuilt("raichu", ::RaichuModel)
        inbuilt("raichu_alolan", ::RaichuAlolanModel)
        inbuilt("rapidash", ::RapidashModel)
        inbuilt("rhydon", ::RhydonModel)
        inbuilt("rhyhorn", ::RhyhornModel)
        inbuilt("sandshrew", ::SandshrewModel)
        inbuilt("sandslash", ::SandslashModel)
        inbuilt("scyther", ::ScytherModel)
        inbuilt("seadra", ::SeadraModel)
        inbuilt("seaking", ::SeakingModel)
        inbuilt("seel", ::SeelModel)
        inbuilt("shellder", ::ShellderModel)
        inbuilt("slowbro", ::SlowbroModel)
        inbuilt("slowpoke", ::SlowpokeModel)
        inbuilt("spearow", ::SpearowModel)
        inbuilt("starmie", ::StarmieModel)
        inbuilt("staryu", ::StaryuModel)
        inbuilt("steelix", ::SteelixModel)
        inbuilt("tangela", ::TangelaModel)
        inbuilt("tauros", ::TaurosModel)
        inbuilt("tentacool", ::TentacoolModel)
        inbuilt("tentacruel", ::TentacruelModel)
        inbuilt("vaporeon", ::VaporeonModel)
        inbuilt("venomoth", ::VenomothModel)
        inbuilt("venonat", ::VenonatModel)
        inbuilt("victreebel", ::VictreebelModel)
        inbuilt("vulpix", ::VulpixModel)
        inbuilt("weepinbell", ::WeepinbellModel)
        inbuilt("weezing", ::WeezingModel)
        inbuilt("wigglytuff", ::WigglytuffModel)
        inbuilt("zapdos", ::ZapdosModel)
        inbuilt("elekid", ::ElekidModel)
        inbuilt("igglybuff", ::IgglybuffModel)
        inbuilt("magby", ::MagbyModel)
        inbuilt("pichu", ::PichuModel)
        inbuilt("smoochum", ::SmoochumModel)
        inbuilt("tyrogue", ::TyrogueModel)
        inbuilt("hitmontop", ::HitmontopModel)
        inbuilt("electivire", ::ElectivireModel)
        inbuilt("glaceon", ::GlaceonModel)
        inbuilt("happiny", ::HappinyModel)
        inbuilt("leafeon", ::LeafeonModel)
        inbuilt("lickilicky", ::LickilickyModel)
        inbuilt("magmortar", ::MagmortarModel)
        inbuilt("magnezone", ::MagnezoneModel)
        inbuilt("mimejr", ::MimejrModel)
        inbuilt("porygon2", ::Porygon2Model)
        inbuilt("porygonz", ::PorygonzModel)
        inbuilt("rhyperior", ::RhyperiorModel)
        inbuilt("scizor", ::ScizorModel)
        inbuilt("tangrowth", ::TangrowthModel)
        inbuilt("sylveon", ::SylveonModel)
        inbuilt("umbreon", ::UmbreonModel)
        inbuilt("espeon", ::EspeonModel)
        inbuilt("blissey", ::BlisseyModel)
        inbuilt("kingdra", ::KingdraModel)
        inbuilt("piloswine", ::PiloswineModel)
        inbuilt("quagsire", ::QuagsireModel)
        inbuilt("slowking", ::SlowkingModel)
        inbuilt("swinub", ::SwinubModel)
        inbuilt("wooper", ::WooperModel)
        inbuilt("wooper_paldean", ::WooperPaldeanModel)
        inbuilt("yanma", ::YanmaModel)
        inbuilt("blaziken", ::BlazikenModel)
        inbuilt("combusken", ::CombuskenModel)
        inbuilt("marshtomp", ::MarshtompModel)
        inbuilt("minun", ::MinunModel)
        inbuilt("mudkip", ::MudkipModel)
        inbuilt("plusle", ::PlusleModel)
        inbuilt("rayquaza", ::RayquazaModel)
        inbuilt("swampert", ::SwampertModel)
        inbuilt("torchic", ::TorchicModel)
        inbuilt("bibarel", ::BibarelModel)
        inbuilt("bidoof", ::BidoofModel)
        inbuilt("buneary", ::BunearyModel)
        inbuilt("empoleon", ::EmpoleonModel)
        inbuilt("lopunny", ::LopunnyModel)
        inbuilt("mamoswine", ::MamoswineModel)
        inbuilt("pachirisu", ::PachirisuModel)
        inbuilt("piplup", ::PiplupModel)
        inbuilt("prinplup", ::PrinplupModel)
        inbuilt("yanmega", ::YanmegaModel)
        inbuilt("basculin", ::BasculinModel)
        inbuilt("crustle", ::CrustleModel)
        inbuilt("dwebble", ::DwebbleModel)
        inbuilt("emolga", ::EmolgaModel)
        inbuilt("maractus", ::MaractusModel)
        inbuilt("bounsweet", ::BounsweetModel)
        inbuilt("dartrix", ::DartrixModel)
        inbuilt("decidueye", ::DecidueyeModel)
        inbuilt("incineroar", ::IncineroarModel)
        inbuilt("litten", ::LittenModel)
        inbuilt("mimikyu", ::MimikyuModel)
        inbuilt("naganadel", ::NaganadelModel)
        inbuilt("poipole", ::PoipoleModel)
        inbuilt("rowlet", ::RowletModel)
        inbuilt("steenee", ::SteeneeModel)
        inbuilt("torracat", ::TorracatModel)
        inbuilt("tsareena", ::TsareenaModel)
        inbuilt("centiskorch", ::CentiskorchModel)
        inbuilt("sizzlipede", ::SizzlipedeModel)
        inbuilt("kleavor", ::KleavorModel)
        inbuilt("pyukumuku", ::PyukumukuModel)
        inbuilt("deerling", ::DeerlingModel)
        inbuilt("sawsbuck", ::SawsbuckModel)
        inbuilt("sableye", ::SableyeModel)
        inbuilt("natu", ::NatuModel)
        inbuilt("xatu", ::XatuModel)
        inbuilt("wailmer", ::WailmerModel)
        inbuilt("wailord", ::WailordModel)
        inbuilt("murkrow", ::MurkrowModel)
        inbuilt("honchkrow", ::HonchkrowModel)
        inbuilt("nacli", :: NacliModel)
        inbuilt("naclstack", :: NaclstackModel)
        inbuilt("garganacl", ::GarganaclModel)
        inbuilt("dhelmise", :: DhelmiseModel)
        inbuilt("alcremie", :: AlcremieModel)
        inbuilt("milcery", :: MilceryModel)
        inbuilt("turtwig", :: TurtwigModel)
        inbuilt("grotle", :: GrotleModel)
        inbuilt("torterra", :: TorterraModel)
        inbuilt("torterra_cherry", :: TorterraCherryModel)
        inbuilt("xerneas", :: XerneasModel)
        inbuilt("klink", :: KlinkModel)
        inbuilt("klang", :: KlangModel)
        inbuilt("klinklang", :: KlinklangModel)
        inbuilt("morelull", :: MorelullModel)
        inbuilt("shiinotic", :: ShiinoticModel)
        inbuilt("joltik", :: JoltikModel)
        inbuilt("galvantula", :: GalvantulaModel)
        inbuilt("riolu", :: RioluModel)
        inbuilt("lucario", :: LucarioModel)
        inbuilt("treecko", :: TreeckoModel)
        inbuilt("grovyle", :: GrovyleModel)
        inbuilt("sceptile", :: SceptileModel)
        inbuilt("honedge", :: HonedgeModel)
        inbuilt("spiritomb", :: SpiritombModel)
        inbuilt("baltoy", :: BaltoyModel)
        inbuilt("claydol", :: ClaydolModel)
        inbuilt("chespin", :: ChespinModel)
        inbuilt("quilladin", :: QuilladinModel)
        inbuilt("chesnaught", :: ChesnaughtModel)
        inbuilt("elgyem", :: ElgyemModel)
        inbuilt("beheeyem", :: BeheeyemModel)
        inbuilt("gible", :: GibleModel)
        inbuilt("gabite", :: GabiteModel)
        inbuilt("garchomp", :: GarchompModel)
        inbuilt("pineco", :: PinecoModel)
        inbuilt("forretress", :: ForretressModel)
        inbuilt("doublade", :: DoubladeModel)
        inbuilt("aegislash", :: AegislashModel)
        inbuilt("lotad", :: LotadModel)
        inbuilt("lombre", :: LombreModel)
        inbuilt("ludicolo", :: LudicoloModel)
        inbuilt("golett", :: GolettModel)
        inbuilt("golurk", :: GolurkModel)
        inbuilt("stantler", :: StantlerModel)
        inbuilt("wyrdeer", :: WyrdeerModel)
        inbuilt("sneasel", :: SneaselModel)
        inbuilt("weavile", :: WeavileModel)
        inbuilt("bergmite", :: BergmiteModel)
        inbuilt("avalugg", :: AvaluggModel)
        inbuilt("misdreavus", :: MisdreavusModel)
        inbuilt("mismagius", :: MismagiusModel)
        inbuilt("whismur", :: WhismurModel)
        inbuilt("loudred", :: LoudredModel)
        inbuilt("exploud", :: ExploudModel)
        inbuilt("luvdisc", :: LuvdiscModel)
        inbuilt("cryogonal", :: CryogonalModel)
        inbuilt("sigilyph", :: SigilyphModel)
        inbuilt("pumpkaboo", :: PumpkabooModel)
        inbuilt("gourgeist", :: GourgeistModel)
        inbuilt("eiscue", :: EiscueModel)
        inbuilt("tatsugiri", :: TatsugiriModel)
        inbuilt("wooloo", :: WoolooModel)
        inbuilt("dubwool", :: DubwoolModel)
        inbuilt("chimchar", :: ChimcharModel)
        inbuilt("monferno", :: MonfernoModel)
        inbuilt("infernape", :: InfernapeModel)
        inbuilt("popplio", :: PopplioModel)
        inbuilt("brionne", :: BrionneModel)
        inbuilt("primarina", ::PrimarinaModel)
        inbuilt("spinda", ::SpindaModel)
        inbuilt("seedot", ::SeedotModel)
        inbuilt("nuzleaf", ::NuzleafModel)
        inbuilt("shiftry", ::ShiftryModel)
        inbuilt("kricketot", ::KricketotModel)
        inbuilt("kricketune", ::KricketuneModel)
        inbuilt("heatmor", ::HeatmorModel)
        inbuilt("durant", ::DurantModel)
        inbuilt("carvanha", ::CarvanhaModel)
        inbuilt("sharpedo", ::SharpedoModel)
        inbuilt("mawile", ::MawileModel)
        inbuilt("walkingwake", ::WalkingwakeModel)
        inbuilt("ironleaves", ::IronleavesModel)
        inbuilt("miltank", ::MiltankModel)
        inbuilt("torkoal", ::TorkoalModel)
        inbuilt("fennekin", ::FennekinModel)
        inbuilt("braixen", ::BraixenModel)
        inbuilt("delphox", ::DelphoxModel)
        inbuilt("froakie", ::FroakieModel)
        inbuilt("frogadier", ::FrogadierModel)
        inbuilt("greninja", ::GreninjaModel)
        inbuilt("tepig", ::TepigModel)
        inbuilt("pignite", ::PigniteModel)
        inbuilt("emboar", ::EmboarModel)
        inbuilt("grookey", ::GrookeyModel)
        inbuilt("thwackey", ::ThwackeyModel)
        inbuilt("rillaboom", ::RillaboomModel)
        inbuilt("scorbunny", ::ScorbunnyModel)
        inbuilt("raboot", ::RabootModel)
        inbuilt("cinderace", ::CinderaceModel)
        inbuilt("sobble", ::SobbleModel)
        inbuilt("drizzile", ::DrizzileModel)
        inbuilt("inteleon", ::InteleonModel)
        inbuilt("oshawott", ::OshawottModel)
        inbuilt("dewott", ::DewottModel)
        inbuilt("samurott", ::SamurottModel)
        inbuilt("snivy", ::SnivyModel)
        inbuilt("servine", ::ServineModel)
        inbuilt("serperior", ::SerperiorModel)
        inbuilt("slugma", ::SlugmaModel)
        inbuilt("magcargo", ::MagcargoModel)
        inbuilt("slugma_shiny", ::SlugmaShinyModel)
        inbuilt("magcargo_shiny", ::MagcargoShinyModel)
        inbuilt("nosepass", ::NosepassModel)
        inbuilt("probopass", ::ProbopassModel)
        inbuilt("chinchou", ::ChinchouModel)
        inbuilt("clamperl", ::ClamperlModel)
        inbuilt("huntail", ::HuntailModel)
        inbuilt("gorebyss", ::GorebyssModel)
        inbuilt("spinarak", ::SpinarakModel)
        inbuilt("ariados", ::AriadosModel)
        inbuilt("shuckle", ::ShuckleModel)
        inbuilt("taillow", ::TaillowModel)
        inbuilt("swellow", ::SwellowModel)
        inbuilt("relicanth", ::RelicanthModel)
        inbuilt("mudbray", ::MudbrayModel)
        inbuilt("mudsdale", ::MudsdaleModel)
        inbuilt("comfey", ::ComfeyModel)
        inbuilt("tandemaus", ::TandemausModel)
        inbuilt("maushold", ::MausholdModel)
        inbuilt("mausholdfour", ::MausholdfourModel)
        inbuilt("varoom", ::VaroomModel)
        inbuilt("revavroom", ::RevavroomModel)
        inbuilt("lanturn", ::LanturnModel)
        inbuilt("chingling", ::ChinglingModel)
        inbuilt("chimecho", ::ChimechoModel)
        inbuilt("fidough", ::FidoughModel)
        inbuilt("dachsbun", ::DachsbunModel)
        inbuilt("chatot", ::ChatotModel)
        inbuilt("gligar", ::GligarModel)
        inbuilt("gliscor", ::GliscorModel)
        inbuilt("poochyena", ::PoochyenaModel)
        inbuilt("mightyena", ::MightyenaModel)
        inbuilt("sprigatito", ::SprigatitoModel)
        inbuilt("floragato", ::FloragatoModel)
        inbuilt("meowscarada", ::MeowscaradaModel)
        inbuilt("shroomish", ::ShroomishModel)
        inbuilt("breloom", ::BreloomModel)
        inbuilt("charcadet", ::CharcadetModel)
        inbuilt("armarouge", ::ArmarougeModel)
        inbuilt("ceruledge", ::CeruledgeModel)
        inbuilt("flittle", ::FlittleModel)
        inbuilt("espathra", ::EspathraModel)
        inbuilt("surskit", ::SurskitModel)
        inbuilt("masquerain", ::MasquerainModel)
        inbuilt("cutiefly", ::CutieflyModel)
        inbuilt("ribombee", ::RibombeeModel)
        inbuilt("carnivine", ::CarnivineModel)
        inbuilt("falinks", ::FalinksModel)
        inbuilt("stufful", ::StuffulModel)
        inbuilt("bewear", ::BewearModel)
        inbuilt("scatterbug", ::ScatterbugModel)
        inbuilt("spewpa", ::SpewpaModel)
        inbuilt("vivillon", ::VivillonModel)
        inbuilt("barboach", ::BarboachModel)
        inbuilt("whiscash", ::WhiscashModel)
        inbuilt("combee", ::CombeeModel)
        inbuilt("vespiquen", ::VespiquenModel)
        inbuilt("lillipup", ::LillipupModel)
        inbuilt("herdier", ::HerdierModel)
        inbuilt("stoutland", ::StoutlandModel)
        inbuilt("sirfetchd", ::SirfetchdModel)
        inbuilt("rookidee", ::RookideeModel)
        inbuilt("corvisquire", ::CorvisquireModel)
        inbuilt("corviknight", ::CorviknightModel)
        inbuilt("duskull", ::DuskullModel)
        inbuilt("dusclops", ::DusclopsModel)
        inbuilt("dusknoir", ::DusknoirModel)
        inbuilt("nickit", ::NickitModel)
        inbuilt("thievul", ::ThievulModel)
        inbuilt("cacnea", ::CacneaModel)
        inbuilt("cacturne", ::CacturneModel)
        inbuilt("glimmet", ::GlimmetModel)
        inbuilt("glimmora", ::GlimmoraModel)
        inbuilt("bonsly", ::BonslyModel)
        inbuilt("sudowoodo", ::SudowoodoModel)
        inbuilt("bouffalant", ::BouffalantModel)
        inbuilt("cetoddle", ::CetoddleModel)
        inbuilt("cetitan", ::CetitanModel)
        inbuilt("venipede", ::VenipedeModel)
        inbuilt("whirlipede", ::WhirlipedeModel)
        inbuilt("scolipede", ::ScolipedeModel)
        inbuilt("aipom", ::AipomModel)
        inbuilt("ambipom", ::AmbipomModel)
        inbuilt("hoothoot", ::HoothootModel)
        inbuilt("noctowl", ::NoctowlModel)
        inbuilt("wingull", ::WingullModel)
        inbuilt("pelipper", ::PelipperModel)
        inbuilt("shinx", ::ShinxModel)
        inbuilt("luxio", ::LuxioModel)
        inbuilt("luxray", ::LuxrayModel)
        inbuilt("numel", ::NumelModel)
        inbuilt("camerupt", ::CameruptModel)
        inbuilt("vulpix_alolan", ::VulpixAlolanModel)
        inbuilt("ninetales_alolan", ::NinetalesAlolanModel)
        inbuilt("roggenrola", ::RoggenrolaModel)
        inbuilt("boldore", ::BoldoreModel)
        inbuilt("gigalith", ::GigalithModel)
        inbuilt("yamask", ::YamaskModel)
        inbuilt("cofagrigus", ::CofagrigusModel)
        inbuilt("mareep", ::MareepModel)
        inbuilt("flaaffy", ::FlaaffyModel)
        inbuilt("ampharos", ::AmpharosModel)
        inbuilt("patrat", ::PatratModel)
        inbuilt("watchog", ::WatchogModel)
        inbuilt("skrelp", ::SkrelpModel)
        inbuilt("dragalge", ::DragalgeModel)
        inbuilt("bunnelby", ::BunnelbyModel)
        inbuilt("diggersby", ::DiggersbyModel)
        inbuilt("arrokuda", ::ArrokudaModel)
        inbuilt("barraskewda", ::BarraskewdaModel)
        inbuilt("shroodle", ::ShroodleModel)
        inbuilt("grafaiai", ::GrafaiaiModel)
        inbuilt("squawkabilly", ::SquawkabillyModel)
        inbuilt("annihilape", ::AnnihilapeModel)
        inbuilt("ponyta_galarian", ::PonytaGalarianModel)
        inbuilt("rapidash_galarian", ::RapidashGalarianModel)
        inbuilt("volbeat", ::VolbeatModel)
        inbuilt("illumise", ::IllumiseModel)
        inbuilt("yamper", ::YamperModel)
        inbuilt("boltund", ::BoltundModel)
        inbuilt("tinkatink", ::TinkatinkModel)
        inbuilt("tinkatuff", ::TinkatuffModel)
        inbuilt("tinkaton", ::TinkatonModel)
        inbuilt("fuecoco", :: FuecocoModel)
        inbuilt("crocalor", :: CrocalorModel)
        inbuilt("skeledirge", :: SkeledirgeModel)
        inbuilt("quaxly", :: QuaxlyModel)
        inbuilt("quaxwell", :: QuaxwellModel)
        inbuilt("quaquaval", :: QuaquavalModel)
        inbuilt("snubbull", :: SnubbullModel)
        inbuilt("granbull", :: GranbullModel)
        inbuilt("maschiff", :: MaschiffModel)
        inbuilt("mabosstiff", :: MabosstiffModel)
        inbuilt("phanpy", :: PhanpyModel)
        inbuilt("donphan", :: DonphanModel)
        inbuilt("buizel", :: BuizelModel)
        inbuilt("floatzel", :: FloatzelModel)
        inbuilt("zigzagoon", :: ZigzagoonModel)
        inbuilt("linoone", :: LinooneModel)
        inbuilt("zigzagoon_galarian", :: ZigzagoonGalarianModel)
        inbuilt("linoone_galarian", :: LinooneGalarianModel)
        inbuilt("obstagoon", :: ObstagoonModel)
        inbuilt("cottonee", :: CottoneeModel)
        inbuilt("whimsicott", :: WhimsicottModel)
        inbuilt("wishiwashi_solo", :: WishiwashiSoloModel)
        inbuilt("wishiwashi_schooling", :: WishiwashiSchoolingModel)
        inbuilt("meowth_alolan", ::MeowthAlolanModel)
        inbuilt("meowth_galarian", ::MeowthGalarianModel)
        inbuilt("persian_alolan", ::PersianAlolanModel)
        inbuilt("perrserker", ::PerrserkerModel)
        inbuilt("starly", ::StarlyModel)
        inbuilt("staravia", ::StaraviaModel)
        inbuilt("staraptor", ::StaraptorModel)
        inbuilt("komala", ::KomalaModel)
        inbuilt("phantump", ::PhantumpModel)
        inbuilt("trevenant", ::TrevenantModel)
        inbuilt("totodile", ::TotodileModel)
        inbuilt("croconaw", ::CroconawModel)
        inbuilt("feraligatr", ::FeraligatrModel)
        inbuilt("cyndaquil", ::CyndaquilModel)
        inbuilt("quilava", ::QuilavaModel)
        inbuilt("typhlosion", ::TyphlosionModel)
        inbuilt("chikorita", ::ChikoritaModel)
        inbuilt("bayleef", ::BayleefModel)
        inbuilt("meganium", ::MeganiumModel)
        inbuilt("fletchling", ::FletchlingModel)
        inbuilt("fletchinder", ::FletchinderModel)
        inbuilt("talonflame", ::TalonflameModel)
        inbuilt("crabrawler", ::CrabrawlerModel)
        inbuilt("crabominable", ::CrabominableModel)
        inbuilt("wimpod", ::WimpodModel)
        inbuilt("golisopod", ::GolisopodModel)
        inbuilt("nincada", ::NincadaModel)
        inbuilt("ninjask", ::NinjaskModel)
        inbuilt("shedinja", ::ShedinjaModel)
        inbuilt("ralts", ::RaltsModel)
        inbuilt("kirlia", ::KirliaModel)
        inbuilt("gardevoir", ::GardevoirModel)
        inbuilt("gallade", ::GalladeModel)
        inbuilt("beldum", ::BeldumModel)
        inbuilt("metang", ::MetangModel)
        inbuilt("metagross", ::MetagrossModel)
        inbuilt("ursaluna", ::UrsalunaModel)
        inbuilt("lechonk", ::LechonkModel)
        inbuilt("oinkologne_male", ::OinkologneMaleModel)
        inbuilt("oinkologne_female", ::OinkologneFemaleModel)
        inbuilt("pidove", ::PidoveModel)
        inbuilt("tranquill", ::TranquillModel)
        inbuilt("unfezant", ::UnfezantModel)
        inbuilt("timburr", ::TimburrModel)
        inbuilt("gurdurr", ::GurdurrModel)
        inbuilt("conkeldurr", ::ConkeldurrModel)
        inbuilt("clodsire", ::ClodsireModel)
        inbuilt("teddiursa", ::TeddiursaModel)
        inbuilt("ursaring", ::UrsaringModel)
        inbuilt("litwick", ::LitwickModel)
        inbuilt("lampent", ::LampentModel)
        inbuilt("chandelure", ::ChandelureModel)
        inbuilt("gimmighoulroaming", ::GimmighoulRoamingModel)
        inbuilt("gimmighoulchest", ::GimmighoulChestModel)
        inbuilt("gholdengo", ::GholdengoModel)
        inbuilt("drifloon", ::DrifloonModel)
        inbuilt("drifblim", ::DrifblimModel)
        inbuilt("lileep", ::LileepModel)
        inbuilt("cradily", ::CradilyModel)
        inbuilt("tirtouga", ::TirtougaModel)
        inbuilt("carracosta", ::CarracostaModel)
        inbuilt("arctovish", ::ArctovishModel)
        inbuilt("dracovish", ::DracovishModel)
        inbuilt("arctozolt", ::ArctozoltModel)
        inbuilt("dracozolt", ::DracozoltModel)
        inbuilt("shieldon", ::ShieldonModel)
        inbuilt("bastiodon", ::BastiodonModel)
        inbuilt("cranidos", ::CranidosModel)
        inbuilt("rampardos", ::RampardosModel)
        inbuilt("basculegion", ::BasculegionModel)
        inbuilt("tyrunt", ::TyruntModel)
        inbuilt("tyrantrum", ::TyrantrumModel)
        inbuilt("anorith", ::AnorithModel)
        inbuilt("armaldo", ::ArmaldoModel)
        inbuilt("archen", ::ArchenModel)
        inbuilt("archeops", ::ArcheopsModel)
        inbuilt("aron", ::AronModel)
        inbuilt("lairon", ::LaironModel)
        inbuilt("aggron", ::AggronModel)
        inbuilt("hippopotas", ::HippopotasModel)
        inbuilt("hippowdon", ::HippowdonModel)
        inbuilt("zorua", ::ZoruaModel)
        inbuilt("zorua_hisuian", ::ZoruaHisuianModel)
        inbuilt("zoroark", ::ZoroarkModel)
        inbuilt("zoroark_hisuian", ::ZoroarkHisuianModel)
        inbuilt("gossifleur", ::GossifleurModel)
        inbuilt("eldegoss", ::EldegossModel)
        inbuilt("amaura", ::AmauraModel)
        inbuilt("aurorus", ::AurorusModel)
        inbuilt("voltorb_hisuian", ::VoltorbHisuianModel)
        inbuilt("electrode_hisuian", ::ElectrodeHisuianModel)
        inbuilt("sentret", ::SentretModel)
        inbuilt("furret", ::FurretModel)
        inbuilt("qwilfish", ::QwilfishModel)
        inbuilt("qwilfish_hisuian", ::QwilfishHisuianModel)
        inbuilt("overqwil", ::OverqwilModel)
        inbuilt("sneasel_hisuian", ::SneaselHisuianModel)
        inbuilt("sneasler", ::SneaslerModel)
        inbuilt("tropius", ::TropiusModel)
        inbuilt("petilil", ::PetililModel)
        inbuilt("lilligant", ::LilligantModel)
        inbuilt("petilil_hisui_bias", ::PetililHisuiBiasModel)
        inbuilt("lilligant_hisuian", ::LilligantHisuianModel)
        inbuilt("darumaka", ::DarumakaModel)
        inbuilt("darmanitan", ::DarmanitanModel)
        inbuilt("darmanitan_zen", ::DarmanitanZenModel)
        inbuilt("turtonator", ::TurtonatorModel)
        inbuilt("stonjourner", ::StonjournerModel)
        inbuilt("cufant", ::CufantModel)
        inbuilt("copperajah", ::CopperajahModel)
        inbuilt("budew", ::BudewModel)
        inbuilt("roselia", ::RoseliaModel)
        inbuilt("roserade", ::RoseradeModel)
        inbuilt("solrock", ::SolrockModel)
        inbuilt("lunatone", ::LunatoneModel)
        inbuilt("woobat", ::WoobatModel)
        inbuilt("swoobat", ::SwoobatModel)
        inbuilt("sandile", ::SandileModel)
        inbuilt("krokorok", ::KrokorokModel)
        inbuilt("krookodile", ::KrookodileModel)
        inbuilt("frillish", ::FrillishModel)
        inbuilt("jellicent", ::JellicentModel)
        inbuilt("cubchoo", ::CubchooModel)
        inbuilt("beartic", ::BearticModel)
        inbuilt("deino", ::DeinoModel)
        inbuilt("zweilous", ::ZweilousModel)
        inbuilt("hydreigon", ::HydreigonModel)
        inbuilt("larvesta", ::LarvestaModel)
        inbuilt("volcarona", ::VolcaronaModel)
        inbuilt("fomantis", ::FomantisModel)
        inbuilt("lurantis", ::LurantisModel)
        inbuilt("dreepy", ::DreepyModel)
        inbuilt("drakloak", ::DrakloakModel)
        inbuilt("dragapult", ::DragapultModel)
        inbuilt("diglett_alolan", ::DiglettAlolanModel)
        inbuilt("dugtrio_alolan", ::DugtrioAlolanModel)
        inbuilt("makuhita", ::MakuhitaModel)
        inbuilt("hariyama", ::HariyamaModel)
        inbuilt("alomomola", ::AlomomolaModel)
        inbuilt("ferroseed", ::FerroseedModel)
        inbuilt("ferrothorn", ::FerrothornModel)
        inbuilt("flabebe", ::FlabebeModel)
        inbuilt("floette", ::FloetteModel)
        inbuilt("florges", ::FlorgesModel)
        inbuilt("carbink", ::CarbinkModel)
        inbuilt("goomy", ::GoomyModel)
        inbuilt("goomy_hisui_bias", ::GoomyHisuiBiasModel)
        inbuilt("sliggoo", ::SliggooModel)
        inbuilt("sliggoo_hisuian", ::SliggooHisuianModel)
        inbuilt("goodra", ::GoodraModel)
        inbuilt("goodra_hisuian", ::GoodraHisuianModel)
        inbuilt("heracross", ::HeracrossModel)
        inbuilt("skarmory", ::SkarmoryModel)
        inbuilt("salandit", ::SalanditModel)
        inbuilt("salazzle", ::SalazzleModel)
        inbuilt("jangmo-o", ::JangmoOModel)
        inbuilt("hakamo-o", ::HakamoOModel)
        inbuilt("kommo-o", ::KommoOModel)
        inbuilt("trapinch", ::TrapinchModel)
        inbuilt("vibrava", ::VibravaModel)
        inbuilt("flygon", ::FlygonModel)
        inbuilt("larvitar", ::LarvitarModel)
        inbuilt("pupitar", ::PupitarModel)
        inbuilt("tyranitar", ::TyranitarModel)
        inbuilt("impidimp", ::ImpidimpModel)
        inbuilt("morgrem", ::MorgremModel)
        inbuilt("grimmsnarl", ::GrimmsnarlModel)
        inbuilt("klefki", ::KlefkiModel)
        inbuilt("oshawott_hisui_bias", ::OshawottHisuiBiasModel)
        inbuilt("dewott_hisui_bias", ::DewottHisuiBiasModel)
        inbuilt("samurott_hisuian", ::SamurottHisuianModel)
        inbuilt("cyndaquil_hisui_bias", ::CyndaquilHisuiBiasModel)
        inbuilt("quilava_hisui_bias", ::QuilavaHisuiBiasModel)
        inbuilt("typhlosion_hisuian", ::TyphlosionHisuianModel)
        inbuilt("rowlet_hisui_bias", ::RowletHisuiBiasModel)
        inbuilt("dartrix_hisui_bias", ::DartrixHisuiBiasModel)
        inbuilt("decidueye_hisuian", ::DecidueyeHisuianModel)
        inbuilt("smeargle", ::SmeargleModel)
    }

    fun registerJsonPosers(resourceManager: ResourceManager) {
        for ((directory, poserClass) in poserDirectories) {
            resourceManager
                .listResources(directory) { path -> path.endsWith(".json") }
                .forEach { (identifier, resource) ->
                    resource.open().use { stream ->
                        val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                        posers[resolvedIdentifier] = loadJsonPoser(json, poserClass)
                    }
                }
        }
    }

    fun inbuilt(name: String, model: (ModelPart) -> PosableModel) {
        posers[cobblemonResource(name)] = { bone -> model.invoke(bone as ModelPart) }
    }

    fun registerVariations(resourceManager: ResourceManager) {
        var variationCount = 0
        val nameToModelVariationSets = mutableMapOf<ResourceLocation, MutableList<ModelVariationSet>>()
        for (directory in variationDirectories) {
            resourceManager
                .listResources(directory) { path -> path.endsWith(".json") }
                .forEach { (_, resource) ->
                    resource.open().use { stream ->
                        val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        val modelVariationSet = VaryingRenderableResolver.GSON.fromJson<ModelVariationSet>(json)
                        nameToModelVariationSets.getOrPut(modelVariationSet.name) { mutableListOf() }.add(modelVariationSet)
                        variationCount += modelVariationSet.variations.size
                    }
                }
        }

        for ((species, speciesVariationSets) in nameToModelVariationSets) {
            val variations = speciesVariationSets.sortedBy { it.order }.flatMap { it.variations }.toMutableList()
            this.variations[species] = VaryingRenderableResolver(species, variations)
        }

        variations.values.forEach { it.initialize(this) }

        Cobblemon.LOGGER.info("Loaded $variationCount variations.")
    }

    fun registerModels(resourceManager: ResourceManager) {
        var models = 0
        for (directory in modelDirectories) {
            MODEL_FACTORIES.forEach { (key, func) ->
                resourceManager.listResources(directory) { path -> path.endsWith(key) }
                    .map { func.apply(it.key, it.value) }
                    .forEach {
                        texturedModels[it.first] = it.second
                        models++
                    }
            }

        }

        Cobblemon.LOGGER.info("Loaded $models models.")
    }

    fun reload(resourceManager: ResourceManager) {
        Cobblemon.LOGGER.info("Loading varying Bedrock assets...")
        this.variations.clear()
        this.posers.clear()
        registerModels(resourceManager)
        registerPosers(resourceManager)
        registerVariations(resourceManager)
    }

    fun getPoser(name: ResourceLocation, state: PosableState): PosableModel {
        try {
            val poser = this.variations[name]?.getPoser(state)
            if (poser != null) {
                return poser
            }
        } catch(e: IllegalStateException) {
            e.printStackTrace()
        }
        return this.variations[fallback]!!.getPoser(state)
    }

    fun getTexture(name: ResourceLocation, state: PosableState): ResourceLocation {
        try {
            val texture = this.variations[name]?.getTexture(state)
            if (texture != null) {
                return texture
            }
        } catch(_: IllegalStateException) { }
        return this.variations[fallback]!!.getTexture(state)
    }

    fun getTextureNoSubstitute(name: ResourceLocation, state: PosableState): ResourceLocation? {
        try {
            val texture = this.variations[name]?.getTexture(state)
            if (texture != null && texture.exists()) {
                return texture
            }
        } catch(_: IllegalStateException) {}
        return null
    }

    fun getLayers(name: ResourceLocation, state: PosableState): Iterable<ModelLayer> {
        try {
            val layers = this.variations[name]?.getLayers(state)
            if (layers != null) {
                return layers
            }
        } catch(_: IllegalStateException) { }
        return this.variations[fallback]!!.getLayers(state)
    }

    fun getSprite(name: ResourceLocation, state: PosableState, type: SpriteType): ResourceLocation? {
        try {
            return this.variations[name]?.getSprite(state, type)
        } catch (_: IllegalStateException) {}
        return null
    }

    fun registerFactory(id: String, factory: BiFunction<ResourceLocation, Resource, Pair<ResourceLocation, Bone>>) {
        MODEL_FACTORIES[id] = factory
    }

    /*
        Needs to be java function to work with non kotlin sidemods.
        - Waterpicker
     */
    private var MODEL_FACTORIES = mutableMapOf<String, BiFunction<ResourceLocation, Resource, Pair<ResourceLocation, Bone>>>().also {
        it[".geo.json"] = BiFunction<ResourceLocation, Resource, Pair<ResourceLocation, Bone>> { identifier: ResourceLocation, resource: Resource ->
            resource.open().use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)

                val texturedModel = TexturedModel.from(json)
                resolvedIdentifier to texturedModel.create().bakeRoot()
            }
        }
    }
}