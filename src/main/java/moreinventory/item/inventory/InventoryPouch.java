package moreinventory.item.inventory;

import moreinventory.MoreInventoryMod;
import moreinventory.network.PouchMessage;
import moreinventory.tileentity.storagebox.StorageBoxNetworkManager;
import moreinventory.tileentity.storagebox.TileEntityStorageBox;
import moreinventory.util.CSUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryPouch implements IInventory
{
	private ItemStack[] inv;
	private final int invlength = 54;
	private final int invConfiglength = 18;
	private EntityPlayer usingPlayer;
	private ItemStack usingItem;
	public String customName;
	public boolean isCollectedByBox = true;
	public boolean isAutoCollect = true;
	public boolean isCollectMainInv = true;

	public InventoryPouch(EntityPlayer player, ItemStack itemstack)
	{
		this.inv = new ItemStack[invlength + invConfiglength];
		this.usingPlayer = player;
		this.usingItem = itemstack;
		this.customName = itemstack.getDisplayName();

		readFromNBT();
	}

	public InventoryPouch(ItemStack itemstack)
	{
		inv = new ItemStack[invlength + invConfiglength];
		usingItem = itemstack;
		readFromNBT();
	}

	public int getGrade()
	{
		int dm = usingItem.getItemDamage();
		return (dm - dm % 17) / 17;
	}

	public void setConfigItem(int no, ItemStack itemstack)
	{
		if (itemstack != null && itemstack.getItem() != MoreInventoryMod.Pouch)
		{
			inv[no] = itemstack.copy();
			inv[no].stackSize = 1;
		}
	}

	public boolean canAutoCollect(ItemStack itemstack)
	{
		return isAutoCollect && isCollectableItem(itemstack);
	}

	public boolean isCollectableItem(ItemStack itemstack)
	{
		for (int i = 0; i < 18; i++)
		{
			if (inv[i] != null)
			{
				if (CSUtil.compareStacksWithDamage(inv[i], itemstack))
				{
					return true;
				}
			}
		}

		return false;
	}

	public void collectedByBox(TileEntityStorageBox tile)
	{
		for (int i = 18; i < invlength + 18; i++)
		{
			tile.tryPutIn(getStackInSlot(i));
			CSUtil.checkNullStack(this, i);
		}
	}

	public void linkedPutIn(StorageBoxNetworkManager sbnet)
	{
		for (int i = 18; i < invlength + 18; i++)
		{
			sbnet.linkedPutIn(getStackInSlot(i), null, false);
			CSUtil.checkNullStack(this, i);
		}
	}

	public void collectAllItemStack(IInventory inventory, boolean flag)
	{
		ItemStack itemstack;
		int j = 0;

		if (!isCollectMainInv)
		{
			j = 9;
		}

		for (int i = j; i < inventory.getSizeInventory(); i++)
		{
			itemstack = inventory.getStackInSlot(i);

			if (itemstack != null)
			{
				if (itemstack.getItem() == MoreInventoryMod.Pouch)
				{
					InventoryPouch pouch = new InventoryPouch(itemstack);

					if (pouch.isAutoCollect && flag && itemstack != usingItem)
					{
						pouch.collectAllItemStack(inventory, false);
					}

					inventory.setInventorySlotContents(i, itemstack);
				}
				else
				{
					if (isCollectableItem(itemstack))
					{
						CSUtil.mergeItemStack(itemstack, this);
					}
				}
			}
		}

		CSUtil.checkNull(inventory);
	}

	public void transferToChest(IInventory tile)
	{
		if (tile.getSizeInventory() >= 27)
		{
			int m = invlength + 18;

			for (int i = 18; i < m; i++)
			{
				ItemStack itemstack = getStackInSlot(i);

				if (itemstack != null)
				{
					CSUtil.mergeItemStack(itemstack, tile);
					CSUtil.checkNullStack(this, i);
				}
			}
		}
	}

	public void onCrafting(ItemStack itemstack)
	{
		InventoryPouch pouch = new InventoryPouch(itemstack);
		pouch.inv = inv;
		pouch.isAutoCollect = isAutoCollect;
		pouch.isCollectedByBox = isCollectedByBox;
		pouch.isCollectMainInv = isCollectMainInv;
		pouch.customName = customName;
		pouch.writeToNBT(itemstack);
	}

	@Override
	public int getSizeInventory()
	{
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inv[slot];
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		inv[slot] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}

		markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "InvPouch";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack itemtack = getStackInSlot(slot);

		if (itemtack != null)
		{
			if (itemtack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			}
			else
			{
				itemtack = itemtack.splitStack(amount);

				if (itemtack.stackSize == 0)
				{
					setInventorySlotContents(slot, null);
				}
			}
		}

		markDirty();
		return itemtack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (slot < 18)
		{
			return null;
		}

		ItemStack itemstack = getStackInSlot(slot);

		if (itemstack != null)
		{
			setInventorySlotContents(slot, null);
		}

		return itemstack;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		writeToNBT(usingItem);
	}

	public void readFromNBT()
	{
		if (usingItem != null)
		{
			NBTTagCompound nbt = usingItem.getTagCompound();
			inv = new ItemStack[getSizeInventory()];

			if (nbt == null)
			{
				return;
			}

			NBTTagList list = nbt.getTagList("Items", 10);

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound data = list.getCompoundTagAt(i);
				int slot = data.getByte("Slot") & 0xFF;

				if (slot >= 0 && slot < inv.length)
				{
					inv[slot] = ItemStack.loadItemStackFromNBT(data);
				}
			}

			isCollectedByBox = nbt.getBoolean("isCollectedByBox");
			isCollectMainInv = nbt.getBoolean("isCollectMainInv");
			isAutoCollect = nbt.getBoolean("isAutoCollect");
		}
	}

	public void writeToNBT(ItemStack itemstack)
	{
		if (usingItem != null)
		{
			NBTTagList list = new NBTTagList();

			for (int i = 0; i < inv.length; i++)
			{
				if (inv[i] != null)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setByte("Slot", (byte)i);
					inv[i].writeToNBT(nbt);
					list.appendTag(nbt);
				}
			}

			NBTTagCompound nbt = itemstack.getTagCompound();

			if (nbt == null)
			{
				nbt = new NBTTagCompound();
			}

			nbt.setTag("Items", list);
			nbt.setBoolean("isCollectedByBox", isCollectedByBox);
			nbt.setBoolean("isCollectMainInv", isCollectMainInv);
			nbt.setBoolean("isAutoCollect", isAutoCollect);
			itemstack.setTagCompound(nbt);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		usingItem = usingPlayer.getCurrentEquippedItem();
		readFromNBT();

		if (!CSUtil.compareItems(player.getCurrentEquippedItem(), MoreInventoryMod.Pouch))
		{
			return false;
		}

		return true;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return 18 <= i;
	}

	public void handleClientPacket(boolean isCollectedByBox, boolean isCollectMainInv, boolean isAutoCollect)
	{
		this.isCollectedByBox = isCollectedByBox;
		this.isCollectMainInv = isCollectMainInv;
		this.isAutoCollect = isAutoCollect;

		writeToNBT(usingItem);
	}

	public void handleServerPacket(boolean isCollectedByBox, boolean isCollectMainInv, boolean isAutoCollect)
	{
		if (isCollectedByBox)
		{
			this.isCollectedByBox = !this.isCollectedByBox;
		}
		else if (isCollectMainInv)
		{
			this.isCollectMainInv = !this.isCollectMainInv;
		}
		else if (isAutoCollect)
		{
			this.isAutoCollect = !this.isAutoCollect;
		}

		writeToNBT(usingItem);
		sendPacket();
	}

	public void sendPacket()
	{
		MoreInventoryMod.network.sendTo(new PouchMessage(isCollectedByBox, isCollectMainInv, isAutoCollect), (EntityPlayerMP)usingPlayer);
	}

	public void sendPacketToServer(int channel)
	{
		MoreInventoryMod.network.sendToServer(new PouchMessage(channel == 0, channel == 1, channel == 2));
	}
}