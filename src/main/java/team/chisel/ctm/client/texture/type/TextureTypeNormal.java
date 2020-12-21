package team.chisel.ctm.client.texture.type;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.TextureNormal;

public class TextureTypeNormal implements TextureType {
	public static final TextureTypeNormal INSTANCE = new TextureTypeNormal();
	@NotNull
	private static final TextureContext EMPTY_CONTEXT = () -> 0L;

	@Override
	public CTMTexture<TextureTypeNormal> makeTexture(TextureInfo info) {
		return new TextureNormal(this, info);
	}

	@Override
	public TextureContext getTextureContext(BlockState state, BlockView world, BlockPos pos, CTMTexture<?> tex) {
		return EMPTY_CONTEXT;
	}

	@Override
	public TextureContext getContextFromData(long data) {
		return EMPTY_CONTEXT;
	}
}