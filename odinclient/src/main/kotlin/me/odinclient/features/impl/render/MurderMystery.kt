package me.odinclient.features.impl.render

import me.odinmain.OdinMain.mc
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runOnMCThread
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class MurderMystery : Module(
    "MurderMystery",
    category = Category.RENDER,
    description = "Detects and renders ESP for Murder Mystery players and items."
) {
    private val espInnocents by BooleanSetting("ESP Innocents", true)
    private val espMurderers by BooleanSetting("ESP Murderers", true)
    private val espGold by BooleanSetting("ESP Gold", true)
    private val espBow by BooleanSetting("ESP Bow", true)

    private val announceMurderer by BooleanSetting("Announce Murderer", true)
    private val announceBow by BooleanSetting("Announce Bow", true)

    private val murderItems = listOf(
        Items.iron_sword, Items.stone_sword, Items.wooden_sword, Items.golden_sword, Items.diamond_sword,
        Items.stick, Items.bow, Items.blaze_rod, Items.diamond_pickaxe, Items.diamond_axe,
        Items.carrot_on_a_stick, Items.egg, Items.feather, Items.fishing_rod, Items.flint_and_steel,
        Items.potionitem, Items.reeds, Items.record_11, Items.record_13, Items.record_cat,
        Items.record_chirp, Items.record_far, Items.record_mall, Items.record_mellohi, Items.record_stal,
        Items.record_strad, Items.record_wait, Items.record_ward, Items.shears, Items.snowball,
        Items.wheat, Items.rotten_flesh, Items.spider_eye, Items.sugar, Items.cookie,
        Items.cooked_beef, Items.cooked_chicken, Items.melon, Items.name_tag, Items.book,
        Items.written_book, Items.dye, Items.coal, Items.leather, Items.paper,
        Item.getItemFromBlock(Blocks.command_block), Item.getItemFromBlock(Blocks.vine),
        Item.getItemFromBlock(Blocks.ice), Item.getItemFromBlock(Blocks.double_plant),
        Item.getItemFromBlock(Blocks.red_flower), Item.getItemFromBlock(Blocks.yellow_flower),
        Item.getItemFromBlock(Blocks.bookshelf), Item.getItemFromBlock(Blocks.sapling),
        Item.getItemFromBlock(Blocks.netherrack)
    )

    private val knownMurderers = mutableSetOf<String>()
    private var bowLocation: AxisAlignedBB? = null

    private fun isMurderMystery(): Boolean {
        val d = mc.thePlayer?.func_96123_co()?.func_96539_a(1)?.func_96678_d() ?: return false
        return d.contains("MURDER") || d.contains("MYSTERY")
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!isMurderMystery()) return

        for (entity in mc.theWorld.loadedEntityList) {
            when (entity) {
                is EntityOtherPlayerMP -> handlePlayer(entity)
                is EntityArmorStand -> handleArmorStand(entity)
            }
        }

        if (espBow && bowLocation != null) {
            Renderer.drawBox(bowLocation!!, color = Color.CYAN, fillAlpha = 0.3f, depth = false)
        }
    }

    private fun handlePlayer(entity: EntityOtherPlayerMP) {
        if (entity.isInvisible || entity == mc.thePlayer) return

        val name = entity.name
        val heldItem = entity.heldItem?.item

        if (heldItem in murderItems) {
            if (espMurderers) Renderer.drawBox(entity, color = Color.RED, fillAlpha = 0.25f, depth = false)
            if (announceMurderer && knownMurderers.add(name)) {
                modMessage("§cMurderer Found: §f$name")
            }
        } else {
            if (espInnocents) Renderer.drawBox(entity, color = Color.GREEN, fillAlpha = 0.1f, depth = true)
        }

        // Bow detection
        if (announceBow && heldItem == Items.bow && name !in knownMurderers) {
            modMessage("§bDetective Bow Found: §f$name")
        }
    }

    private fun handleArmorStand(entity: EntityArmorStand) {
        val displayName = entity.name?.toLowerCase() ?: return
        if ("bow" in displayName && "pickup" in displayName && espBow) {
            bowLocation = entity.entityBoundingBox
            if (announceBow) {
                modMessage("§bBow has been dropped!")
            }
        }
    }

    private fun modMessage(message: Any?, prefix: String = "§3Odin §8»§r ") {
        val chatComponent = net.minecraft.util.ChatComponentText("$prefix$message")
        runOnMCThread {
            mc.thePlayer?.addChatMessage(chatComponent)
        }
    }
}
