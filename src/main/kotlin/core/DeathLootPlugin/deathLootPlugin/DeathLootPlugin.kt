package core.DeathLootPlugin.deathLootPlugin

import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.ChatColor
import java.util.*
import org.bukkit.event.inventory.InventoryDragEvent
class DeathLootPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)

        val mes = """
            Most Polished Plugin
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

███████╗██╗  ██╗██╗   ██╗██╗     ██╗  ██╗███████╗██████╗ 
██╔════╝██║  ██║██║   ██║██║     ██║ ██╔╝██╔════╝██╔══██╗
███████╗███████║██║   ██║██║     █████╔╝ █████╗  ██████╔╝
╚════██║██╔══██║██║   ██║██║     ██╔═██╗ ██╔══╝  ██╔══██╗
███████║██║  ██║╚██████╔╝███████╗██║  ██╗███████╗██║  ██║
╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++                                                     
        """.trimIndent()

        logger.info(mes)
        logger.info("Developed by Asparagus Group")

    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val world = player.world
        val deathLocation = player.location

        var shulkerBox1 = createShulkerBox(player, 1)
        var shulkerBox2: ItemStack? = null

        val droppedItems = player.inventory.contents.filterNotNull().toMutableList()
        val shulkerItems = droppedItems.filter { it.type.name.endsWith("_SHULKER_BOX") }
        droppedItems.removeAll(shulkerItems)

        shulkerItems.forEach {
            world.dropItemNaturally(deathLocation, it)
        }

        val headItem = ItemStack(Material.PLAYER_HEAD)
        val headMeta = headItem.itemMeta as SkullMeta
        headMeta.owningPlayer = player
        headItem.itemMeta = headMeta

        val shulkerMeta1 = shulkerBox1.itemMeta as BlockStateMeta
        val shulkerState1 = shulkerMeta1.blockState as ShulkerBox
        shulkerState1.inventory.addItem(headItem)

        for (item in droppedItems) {
            if (shulkerState1.inventory.firstEmpty() != -1) {
                shulkerState1.inventory.addItem(item)
            } else {
                if (shulkerBox2 == null) {
                    shulkerBox2 = createShulkerBox(player, 2)
                }
                val shulkerMeta2 = shulkerBox2.itemMeta as BlockStateMeta
                val shulkerState2 = shulkerMeta2.blockState as ShulkerBox
                shulkerState2.inventory.addItem(item)
                shulkerMeta2.blockState = shulkerState2
                shulkerBox2.itemMeta = shulkerMeta2
            }
        }

        shulkerMeta1.blockState = shulkerState1
        shulkerBox1.itemMeta = shulkerMeta1

        world.dropItemNaturally(deathLocation, shulkerBox1)
        if (shulkerBox2 != null) {
            world.dropItemNaturally(deathLocation, shulkerBox2)
        }

        event.drops.clear()
    }

    private fun createShulkerBox(player: Player, number: Int): ItemStack {
        val shulkerColors = Material.values().filter { it.name.endsWith("_SHULKER_BOX") }
        val randomColor = shulkerColors[Random().nextInt(shulkerColors.size)]
        val shulkerBoxItem = ItemStack(randomColor)
        val meta = shulkerBoxItem.itemMeta as BlockStateMeta
        val shulkerBox = meta.blockState as ShulkerBox

        meta.setDisplayName("§c§l${player.name}의 전리품 #$number")
        meta.lore = listOf("${ChatColor.RED}${ChatColor.BOLD}전리품 상자")
        shulkerBox.inventory.contents = arrayOfNulls(27)

        meta.blockState = shulkerBox
        shulkerBoxItem.itemMeta = meta

        return shulkerBoxItem
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory ?: return
        val topInventory = event.view.topInventory

        if (topInventory.holder is ShulkerBox) {
            val shulkerBox = topInventory.holder as ShulkerBox
            val customName = shulkerBox.customName

            if (customName != null && customName.contains("전리품")) {

                when (event.action) {
                    InventoryAction.PLACE_ALL,
                    InventoryAction.PLACE_SOME,
                    InventoryAction.PLACE_ONE,
                    InventoryAction.SWAP_WITH_CURSOR,
                    InventoryAction.HOTBAR_SWAP,
                    InventoryAction.HOTBAR_MOVE_AND_READD -> {
                        if (event.clickedInventory == topInventory) {
                            event.isCancelled = true
                            event.whoClicked.sendMessage("${ChatColor.RED}${ChatColor.BOLD} 전리품 상자에는 아이템을 추가할 수 없습니다!")
                        }
                    }
                    InventoryAction.MOVE_TO_OTHER_INVENTORY -> {
                        if (event.clickedInventory != event.whoClicked.inventory) {

                            event.isCancelled = false
                        } else {

                            event.isCancelled = true
                            event.whoClicked.sendMessage("${ChatColor.RED}${ChatColor.BOLD} 전리품 상자에는 아이템을 추가할 수 없습니다!")
                        }
                    }
                    else -> {

                    }
                }
            }
        }

        if (clickedInventory.holder is Player && topInventory.holder is ShulkerBox) {
            val shulkerBox = topInventory.holder as ShulkerBox
            val customName = shulkerBox.customName

            if (customName != null && customName.contains("전리품")) {
                if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.isCancelled = true
                    event.whoClicked.sendMessage("${ChatColor.RED}${ChatColor.BOLD} 전리품 상자에는 아이템을 추가할 수 없습니다!")
                }
            }
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val topInventory = event.view.topInventory

        if (topInventory.holder is ShulkerBox) {
            val shulkerBox = topInventory.holder as ShulkerBox
            val customName = shulkerBox.customName

            if (customName != null && customName.contains("전리품")) {

                for (slot in event.rawSlots) {
                    if (slot < topInventory.size) {
                        event.isCancelled = true
                        event.whoClicked.sendMessage("${ChatColor.RED}${ChatColor.BOLD} 전리품 상자에는 아이템을 추가할 수 없습니다!")
                        break
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val inventory = event.inventory

        val firstItem = inventory.getItem(0) ?: return
        val itemMeta = firstItem.itemMeta ?: return

        val displayName = itemMeta.displayName
        if (displayName != null && displayName.contains("전리품")) {
            event.result = null
            event.view.player.sendMessage("${ChatColor.RED}${ChatColor.BOLD} 전리품 상자는 모루를 통해 이름을 변경할 수 없습니다!")
        }
    }

}