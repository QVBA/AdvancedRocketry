package zmaster587.advancedRocketry.api.network;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import zmaster587.advancedRocketry.api.dimension.DimensionManager;
import zmaster587.advancedRocketry.api.dimension.DimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;

public class PacketStellarInfo extends BasePacket {
	StellarBody star;
	int starId;

	public PacketStellarInfo() {}

	public PacketStellarInfo(int starId,StellarBody star) {
		this.star = star;
		this.starId = starId;
	}

	@Override
	public void write(ByteBuf out) {
		NBTTagCompound nbt = new NBTTagCompound();
		out.writeInt(starId);
		out.writeBoolean(star == null);


		star.writeToNBT(nbt);

		PacketBuffer packetBuffer = new PacketBuffer(out);
		//TODO: error handling
		try {
			packetBuffer.writeNBTTagCompoundToBuffer(nbt);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void readClient(ByteBuf in) {
		PacketBuffer packetBuffer = new PacketBuffer(in);
		NBTTagCompound nbt;
		starId = in.readInt();

		if(in.readBoolean())
			if(DimensionManager.getInstance().isDimensionCreated(starId)) {
				DimensionManager.getInstance().removeStar(starId);
			}
			else {
				//TODO: error handling
				try {
					nbt = packetBuffer.readNBTTagCompoundFromBuffer();

				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				StellarBody star;

				if(starId == 0) {
					DimensionManager.getSol().readFromNBT(nbt);
				}
				else if((star = DimensionManager.getInstance().getStar(starId)) != null) {
					star.readFromNBT(nbt);
				} else {
					star = new StellarBody();
					star.readFromNBT(nbt);
					DimensionManager.getInstance().addStar(star);
				}
			}
	}

	@Override
	public void read(ByteBuf in) {
		//Should never be read on the server!
	}

	@Override
	public void executeClient(EntityPlayer thePlayer) {}

	@Override
	public void executeServer(EntityPlayerMP player) {}

}
