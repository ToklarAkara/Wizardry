package electroblob.wizardry.packet;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/** <b>[Server -> Client]</b> This packet is sent to sync server-side spell properties with clients on login. */
public class PacketSpellProperties implements IMessageHandler<PacketSpellProperties.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		if(ctx.side.isClient()){
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
				for(SpellProperties properties : message.propertiesArray){
					properties.getSpell().setPropertiesClient(properties);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private SpellProperties[] propertiesArray;

		public Message(){}

		public Message(SpellProperties... properties){
			this.propertiesArray = properties;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			List<SpellProperties> propertiesList = new ArrayList<>();

			while(buf.isReadable()){
				String name = ByteBufUtils.readUTF8String(buf);
				Spell spell = Spell.registry.getValue(new ResourceLocation(name));
				propertiesList.add(new SpellProperties(spell, buf));
			}

			propertiesArray = propertiesList.toArray(new SpellProperties[0]);
		}

		@Override
		public void toBytes(ByteBuf buf){
			for(SpellProperties properties : propertiesArray){
				ResourceLocation name = properties.getSpell().getRegistryName();
				ByteBufUtils.writeUTF8String(buf, name.toString());
				properties.write(buf);
			}
		}
	}
}