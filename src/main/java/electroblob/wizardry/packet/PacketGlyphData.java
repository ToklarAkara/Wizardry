package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketGlyphData implements IMessageHandler<PacketGlyphData.Message, IMessage> {

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                Wizardry.proxy.handleGlyphDataPacket(message)
            );
        }
        return null;
    }

    public static class Message implements IMessage {
        public List<GlyphEntry> glyphs;

        public Message() {}

        public Message(List<GlyphEntry> glyphs) {
            this.glyphs = glyphs;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int count = buf.readInt();
            glyphs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                String spellName = ByteBufUtils.readUTF8String(buf);
                String glyphName = ByteBufUtils.readUTF8String(buf);
                String glyphDesc = ByteBufUtils.readUTF8String(buf);
                glyphs.add(new GlyphEntry(spellName, glyphName, glyphDesc));
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(glyphs.size());
            for (GlyphEntry entry : glyphs) {
                ByteBufUtils.writeUTF8String(buf, entry.spellName);
                ByteBufUtils.writeUTF8String(buf, entry.glyphName != null ? entry.glyphName : "error");
                ByteBufUtils.writeUTF8String(buf, entry.glyphDesc != null ? entry.glyphDesc : "error");
            }
        }
    }

    public static class GlyphEntry {
        public final String spellName;
        public final String glyphName;
        public final String glyphDesc;

        public GlyphEntry(String spellName, String glyphName, String glyphDesc) {
            this.spellName = spellName;
            this.glyphName = glyphName;
            this.glyphDesc = glyphDesc;
        }

        public Spell resolveSpell() {
            return Spell.registry.getValue(new ResourceLocation(spellName));
        }
    }
}