package gregicality.multiblocks.api.recipes.alloyblast;

import static gregtech.api.GTValues.MV;
import static gregtech.api.GTValues.VA;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.CraftingComponent;

import gregicality.multiblocks.api.fluids.GCYMFluidStorageKeys;
import gregicality.multiblocks.api.recipes.GCYMRecipeMaps;
import gregicality.multiblocks.api.unification.GCYMMaterialFlags;

public class AlloyBlastRecipeProducer {

    public static final AlloyBlastRecipeProducer DEFAULT_PRODUCER = new AlloyBlastRecipeProducer();

    /**
     * Generates alloy blast recipes for a material
     *
     * @param material      the material to generate for
     * @param blastProperty the blast property of the material
     */
    public void produce(@NotNull Material material, @NotNull BlastProperty blastProperty) {
        // do not generate for disabled materials
        if (material.hasFlag(GCYMMaterialFlags.NO_ALLOY_BLAST_RECIPES)) return;

        final int componentAmount = material.getMaterialComponents().size();

        // ignore non-alloys
        if (componentAmount < 2) return;

        // get the output fluid
        Fluid output = material.getFluid(GCYMFluidStorageKeys.MOLTEN);
        if (output == null) {
            output = material.getFluid(FluidStorageKeys.LIQUID);
            if (output == null) return;
        }

        RecipeBuilder<BlastRecipeBuilder> builder = createBuilder(blastProperty, material);

        int outputAmount = addInputs(material, builder);
        if (outputAmount <= 0) return;

        buildRecipes(blastProperty, output, outputAmount, componentAmount, builder);

        // if the material does not need a vacuum freezer, exit
        if (!OrePrefix.ingotHot.doGenerateItem(material)) return;

        addFreezerRecipes(material, output, blastProperty);
    }

    /**
     * Creates the recipeBuilder with duration and EUt
     *
     * @param property the blast property of the material
     * @param material the material
     * @return the builder
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected @NotNull BlastRecipeBuilder createBuilder(@NotNull BlastProperty property, @NotNull Material material) {
        BlastRecipeBuilder builder = GCYMRecipeMaps.ALLOY_BLAST_RECIPES.recipeBuilder();
        // apply the duration override
        int duration = property.getDurationOverride();
        if (duration < 0) duration = Math.max(1, (int) (material.getMass() * property.getBlastTemperature() / 100L));
        builder.duration(duration);

        // apply the EUt override
        int EUt = property.getEUtOverride();
        if (EUt < 0) EUt = GTValues.VA[GTValues.MV];
        builder.EUt(EUt);

        return builder.blastFurnaceTemp(property.getBlastTemperature());
    }

    /**
     * @param material the material to start recipes for
     * @param builder  the recipe builder to append to
     * @return the outputAmount if the recipe is valid, otherwise -1
     */
    protected int addInputs(@NotNull Material material, @NotNull RecipeBuilder<BlastRecipeBuilder> builder) {
        // calculate the output amount and add inputs
        int outputAmount = 0;
        int fluidAmount = 0;
        int dustAmount = 0;
        for (MaterialStack materialStack : material.getMaterialComponents()) {
            final Material msMat = materialStack.material;
            final int msAmount = (int) materialStack.amount;

            if (msMat.hasProperty(PropertyKey.DUST)) {
                if (dustAmount >= 9) return -1; // more than 9 dusts won't fit in the machine
                dustAmount++;
                builder.input(OrePrefix.dust, msMat, msAmount);
            } else if (msMat.hasProperty(PropertyKey.FLUID)) {
                if (fluidAmount >= 2) return -1; // more than 2 fluids won't fit in the machine
                fluidAmount++;
                // assume all fluids have 1000mB/mol, since other quantities should be as an item input
                builder.fluidInputs(msMat.getFluid(1000 * msAmount));
            } else return -1; // no fluid or item prop means no valid recipe
            outputAmount += msAmount;
        }
        return outputAmount;
    }

    /**
     * Builds the alloy blast recipes
     *
     * @param property        the blast property to utilize
     * @param molten          the molten fluid
     * @param outputAmount    the amount of material to output
     * @param componentAmount the amount of different components in the material
     * @param builder         the builder to continue
     */
    protected void buildRecipes(@NotNull BlastProperty property, @NotNull Fluid molten, int outputAmount,
                                int componentAmount,
                                @NotNull RecipeBuilder<BlastRecipeBuilder> builder) {
        // add the fluid output with the correct amount
        builder.fluidOutputs(new FluidStack(molten, GTValues.L * outputAmount));

        // apply alloy blast duration reduction: 3/4
        int duration = builder.getDuration() * outputAmount * 3 / 4;

        // build the gas recipe if it exists
        if (property.getGasTier() != null) {
            RecipeBuilder<BlastRecipeBuilder> builderGas = builder.copy();
            FluidStack gas = CraftingComponent.EBF_GASES.get(property.getGasTier());
            builderGas.notConsumable(new IntCircuitIngredient(getGasCircuitNum(componentAmount)))
                    .fluidInputs(new FluidStack(gas, gas.amount * outputAmount))
                    .duration((int) (duration * 0.67))
                    .buildAndRegister();
        }

        // build the non-gas recipe
        builder.notConsumable(new IntCircuitIngredient(getCircuitNum(componentAmount)))
                .duration(duration)
                .buildAndRegister();
    }

    /**
     * @param componentAmount the amount of different components in the material
     * @return the circuit number for the regular recipe
     */
    protected int getCircuitNum(int componentAmount) {
        return componentAmount;
    }

    /**
     * @param componentAmount the amount of different components in the material
     * @return the circuit number for the gas-boosted recipe
     */
    protected int getGasCircuitNum(int componentAmount) {
        return componentAmount + 10;
    }

    /**
     * Add the freezer recipes for the material
     *
     * @param material the material to generate for
     * @param molten   the molten fluid
     * @param property the blast property of the material
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected void addFreezerRecipes(@NotNull Material material, @NotNull Fluid molten,
                                     @NotNull BlastProperty property) {
        int vacuumDuration = property.getVacuumDurationOverride() == -1 ? (int) material.getMass() * 3 :
                property.getVacuumDurationOverride();
        int vacuumEUt = property.getVacuumEUtOverride() == -1 ? VA[MV] : property.getVacuumEUtOverride();

        // build the freezer recipe
        RecipeBuilder<?> freezerBuilder = RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .fluidInputs(new FluidStack(molten, GTValues.L))
                .notConsumable(MetaItems.SHAPE_MOLD_INGOT.getStackForm())
                .output(OrePrefix.ingot, material)
                .duration(vacuumDuration)
                .EUt(vacuumEUt);

        // helium for when >= 5000K temperature
        if (property.getBlastTemperature() >= 5000) {
            freezerBuilder.fluidInputs(Materials.Helium.getFluid(FluidStorageKeys.LIQUID, 500))
                    .fluidOutputs(Materials.Helium.getFluid(250));
        }
        freezerBuilder.buildAndRegister();
    }
}
