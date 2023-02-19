package gregicality.multiblocks.api.unification.properties;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import java.util.List;

public class GCYMAutogeneratedPropertyAddition {

    public static void init() {
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            addAlloyBlastProperty(material);
        }
    }

    public static void addAlloyBlastProperty(Material material) {
        final List<MaterialStack> components = material.getMaterialComponents();
        // ignore materials which are not alloys
        if (components.size() <= 1) return;

        if (material.hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) return;

        BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);
        if (blastProperty == null) return;

        if (!OrePrefix.ingotHot.doGenerateItem(material)) return;
        if (!material.hasProperty(PropertyKey.FLUID)) return;

        // if there are more than 2 fluid-only components in the material, do not generate a hot fluid
        if (components.stream().filter(ms -> !ms.material.hasProperty(PropertyKey.DUST) &&
                ms.material.hasProperty(PropertyKey.FLUID)).count() > 2) {
            return;
        }

        material.setProperty(GCYMPropertyKey.ALLOY_BLAST, new AlloyBlastProperty(material.getBlastTemperature()));
    }
}
