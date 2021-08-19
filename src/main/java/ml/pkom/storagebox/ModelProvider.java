package ml.pkom.storagebox;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class ModelProvider implements ModelResourceProvider {
    public static final Identifier STORAGE_BOX_MODEL = StorageBoxMod.id("nmodels/storagebox");

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        if(identifier.equals(STORAGE_BOX_MODEL)) {
            return new StorageBoxModel();
        } else {
            return null;
        }
    }
}
