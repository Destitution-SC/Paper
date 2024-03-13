package io.papermc.generator.types.craftblockdata.property;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.types.StructuredGenerator;
import io.papermc.generator.types.craftblockdata.property.converter.Converters;
import io.papermc.generator.utils.NamingManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class IntegerPropertyWriter extends PropertyWriter<Integer> {

    protected IntegerPropertyWriter(final IntegerProperty property) {
        super(property);
    }

    @Override
    public void addExtras(TypeSpec.Builder builder, FieldSpec field, StructuredGenerator<?> generator, NamingManager naming) {
        if (Converters.has(this.property)) {
            return;
        }

        IntegerProperty property = (IntegerProperty) this.property;

        if (property.min != 0 || this.property.getName().equals(BlockStateProperties.LEVEL.getName())) { // special case (levelled)
            MethodSpec.Builder methodBuilder = generator.createMethod(naming.getterName(name -> true).pre("Minimum").concat());
            methodBuilder.addStatement("return $N.min", field);
            methodBuilder.returns(this.getApiType());

            builder.addMethod(methodBuilder.build());
        }

        {
            MethodSpec.Builder methodBuilder = generator.createMethod(naming.getterName(name -> true).pre("Maximum").concat());
            methodBuilder.addStatement("return $N.max", field);
            methodBuilder.returns(this.getApiType());

            builder.addMethod(methodBuilder.build());
        }
    }
}
