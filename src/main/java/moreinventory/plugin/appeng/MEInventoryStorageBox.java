package moreinventory.plugin.appeng;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import moreinventory.tileentity.storagebox.TileEntityStorageBox;
import moreinventory.util.MIMUtils;
import net.minecraft.item.ItemStack;

public class MEInventoryStorageBox implements IMEInventory<IAEItemStack>
{
	protected final TileEntityStorageBox storage;

	public MEInventoryStorageBox(TileEntityStorageBox tile)
	{
		this.storage = tile;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
	{
		if (input == null || input.getStackSize() == 0L)
		{
			return null;
		}

		ItemStack template = storage.getContents();

		if (template == null || !input.isSameType(template))
		{
			return input;
		}

		int storedCount = storage.getContentItemCount();
		int maxStacksCount = storage.getUsableInventorySize();
		int maxOneStackSize = template.getMaxStackSize();
		int maxStorableCount;

		if (storage.getTypeName().equals("Ender"))
		{
			maxStorableCount = Integer.MAX_VALUE;
		}
		else
		{
			maxStorableCount = maxStacksCount * maxOneStackSize;
		}

		int remainingFreeCount = maxStorableCount - storedCount;

		if (remainingFreeCount <= 0)
		{
			return input;
		}

		IAEItemStack result = input.copy();

		result.setStackSize(Math.max(0, input.getStackSize() - remainingFreeCount));

		boolean isModulate = type == Actionable.MODULATE;

		if (isModulate)
		{
			long remainingInjectCount = input.getStackSize() - result.getStackSize();

			do
			{
				ItemStack tmp = input.getItemStack().copy();
				int workCount = (int)Math.min(tmp.getMaxStackSize(),remainingInjectCount);
				tmp.stackSize = workCount;

				storage.tryPutIn(tmp);

				if (0 < tmp.stackSize)
				{
					result.setStackSize(Math.min(input.getStackSize() , result.getStackSize() + tmp.stackSize));
					break;
				}
				else
				{
					remainingInjectCount -= workCount;
				}
			}
			while (0 < remainingInjectCount);
		}

		if (result.getStackSize() == 0)
		{
			result = null;
		}

		storage.markDirty();
		storage.getWorldObj().markBlockForUpdate(storage.xCoord, storage.yCoord, storage.zCoord);
		storage.getWorldObj().notifyBlockChange(storage.xCoord, storage.yCoord, storage.zCoord, storage.getBlockType());

		return result;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable type, BaseActionSource src)
	{
		if (request == null)
		{
			return null;
		}

		ItemStack requestStack = request.getItemStack();

		if (storage.getFirstItemIndex() < 0)
		{
			return null;
		}

		ItemStack template = storage.getContents();

		if(template == null || !(MIMUtils.compareStacksWithDamage(requestStack, template) && ItemStack.areItemStackTagsEqual(requestStack,template)))
		{
			return null;
		}

		int storedCount = storage.getContentItemCount();

		if(storedCount == 0)
		{
			return null;
		}

		long extractCount = Math.min(storedCount, requestStack.stackSize);
		IAEItemStack result = AEApi.instance().storage().createItemStack(storage.getStackInSlot(storage.getFirstItemIndex()));

		result.setStackSize(extractCount);

		boolean isModulate = type == Actionable.MODULATE;

		if(isModulate)
		{
			long remainingExtractCount = extractCount;

			do
			{
				ItemStack extract = storage.loadItemStack((int)Math.min(template.getMaxStackSize(),remainingExtractCount));

				remainingExtractCount -= extract.stackSize;
			}
			while(0 < remainingExtractCount);
		}

		storage.markDirty();
		storage.getWorldObj().markBlockForUpdate(storage.xCoord, storage.yCoord, storage.zCoord);
		storage.getWorldObj().notifyBlockChange(storage.xCoord, storage.yCoord, storage.zCoord, storage.getBlockType());

		return result;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out)
	{
		if (storage.getContents() == null)
		{
			return out;
		}

		int allCount = storage.getContentItemCount();

		if(allCount <= 0)
		{
			return out;
		}

		IAEItemStack stored = AEApi.instance().storage().createItemStack(storage.getStackInSlot(storage.getFirstItemIndex()));

		stored.setStackSize(allCount);
		out.add(stored);

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}