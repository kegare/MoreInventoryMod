package moreinventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDisplay extends Slot
{

	public SlotDisplay(IInventory par1iInventory, int par2, int par3,int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	public ItemStack getStack()
	{
		ItemStack itemstack = this.inventory.getStackInSlot(this.getSlotIndex());
		ItemStack retItem = null;
		if (itemstack != null)
		{
			retItem = itemstack.copy();
			retItem.stackSize = 1;
		}

		return retItem;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

}