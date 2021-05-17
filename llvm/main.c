#include <graalvm/llvm/polyglot.h>
#include <stdbool.h>

void* Block;
void* AbstractBlockSettings;
void* AbstractBlockSettings_arr;
void* Class_arr;
void* Material;
void* BlockItem;
void* ItemGroup;
void* Registry;
void* Identifier;
void* ActionResult;
void* LiteralText;
void* FabricBlockSettings;
void* FabricItemSettings;
void* IncludeMethodFilter;
void* ProxyFactory;
void* ArrayList;
void* Util;
void* MessageType;

void* onUseCBlock(void* block, void* state, void* world, void* pos, void* player, void* hand, void* hit) {
    void* javaIsClient = polyglot_get_member(world, "isClient");
    bool isClient = polyglot_as_boolean(javaIsClient);

    if (!isClient) {
        void* string = polyglot_from_string("Hello from C!", "UTF-8");
        void* text = polyglot_new_instance(LiteralText, string);

        void* chatMessage = polyglot_get_member(MessageType, "CHAT");
        void* nilUuid = polyglot_get_member(Util, "NIL_UUID");

        polyglot_invoke(player, "sendMessage", text, chatMessage, nilUuid);
    }

    return polyglot_get_member(ActionResult, "SUCCESS");
}

void* cBlockHandler(void* self, void* method, void* proceed, void* args) {
    void* state = polyglot_get_array_element(args, 0);
    void* world = polyglot_get_array_element(args, 1);
    void* pos = polyglot_get_array_element(args, 2);
    void* player = polyglot_get_array_element(args, 3);
    void* hand = polyglot_get_array_element(args, 4);
    void* hit = polyglot_get_array_element(args, 5);
    return onUseCBlock(self, state, world, pos, player, hand, hit);
}

void onInitialize() {
    Block = polyglot_java_type("net.minecraft.block.Block");
    AbstractBlockSettings = polyglot_java_type("net.minecraft.block.AbstractBlock$Settings");
    AbstractBlockSettings_arr = polyglot_java_type("net.minecraft.block.AbstractBlock$Settings[]");
    Class_arr = polyglot_java_type("java.lang.Class[]");
    Material = polyglot_java_type("net.minecraft.block.Material");
    BlockItem = polyglot_java_type("net.minecraft.item.BlockItem");
    ItemGroup = polyglot_java_type("net.minecraft.item.ItemGroup");
    Registry = polyglot_java_type("net.minecraft.util.registry.Registry");
    Identifier = polyglot_java_type("net.minecraft.util.Identifier");
    ActionResult = polyglot_java_type("net.minecraft.util.ActionResult");
    LiteralText = polyglot_java_type("net.minecraft.text.LiteralText");
    FabricBlockSettings = polyglot_java_type("net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings");
    FabricItemSettings = polyglot_java_type("net.fabricmc.fabric.api.item.v1.FabricItemSettings");
    IncludeMethodFilter = polyglot_java_type("dev.vriska.quiltlangpolyglot.IncludeMethodFilter");
    ProxyFactory = polyglot_java_type("javassist.util.proxy.ProxyFactory");
    ArrayList = polyglot_java_type("java.util.ArrayList");
    Util = polyglot_java_type("net.minecraft.util.Util");
    MessageType = polyglot_java_type("net.minecraft.network.MessageType");

    void* blockFactory = polyglot_new_instance(ProxyFactory);
    polyglot_invoke(blockFactory, "setSuperclass", Block);

    void* filteredMethods = polyglot_new_instance(ArrayList, 1);
    void* onUse = polyglot_from_string("onUse", "UTF-8");
    polyglot_invoke(filteredMethods, "add", onUse);

    void* methodFilter = polyglot_new_instance(IncludeMethodFilter, Block, filteredMethods);
    polyglot_invoke(blockFactory, "setFilter", methodFilter);

    void* metal = polyglot_get_member(Material, "METAL");
    void* cBlockSettings = polyglot_invoke(FabricBlockSettings, "of", metal);
    cBlockSettings = polyglot_invoke(cBlockSettings, "strength", 4.0f);

    void* paramTypes = polyglot_new_instance(Class_arr, 1);
    polyglot_set_array_element(paramTypes, 0, AbstractBlockSettings);

    void* params = polyglot_new_instance(AbstractBlockSettings_arr, 1);
    polyglot_set_array_element(params, 0, cBlockSettings);

    void* C_BLOCK = polyglot_invoke(blockFactory, "create", paramTypes, params, cBlockHandler);

    void* modName = polyglot_from_string("quilt_lang_polyglot", "UTF-8");
    void* blockName = polyglot_from_string("c_block", "UTF-8");
    void* identifier = polyglot_new_instance(Identifier, modName, blockName);

    void* blockRegistry = polyglot_get_member(Registry, "BLOCK");
    polyglot_invoke(Registry, "register", blockRegistry, identifier, C_BLOCK);

    void* cItemSettings = polyglot_new_instance(FabricItemSettings);
    void* miscGroup = polyglot_get_member(ItemGroup, "MISC");
    cItemSettings = polyglot_invoke(cItemSettings, "group", miscGroup);

    void* blockItem = polyglot_new_instance(BlockItem, C_BLOCK, cItemSettings);

    void* itemRegistry = polyglot_get_member(Registry, "ITEM");
    polyglot_invoke(Registry, "register", itemRegistry, identifier, blockItem);
}
