package gregicality.multiblocks.api.fluids;

import gregicality.multiblocks.GregicalityMultiblocks;
import gregicality.multiblocks.api.fluids.fluidType.GCYMFluidTypes;
import gregicality.multiblocks.api.unification.GCYMMaterialFlags;
import gregicality.multiblocks.api.unification.properties.AlloyBlastProperty;
import gregicality.multiblocks.api.unification.properties.GCYMPropertyKey;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.MetaFluids;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class GCYMMetaFluids {

    private static final Collection<ResourceLocation> fluidSprites = new ObjectOpenHashSet<>();

    public static final ResourceLocation AUTO_GENERATED_MOLTEN_TEXTURE = new ResourceLocation(
            GregicalityMultiblocks.MODID, "blocks/fluids/fluid.molten.autogenerated");

    private GCYMMetaFluids() {/**/}

    public static void init() {
        fluidSprites.add(AUTO_GENERATED_MOLTEN_TEXTURE);

        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            createMoltenFluid(material);
        }
    }

    public static void createMoltenFluid(@Nonnull Material material) {
        if (material.hasFlag(GCYMMaterialFlags.DISABLE_ALLOY_BLAST)) return;
        if (!OrePrefix.ingotHot.doGenerateItem(material)) return;

        // ignore materials which are not alloys
        if (material.getMaterialComponents().size() <= 1) return;

        BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);
        if (blastProperty == null) return;

        AlloyBlastProperty alloyBlastProperty = material.getProperty(GCYMPropertyKey.ALLOY_BLAST);
        if (alloyBlastProperty == null) return;

        // use molten texture instead of iconset textures
        MetaFluids.setMaterialFluidTexture(material, GCYMFluidTypes.MOLTEN, AUTO_GENERATED_MOLTEN_TEXTURE);

        int temperature = blastProperty.getBlastTemperature();
        Fluid fluid = MetaFluids.registerFluid(material, GCYMFluidTypes.MOLTEN, temperature, false);

        alloyBlastProperty.setFluid(fluid);
        alloyBlastProperty.setTemperature(blastProperty.getBlastTemperature());
    }

    public static void registerSprites(@Nonnull TextureMap textureMap) {
        fluidSprites.forEach(textureMap::registerSprite);
    }
}
