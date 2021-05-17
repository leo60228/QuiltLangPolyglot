package dev.vriska.quiltlangpolyglot;

import org.objectweb.asm.Type;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.util.EntryTriple;
import net.fabricmc.mapping.util.ClassMapper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import dev.vriska.quiltlangpolyglot.GraalRemapper.FieldLookup;
import dev.vriska.quiltlangpolyglot.GraalRemapper.MethodLookup;

public class GraalMappings {
    public static Map<String, String> classNames = null;
    public static Map<String, String> inverseClassNames = null;
    public static Map<FieldLookup, String> fields = null;
    public static Map<MethodLookup, String> methods = null;

    public static void loadMappings() {
        try {
            InputStream mappingStream = GraalRemapper.class.getResourceAsStream("/mappings.tiny");
            BufferedReader mappingReader = new BufferedReader(new InputStreamReader(mappingStream));
            TinyTree mappings = TinyMappingFactory.loadWithDetection(mappingReader);
            mappingStream.close();

            classNames = new HashMap<>();
            inverseClassNames = new HashMap<>();

            MappingResolver fabricResolver = FabricLoader.getInstance().getMappingResolver();

            for (ClassDef classDef : mappings.getClasses()) {
                String named = classDef.getName("named");
                String intermediary = classDef.getName("intermediary").replace('/', '.');
                String mapped = fabricResolver.mapClassName("intermediary", intermediary).replace('.', '/');
                classNames.put(named, mapped);
                inverseClassNames.put(mapped, named);
            }

            ClassMapper classMapper = new ClassMapper(classNames);
            fields = new HashMap<>();
            methods = new HashMap<>();

            for (ClassDef classDef : mappings.getClasses()) {
                String classNamed = classDef.getName("named");
                String classIntermediary = classDef.getName("intermediary").replace('/', '.');
                String classMapped = classNames.get(classNamed);
                for (FieldDef fieldDef : classDef.getFields()) {
                    String fieldNamed = fieldDef.getName("named");

                    String fieldIntermediary = fieldDef.getName("intermediary");
                    String fieldDescIntermediary = fieldDef.getDescriptor("intermediary");

                    String fieldMapped = fabricResolver.mapFieldName("intermediary", classIntermediary, fieldIntermediary, fieldDescIntermediary);

                    FieldLookup lookup = new FieldLookup(classMapped, fieldMapped);

                    fields.put(lookup, fieldNamed);
                }

                for (MethodDef methodDef : classDef.getMethods()) {
                    String methodNamed = methodDef.getName("named");
                    String methodDescNamed = methodDef.getDescriptor("named");

                    String methodIntermediary = methodDef.getName("intermediary");
                    String methodDescIntermediary = methodDef.getDescriptor("intermediary");

                    String methodMapped = fabricResolver.mapMethodName("intermediary", classIntermediary, methodIntermediary, methodDescIntermediary);

                    EntryTriple named = new EntryTriple(classNamed, methodNamed, methodDescNamed);
                    EntryTriple mapped = named.map(classMapper, methodMapped);

                    if (methodNamed.equals("register")) {
                        System.out.println(mapped);
                    }

                    MethodLookup lookup = new MethodLookup(mapped.getOwner(), mapped.getName(), mapped.getDescriptor());

                    methods.put(lookup, methodNamed);
                }
            }
        } catch (IOException ex) {
            throw new IOError(ex);
        }
    }
}
