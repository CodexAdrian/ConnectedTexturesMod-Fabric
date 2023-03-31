package team.chisel.ctm.client.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import team.chisel.ctm.client.event.ModelsAddedCallback;
import team.chisel.ctm.client.mixin.JsonUnbakedModelAccessor;
import team.chisel.ctm.client.model.CTMUnbakedModel;
import team.chisel.ctm.client.model.JsonCTMUnbakedModel;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.ResourceUtil;
import team.chisel.ctm.client.util.VoidSet;

public class ModelsAddedCallbackHandler implements ModelsAddedCallback {
	private final Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap;

	public ModelsAddedCallbackHandler(Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap) {
		this.jsonOverrideMap = jsonOverrideMap;
	}

	@Override
	public void onModelsAdded(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, Map<Identifier, UnbakedModel> unbakedModels, Map<Identifier, UnbakedModel> modelsToBake) {
		Map<Identifier, UnbakedModel> wrappedModels = new HashMap<>();

		// Switch to using vanilla load method as some mods depend on this.
		Function<Identifier, UnbakedModel> unbakedModelGetter = modelLoader::getOrLoadModel;
		VoidSet<Pair<String, String>> voidSet = VoidSet.get();
		HashMap<Identifier, UnbakedModel> copy = new HashMap<>(unbakedModels); //Copy map to avoid concurrent modification
		// Check which models should be wrapped
		for (Map.Entry<Identifier, UnbakedModel> entry : copy.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel unbakedModel = entry.getValue();

			// Do not wrap weighted models because they always get quads from other models
			if (unbakedModel instanceof WeightedUnbakedModel) {
				continue;
			}

			Collection<SpriteIdentifier> dependencies = unbakedModel.getTextureDependencies(unbakedModelGetter, voidSet);
			if (unbakedModel instanceof JsonUnbakedModel) {
				JsonUnbakedModel jsonModel = (JsonUnbakedModel) unbakedModel;
				// Do not wrap builtin models
				// Root model check after getTextureDependencies so it's actually set
				if (jsonModel.getRootModel() == ModelLoader.GENERATION_MARKER || jsonModel.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
					continue;
				}
				Int2ObjectMap<JsonElement> overrides = getOverrides(jsonModel);
				if (overrides != null && !overrides.isEmpty()) {
					// Wrap models with overrides
					wrappedModels.put(identifier, new JsonCTMUnbakedModel(jsonModel, overrides));
					continue;
				}
			}
			for (SpriteIdentifier spriteId : dependencies) {
				CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(spriteId.getTextureId()));
				if (metadata != null) {
					// At least one texture has CTM metadata, so this model should be wrapped
					wrappedModels.put(identifier, new CTMUnbakedModel(unbakedModel));
					break;
				}
			}
		}
		jsonOverrideMap.clear();

		// Inject wrapped models
		for (Map.Entry<Identifier, UnbakedModel> entry : wrappedModels.entrySet()) {
			Identifier identifier = entry.getKey();
			UnbakedModel wrapped = entry.getValue();

			unbakedModels.put(identifier, wrapped);
			if (modelsToBake.containsKey(identifier)) {
				modelsToBake.put(identifier, wrapped);
			}
		}
	}

	private Int2ObjectMap<JsonElement> getOverrides(JsonUnbakedModel unbakedModel) {
		Int2ObjectMap<JsonElement> overrides = jsonOverrideMap.get(unbakedModel);
		if (overrides == null) {
			JsonUnbakedModel parent = ((JsonUnbakedModelAccessor) unbakedModel).getParent();
			if (parent != null) {
				return getOverrides(parent);
			}
			return null;
		}
		return overrides;
	}
}
