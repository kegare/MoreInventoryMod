package moreinventory.container;

import moreinventory.slot.SlotDisplay;
import moreinventory.tileentity.storagebox.TileEntityStorageBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerStorageBox extends Container
{
	private final TileEntityStorageBox storageBox;

	public ContainerStorageBox(InventoryPlayer inventoryPlayer, TileEntityStorageBox tile)
	{
		this.storageBox = tile;
		this.addSlotToContainer(new SlotDisplay(tile, tile.getFirstItemIndex(), 30, 16));
		System.out.println(tile.getFirstItemIndex());
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return storageBox != null && storageBox.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		return null;
	}
}