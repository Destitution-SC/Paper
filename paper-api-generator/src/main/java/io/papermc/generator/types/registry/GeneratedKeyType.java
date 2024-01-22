package io.papermc.generator.types.registry;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.papermc.generator.Main;
import io.papermc.generator.types.SimpleGenerator;
import io.papermc.generator.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.Javadocs;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static io.papermc.generator.utils.Annotations.EXPERIMENTAL_API_ANNOTATION;
import static io.papermc.generator.utils.Annotations.NOT_NULL;
import static io.papermc.generator.utils.Annotations.experimentalAnnotations;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@DefaultQualifier(NonNull.class)
public class GeneratedKeyType<T, A> extends SimpleGenerator {

    private static final String CREATE_JAVADOC = """
        Creates a key for {@link $T} in a registry.
        
        @param key the value's key in the registry
        @return a new typed key
        """;

    private final Class<A> apiType;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final RegistryKey<A> apiRegistryKey;
    private final boolean publicCreateKeyMethod;

    public GeneratedKeyType(final String className, final Class<A> apiType, final String pkg, final ResourceKey<? extends Registry<T>> registryKey, final RegistryKey<A> apiRegistryKey, final boolean publicCreateKeyMethod) {
        super(className, pkg);
        this.apiType = apiType;
        this.registryKey = registryKey;
        this.apiRegistryKey = apiRegistryKey;
        this.publicCreateKeyMethod = publicCreateKeyMethod;
    }

    private MethodSpec.Builder createMethod(final TypeName returnType) {
        final TypeName keyType = TypeName.get(Key.class).annotated(NOT_NULL);

        final ParameterSpec keyParam = ParameterSpec.builder(keyType, "key", FINAL).build();
        final MethodSpec.Builder create = MethodSpec.methodBuilder("create")
            .addModifiers(this.publicCreateKeyMethod ? PUBLIC : PRIVATE, STATIC)
            .addParameter(keyParam)
            .addCode("return $T.create($T.$L, $N);", TypedKey.class, RegistryKey.class, requireNonNull(RegistryUtils.getRegistryKeyFieldNames().get(this.apiRegistryKey), "Missing field for " + this.apiRegistryKey), keyParam)
            .returns(returnType.annotated(NOT_NULL));
        if (this.publicCreateKeyMethod) {
            create.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO remove once not experimental
            create.addJavadoc(CREATE_JAVADOC, this.apiType);
        }
        return create;
    }

    private TypeSpec.Builder keyHolderType() {
        return classBuilder(this.className)
            .addModifiers(PUBLIC, FINAL)
            .addJavadoc(Javadocs.getVersionDependentClassHeader("{@link $T#$L}"), RegistryKey.class, RegistryUtils.getRegistryKeyFieldNames().get(this.apiRegistryKey))
            .addAnnotations(Annotations.CLASS_HEADER)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .build()
            );
    }

    @Override
    protected TypeSpec getTypeSpec() {
        final TypeName typedKey = ParameterizedTypeName.get(TypedKey.class, this.apiType);

        final TypeSpec.Builder typeBuilder = this.keyHolderType();
        typeBuilder.addAnnotation(EXPERIMENTAL_API_ANNOTATION); // TODO experimental API
        final MethodSpec.Builder createMethod = this.createMethod(typedKey);

        final Registry<T> registry = Main.REGISTRY_ACCESS.registryOrThrow(this.registryKey);
        final List<ResourceKey<T>> experimental = RegistryUtils.collectExperimentalKeys(registry);

        boolean allExperimental = true;
        for (final T value : registry) {
            final ResourceKey<T> key = registry.getResourceKey(value).orElseThrow();
            final String keyPath = key.location().getPath();
            final String fieldName = Formatting.formatPathAsField(keyPath);
            final FieldSpec.Builder fieldBuilder = FieldSpec.builder(typedKey, fieldName, PUBLIC, STATIC, FINAL)
                .initializer("$N(key($S))", createMethod.build(), keyPath)
                .addJavadoc(Javadocs.getVersionDependentField("{@code $L}"), key.location().toString());
            if (experimental.contains(key)) {
                fieldBuilder.addAnnotations(experimentalAnnotations(FeatureFlags.UPDATE_1_21));
            } else {
                allExperimental = false;
            }
            typeBuilder.addField(fieldBuilder.build());
        }
        if (allExperimental) {
            typeBuilder.addAnnotations(experimentalAnnotations(FeatureFlags.UPDATE_1_21));
            createMethod.addAnnotations(experimentalAnnotations(FeatureFlags.UPDATE_1_21));
        }
        return typeBuilder.addMethod(createMethod.build()).build();
    }

    @Override
    protected JavaFile.Builder file(JavaFile.Builder builder) {
        return builder
            .skipJavaLangImports(true)
            .addStaticImport(Key.class, "key")
            .indent("    ");
    }
}