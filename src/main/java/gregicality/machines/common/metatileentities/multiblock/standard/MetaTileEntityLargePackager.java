package gregicality.machines.common.metatileentities.multiblock.standard;

import gregicality.machines.api.metatileentity.GCYMRecipeMapMultiblockController;
import gregicality.machines.api.render.GCYMultiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class MetaTileEntityLargePackager extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargePackager(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PACKER_RECIPES);
        this.recipeMaps = new RecipeMap[]{RecipeMaps.PACKER_RECIPES, RecipeMaps.UNPACKER_RECIPES};
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityLargePackager(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XAX", "XXX").setRepeatable(3)
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(30).or(autoAbilities()))
                .where('A', air())
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return GCYMultiTextures.LARGE_PACKAGER_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public boolean hasMultipleRecipeMaps() {
        return true;
    }
}